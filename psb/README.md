# Ocopea Paas Broker

## Description

The Paas Broker API (PSB) is the API used by the Ocopea site in order to communicate paas/ container schedulers
The DSB API consists of 4 high main functions:

- Deploy application
- Undeploy application
- Retrieve application configuration

For java developers, this repository contains java jax-rs interfaces, making it easy to write new PSBs

Sample PSB for kubernetes written in go can be found 
[here](https://github.com/ocopea/kubernetes/tree/master/k8spsb)