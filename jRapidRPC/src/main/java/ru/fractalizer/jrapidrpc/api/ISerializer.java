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

package ru.fractalizer.jrapidrpc.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Common interface for library-compliant serializers
 */
public interface ISerializer {

    /**
     * Method should send serialized RPC request to a given stream
     *
     * @param msg          Message to serialize
     * @param outputStream A stream to send request to
     * @throws IOException Is thrown on any transportation problem
     */
    void sendRpcRequest(MsgRpcRequest msg, OutputStream outputStream) throws IOException;

    /**
     * Method should deserialize RPC request from a given stream
     *
     * @param inputStream A stream to read request from
     * @return Deserialized message
     * @throws IOException Is thrown on any transportation problem
     */
    MsgRpcRequest receiveRpcRequest(InputStream inputStream) throws IOException, ProtocolDataException;

    /**
     * Method should serialize RPC reply message to a given stream
     *
     * @param msg          Message to serialize
     * @param outputStream A stream to send reply to
     * @throws IOException Is thrown on any transportation problem
     */
    void sendRpcReply(MsgRpcReply msg, OutputStream outputStream) throws IOException;

    /**
     * Method should deseriazlie RPC reply from a given stream
     *
     * @param inputStream A stream to read reply from
     * @return Deserialized message
     * @throws IOException Is thrown on any transportation problem
     */
    MsgRpcReply receiveRpcReply(InputStream inputStream) throws IOException, ProtocolDataException;
}