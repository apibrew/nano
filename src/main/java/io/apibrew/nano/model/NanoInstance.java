package io.apibrew.nano.model;

import java.util.Objects;
import io.apibrew.client.EntityInfo;
import io.apibrew.client.Entity;
import io.apibrew.client.Client;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.apibrew.client.controller.model.ControllerInstance;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NanoInstance extends Entity implements ControllerInstance {
    
    private NanoInstance.InstanceLimitations limitations;
    
    private NanoInstance.ServerConfig serverConfig;
    
    private java.util.UUID id;
    
    private String name;
    
    private int version;
    
    private NanoInstance.AuditData auditData;
    
    private java.util.Map<String, String> annotations;

    public static final String NAMESPACE = "default";
    public static final String RESOURCE = "NanoInstance";

    @JsonIgnore
    public static final EntityInfo<NanoInstance> entityInfo = new EntityInfo<>("default", "NanoInstance", NanoInstance.class, "nanoinstance");

    public static class ServerConfig implements ControllerInstance.ServerConfig {
        
        private NanoInstance.ServerConfigAuthentication authentication;
        
        private String host;
        
        private int port;
        
        private int httpPort;
        
        private boolean insecure;

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
        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public ServerConfig withPort(int port) {
            this.port = port;

            return this;
        }
        public int getHttpPort() {
            return httpPort;
        }

        public void setHttpPort(int httpPort) {
            this.httpPort = httpPort;
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ServerConfig)) {
                return false;
            }

            ServerConfig obj = (ServerConfig) o;

            if (!Objects.equals(this.authentication, obj.authentication)) {
                return false;
            }
            if (!Objects.equals(this.host, obj.host)) {
                return false;
            }
            if (!Objects.equals(this.port, obj.port)) {
                return false;
            }
            if (!Objects.equals(this.httpPort, obj.httpPort)) {
                return false;
            }
            if (!Objects.equals(this.insecure, obj.insecure)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
           return Objects.hash(authentication, host, port, httpPort, insecure);
        }
    }
    public static class ServerConfigAuthentication implements ControllerInstance.Authentication {
        
        private String token;
        
        private String password;
        
        private String username;

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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ServerConfigAuthentication)) {
                return false;
            }

            ServerConfigAuthentication obj = (ServerConfigAuthentication) o;

            if (!Objects.equals(this.token, obj.token)) {
                return false;
            }
            if (!Objects.equals(this.password, obj.password)) {
                return false;
            }
            if (!Objects.equals(this.username, obj.username)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
           return Objects.hash(token, password, username);
        }
    }
    public static class InstanceLimitations {
        
        private Integer maxExecutionTime;
        
        private Integer maxConcurrentExecutions;

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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InstanceLimitations)) {
                return false;
            }

            InstanceLimitations obj = (InstanceLimitations) o;

            if (!Objects.equals(this.maxExecutionTime, obj.maxExecutionTime)) {
                return false;
            }
            if (!Objects.equals(this.maxConcurrentExecutions, obj.maxConcurrentExecutions)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
           return Objects.hash(maxExecutionTime, maxConcurrentExecutions);
        }
    }
    public static class AuditData {
        
        private String createdBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        private java.time.Instant createdOn;
        
        private String updatedBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        private java.time.Instant updatedOn;

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public AuditData withCreatedBy(String createdBy) {
            this.createdBy = createdBy;

            return this;
        }
        public java.time.Instant getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(java.time.Instant createdOn) {
            this.createdOn = createdOn;
        }

        public AuditData withCreatedOn(java.time.Instant createdOn) {
            this.createdOn = createdOn;

            return this;
        }
        public String getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public AuditData withUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;

            return this;
        }
        public java.time.Instant getUpdatedOn() {
            return updatedOn;
        }

        public void setUpdatedOn(java.time.Instant updatedOn) {
            this.updatedOn = updatedOn;
        }

        public AuditData withUpdatedOn(java.time.Instant updatedOn) {
            this.updatedOn = updatedOn;

            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AuditData)) {
                return false;
            }

            AuditData obj = (AuditData) o;

            if (!Objects.equals(this.createdBy, obj.createdBy)) {
                return false;
            }
            if (!Objects.equals(this.createdOn, obj.createdOn)) {
                return false;
            }
            if (!Objects.equals(this.updatedBy, obj.updatedBy)) {
                return false;
            }
            if (!Objects.equals(this.updatedOn, obj.updatedOn)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
           return Objects.hash(createdBy, createdOn, updatedBy, updatedOn);
        }
    }


    

    public NanoInstance() {
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
    public NanoInstance.AuditData getAuditData() {
        return auditData;
    }

    public void setAuditData(NanoInstance.AuditData auditData) {
        this.auditData = auditData;
    }

    public NanoInstance withAuditData(NanoInstance.AuditData auditData) {
        this.auditData = auditData;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NanoInstance)) {
            return false;
        }

        NanoInstance obj = (NanoInstance) o;

        if (!Objects.equals(this.limitations, obj.limitations)) {
            return false;
        }
        if (!Objects.equals(this.serverConfig, obj.serverConfig)) {
            return false;
        }
        if (!Objects.equals(this.id, obj.id)) {
            return false;
        }
        if (!Objects.equals(this.name, obj.name)) {
            return false;
        }
        if (!Objects.equals(this.version, obj.version)) {
            return false;
        }
        if (!Objects.equals(this.auditData, obj.auditData)) {
            return false;
        }
        if (!Objects.equals(this.annotations, obj.annotations)) {
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


