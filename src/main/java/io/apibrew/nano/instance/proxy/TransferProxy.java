package io.apibrew.nano.instance.proxy;

import io.apibrew.client.model.Resource;
import org.graalvm.polyglot.Value;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class TransferProxy {
    public static Value wrap(Field field, Object object) {
        return null;
    }

    public static Object unwrap(Value value) {
        if (value.isString()) {
            return value.asString();
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            return value.asDouble();
        } else if (value.isDate()) {
            return value.asDate();
        } else if (value.isDuration()) {
            return value.asDuration();
        } else if (value.isException()) {
            return value.as(Exception.class);
        } else if (value.isInstant()) {
            return value.asInstant();
        } else if (value.isNull()) {
            return null;
        } else if (value.isTime()) {
            return value.asTime();
        } else if (value.isHostObject()) {
            return value.asHostObject();
        } else if (value.isProxyObject()) {
            return value.asProxyObject();
        } else {
            throw new RuntimeException("Unsupported type: " + value);
        }
    }

    public static Value wrap(Resource.Property property, Object value) {
        return Value.asValue(value);
    }

    public static Object unwrap(Resource.Property property, Value value) {
        return switch (property.getType()) {
            case BOOL -> value.asBoolean();
            case INT32 -> value.asInt();
            case INT64 -> value.asLong();
            case FLOAT32 -> value.asFloat();
            case FLOAT64 -> value.asDouble();
            case STRING, ENUM, UUID, BYTES -> value.asString();
            case TIME -> value.asTime();
            case TIMESTAMP -> value.asInstant();
            case DATE -> value.asDate();
            case OBJECT, MAP, REFERENCE, STRUCT -> value.as(Map.class);
            case LIST -> value.as(List.class);
        };
    }

    public static Value wrapGeneric(Object o) {
        if (o instanceof Map<?,?>) {
            return Value.asValue(new MapProxy((Map<String, Object>) o));
        } else if (o instanceof List<?>) {
            return Value.asValue(new ListProxy((List<Object>) o));
        }
        return Value.asValue(o);
    }
}
