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

package ru.fractalizer.jrapidrpc.client.simple;

import ru.fractalizer.jrapidrpc.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * Simple TCP client
 */
@SuppressWarnings({"unchecked"})
public class SimpleTCPClient implements InvocationHandler {

    private String       serverHost;
    private int          serverPort;
    private ISerializer  serializer;
    private Socket       socket;
    private OutputStream outputStream;
    private InputStream  inputStream;

    /**
     * Default constructor
     *
     * @param serverHost Host to use as a server
     * @param serverPort Port to use at host
     * @param serializer Serializer to use
     */
    public SimpleTCPClient(String serverHost, int serverPort, ISerializer serializer) {
        super();
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serializer = serializer;
    }

    /**
     * Creates a client socket and connects to the server. If successful, returns an RPC object which methods you can
     * call. All calls will be forwarded to the server
     *
     * @param serviceInterface An RPC interface, defining methods of RPC communication
     * @return A proxy object which methods you can call. All calls will be forwarded to server
     * @throws IOException Is thrown on any connection problem
     */
    public <T> T connect(Class<T> serviceInterface) throws IOException {
        if (!serviceInterface.isInterface()) {
            throw new ClassFormatError("serviceInterface must be of interface type!");
        }
        try {
            socket = new Socket(this.serverHost, this.serverPort);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            if (inputStream != null) {inputStream.close();}
            if (outputStream != null) {outputStream.close();}
            if (socket != null) {socket.close();}

            throw e;
        }
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{serviceInterface}, this);
    }

    /**
     * Closes socket and disconnects from server. Any calls to proxy object after this call will result in IOException.
     *
     * @throws IOException Is thrown on any connection problem
     */
    public void disconnect() throws IOException {
        inputStream.close();
        outputStream.close();

        if (!socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws RpcMethodInvocationException, IOException, ProtocolDataException {
        serializer.sendRpcRequest(new MsgRpcRequest(method.getName(), args), outputStream);

        MsgRpcReply msgRpcReply = serializer.receiveRpcReply(inputStream);
        if (msgRpcReply.getErrorMessage() != null) {
            throw new RpcMethodInvocationException(msgRpcReply.getErrorMessage());
        }
        return msgRpcReply.getMethodReturnValue();
    }

    /**
     * Returns socket connection status
     *
     * @return True, if socket is connected, false otherwise
     */
    public boolean isConnected() {
        return (socket != null) && (!socket.isClosed()) && socket.isConnected();
    }
}