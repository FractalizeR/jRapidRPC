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

/**
 * Class incapsulates RPC Reply
 */
public final class MsgRpcReply {

    private final String errorMessage;
    private final Object methodReturnValue;

    /**
     * Default constructor.
     *
     * @param errorMessage      Error message if an error occured, null otherwise
     * @param methodReturnValue The return value of the RPC method we called
     */
    public MsgRpcReply(String errorMessage, Object methodReturnValue) {
        super();
        this.errorMessage = errorMessage;
        this.methodReturnValue = methodReturnValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getMethodReturnValue() {
        return methodReturnValue;
    }
}
