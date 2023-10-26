package io.apibrew.nano.instance.proxy;

import io.apibrew.client.GenericRecord;
import io.apibrew.client.model.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericRecordProxy implements ProxyObject {
    private final Resource resource;
    @Getter
    private final GenericRecord record;

    private final List<String> members = new ArrayList<>();
    private final Map<String, MemberInfo> memberInfoMap = new HashMap<>();


    @Data
    @Builder
    private static class MemberInfo {
        private final boolean isProperty;
        private final Resource.Property property;
    }

    public GenericRecordProxy(Resource resource, GenericRecord record) {
        this.resource = resource;
        this.record = record;

        for (Resource.Property property : resource.getProperties()) {
            members.add(property.getName());
            memberInfoMap.put(property.getName(), MemberInfo.builder()
                    .isProperty(true)
                    .property(property)
                    .build());
        }
    }

    @Override
    public Object getMember(String key) {
        MemberInfo memberInfo = memberInfoMap.get(key);

        if (memberInfo.isProperty) {
            return TransferProxy.wrap(memberInfo.getProperty(), record.getProperties().get(key));
        }

        return null;
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
        MemberInfo memberInfo = memberInfoMap.get(key);

        if (memberInfo.isProperty) {
            record.getProperties().put(key, TransferProxy.unwrap(memberInfo.getProperty(), value));
        }
    }
}
