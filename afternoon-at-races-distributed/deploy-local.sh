#!/bin/bash

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

mkdir -p logs

for i in ${!MODULES[@]}; do
    printf "\n\e[38;5;220m Decompressing and compiling ${MODULES[$i]}... \n\e[0m";
    rm -rf out-${MODULES[$i]} && mkdir out-${MODULES[$i]} && \
        javac -d out-${MODULES[$i]} -sourcepath ${MODULES[$i]}/src ${MODULES[$i]}/src/main/${CLASSES[$i]}.java

    screen -d -m ./run.sh ${MODULES[$i]} main.${CLASSES[$i]} &
done
