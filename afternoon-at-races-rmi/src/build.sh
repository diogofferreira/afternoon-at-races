#!bin/bash

javac utils/*.java states/*.java registries/*.java interfaces/*.java sharedRegions/*.java registry/*.java main/*.java

# Copy Register interface
mkdir -p ~/out-registry/interfaces
mkdir -p ~/out-registry/registry
cp interfaces/Register.class ~/out-registry/interfaces/
cp registry/*.class ~/out-registry/registry/

# Copy Server interface
mkdir -p ~/out/utils
mkdir -p ~/out/states
mkdir -p ~/out/registries
mkdir -p ~/out/interfaces
mkdir -p ~/out/sharedRegions
mkdir -p ~/out/main
cp utils/*.class ~/out/utils/
cp states/*.class ~/out/states/
cp registries/*.class ~/out/registries/
cp interfaces/*.class ~/out/interfaces/
cp sharedRegions/*.class ~/out/sharedRegions/
cp main/$1.class ~/out/interfaces/


mkdir -p /home/sd0401/Public/classes
mkdir -p /home/sd0401/Public/classes/interfaces
cp interfaces/*.class /home/sd0401/Public/classes/interfaces
cp set-rmiregistry.sh /home/sd0401
cp set-rmiregistry-alt.sh /home/sd0401


