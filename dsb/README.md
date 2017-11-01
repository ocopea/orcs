# Ocopea Data Service Broker

## Description

The Data Service Broker API (DSB) is the API used by the Ocopea site in order to communicate with stateful services.
The DSB API consists of 4 main functions:

- Create service
- Remove service
- Create service copy
- Create service from an existing copy

The API, inspired by the Cloud Foundry Service Broker API, can be found 
[here](https://github.com/ocopea/orcs/blob/master/dsb/dsb-web-api/src/main/resources/swagger.yaml).

For Java developers, this repository contains code generator for Java jax-rs API, making it easy to write new DSBs.
A sample DSB for MongoDB written in go can be found 
[here](https://github.com/ocopea/k8s-mongodsb).
