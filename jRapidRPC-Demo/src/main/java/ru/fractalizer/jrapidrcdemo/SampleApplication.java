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

package ru.fractalizer.jrapidrcdemo;

import ru.fractalizer.jrapidrpc.client.simple.SimpleTCPClient;
import ru.fractalizer.jrapidrpc.serializer.protostuff.SerializerCore;
import ru.fractalizer.jrapidrpc.server.simple.SimpleTCPServer;
import ru.fractalizer.jrapidrpc.server.simple.ThreadModelType;

public class SampleApplication {

    /**
     * Some random TCP port number
     */
    private static final int PORT_NUM = 64584;

    /**
     * How many cycles to make to test speed
     */
    private static final int BENCHMARK_CYCLES = 100000; //Make this number of cycles in each benchmark

    /**
     * Main application entry method
     *
     * @param args Arguments (not used).
     * @throws Exception Is thrown in case something went wrong
     */
    public static void main(String[] args) throws Exception {

        //Creating server object, providing serializer, portnumber and threading model to use
        SimpleTCPServer server = new SimpleTCPServer(new SerializerCore(ServiceInterface.class, 500), PORT_NUM,
                ThreadModelType.Singleton);

        //Starting server
        server.start(ServiceInterface.class, ServiceObject.class);
        System.out.println("Server started.");

        //Creating client to comminicate to this server
        SimpleTCPClient client =
                new SimpleTCPClient("localhost", PORT_NUM, new SerializerCore(ServiceInterface.class, 500));

        //Connecting client to the server
        ServiceInterface serviceObjectProxy = client.connect(ServiceInterface.class);

        //Benchmarking how much does the empty call cost in milliseconds
        System.out.println("Benchmarks started...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < BENCHMARK_CYCLES; i++) {
            serviceObjectProxy.voidMethod();
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Average execution time of a void method is %15.4f milliseconds %n",
                (endTime - startTime) * 1.0 / BENCHMARK_CYCLES);


        //Benchmarking how much does the method with one string param and one string result cost in milliseconds
        startTime = System.currentTimeMillis();
        for (int i = 0; i < BENCHMARK_CYCLES; i++) {
            serviceObjectProxy.mirror("Here was Vasya!");
        }
        endTime = System.currentTimeMillis();
        System.out.printf("Average execution time of a method with string param and result is %15.4f milliseconds %n",
                (endTime - startTime) * 1.0 / BENCHMARK_CYCLES);
        System.out.println("Benchmarks finished.");


        //Testing complex type
        ComplexType testObj = new ComplexType();
        testObj.testField1 = 10;
        testObj.testField2 = true;

        //Testing complex method calls
        System.out.println("Testing complex type method calls...");
        ComplexType testObj2 = serviceObjectProxy.complexTypeMethod(testObj);
        assert testObj2.testField1 == testObj.testField1 + 1;
        assert testObj2.testField2 = !testObj.testField2;
        System.out.println("Complex type method tests completed successfully.");

        //Disconnecting from server
        client.disconnect();
        System.out.println("Client disconnected.");
        System.out.println("Press any key to stop the server...");

        //Waiting for a keypress and stopping the server
        System.in.read();
        System.out.println("Shutting down the server...");
        server.shutdown(5000);
        System.out.println("Server stopped.");

    }

}
