FROM ghcr.io/graalvm/jdk-community:21

WORKDIR /app

COPY target/faas-jar-with-dependencies.jar /app/faas.jar

CMD ["java", "-jar", "faas.jar"]
