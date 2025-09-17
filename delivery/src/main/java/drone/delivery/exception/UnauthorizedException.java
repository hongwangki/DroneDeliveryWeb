package drone.delivery.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException() { super(ErrorCode.UNAUTHORIZED); }
    public UnauthorizedException(String message) { super(ErrorCode.UNAUTHORIZED, message); }
}
