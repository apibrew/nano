package io.apibrew.faas.model;

import java.util.Objects;
import io.apibrew.client.EntityInfo;
import io.apibrew.client.Entity;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaasInstance extends Entity {
    
    private java.util.UUID id;
    
    private String name;
    
    private FaasInstance.ServerConfig serverConfig;
    
    private FaasInstance.InstanceLimitations limitations;
    
    private java.util.Map<String, String> annotations;
    
    private String createdBy;
    
    private String updatedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private java.time.Instant createdOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private java.time.Instant updatedOn;
    
    private int version;

    public static final String NAMESPACE = "default";
    public static final String RESOURCE = "FaasInstance";

    @JsonIgnore
    public static final EntityInfo<FaasInstance> entityInfo = new EntityInfo<>("default", "FaasInstance", FaasInstance.class, "faasinstance");

    public static class ServerConfig {
        
        private String host;
        
        private boolean insecure;
        
        private FaasInstance.ServerConfigAuthentication authentication;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public ServerConfig withHost(String host) {
            this.host = host;

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
        public FaasInstance.ServerConfigAuthentication getAuthentication() {
            return authentication;
        }

        public void setAuthentication(FaasInstance.ServerConfigAuthentication authentication) {
            this.authentication = authentication;
        }

        public ServerConfig withAuthentication(FaasInstance.ServerConfigAuthentication authentication) {
            this.authentication = authentication;

            return this;
        }
    }
    public static class ServerConfigAuthentication {
        
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


    public FaasInstance() {
    }

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public FaasInstance withId(java.util.UUID id) {
        this.id = id;

        return this;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FaasInstance withName(String name) {
        this.name = name;

        return this;
    }
    public FaasInstance.ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(FaasInstance.ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public FaasInstance withServerConfig(FaasInstance.ServerConfig serverConfig) {
        this.serverConfig = serverConfig;

        return this;
    }
    public FaasInstance.InstanceLimitations getLimitations() {
        return limitations;
    }

    public void setLimitations(FaasInstance.InstanceLimitations limitations) {
        this.limitations = limitations;
    }

    public FaasInstance withLimitations(FaasInstance.InstanceLimitations limitations) {
        this.limitations = limitations;

        return this;
    }
    public java.util.Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(java.util.Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public FaasInstance withAnnotations(java.util.Map<String, String> annotations) {
        this.annotations = annotations;

        return this;
    }
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public FaasInstance withCreatedBy(String createdBy) {
        this.createdBy = createdBy;

        return this;
    }
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public FaasInstance withUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;

        return this;
    }
    public java.time.Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(java.time.Instant createdOn) {
        this.createdOn = createdOn;
    }

    public FaasInstance withCreatedOn(java.time.Instant createdOn) {
        this.createdOn = createdOn;

        return this;
    }
    public java.time.Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(java.time.Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    public FaasInstance withUpdatedOn(java.time.Instant updatedOn) {
        this.updatedOn = updatedOn;

        return this;
    }
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public FaasInstance withVersion(int version) {
        this.version = version;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FaasInstance)) {
            return false;
        }

        FaasInstance obj = (FaasInstance) o;

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


