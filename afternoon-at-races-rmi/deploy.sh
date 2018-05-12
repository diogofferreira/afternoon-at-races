#!/bin/bash

HOSTNAMES=(
    l040101-ws01.ua.pt
    l040101-ws02.ua.pt
    l040101-ws03.ua.pt
    l040101-ws04.ua.pt
    l040101-ws05.ua.pt
    l040101-ws06.ua.pt
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

printf "\n\e[38;5;220m Compressing source folder... \n\e[0m";
mkdir afternoon-at-races-rmi
cp -r src ${FOLDER}/
tar -zcvf ${FOLDER}.tgz ${FOLDER}

for i in ${!HOSTNAMES[@]}; do 
    printf "\n\e[38;5;220m Copying source folder to ${HOSTNAMES[$i]}... \n\e[0m";
    scp ${FOLDER}.tgz $USERNAME@${HOSTNAMES[$i]}:~

    printf "\n\e[38;5;220m Decompressing source folder in ${HOSTNAMES[$i]}... \n\e[0m";
    COMMAND="rm -rf out && tar -zxvf ${FOLDER}.tgz && mkdir -p out &&
        cd ${FOLDER}/src && sh build.sh ${CLASSES[$i]} && cd"

    
    if [ "${CLASSES[$i]}"="GeneralRepositoryMain" ]; then
        printf "\n\e[38;5;220m RMI Registry will be running in ${HOSTNAMES[$i]}... \n\e[0m";
        COMMAND="$COMMAND && mkdir ~/logs && nohup sh set-rmiregistry.sh & ";
    fi

    if [ $i > 5 ] then
        COMMAND="$COMMAND && cd out-client && nohup sh run-client ${CLASSES[$i]} &"
    else
        COMMAND="$COMMAND && cd out-server && nohup sh run-server ${CLASSES[$i]} &"
    fi

    ssh $USERNAME@${HOSTNAMES[$i]} "$COMMAND"
    
    #ssh $USERNAME@${HOSTNAMES[$i]} "screen -d -m java -cp out main.${CLASSES[$i]} &"

    printf "\n\e[38;5;220m Cleaning local source folder... \n\e[0m";
    rm ~/${FOLDER}.tgz
done


