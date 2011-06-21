/*
 * ========================================================================
 * Copyright (c) 2011 Vladislav "FractalizeR" Rastrusny
 * Website: http://www.fractalizer.ru
 * Email: FractalizeR@yandex.ru
 * ========================================================================
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

import ru.fractalizer.jrapidrpc.api.Serializer;
import ru.fractalizer.jrapidrpc.api.ServerStartupException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class implementing simple multithreaded server
 */
public class SimpleTCPServer implements TerminateSignaller {

    private Serializer               serializer;
    private int                      port;
    private ExecutorService          executorService;
    private ThreadModelType          threadModelType;
    private ThreadPoolOverflowPolicy threadPoolOverflowPolicy;
    private int                      backlog;
    private InetAddress              bindAddr;
    private ServerSocket             socket;
    private Thread                   acceptor;

    private boolean isTerminateRequested = false;

    /**
     * SimpleTCP server class
     *
     * @param serializer               Protocol data serializer instance to use. Must be thread-safe.
     * @param port                     TCP port on which to listen for client connections
     * @param threadModelType          Threading model to use
     * @param executorService          Thread pool manager to use when handling client threads
     * @param threadPoolOverflowPolicy A policy to use when thread pool overflow is detected
     * @param backlog                  Controls how ServerSocket is created by the component. See {@link java.net.ServerSocket#ServerSocket(int, int, java.net.InetAddress)}  ServerSocket constructor} for details
     * @param bindAddr                 Controls how ServerSocket is created by the component. See {@link java.net.ServerSocket#ServerSocket(int, int, java.net.InetAddress)}  ServerSocket constructor} for details
     */
    public SimpleTCPServer(Serializer serializer, int port, ThreadModelType threadModelType,
                           ExecutorService executorService, ThreadPoolOverflowPolicy threadPoolOverflowPolicy,
                           int backlog, InetAddress bindAddr) {
        super();
        this.serializer = serializer;
        this.port = port;
        this.executorService = executorService;
        this.threadModelType = threadModelType;
        this.threadPoolOverflowPolicy = threadPoolOverflowPolicy;
        this.backlog = backlog;
        this.bindAddr = bindAddr;
    }

    /**
     * SimpleTCP server class
     *
     * @param serializer               Protocol data serializer instance to use. Must be thread-safe.
     * @param port                     TCP port on which to listen for client connections
     * @param threadModelType          Threading model to use
     * @param executorService          Thread pool manager to use when handling client threads
     * @param threadPoolOverflowPolicy A policy to use when thread pool overflow is detected
     * @param backlog                  Controls how ServerSocket is created by the component. See {@link java.net.ServerSocket#ServerSocket(int, int, java.net.InetAddress)}  ServerSocket constructor} for details
     */
    public SimpleTCPServer(Serializer serializer, int port, ThreadModelType threadModelType,
                           ExecutorService executorService, ThreadPoolOverflowPolicy threadPoolOverflowPolicy,
                           int backlog) {
        this(serializer, port, threadModelType, executorService, threadPoolOverflowPolicy, backlog, null);
    }

    /**
     * SimpleTCP server class
     *
     * @param serializer               Protocol data serializer instance to use. Must be thread-safe.
     * @param port                     TCP port on which to listen for client connections
     * @param threadModelType          Threading model to use
     * @param executorService          Thread pool manager to use when handling client threads
     * @param threadPoolOverflowPolicy A policy to use when thread pool overflow is detected
     */
    public SimpleTCPServer(Serializer serializer, int port, ThreadModelType threadModelType,
                           ExecutorService executorService, ThreadPoolOverflowPolicy threadPoolOverflowPolicy) {
        this(serializer, port, threadModelType, executorService, threadPoolOverflowPolicy, 0, null);
    }

    /**
     * SimpleTCP server class
     *
     * @param serializer      Protocol data serializer instance to use. Must be thread-safe.
     * @param port            TCP port on which to listen for client connections
     * @param threadModelType Threading model to use
     * @param executorService Thread pool manager to use when handling client threads
     */
    public SimpleTCPServer(Serializer serializer, int port, ThreadModelType threadModelType,
                           ExecutorService executorService) {
        this(serializer, port, threadModelType, executorService, ThreadPoolOverflowPolicy.Terminate, 0, null);
    }

    /**
     * SimpleTCP server class
     *
     * @param serializer      Protocol data serializer instance to use. Must be thread-safe.
     * @param port            TCP port on which to listen for client connections
     * @param threadModelType Threading model to use
     */
    public SimpleTCPServer(Serializer serializer, int port, ThreadModelType threadModelType) {
        this(serializer, port, threadModelType, Executors.newCachedThreadPool(), ThreadPoolOverflowPolicy.Terminate, 0,
                null);
    }

    /**
     * Methos starts the server. First a new ServerSocket is created. Then Acceptor thread is created and starts to accept user connections
     *
     * @param serviceInterface   An interface which is used in RPC communication (must be implemented by @see serviceObjectClass type)
     * @param serviceObjectClass Object type which instance is supposed to receive RPC requests
     * @throws ServerStartupException In case something goes wrong...
     */
    public <T, V extends T> void start(Class<T> serviceInterface, Class<V> serviceObjectClass)
            throws ServerStartupException {
        try {
            socket = new ServerSocket(this.port, this.backlog, this.bindAddr);
        } catch (IOException e) {
            throw new ServerStartupException("Cannot create server socket!", e);
        }
        acceptor = new Thread(
                new Acceptor<T, V>(this, socket, threadModelType, executorService, serializer, serviceInterface,
                        serviceObjectClass, threadPoolOverflowPolicy));
        acceptor.start();
    }

    /**
     * This method requests graceful server shutdown and blocks (for a specified number of milliseconds) until it completes
     *
     * @param millis Milliseconds to give active workers to complete I/O
     * @throws IOException          Thrown if there was an error closing server socket
     * @throws InterruptedException Thrown if there was an interruption of wait for threads to complete
     */
    public void shutdown(long millis) throws IOException, InterruptedException {
        isTerminateRequested = true;
        socket.close();
        acceptor.join(); //On socket.close there will be SocketException in Acceptor's cycle
        executorService.shutdown();
        if (!executorService.awaitTermination(millis, TimeUnit.MILLISECONDS)) {
            List<Runnable> unfinishedJobsList = executorService.shutdownNow();

            //Closing sockets on all unfinished jobs causing them to exit
            for (Runnable job : unfinishedJobsList) {
                ((Worker) job).closeClientSocket();
            }
        }
    }

    @Override
    public final boolean IsTerminateRequested() {
        return this.isTerminateRequested;
    }
}
