package io.apibrew.nano.instance.proxy;

import io.apibrew.client.Repository;
import io.apibrew.nano.model.Log;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ConsoleProxy extends AbstractProxyObject {

    private final Repository<Log> logRepository;

    public ConsoleProxy(Repository<Log> logRepository) {
        this.logRepository = logRepository;
        registerFunction("trace", (Value[] value) -> log(Log.Level.TRACE, value));
        registerFunction("debug", (Value[] value) -> log(Log.Level.DEBUG, value));
        registerFunction("info", (Value[] value) -> log(Log.Level.INFO, value));
        registerFunction("warn", (Value[] value) -> log(Log.Level.WARN, value));
        registerFunction("error", (Value[] value) -> log(Log.Level.ERROR, value));
        registerFunction("fatal", (Value[] value) -> log(Log.Level.FATAL, value));
        registerFunction("log", (Value[] value) -> log(Log.Level.INFO, value));
    }

    public void log(Log.Level level, Value[] value) {
        String msg = Stream.of(value).map(item -> item.as(Object.class).toString()).collect(Collectors.joining(","));

        log.debug(msg);

        logRepository.create(new Log().withLevel(level).withMessage(msg));
    }
}
