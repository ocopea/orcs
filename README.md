# Ocopea Orcs

## Description

The Ocopea orcs component contains all the codebase for building the orchestration components of Ocopea and 
the orcs Docker image.
Visit the Ocopea extensions for platform specific installation instructions.

**Learn More:**

* [Kubernetes Extension](https://github.com/ocopea/kubernetes)
* [Cloud Foundry Extension](https://github.com/ocopea/cloudfoundry)

## Repository Components

- [The Ocopea Hub](https://github.com/ocopea/orcs/tree/master/hub)
- [The Ocopea Site](https://github.com/ocopea/orcs/tree/master/site)
- [Ocopea UI](https://github.com/ocopea/orcs/tree/master/ui)

## API Spec
- [Ocopea Data Service Broker](https://github.com/ocopea/orcs/tree/master/dsb)
- [Ocopea Copy Repository Broker](https://github.com/ocopea/orcs/tree/master/crb)
- [Ocopea PaaS Broker](https://github.com/ocopea/orcs/tree/master/psb)

## How to use

The Orcs repository contains codebase for several independent microservices and components that can be built separately.
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
Once the project is built, development of each service can be done separately using maven from the component folder.
For example, for building the Site microservice and running all Site tests:

```
$ cd site
$ mvn clean install
```

In order to build a Docker image containing the local code, use the deployer/orcs-docker-image project:

```
$ cd deployer/orcs-docker-image
$ mvn clean install
$ cd target/docker
$ ./buildImage.sh
```

This will build the orcs Docker image (ocopea/orcs-k8s-runner) locally on the Docker machine that your 
Docker client is using.


## Contribution

* [Contributing to Ocopea](https://github.com/ocopea/documentation/blob/master/docs/contributing.md)
* [Ocopea Developer Guidelines](https://github.com/ocopea/documentation/blob/master/docs/guidelines.md)

## Quality

Every pull request must pass the following:
1) Code checkstyle must be enforced (use the "checkstyle" maven profile to validate)
2) Unit tests of each modified module must pass
3) Integration tests must pass. to run the integration tests build the "deployer" module using maven
