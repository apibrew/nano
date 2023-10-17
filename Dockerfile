FROM ghcr.io/graalvm/jdk-community:21

WORKDIR /app

COPY target/nano-jar-with-dependencies.jar /app/nano.jar

CMD ["java", "-jar", "nano.jar"]
