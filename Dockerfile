FROM maven:3.6.3-jdk-11 AS build

ADD pom.xml /pom.xml
ADD src /src

RUN mvn clean install

FROM openjdk:11.0.10-jre

COPY --from=build target/koppeltaal-2.0-poc-portal.jar /koppeltaal-2.0-poc-portal.jar

ENV TZ="Europe/Amsterdam"

EXPOSE 8080

ENV FHIR_PROFILE "R4"

ENTRYPOINT [ "sh", "-c", "java -jar /koppeltaal-2.0-poc-portal.jar" ]
