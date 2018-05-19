#!bin/bash

SERVERCLASSES=(
    GeneralRepositoryMain
    StableMain
    ControlCentreMain
    PaddockMain
    RacingTrackMain
    BettingCentreMain
)

CLIENTCLASSES=(
    HorsesMain
    SpectatorsMain
    BrokerMain
)

javac utils/*.java states/*.java registries/*.java interfaces/*.java sharedRegions/*.java entities/*.java registry/*.java main/*.java

if [ "$1" = "GeneralRepositoryMain" ]; then
    # Copy Register directory
    cp -r out-registry ~/
    cp interfaces/Register.class ~/out-registry/interfaces/
    cp registry/*.class ~/out-registry/registry/

    # Copy to HTTP server folder
    mkdir -p /home/sd0401/Public/classes
    mkdir -p /home/sd0401/Public/classes/interfaces
    mkdir -p /home/sd0401/Public/classes/states
    mkdir -p /home/sd0401/Public/classes/registries
    cp interfaces/*.class /home/sd0401/Public/classes/interfaces
    cp states/*.class /home/sd0401/Public/classes/states
    cp registries/*.class /home/sd0401/Public/classes/registries
    cp set-rmiregistry.sh /home/sd0401
    cp set-rmiregistry-local.sh /home/sd0401
fi

# Copy Client directory
if [[ " ${CLIENTCLASSES[*]} " == *" $1 "* ]]; then
    cp -r out-client ~/
    cp utils/*.class ~/out-client/utils/
    cp states/*.class ~/out-client/states/
    cp registries/*.class ~/out-client/registries/
    cp interfaces/*.class ~/out-client/interfaces/
    cp entities/*.class ~/out-client/entities/
    cp main/$1.class ~/out-client/main/
fi

# Copy Server directory
if [[ " ${SERVERCLASSES[*]} " == *" $1 "* ]]; then
    cp -r out-server ~/
    cp utils/*.class ~/out-server/utils/
    cp states/*.class ~/out-server/states/
    cp registries/*.class ~/out-server/registries/
    cp interfaces/*.class ~/out-server/interfaces/
    cp sharedRegions/*.class ~/out-server/sharedRegions/
    cp main/$1.class ~/out-server/main/
fi
