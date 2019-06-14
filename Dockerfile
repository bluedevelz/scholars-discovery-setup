FROM maven:3.3-jdk-8 AS build

COPY application-dev.yml /app/application-dev.yml

COPY ./scholars-discovery /app/
#ADD ./application-dev.yml /app/application-dev.yml
#COPY application-dev.yml /app/application-dev.yml
#COPY ./application-dev.yml /app/application-dev.yml

RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

#ADD ./application-dev.yml /app/application-dev.yml
#COPY application-dev.yml /app/application-dev.yml
#COPY ./application-dev.yml /app/application-dev.yml


WORKDIR /app


#FROM openjdk:8
#COPY --from=build /app/target/middleware-0.2.0-SNAPSHOT.jar /usr/local/lib/middleware.jar
EXPOSE 9000
