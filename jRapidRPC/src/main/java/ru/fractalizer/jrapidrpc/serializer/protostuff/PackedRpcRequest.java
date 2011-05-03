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

package ru.fractalizer.jrapidrpc.serializer.protostuff;

/**
 * RPC request structure ready for immediate serialization
 */
class PackedRpcRequest {

    private short methodId;

    private Object[] methodParameters;


    /**
     * Noargs constructor for deserialization
     */
    public PackedRpcRequest() {
        super();
    }


    public PackedRpcRequest(short methodId, Object[] methodParameters) {
        super();
        this.methodId = methodId;
        this.methodParameters = methodParameters;
    }

    public short getMethodId() {
        return methodId;
    }

    public Object[] getMethodParameters() {
        return methodParameters;
    }
}
