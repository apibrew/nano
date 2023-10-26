package io.apibrew.nano.instance.proxy;

import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ConsoleProxy extends AbstractProxyObject {

    public ConsoleProxy() {
        registerFunction("log", this::log);
    }

    public void log(Value[] value) {
        log.debug(Stream.of(value).map(item -> item.as(Object.class)).collect(Collectors.toList()));
    }
}
