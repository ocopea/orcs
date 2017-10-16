# Ocopea Orcs

## Description

The Ocopea orcs component contains all the codebase for building the orchestration components of ocopea and 
the orcs docker image.
Visit the ocopea [kubernetes](https://github.com/ocopea/kubernetes) and 
[cloud foundry](https://github.com/ocopea/cloudfoundry) for installation

## Repository Components

- [The Ocopea Hub](https://github.com/ocopea/orcs/tree/master/hub)
- [The Ocopea Site](https://github.com/ocopea/orcs/tree/master/site)
- [Ocopea UI](https://github.com/ocopea/orcs/tree/master/ui)

## API Spec
- [Ocopea Data Service Broker](https://github.com/ocopea/orcs/tree/master/dsb)
- [Ocopea Copy Repository Broker](https://github.com/ocopea/orcs/tree/master/crb)
- [Ocopea Paas Broker](https://github.com/ocopea/orcs/tree/master/psb)

## How to use

The Orcs repository contains codebase for several independent microservices and components that can be built separately
It is however recommended to build the project as a whole at least once in order to populate the local maven repository
with latest versions of all libraries.
The project is using maven, so in order to build, clone the repo and use the mvn command.

### Pre-requisites
- maven 3.2.5
- JDK8

### How-to-build
```
$ git clone https://github.com/ocopea/orcs.git
$ cd orcs
$ mvn clean install
```
once the project is built, development of each service can be done separately using maven from the component folder.
For example, for building the site microservice and running all site tests

```
$ cd site
$ mvn clean install
```

In order to build a docker image containing the local code, use the deployer/orcs-docker-image project
```
$ cd deployer/orcs-docker-image
$ mvn clean install
$ cd target/docker
$ ./buildImage.sh
```

This will build the orcs docker image (ocopea/orcs-k8s-runner) locally on the docker machine that your 
docker client is using.


## Contribution
Create a fork of the project into your own repository. Make all your necessary changes and create a pull request 
with a description on what was added or removed and details explaining the changes in lines of code. 
If approved, project owners will merge it.

## Quality

Every pull request must pass the following:
1) Code checkstyle must be enforced (use the "checkstyle" maven profile to validate)
2) Unit tests of each modified module must pass
3) Integration tests must pass. to run the integration tests build the "deployer" module using maven


## Licensing
**{code} does not provide legal guidance on which open source license should be used in projects. We do expect that all
projects and contributions will have a valid open source license within the repo/project, or align to the appropriate 
license for the project/contribution.** 
The default license used for {code} Projects is the [MIT License](http://codedellemc.com/sampledocs/LICENSE "LICENSE").

Ocopea kubernetes extension is freely distributed under the 
[MIT License](http://emccode.github.io/sampledocs/LICENSE "LICENSE"). See LICENSE for details.


## Support
-------
Please file bugs and issues on the Github issues page for this project. 
This is to help keep track and document everything related to this repo. 
For general discussions and further support you can join the 
[{code} Community slack channel](http://community.codedellemc.com/). 
The code and documentation are released with no warranties or SLAs and are intended to be supported through a 
community driven process.
