# Ocopea Copy Repository Broker

## Description

The Copy Repository Broker API (CRB) is the API used by the Ocopea site in order to communicate with copy repositories.
Copy repositories are used by stateful services to store offline copies.
Copy repositories could be implemented using a simple filesystem, a SaaS offering like S3.
Copy repositories can expose to data services various protocols for transferring data into the repository
See more details in the swagger definition 
[here](https://github.com/ocopea/orcs/blob/master/crb/crb-web-api/src/main/resources/swagger.yaml)

For java developers, this repository contains code generator for java jax-rs interfaces, 
making it easy to write new CRBs.
