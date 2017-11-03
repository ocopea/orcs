# Ocopea Single Jar

Ocopea single jar deployment builds a single jar that includes all orcs
services including implementations for DSB, PSB, CRB for testing usage and
debugging

The single-jar site deployment includes the following in-memory brokers:
- For PSB it uses shpanpaas
- For DSB it supports H2 DSB and shpanblob DSB
- For CRB it uses ShpanCopyRepo based on shpanblob

## Using the Single Jar runner

In order to run the single jar simply build the project:
 ```
$ mvn clean install
 ```
then run it:

 ```
$ java -jar single-jar-runner/target/docker/single-jar-demo.jar
```

