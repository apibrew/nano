package io.apibrew.nano.model;

import java.util.Objects;
import io.apibrew.client.EntityInfo;
import io.apibrew.client.Entity;
import io.apibrew.client.Client;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Log extends Entity {
    
    private java.util.UUID id;
    
    private Log.Level level;
    
    private String message;
    
    private int version;

    public static final String NAMESPACE = "nano";
    public static final String RESOURCE = "Log";

    @JsonIgnore
    public static final EntityInfo<Log> entityInfo = new EntityInfo<>("nano", "Log", Log.class, "nano-log");


    public static enum Level {
        TRACE("TRACE"),
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR"),
        FATAL("FATAL");

        private final String value;

        Level(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    

    public Log() {
    }

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public Log withId(java.util.UUID id) {
        this.id = id;

        return this;
    }
    public Log.Level getLevel() {
        return level;
    }

    public void setLevel(Log.Level level) {
        this.level = level;
    }

    public Log withLevel(Log.Level level) {
        this.level = level;

        return this;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Log withMessage(String message) {
        this.message = message;

        return this;
    }
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Log withVersion(int version) {
        this.version = version;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Log)) {
            return false;
        }

        Log obj = (Log) o;

        if (!Objects.equals(this.id, obj.id)) {
            return false;
        }
        if (!Objects.equals(this.level, obj.level)) {
            return false;
        }
        if (!Objects.equals(this.message, obj.message)) {
            return false;
        }
        if (!Objects.equals(this.version, obj.version)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }

        return id.hashCode();
    }
}


