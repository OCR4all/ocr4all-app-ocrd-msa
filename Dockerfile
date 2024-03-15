FROM ocrd/all:2023-04-02
WORKDIR application

# TODO: install java 17

COPY target/ocr4all-app-ocrd-spi-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]