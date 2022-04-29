#
# Build stage
#
FROM maven:3.8.4-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-ea-17-jre-slim
COPY --from=build /home/app/target/Consumer-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/consumer.jar
# EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/consumer.jar"]
CMD ["localhost", "skier"]