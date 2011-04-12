package ru.fractalizer.jrapidrcdemo;

/**
 * Copyright (c) 2011 by Vladislav "FractalizeR" Rastrusny
 */
public class ServiceObject implements ServiceInterface {

    @Override
    public void voidMethod() {
        //Doing nothing
    }

    @Override
    public String mirror(String arg) {
        return arg;
    }

    @Override
    public void exceptionTest() throws Exception {
        throw new Exception("This is a test exception message!");
    }

    @Override
    public ComplexType complexTypeMethod(ComplexType arg) {
        arg.testField1++;
        arg.testField2 = !arg.testField2;
        return arg;
    }
}
