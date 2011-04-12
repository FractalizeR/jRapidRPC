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

package ru.fractalizer.jrapidrpc.serializer.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufException;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import ru.fractalizer.jrapidrpc.api.ISerializer;
import ru.fractalizer.jrapidrpc.api.MsgRpcReply;
import ru.fractalizer.jrapidrpc.api.MsgRpcRequest;
import ru.fractalizer.jrapidrpc.api.ProtocolDataException;
import ru.fractalizer.jrapidrpc.tools.ReflectionCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * RPC data serializer based on http://code.google.com/p/protostuff/ library. It is thread-safe
 */
public final class SerializerCore implements ISerializer {

    /**
     * Here we cache metadata about the service class. Cache should be thread-safe
     */
    private ReflectionCache reflectionCache;

    /**
     * Cached schema for serialized RPC request
     */
    private Schema<PackedRpcRequest> packedRpcRequestSchema = RuntimeSchema.getSchema(PackedRpcRequest.class);

    /**
     * Cached schema for serialized RPC reply
     */
    private Schema<PackedRpcReply> packedRpcReplySchema = RuntimeSchema.getSchema(PackedRpcReply.class);

    /**
     * Optimal buffer size for serializing messages
     */
    private int optimalBufferSize;

    /**
     * Local serialization buffer for each thread
     */
    private ThreadLocal<LinkedBuffer> linkedBuffer = new ThreadLocal<LinkedBuffer>() {
        @Override
        protected LinkedBuffer initialValue() {
            return LinkedBuffer.allocate(optimalBufferSize);
        }
    };

    /**
     * Default constructor
     *
     * @param serviceInterface  An interface containing RPC methods
     * @param optimalBufferSize Buffer size in bytes that is able to hold the largest message possible in this RPC protocol
     *                          If you will provide a value that is too small, several sequential linked buffers will be created
     *                          automatically (and the thing will be a little slower with that)
     */
    public SerializerCore(Class<?> serviceInterface, int optimalBufferSize) {
        super();
        reflectionCache = new ReflectionCache(serviceInterface);
        this.optimalBufferSize = optimalBufferSize;
    }


    @Override
    public void sendRpcRequest(MsgRpcRequest msg, OutputStream outputStream) throws IOException {
        LinkedBuffer linkedBuffer = this.linkedBuffer.get();
        linkedBuffer.clear();
        short methodId = reflectionCache.getMethodId(msg.getMethodName());
        PackedRpcRequest packedRpcRequest = new PackedRpcRequest(methodId, msg.getMethodParameters());
        ProtostuffIOUtil.writeDelimitedTo(outputStream, packedRpcRequest, packedRpcRequestSchema, linkedBuffer);
    }

    @Override
    public MsgRpcRequest receiveRpcRequest(InputStream inputStream) throws IOException, ProtocolDataException {
        LinkedBuffer linkedBuffer = this.linkedBuffer.get();
        linkedBuffer.clear();
        PackedRpcRequest packedRpcRequest = new PackedRpcRequest();

        //TODO: handle situation when ProtobufException is thrown by socket graceful disconnect
        try {
            ProtostuffIOUtil.mergeDelimitedFrom(inputStream, packedRpcRequest, packedRpcRequestSchema, linkedBuffer);
        } catch (ProtobufException e) {
            throw new ProtocolDataException("Something is wrong with the data", e);
        }

        return new MsgRpcRequest(reflectionCache.getMethodName(packedRpcRequest.getMethodId()),
                packedRpcRequest.getMethodParameters());
    }

    @Override
    public void sendRpcReply(MsgRpcReply msg, OutputStream outputStream) throws IOException {
        LinkedBuffer linkedBuffer = this.linkedBuffer.get();
        linkedBuffer.clear();
        PackedRpcReply packedRpcReply = new PackedRpcReply(msg.getErrorMessage(), msg.getMethodReturnValue());
        ProtostuffIOUtil.writeDelimitedTo(outputStream, packedRpcReply, packedRpcReplySchema, linkedBuffer);
    }

    @Override
    public MsgRpcReply receiveRpcReply(InputStream inputStream) throws IOException, ProtocolDataException {
        LinkedBuffer linkedBuffer = this.linkedBuffer.get();
        linkedBuffer.clear();
        PackedRpcReply packedRpcReply = new PackedRpcReply();

        //TODO: handle situation when ProtobufException is thrown by socket graceful disconnect
        try {
            ProtostuffIOUtil.mergeDelimitedFrom(inputStream, packedRpcReply, packedRpcReplySchema, linkedBuffer);
        } catch (ProtobufException e) {
            throw new ProtocolDataException("Something is wrong with the data", e);
        }

        return new MsgRpcReply(packedRpcReply.getErrorMessage(), packedRpcReply.getMethodReturnValue());
    }
}
