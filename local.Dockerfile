FROM ghcr.io/graalvm/jdk-community:21

WORKDIR /app

COPY target/nano.jar /app/nano.jar
COPY target/lib /app/lib

ENV CONFIG_FILE /app/config.json

CMD java -cp "/app/nano.jar:/app/lib/*" io.apibrew.nano.Main --config $CONFIG_FILE
