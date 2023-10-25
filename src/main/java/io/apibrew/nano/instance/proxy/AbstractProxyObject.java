package io.apibrew.nano.instance.proxy;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractProxyObject implements ProxyObject {

    private final List<String> members = new ArrayList<>();
    private final Map<String, Method> methodMap = new HashMap<>();
    private final Map<String, Function<Value[], Value>> inlineMethods = new HashMap<>();
    private final Map<String, Field> fieldMap = new HashMap<>();

    public AbstractProxyObject() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            members.add(method.getName());
            methodMap.put(method.getName(), method);
        }
        for (Field field : this.getClass().getDeclaredFields()) {
            members.add(field.getName());
            fieldMap.put(field.getName(), field);
        }
        for (Map.Entry<String, Function<Value[], Value>> entry : methods().entrySet()) {
            members.add(entry.getKey());
            inlineMethods.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Function<Value[], Value>> methods() {
        return Map.of();
    }

    @Override
    public Object getMember(String key) {
        if (methodMap.containsKey(key)) {
            return Value.asValue(invocableMethodCall(methodMap.get(key)));
        }

        if (inlineMethods.containsKey(key)) {
            return Value.asValue(invocableInlineMethodCall(inlineMethods.get(key)));
        }

        if (fieldMap.containsKey(key)) {
            return TransferProxy.wrap(fieldMap.get(key), this);
        }

        throw new UnsupportedOperationException("Member " + key + " not found");
    }

    private Object invocableInlineMethodCall(Function<Value[], Value> valueValueFunction) {
        return (ProxyExecutable) arguments -> {
            try {
                return valueValueFunction.apply(arguments);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Object invocableMethodCall(Method method) {
        return (ProxyExecutable) arguments -> {
            try {
                return method.invoke(this, arguments);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Object getMemberKeys() {
        return members;
    }

    @Override
    public boolean hasMember(String key) {
        return members.contains(key);
    }

    @Override
    public void putMember(String key, Value value) {

    }
}
