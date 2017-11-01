# Ocopea Copy Repository Broker

## Description

The Copy Repository Broker API (CRB) is the API used by the Ocopea site in order to communicate with Copy Repositories.
Copy Repositories are used by stateful services to store offline copies.
Copy Repositories could be implemented using a simple filesystem, a SaaS offering like S3.
Copy Repositories can expose to data services various protocols for transferring data into the Repository.
See more details in the Swagger definition 
[here](https://github.com/ocopea/orcs/blob/master/crb/crb-web-api/src/main/resources/swagger.yaml).

For Java developers, this repository contains code generator for Java jax-rs interfaces, 
making it easy to write new CRBs.
