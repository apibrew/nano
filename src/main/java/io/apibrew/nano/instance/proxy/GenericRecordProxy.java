package io.apibrew.nano.instance.proxy;

import io.apibrew.client.GenericRecord;
import io.apibrew.client.model.Resource;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

@RequiredArgsConstructor
public class GenericRecordProxy implements ProxyObject {
    private final Resource resource;
    private final GenericRecord record;
    @Override
    public Object getMember(String key) {
        return null;
    }

    @Override
    public Object getMemberKeys() {
        return null;
    }

    @Override
    public boolean hasMember(String key) {
        return false;
    }

    @Override
    public void putMember(String key, Value value) {

    }
}
