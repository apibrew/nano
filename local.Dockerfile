FROM ghcr.io/graalvm/jdk-community:21

WORKDIR /app

COPY target/lib /app/lib
COPY target/nano.jar /app/nano.jar

ENV CONFIG_FILE /app/config.json

CMD java -cp "/app/nano.jar:/app/lib/*" io.apibrew.nano.Main --config $CONFIG_FILE
