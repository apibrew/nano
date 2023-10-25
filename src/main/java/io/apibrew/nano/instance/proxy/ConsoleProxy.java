package io.apibrew.nano.instance.proxy;

import io.apibrew.nano.instance.GraalVmNanoEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;

@Log4j2
@RequiredArgsConstructor
public class ConsoleProxy extends AbstractProxyObject {

    public void log(Value value) {
        log.debug(value);
    }
}
