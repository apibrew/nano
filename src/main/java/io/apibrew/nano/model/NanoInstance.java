package io.apibrew.nano.model;

import java.util.Objects;

import io.apibrew.client.EntityInfo;
import io.apibrew.client.Entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.apibrew.client.controller.model.ControllerInstance;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NanoInstance extends Entity implements ControllerInstance {

    private java.util.UUID id;

    private String name;

    private NanoInstance.ServerConfig serverConfig;

    private NanoInstance.InstanceLimitations limitations;

    private java.util.Map<String, String> annotations;

    private String createdBy;

    private String updatedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private java.time.Instant createdOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private java.time.Instant updatedOn;

    private int version;

    public static final String NAMESPACE = "default";
    public static final String RESOURCE = "NanoInstance";

    @JsonIgnore
    public static final EntityInfo<NanoInstance> entityInfo = new EntityInfo<>("default", "NanoInstance", NanoInstance.class, "nanoinstance");

    public static class ServerConfig implements ControllerInstance.ServerConfig {

        private String host;
        private int port;
        private int httpPort;

        private boolean insecure;

        private NanoInstance.ServerConfigAuthentication authentication;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public int getHttpPort() {
            return httpPort;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setHttpPort(int httpPort) {
            this.httpPort = httpPort;
        }

        public ServerConfig withHost(String host) {
            this.host = host;

            return this;
        }

        public ServerConfig withPort(int port) {
            this.port = port;

            return this;
        }

        public ServerConfig withHttpPort(int httpPort) {
            this.httpPort = httpPort;

            return this;
        }

        public boolean getInsecure() {
            return insecure;
        }

        public void setInsecure(boolean insecure) {
            this.insecure = insecure;
        }

        public ServerConfig withInsecure(boolean insecure) {
            this.insecure = insecure;

            return this;
        }

        public NanoInstance.ServerConfigAuthentication getAuthentication() {
            return authentication;
        }

        public void setAuthentication(NanoInstance.ServerConfigAuthentication authentication) {
            this.authentication = authentication;
        }

        public ServerConfig withAuthentication(NanoInstance.ServerConfigAuthentication authentication) {
            this.authentication = authentication;

            return this;
        }
    }

    public static class ServerConfigAuthentication implements ControllerInstance.Authentication {

        private String username;

        private String password;

        private String token;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public ServerConfigAuthentication withUsername(String username) {
            this.username = username;

            return this;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public ServerConfigAuthentication withPassword(String password) {
            this.password = password;

            return this;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public ServerConfigAuthentication withToken(String token) {
            this.token = token;

            return this;
        }
    }

    public static class InstanceLimitations {

        private Integer maxConcurrentExecutions;

        private Integer maxExecutionTime;

        public Integer getMaxConcurrentExecutions() {
            return maxConcurrentExecutions;
        }

        public void setMaxConcurrentExecutions(Integer maxConcurrentExecutions) {
            this.maxConcurrentExecutions = maxConcurrentExecutions;
        }

        public InstanceLimitations withMaxConcurrentExecutions(Integer maxConcurrentExecutions) {
            this.maxConcurrentExecutions = maxConcurrentExecutions;

            return this;
        }

        public Integer getMaxExecutionTime() {
            return maxExecutionTime;
        }

        public void setMaxExecutionTime(Integer maxExecutionTime) {
            this.maxExecutionTime = maxExecutionTime;
        }

        public InstanceLimitations withMaxExecutionTime(Integer maxExecutionTime) {
            this.maxExecutionTime = maxExecutionTime;

            return this;
        }
    }


    public NanoInstance() {
    }

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public NanoInstance withId(java.util.UUID id) {
        this.id = id;

        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NanoInstance withName(String name) {
        this.name = name;

        return this;
    }

    public NanoInstance.ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(NanoInstance.ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public NanoInstance withServerConfig(NanoInstance.ServerConfig serverConfig) {
        this.serverConfig = serverConfig;

        return this;
    }

    public NanoInstance.InstanceLimitations getLimitations() {
        return limitations;
    }

    public void setLimitations(NanoInstance.InstanceLimitations limitations) {
        this.limitations = limitations;
    }

    public NanoInstance withLimitations(NanoInstance.InstanceLimitations limitations) {
        this.limitations = limitations;

        return this;
    }

    public java.util.Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(java.util.Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public NanoInstance withAnnotations(java.util.Map<String, String> annotations) {
        this.annotations = annotations;

        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public NanoInstance withCreatedBy(String createdBy) {
        this.createdBy = createdBy;

        return this;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public NanoInstance withUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;

        return this;
    }

    public java.time.Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(java.time.Instant createdOn) {
        this.createdOn = createdOn;
    }

    public NanoInstance withCreatedOn(java.time.Instant createdOn) {
        this.createdOn = createdOn;

        return this;
    }

    public java.time.Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(java.time.Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    public NanoInstance withUpdatedOn(java.time.Instant updatedOn) {
        this.updatedOn = updatedOn;

        return this;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public NanoInstance withVersion(int version) {
        this.version = version;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NanoInstance)) {
            return false;
        }

        NanoInstance obj = (NanoInstance) o;

        if (!Objects.equals(this.id, obj.id)) {
            return false;
        }
        if (!Objects.equals(this.name, obj.name)) {
            return false;
        }
        if (!Objects.equals(this.serverConfig, obj.serverConfig)) {
            return false;
        }
        if (!Objects.equals(this.limitations, obj.limitations)) {
            return false;
        }
        if (!Objects.equals(this.annotations, obj.annotations)) {
            return false;
        }
        if (!Objects.equals(this.createdBy, obj.createdBy)) {
            return false;
        }
        if (!Objects.equals(this.updatedBy, obj.updatedBy)) {
            return false;
        }
        if (!Objects.equals(this.createdOn, obj.createdOn)) {
            return false;
        }
        if (!Objects.equals(this.updatedOn, obj.updatedOn)) {
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


