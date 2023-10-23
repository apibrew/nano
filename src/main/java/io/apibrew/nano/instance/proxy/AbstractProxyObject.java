package io.apibrew.nano.instance.proxy;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractProxyObject implements ProxyObject {

    private final List<String> members = new ArrayList<>();
    private final Map<String, Method> methodMap = new HashMap<>();
    private final Map<String, Field> fieldMap = new HashMap<>();

    public AbstractProxyObject() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            members.add(method.getName());
            methodMap.put(method.getName(), method);
        }
        for (Field field : this.getClass().getDeclaredFields()) {
            members.add(field.getName());
            fieldMap.put(field.getName(), field);
        }
    }

    @Override
    public Object getMember(String key) {
        if (methodMap.containsKey(key)) {
            return new InvocableMethodCall(methodMap.get(key), this);
        }

        if (fieldMap.containsKey(key)) {
            return TransferProxy.wrap(fieldMap.get(key), this);
        }

        throw new UnsupportedOperationException("Member " + key + " not found");
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
