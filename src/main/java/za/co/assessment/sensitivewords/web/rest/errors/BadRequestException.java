package za.co.assessment.sensitivewords.web.rest.errors;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
