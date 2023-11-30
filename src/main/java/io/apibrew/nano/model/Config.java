package io.apibrew.nano.model;

import java.util.Objects;
import io.apibrew.client.EntityInfo;
import io.apibrew.client.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config extends Entity {
    
    private java.util.UUID id;
    
    private java.util.List<NanoInstance> instances;
    
    private Config.ServerConfig controller;
    
    private int version;

    public static final String NAMESPACE = "default";
    public static final String RESOURCE = "Config";

    @JsonIgnore
    public static final EntityInfo<Config> entityInfo = new EntityInfo<>("default", "Config", Config.class, "config");

    public static class ServerConfig {
        
        private String host;
        private int port;
        private int httpPort;

        private boolean insecure;
        
        private Config.ServerConfigAuthentication authentication;

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
        public Config.ServerConfigAuthentication getAuthentication() {
            return authentication;
        }

        public void setAuthentication(Config.ServerConfigAuthentication authentication) {
            this.authentication = authentication;
        }

        public ServerConfig withAuthentication(Config.ServerConfigAuthentication authentication) {
            this.authentication = authentication;

            return this;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getHttpPort() {
            return httpPort;
        }

        public void setHttpPort(int httpPort) {
            this.httpPort = httpPort;
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


    public Config() {
    }

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public Config withId(java.util.UUID id) {
        this.id = id;

        return this;
    }
    public java.util.List<NanoInstance> getInstances() {
        return instances;
    }

    public void setInstances(java.util.List<NanoInstance> instances) {
        this.instances = instances;
    }

    public Config withInstances(java.util.List<NanoInstance> instances) {
        this.instances = instances;

        return this;
    }
    public Config.ServerConfig getController() {
        return controller;
    }

    public void setController(Config.ServerConfig controller) {
        this.controller = controller;
    }

    public Config withController(Config.ServerConfig controller) {
        this.controller = controller;

        return this;
    }
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Config withVersion(int version) {
        this.version = version;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Config)) {
            return false;
        }

        Config obj = (Config) o;

        if (!Objects.equals(this.id, obj.id)) {
            return false;
        }
        if (!Objects.equals(this.instances, obj.instances)) {
            return false;
        }
        if (!Objects.equals(this.controller, obj.controller)) {
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


