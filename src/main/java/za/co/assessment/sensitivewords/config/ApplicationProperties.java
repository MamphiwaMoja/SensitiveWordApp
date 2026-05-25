package za.co.assessment.sensitivewords.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sensitive-words")
public class ApplicationProperties {

    private final Retry retry = new Retry();

    public Retry getRetry() {
        return retry;
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
}
