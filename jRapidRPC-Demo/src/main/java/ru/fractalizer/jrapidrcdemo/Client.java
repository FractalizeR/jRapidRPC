package ru.fractalizer.jrapidrcdemo;
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

import ru.fractalizer.jrapidrpc.client.simple.SimpleTCPClient;
import ru.fractalizer.jrapidrpc.serializer.protostuff.SerializerCore;

public class Client {

    private static final int PORT_NUM         = 64584;
    private static final int BENCHMARK_CYCLES = 100000;

    public static void main(String[] args) throws Exception {

        SimpleTCPClient client =
                new SimpleTCPClient("localhost", PORT_NUM, new SerializerCore(ServiceInterface.class, 500));

        ServiceInterface proxy = client.connect(ServiceInterface.class);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < BENCHMARK_CYCLES; i++) {
            proxy.voidMethod();
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Average execution time of a void method is %d %n", (endTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < BENCHMARK_CYCLES; i++) {
            proxy.mirror("Here was Vasya!");
        }
        endTime = System.currentTimeMillis();
        System.out.printf("Average execution time of a method with string param is %d %n", (endTime - startTime));
        client.disconnect();
    }

}
