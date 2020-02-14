package org.kasource.spring.nats.config;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import io.nats.client.Options;

@ConfigurationProperties(prefix = "spring.nats")
@Validated
public class NatsConfigProperties {

    private boolean autoStartConsumers = true;
    private Connection connection = new Connection();
    private Jackson jackson;
    private SerDeType serDeType;
    private boolean enableValidation;

    public boolean isAutoStartConsumers() {
        return autoStartConsumers;
    }

    public void setAutoStartConsumers(boolean autoStartConsumers) {
        this.autoStartConsumers = autoStartConsumers;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Jackson getJackson() {
        return jackson;
    }

    public void setJackson(Jackson jackson) {
        this.jackson = jackson;
    }

    public SerDeType getSerDeType() {
        return serDeType;
    }

    public void setSerDeType(SerDeType serDeType) {
        this.serDeType = serDeType;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }

    public static class Jackson {
        @NotNull
        private JsonSchema jsonSchema;

        public JsonSchema getJsonSchema() {
            return jsonSchema;
        }

        public void setJsonSchema(JsonSchema jsonSchema) {
            this.jsonSchema = jsonSchema;
        }
    }


    public static class Connection {
        @NotNull
        private Tls tls = new Tls();
        private Jwt jwt;
        private List<String> urls = new ArrayList<>(List.of(Options.DEFAULT_URL));
        private int maxReconnect = Options.DEFAULT_MAX_RECONNECT;
        private String name;
        private String username;
        private String password;
        private long timeoutSeconds = Options.DEFAULT_CONNECTION_TIMEOUT.toSeconds();
        private long drainTimeoutSeconds = Options.DEFAULT_CONNECTION_TIMEOUT.toSeconds();

        public Tls getTls() {
            return tls;
        }

        public void setTls(Tls tls) {
            this.tls = tls;
        }

        public Jwt getJwt() {
            return jwt;
        }

        public void setJwt(Jwt jwt) {
            this.jwt = jwt;
        }

        public List<String> getUrls() {
            return urls;
        }

        public void setUrls(List<String> urls) {
            this.urls = urls;
        }

        public int getMaxReconnect() {
            return maxReconnect;
        }

        public void setMaxReconnect(int maxReconnect) {
            this.maxReconnect = maxReconnect;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public long getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public long getDrainTimeoutSeconds() {
            return drainTimeoutSeconds;
        }

        public void setDrainTimeoutSeconds(long drainTimeoutSeconds) {
            this.drainTimeoutSeconds = drainTimeoutSeconds;
        }
    }

    public static class Tls {
        private boolean enabled;
        private Resource trustStore;
        private String trustStorePassword;
        private Resource identityStore;
        private String identityStorePassword;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }


        public Resource getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(Resource trustStore) {
            this.trustStore = trustStore;
        }


        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }


        public Resource getIdentityStore() {
            return identityStore;
        }

        public void setIdentityStore(Resource identityStore) {
            this.identityStore = identityStore;
        }

        public String getIdentityStorePassword() {
            return identityStorePassword;
        }

        public void setIdentityStorePassword(String identityStorePassword) {
            this.identityStorePassword = identityStorePassword;
        }
    }

    public static class Jwt {
        @NotEmpty
        private String token;

        @NotEmpty
        private String nKey;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getnKey() {
            return nKey;
        }

        public void setnKey(String nKey) {
            this.nKey = nKey;
        }
    }

    public static class JsonSchema {
        @NotEmpty
        private List<String> scanPackages;
        private boolean enableValidation = true;

        public List<String> getScanPackages() {
            return scanPackages;
        }

        public void setScanPackages(List<String> scanPackages) {
            this.scanPackages = scanPackages;
        }

        public boolean isEnableValidation() {
            return enableValidation;
        }

        public void setEnableValidation(boolean enableValidation) {
            this.enableValidation = enableValidation;
        }
    }
}
