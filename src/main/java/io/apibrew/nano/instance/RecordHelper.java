package io.apibrew.nano.instance;

import io.apibrew.client.Container;
import io.apibrew.client.GenericRecord;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Resource;
import io.apibrew.nano.instance.proxy.GenericRecordProxy;
import io.apibrew.nano.util.BooleanExpressionUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Value;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.apibrew.client.BooleanExpressionBuilder.and;
import static io.apibrew.client.BooleanExpressionBuilder.eq;

@RequiredArgsConstructor
public class RecordHelper {
    private final CodeExecutor codeExecutor;
    private final Resource resource;
    private final Repository<GenericRecord> repository;

    public GenericRecord resolveRecordFromValue(Value searchValue) {
        if (searchValue.isString()) {
            String id = searchValue.asString();

            final GenericRecord record = new GenericRecord();

            record.setId(UUID.fromString(id));

            return record;
        } else if (searchValue.isProxyObject()) {
            GenericRecordProxy proxy = searchValue.asProxyObject();
            return proxy.getRecord();
        } else if (searchValue.isHostObject()) {
            Map<String, Object> map = searchValue.asHostObject();
            GenericRecord record = new GenericRecord();

            for (String key : map.keySet()) {
                record.getProperties().put(key, map.get(key));
            }

            return record;
        } else {
            Object value = searchValue.as(Object.class);

            if (value instanceof Map<?,?>) {
                Map<String, Object> map = (Map<String, Object>) value;
                GenericRecord record = new GenericRecord();

                for (String key : map.keySet()) {
                    record.getProperties().put(key, map.get(key));
                }

                return record;
            } else {
                throw new IllegalArgumentException("Expected string, got " + searchValue);
            }
        }
    }

    public String identifyRecord(GenericRecord record) {
        if (record.getId() != null) {
            return record.getId().toString();
        } else {
            record = loadRecord(record);

            return record.getId().toString();
        }
    }

    public String identifyRecord(Value searchValue) {
        if (searchValue.isString()) {
            return searchValue.asString();
        } else {
            GenericRecord record = resolveRecordFromValue(searchValue);

            return identifyRecord(record);
        }
    }

    public GenericRecord loadRecord(GenericRecord record) {
        if (record.getId() != null) {
            return repository.get(record.getId().toString());
        }

        Extension.BooleanExpression expression = and(record.getProperties()
                .entrySet()
                .stream()
                .map(entry -> eq(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));

        Container<GenericRecord> result = repository.list(expression);

        if (result.getTotal() == 0) {
            throw new IllegalArgumentException("No record found for " + record);
        } else if (result.getTotal() > 1) {
            throw new IllegalArgumentException("Multiple records found for " + record);
        }

        return result.getContent().get(0);
    }
}
