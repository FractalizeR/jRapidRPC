What's jRapidRPC?
=============================

jRapidRPC is a Java RPC library that allows you to create client-server applications, that talk in objects.

jRapidRPC is:

*   *Small*. The source code for jRapidRPC is very small. You can learn it very fast
*   *Fast*. jRapidRPC is fast due to its compact size and no complexity. Also by default it uses
[ProtoStuff](http://code.google.com/p/protostuff/) serializer, which is one of the fastest in Java world.
*   *Extensible*. jRapidRPC allows you to extend itself. You can write new client-server or new serializer and
it will be nicely integrated into the ecosystem.

How to use?
=============================

jRapidRPC is very simple to use. You create an interface with some RPC methods. Then you annotate each method in the
interface with `@RPCMethod(methodId = ?)` annotation. You must place method id instead of `?` sign, of course. This will
be the common interface between server and client.

In client you just create an instance of the client object and pass this interface class to it.

In server you need to define a service object class for handling user requests, implementing said interface and start the
server.

Please see demo project for an example.

jRapidRPC architecture
=============================

jRapidRPC consists of three components:
*   Client
*   Server
*   Serializer

Client and server provide transport, and serializer is a "glue" between them, which serializes and deserializes RPC
requests and replies.

All library serializers implement ISerializer interface. Client and server doesn't have to implement any.

There are two main classes in the API, that define RPC communication: MsgRpcRequest and MsgRpcReply. Instances of
these classes are provided to serializer to transport to the other end.

Server RPC method is allowed to throw exception. In this case RpcMethodInvocationException is thrown on the client side
with the description of the original exception thrown in server.

How to install library
=============================

jRapidRPC is Maven-driven, so to install and work with it having Maven is recommended. You can download Maven here
http://maven.apache.org. Just unpack it and add it's `/bin` directory to the system PATH.

Then go to jRapidRPC directory inside the project and execute `mvn install` command. Maven will be invoked to build
sources and install them into its repository. If you want to prepare library to be used in other project
`mvn package` will do the deed. In `target` folder you will find fully packaged library installation.

How to build samples
=============================

Before you can build samples, you should install library itself with `mvn install` as described above.

To build the sample application you can go to jRapidRPC-Demo folder, execute `mvn package` command from there, go to
`target` folder and execute jRapidRPC-Demo-XXX.jar from there.