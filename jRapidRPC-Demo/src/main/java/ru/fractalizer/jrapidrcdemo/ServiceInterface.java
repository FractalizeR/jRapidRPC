package ru.fractalizer.jrapidrcdemo;

import ru.fractalizer.jrapidrpc.api.RPCMethod;

/**
 * Copyright (c) 2011 by Vladislav "FractalizeR" Rastrusny
 */
public interface ServiceInterface {
    @RPCMethod(methodId = 10)
    void voidMethod();

    @RPCMethod(methodId = 20)
    String mirror(String arg);

    @RPCMethod(methodId = 25)
    void exceptionTest() throws Exception;
}
