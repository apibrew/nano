package io.apibrew.nano.instance.proxy;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractProxyObject implements ProxyObject {

    private final List<String> members = new ArrayList<>();
    private final Map<String, Function<Value[], Value>> functions = new HashMap<>();

    public void registerFunction(String name, Consumer<Value[]> fn) {
        registerFunction(name, (Value[] values) -> {
            fn.accept(values);
            return null;
        });
    }
    public void registerFunction(String name, Function<Value[], Value> fn) {
        members.add(name);
        functions.put(name, fn);
    }

    @Override
    public Object getMember(String key) {
        if (functions.containsKey(key)) {
            return Value.asValue(invocableInlineMethodCall(functions.get(key)));
        }

        throw new UnsupportedOperationException("Member " + key + " not found");
    }

    @Override
    public void putMember(String key, Value value) {
        if (functions.containsKey(key)) {
            throw new UnsupportedOperationException("Member " + key + " is read only");
        }

        throw new UnsupportedOperationException("Member " + key + " not found");
    }

    private Object invocableInlineMethodCall(Function<Value[], Value> valueValueFunction) {
        return (ProxyExecutable) valueValueFunction::apply;
    }

    @Override
    public Object getMemberKeys() {
        return members;
    }

    @Override
    public boolean hasMember(String key) {
        return members.contains(key);
    }
}
