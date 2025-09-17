package drone.delivery.exception;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() { super(ErrorCode.FORBIDDEN); }
    public ForbiddenException(String message) { super(ErrorCode.FORBIDDEN, message); }
}
