#!/bin/bash

HOSTNAMES=(
    l040101-ws01.ua.pt
    l040101-ws02.ua.pt
    l040101-ws03.ua.pt
    l040101-ws04.ua.pt
    l040101-ws05.ua.pt
    l040101-ws10.ua.pt
    l040101-ws07.ua.pt
    l040101-ws08.ua.pt
    l040101-ws09.ua.pt
)


CLASSES=(
    GeneralRepositoryMain
    StableMain
    ControlCentreMain
    PaddockMain
    RacingTrackMain
    BettingCentreMain
    HorsesMain
    SpectatorsMain
    BrokerMain
)

USERNAME=sd0401
FOLDER=afternoon-at-races-rmi
REGISTRY_PORT=22400

printf "\n\e[38;5;220m Compressing source folder... \n\e[0m";
mkdir ${FOLDER}
cp -r src ${FOLDER}/
tar -zcvf ${FOLDER}.tgz ${FOLDER}

for i in ${!HOSTNAMES[@]}; do 
    printf "\n\e[38;5;220m Copying source folder to ${HOSTNAMES[$i]}... \n\e[0m";
    scp ${FOLDER}.tgz $USERNAME@${HOSTNAMES[$i]}:~

    printf "\n\e[38;5;220m Decompressing source folder in ${HOSTNAMES[$i]}... \n\e[0m";
    COMMAND="rm -rf out-* && rm -rf Public/classes && tar -zxvf ${FOLDER}.tgz && cd ${FOLDER}/src && sh build.sh ${CLASSES[$i]} && cd && rm -rf ${FOLDER}*"
    ssh $USERNAME@${HOSTNAMES[$i]} "$COMMAND"

    
    if [ "${CLASSES[$i]}" = "GeneralRepositoryMain" ]; then
        printf "\n\e[38;5;220m Setting RMI Registry in ${HOSTNAMES[$i]} at port ${REGISTRY_PORT}... \n\e[0m";
        COMMAND="screen -d -m sh set-rmiregistry.sh ${REGISTRY_PORT} &";
        ssh $USERNAME@${HOSTNAMES[$i]} "$COMMAND"
        printf "\n\e[38;5;220m RMI Registry Engine will be running in ${HOSTNAMES[$i]}... \n\e[0m";
        COMMAND="screen -d -m sh out-registry/run-registry.sh &";
        ssh $USERNAME@${HOSTNAMES[$i]} "$COMMAND"
    fi

    if [ $i -gt 5 ]; then
        printf "\n\e[38;5;220m [CLIENT] ${CLASSES[$i]} will be running in ${HOSTNAMES[$i]}... \n\e[0m";
        COMMAND="screen -d -m sh out-client/run-client.sh ${CLASSES[$i]} &"
    else
        printf "\n\e[38;5;220m [SERVER] ${CLASSES[$i]} will be running in ${HOSTNAMES[$i]}... \n\e[0m";
        COMMAND="screen -d -m sh out-server/run-server.sh ${CLASSES[$i]} &"
    fi
    ssh $USERNAME@${HOSTNAMES[$i]} "$COMMAND"
    
done

printf "\n\e[38;5;220m Cleaning local source folder... \n\e[0m";
rm ${FOLDER}.tgz
rm -rf ${FOLDER}

