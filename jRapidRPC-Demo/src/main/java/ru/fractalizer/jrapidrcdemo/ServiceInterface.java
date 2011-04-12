package ru.fractalizer.jrapidrcdemo;

import ru.fractalizer.jrapidrpc.api.RPCMethod;

/**
 * RPC interface for sample application
 */
public interface ServiceInterface {

    @RPCMethod(methodId = 10)
    void voidMethod();

    @RPCMethod(methodId = 20)
    String mirror(String arg);

    @RPCMethod(methodId = 25)
    void exceptionTest() throws Exception;

    @RPCMethod(methodId = 30)
    ComplexType complexTypeMethod(ComplexType arg);
}
