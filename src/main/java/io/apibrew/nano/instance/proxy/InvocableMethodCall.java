package io.apibrew.nano.instance.proxy;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
@Log4j2
public class InvocableMethodCall implements ProxyExecutable {

    private final Method method;
    private final Object instance;

    @Override
    @SneakyThrows
    public Object execute(Value... arguments) {
        return method.invoke(instance, arguments);
    }
}
