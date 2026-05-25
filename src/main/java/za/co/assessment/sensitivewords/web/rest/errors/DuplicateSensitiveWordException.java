package za.co.assessment.sensitivewords.web.rest.errors;

public class DuplicateSensitiveWordException extends RuntimeException {

    public DuplicateSensitiveWordException(String message) {
        super(message);
    }
}
