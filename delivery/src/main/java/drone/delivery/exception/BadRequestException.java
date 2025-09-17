package drone.delivery.exception;

public class BadRequestException extends BusinessException {
    public BadRequestException() { super(ErrorCode.BAD_REQUEST); }
    public BadRequestException(String message) { super(ErrorCode.BAD_REQUEST, message); }
}
