package ru.fractalizer.jrapidrcdemo;

import ru.fractalizer.jrapidrpc.serializer.protostuff.SerializerCore;
import ru.fractalizer.jrapidrpc.server.simple.SimpleTCPServer;
import ru.fractalizer.jrapidrpc.server.simple.ThreadModelType;

/**
 * Copyright (c) 2011 by Vladislav "FractalizeR" Rastrusny
 */
public class Server {

    private static final int PORT_NUM = 64584;

    public static void main(String[] args) throws Exception {
        SimpleTCPServer server = new SimpleTCPServer(new SerializerCore(ServiceInterface.class, 500), PORT_NUM,
                ThreadModelType.Singleton);

        server.start(ServiceInterface.class, ServiceObject.class);

        System.out.println("Server started. Press any key to terminate...");
        System.in.read();
        server.shutdown();
        System.out.println("Server stopped.");
    }
}
