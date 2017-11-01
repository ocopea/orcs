# Ocopea PaaS Broker

## Description

The PaaS Broker API (PSB) is the API used by the Ocopea site in order to communicate with PaaS/ container schedulers.
The PSB API consists of 3 main functions:

- Deploy application
- Undeploy application
- Retrieve application configuration

For Java developers, this repository contains Java jax-rs interfaces, making it easy to write new PSBs

A sample PSB for Kubernetes written in go can be found 
[here](https://github.com/ocopea/kubernetes/tree/master/k8spsb).
