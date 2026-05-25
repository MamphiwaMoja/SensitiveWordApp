package za.co.assessment.sensitivewords.web.rest;

public final class ApiPaths {

    public static final String API_V1 = "/api/v1";
    public static final String SENSITIVE_WORDS = API_V1 + "/sensitive-words";
    public static final String SANITIZE = API_V1 + "/sanitize";
    public static final String HEALTH = API_V1 + "/health";

    private ApiPaths() {
    }
}
