package za.co.assessment.sensitivewords.web.rest.errors;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
