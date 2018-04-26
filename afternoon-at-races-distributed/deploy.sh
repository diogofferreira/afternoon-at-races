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

MODULES=(
    general-repository
    stable
    control-centre
    paddock
    racing-track
    betting-centre
    horses
    spectators
    broker
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

for i in ${!HOSTNAMES[@]}; do 
    printf "\n\e[38;5;220m Compressing ${MODULES[$i]}... \n\e[0m";
    tar -zcvf ${MODULES[$i]}.tgz ${MODULES[$i]}
    
    printf "\n\e[38;5;220m Copying ${MODULES[$i]} to ${HOSTNAMES[$i]}... \n\e[0m";
    scp ${MODULES[$i]}.tgz $USERNAME@${HOSTNAMES[$i]}:~

    printf "\n\e[38;5;220m Decompressing ${MODULES[$i]} in ${HOSTNAMES[$i]}... \n\e[0m";
    ssh $USERNAME@${HOSTNAMES[$i]} "tar -zxvf ${MODULES[$i]}.tgz && 
        mkdir out && mkdir logs &&
        javac -d out -sourcepath src ${MODULES[$i]}/src/main/${CLASSES[$i]}.java &&
        rm -rf ${MODULES[$i]} &&
        java -cp out main.${CLASSES[$i]}  &&"

    echo "\n\e[38;5;220m Cleaning local ${MODULES[$i]}... \n\e[0m";
    rm ${MODULES[$i]}.tgz

done
