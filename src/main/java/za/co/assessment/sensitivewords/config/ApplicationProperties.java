package za.co.assessment.sensitivewords.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sensitive-words")
public class ApplicationProperties {

    private final Retry retry = new Retry();

    private final Security security = new Security();

    private final Cache cache = new Cache();

    public Retry getRetry() {
        return retry;
    }

    public Security getSecurity() {
        return security;
    }

    public Cache getCache() {
        return cache;
    }

    public static class Retry {

        private int maxAttempts = 3;

        private long backoffMs = 150L;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBackoffMs() {
            return backoffMs;
        }

        public void setBackoffMs(long backoffMs) {
            this.backoffMs = backoffMs;
        }
    }

    public static class Security {

        private final Basic basic = new Basic();

        public Basic getBasic() {
            return basic;
        }

        public static class Basic {

            private String username = "sensitive-words";

            private String password;

            private String role = "API_USER";

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

            public String getRole() {
                return role;
            }

            public void setRole(String role) {
                this.role = role;
            }
        }
    }

    public static class Cache {

        private final Words words = new Words();

        public Words getWords() {
            return words;
        }

        public static class Words {

            private boolean scheduledRefreshEnabled = true;

            private long refreshIntervalMs = 300_000L;

            public boolean isScheduledRefreshEnabled() {
                return scheduledRefreshEnabled;
            }

            public void setScheduledRefreshEnabled(boolean scheduledRefreshEnabled) {
                this.scheduledRefreshEnabled = scheduledRefreshEnabled;
            }

            public long getRefreshIntervalMs() {
                return refreshIntervalMs;
            }

            public void setRefreshIntervalMs(long refreshIntervalMs) {
                this.refreshIntervalMs = refreshIntervalMs;
            }
        }
    }
}
