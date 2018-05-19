cd out-client
java -Djava.rmi.server.codebase="file:///home/sd0401/out-client/"\
     -Djava.rmi.server.useCodebaseOnly=false\
     -Djava.security.policy=java.policy\
     main.$1
