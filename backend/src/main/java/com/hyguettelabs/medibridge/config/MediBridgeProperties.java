package com.hyguettelabs.medibridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medibridge")
public class MediBridgeProperties {
    private final Cors cors = new Cors();
    private final Seed seed = new Seed();

    public Cors getCors() { return cors; }
    public Seed getSeed() { return seed; }

    public static class Cors {
        private String allowedOrigins = "http://localhost:3004";
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Seed {
        private boolean enabled = true;
        private String password = "ChangeMe123!";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
