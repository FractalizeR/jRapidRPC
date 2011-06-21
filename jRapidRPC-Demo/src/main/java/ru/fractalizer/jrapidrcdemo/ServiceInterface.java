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

package ru.fractalizer.jrapidrcdemo;

import ru.fractalizer.jrapidrpc.api.RpcAfterConnect;
import ru.fractalizer.jrapidrpc.api.RpcMethod;

import java.net.Socket;

/**
 * RPC interface for sample application
 */
public interface ServiceInterface {

    @RpcMethod(methodId = 10)
    void voidMethod();

    @RpcMethod(methodId = 20)
    String mirror(String arg);

    @RpcMethod(methodId = 25)
    void exceptionTest() throws Exception;

    @RpcMethod(methodId = 30)
    ComplexType complexTypeMethod(ComplexType arg);

    @RpcAfterConnect
    boolean prelogin(Socket clientSocket);
}
