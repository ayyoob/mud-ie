# mud-ie
SDN based system to translate Manufacturer Usade Description(MUD) to flow rules using Faucet as a controller.

# MUD

This specification allows an operator to lock down the network traffic of the IoT device using access control lists (ACLs) derived from its MUD profile; This work used software defined networking (SDN) as a vehicle to translate MUD profiles into static and dynamic flow rules that can be applied at run-time on OpenFlow-capable switches to limit IoT traffic

# Reference
https://tools.ietf.org/html/draft-ietf-opsawg-mud-25

# Prerequisite
LibPcap (install tcpdump)

# Installation

```sh
$ git clone https://github.com/ayyoob/mud-ie.git
$ cd mud-ie
$ mvn clean install
```

The product pack (zip file) will be available in distribution/target 
# Product execution

This product contains of 3 seperate execution scripts

```sh
 1) Ryu/Faucet/influx db/nats : execute the docker compose file by executing the command docker-compose up
 2) inspector-engine: execute the seer.sh file in the bin directory
 3) mud-collector: execute the seer.shh file in the bin director
```

Before starting the setup it is essential to configure all the switches in faucet.yaml file, A sample file is provided in the faucet/conf directory.


