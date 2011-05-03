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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fractalizer.jrapidrpc.api.ISerializer;
import ru.fractalizer.jrapidrpc.api.MsgRpcReply;
import ru.fractalizer.jrapidrpc.api.MsgRpcRequest;
import ru.fractalizer.jrapidrpc.api.ProtocolDataException;
import ru.fractalizer.jrapidrpc.tools.ReflectionCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread to process client connections
 */
class Worker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private static AtomicLong threadNumber = new AtomicLong(0);

    private ITerminateSignaller terminateSignaller;
    private Socket              clientSocket;
    private ISerializer         serializer;
    private ReflectionCache     reflectionCache;
    private Object              serviceObject;


    /**
     * Default constructor
     *
     * @param terminateSignaller Signaller to use when checking for termination requests
     * @param clientSocket       Connected client socket through which we can communicate to client
     * @param serializer         Serializer to use
     * @param reflectionCache    Reflection cache to use
     * @param serviceObject      Service object to dispatch RPC requests to
     */
    Worker(ITerminateSignaller terminateSignaller, Socket clientSocket, ISerializer serializer,
           ReflectionCache reflectionCache, Object serviceObject) {
        super();
        this.terminateSignaller = terminateSignaller;
        this.clientSocket = clientSocket;
        this.serializer = serializer;
        this.reflectionCache = reflectionCache;
        this.serviceObject = serviceObject;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("SimpleTCPServer Worker Thread #" + threadNumber.incrementAndGet());
        InputStream inputStream;
        OutputStream outputStream;

        //Fetching socket data streams
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        } catch (SocketException e) {
            //Graceful disconnect
            closeClientSocket();
            return;
        } catch (IOException e) {
            logger.error("Error getting streams from client socket", e);
            closeClientSocket();
            return;
        }

        //Client message dispatching cycle
        while (!terminateSignaller.IsTerminateRequested()) {
            //Reading request
            MsgRpcRequest msgRpcRequest;
            try {
                msgRpcRequest = serializer.receiveRpcRequest(inputStream);
            } catch (ProtocolDataException e) {
                if (clientSocket.isClosed()) {
                    logger.error("Error receiving RPC request stream", e);
                }
                closeClientSocket();
                return;
            } catch (Exception e) {
                logger.error("Error receiving RPC request stream", e);
                closeClientSocket();
                return;
            }

            logger.info("Received request to execute method '{}'", msgRpcRequest.getMethodName());
            //Invoking method
            Method method = reflectionCache.getMethodMethod(msgRpcRequest.getMethodName());
            Object methodResult;
            try {
                methodResult = method.invoke(serviceObject, msgRpcRequest.getMethodParameters());
            } catch (IllegalAccessException e) {
                logger.error(
                        "IllegalAccessException when invoking method with name + " + msgRpcRequest.getMethodName() +
                                " on service object of type " + serviceObject.getClass().getName(), e);
                closeClientSocket();
                return;

            } catch (InvocationTargetException e) {
                logger.error(
                        "InvocationTargetException when invoking method with name + " + msgRpcRequest.getMethodName() +
                                " on service object of type " + serviceObject.getClass().getName(), e);
                closeClientSocket();
                return;

            } catch (Exception e) {
                //Attempting to catch any other exception thrown by the method we invoked
                MsgRpcReply rpcReply = new MsgRpcReply(e.getMessage(), null);
                try {
                    serializer.sendRpcReply(rpcReply, outputStream);
                } catch (IOException e1) {
                    logger.error("Error sending RPC reply containing error data for method with name + " +
                            msgRpcRequest.getMethodName(), e1);
                    closeClientSocket();
                    return;
                }
                continue; //Attempting to continue execution
            }

            //Sending reply
            MsgRpcReply rpcReply = new MsgRpcReply(null, methodResult);
            try {
                serializer.sendRpcReply(rpcReply, outputStream);
            } catch (IOException e) {
                logger.error("Error sending RPC reply for method with name + " + msgRpcRequest.getMethodName(), e);
                closeClientSocket();
                return;
            }
            logger.info("Sent '{}' as a result of execution of the method '{}'", methodResult,
                    msgRpcRequest.getMethodName());
        }
        logger.info("terminateSignaller.IsTerminateRequested() flag set. Terminating.");
    }

    public void closeClientSocket() {
        try {
            clientSocket.close();
        } catch (Exception e) {
            logger.warn("Unable to properly close client socket", e);
            //Do nothing
        }
    }
}
