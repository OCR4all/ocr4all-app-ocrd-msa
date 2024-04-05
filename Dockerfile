FROM ocrd/all:2023-04-02
WORKDIR application

# install java 17
RUN apt-get -y update
RUN apt-get install -y openjdk-17-jdk openjdk-17-jre

COPY target/ocr4all-app-ocrd-msa-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]