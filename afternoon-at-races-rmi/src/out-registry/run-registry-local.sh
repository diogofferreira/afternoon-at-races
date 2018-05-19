cd outr-registry
java -Djava.rmi.server.codebase="file:///home/sd0401/out-registry/"\
     -Djava.rmi.server.useCodebaseOnly=false\
     -Djava.security.policy=java.policy\
     registry.ServerRegisterRemoteObject
