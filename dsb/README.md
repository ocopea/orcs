# Ocopea Data Service Broker

## Description

The Data Service Broker API (DSB) is the API used by the Ocopea site in order to communicate with stateful services.
The DSB API consists of 4 high main functions:

- Create service
- Remove service
- Create service copy
- Create service from an existing copy

The API, inspired by the Cloud Foundry Service Broker API, can be found 
[here](https://github.com/ocopea/orcs/blob/master/dsb/dsb-web-api/src/main/resources/swagger.yaml)

For java developers, this repository contains code generator for java jax-rs api, making it easy to write new DSBs
Sample DSB for mongodb written in go can be found 
[here](https://github.com/ocopea/mongodsb)