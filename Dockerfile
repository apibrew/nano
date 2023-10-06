FROM amazoncorretto:21.0.0

WORKDIR /app

COPY target/faas-jar-with-dependencies.jar /app/faas.jar

CMD ["java", "-jar", "faas.jar"]
