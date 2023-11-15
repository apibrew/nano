package io.apibrew.nano.instance.proxy;

import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ListProxy implements ProxyArray {

    private final List map;

    @Override
    public Object get(long index) {
        return TransferProxy.wrapGeneric(map.get((int) index));
    }

    @Override
    public void set(long index, Value value) {
        map.set((int) index, value.as(Object.class));
    }

    @Override
    public long getSize() {
        return map.size();
    }
}
