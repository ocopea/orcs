FROM java:8-jre
#FROM tomcat 
MAINTAINER Amit Lieberman<shpandrak@gmail.com>

RUN mkdir -p -v /opt/ocopea
COPY ${final.jar.name}.jar /opt/ocopea/
COPY run.sh /opt/ocopea/
ADD lib /opt/ocopea/lib
RUN chmod +x /opt/ocopea/run.sh

WORKDIR /opt/ocopea

EXPOSE 8080

ENTRYPOINT /opt/ocopea/run.sh
