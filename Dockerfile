FROM openjdk:21-slim

WORKDIR /app

COPY target/logindemo-1.jar logindemo-app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "logindemo-app.jar"]