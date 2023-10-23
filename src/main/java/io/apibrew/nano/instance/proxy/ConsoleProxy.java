package io.apibrew.nano.instance.proxy;

import io.apibrew.nano.instance.GraalVMCodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

@Log4j2
@RequiredArgsConstructor
public class ConsoleProxy extends AbstractProxyObject {

    private final GraalVMCodeExecutor codeExecutor;

    public void log(Value value) {
        log.debug(value);
    }
}
