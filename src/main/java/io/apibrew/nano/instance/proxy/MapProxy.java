package io.apibrew.nano.instance.proxy;

import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MapProxy implements ProxyObject {

    private final Map<String, Object> map;

    @Override
    public Object getMember(String key) {
        return TransferProxy.wrapGeneric(map.get(key));
    }

    @Override
    public Object getMemberKeys() {
        return new ListProxy(new ArrayList<>(map.keySet()));
    }

    @Override
    public boolean hasMember(String key) {
        return map.containsKey(key);
    }

    @Override
    public void putMember(String key, Value value) {
        map.put(key, value.as(Object.class));
    }
}
