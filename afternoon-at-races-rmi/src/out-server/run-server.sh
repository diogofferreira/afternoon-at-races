cd out-server
java -Djava.rmi.server.codebase="http://l040101-ws01.ua.pt/sd0401/classes/"\
     -Djava.rmi.server.useCodebaseOnly=true\
     -Djava.security.policy=java.policy\
     main.$1
