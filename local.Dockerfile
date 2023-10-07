FROM ghcr.io/graalvm/jdk-community:21

WORKDIR /app

COPY target/faas.jar /app/faas.jar
COPY target/lib /app/lib

ENV CONFIG_FILE /app/config.json

CMD java -cp "/app/faas.jar:/app/lib/*" io.apibrew.faas.Main --config $CONFIG_FILE
