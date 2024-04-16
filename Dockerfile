#
# File: Dockerfile
#
# Assembles a Docker image to run ocr4all msa api for ocrd on a ocrd/all container.
#
# Author: Herbert Baier (baier@itbaier.de)
# Date: 16.04.2024
#
ARG TAG
FROM ocrd/all:${TAG}

WORKDIR application

#
# install required packages
#
RUN apt-get -y update

# java 17
RUN apt-get install -y openjdk-17-jdk openjdk-17-jre

#
# install application
#
ARG APP_VERSION
COPY target/ocr4all-app-ocrd-msa-${APP_VERSION}.jar app.jar

#
# start application
#
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]