/*
 * ========================================================================
 * Copyright (c) 2011 Vladislav "FractalizeR" Rastrusny
 * Website: http://www.fractalizer.ru
 * Email: FractalizeR@yandex.ru
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

package ru.fractalizer.jrapidrpc.server.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fractalizer.jrapidrpc.api.ISerializer;
import ru.fractalizer.jrapidrpc.api.ServerStartupException;
import ru.fractalizer.jrapidrpc.tools.ReflectionCache;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Thread class to simply accept client connections. Actual work is done by {@link Worker} class.
 */
class Acceptor<T, V extends T> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);

    private ITerminateSignaller      terminateSignaller;
    private ServerSocket             serverSocket;
    private ExecutorService          executorService;
    private ISerializer              serializer;
    private Class<V>                 serviceObjectClass;
    private ReflectionCache          reflectionCache;
    private Object                   serviceObjectSingleton;
    private ThreadModelType          threadModelType;
    private ThreadPoolOverflowPolicy threadPoolOverflowPolicy;

    /**
     * Default constructor
     *
     * @param terminateSignaller       Signaller to use when checking for termination requests
     * @param serverSocket             The server socket on which to call accept() and accept client connections
     * @param threadModelType          The threading type on the service object
     * @param executorService          Thread pool to submit user threads to
     * @param serializer               Serializer to use
     * @param serviceInterface         The RPC interface
     * @param serviceObjectClass       The class of the service object which is responsible for handling client connections
     * @param threadPoolOverflowPolicy The policy in case of thread pool overflow
     * @throws ServerStartupException Is thrown in case something went wrong
     */
    Acceptor(ITerminateSignaller terminateSignaller, ServerSocket serverSocket, ThreadModelType threadModelType,
             ExecutorService executorService, ISerializer serializer, Class<T> serviceInterface,
             Class<V> serviceObjectClass, ThreadPoolOverflowPolicy threadPoolOverflowPolicy)
            throws ServerStartupException {

        this.terminateSignaller = terminateSignaller;
        this.serverSocket = serverSocket;
        this.executorService = executorService;
        this.serializer = serializer;
        this.serviceObjectClass = serviceObjectClass;
        this.reflectionCache = new ReflectionCache(serviceInterface);
        this.serviceObjectSingleton = null;
        this.threadModelType = threadModelType;
        this.threadPoolOverflowPolicy = threadPoolOverflowPolicy;

        switch (this.threadModelType) {
            case Singleton:
                try {
                    serviceObjectSingleton = serviceObjectClass.newInstance();
                } catch (InstantiationException e) {
                    throw new ServerStartupException("Cannot create service object singleton instance!", e);
                } catch (IllegalAccessException e) {
                    throw new ServerStartupException("Cannot create service object singleton instance!", e);
                }
                break;
            case InstancePerThread:
                serviceObjectSingleton = null;
                break;
            default:
                throw new ServerStartupException("Unknown threading model!");
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("SimpleTCPServer Acceptor Thread");
        logger.info("Acceptor thread started");

        while (!terminateSignaller.IsTerminateRequested()) {
            //Accepting socket
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (SocketException e) {
                logger.info(
                        "SocketException when calling accept(): shutdown requested by server. Terminating gracefully.");
                //Graceful termination by calling serverSocket.close in parent thread. No need to do something, just exit
                return;
            } catch (Exception e) {
                logger.error("Exception while serverSocket.accept()", e);
                return;
            }

            //Dispatching task
            try {
                switch (this.threadModelType) {
                    case Singleton:
                        executorService.submit(new Worker(terminateSignaller, clientSocket, serializer, reflectionCache,
                                serviceObjectSingleton));
                        break;
                    case InstancePerThread:
                        executorService.submit(new Worker(terminateSignaller, clientSocket, serializer, reflectionCache,
                                serviceObjectClass.newInstance()));
                        break;
                    default:
                        logger.error("Unknown threading model!");
                        return;
                }
            } catch (RejectedExecutionException e) {
                if (executorService.isShutdown()) {
                    //Thread pool graceful shutdown
                    return;
                }

                //Pool os overflown?
                switch (threadPoolOverflowPolicy) {
                    case Continue:
                        try {
                            clientSocket.close();
                        } catch (IOException e1) {
                            logger.warn("Unable to close client socket properly!", e1);
                        }
                        continue;
                    case Terminate:
                        try {
                            clientSocket.close();
                        } catch (IOException e1) {
                            logger.warn("Unable to close client socket properly!", e1);
                        }
                        return;
                    default:
                        logger.error("Unknown threadPoolOverflowPolicy!", e);
                        return;
                }
            } catch (NullPointerException e) {
                logger.error("NullPointerException in a job!", e);
                return;
            } catch (InstantiationException e) {
                logger.error("InstantiationException while calling serviceObjectClass.newInstance()!", e);
                return;
            } catch (IllegalAccessException e) {
                logger.error("IllegalAccessException while calling serviceObjectClass.newInstance()!", e);
                return;
            }
        }
    }
}