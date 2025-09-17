package drone.delivery.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Order
@ControllerAdvice
public class GlobalExceptionHandler {

    // ===== Business / Domain =====
    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("[BusinessException] {} - {}", ex.getErrorCode().getCode(), ex.getMessage());
        ErrorCode code = ex.getErrorCode();
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, ex.getMessage(), request));
        }
        return view(code, ex.getMessage(), request);
    }

    // ===== Validation =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus())
                    .body(ErrorResponse.of(code, code.getDefaultMessage(), request, ex.getBindingResult()));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    @ExceptionHandler(BindException.class)
    public Object handleBind(BindException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus())
                    .body(ErrorResponse.of(code, code.getDefaultMessage(), request, ex.getBindingResult()));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus())
                    .body(ErrorResponse.of(code, code.getDefaultMessage(), request, ex.getConstraintViolations()));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    // ===== HTTP / Framework =====
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.METHOD_NOT_ALLOWED;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, code.getDefaultMessage(), request));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.BAD_REQUEST;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, code.getDefaultMessage(), request));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.BAD_REQUEST;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, code.getDefaultMessage(), request));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    // 기본값(권장): Boot의 BasicErrorController가 404를 렌더링하므로 아래 핸들러는 굳이 필요 없음.
    // 아래 설정을 쓰고 싶다면 application.properties에
    // spring.mvc.throw-exception-if-no-handler-found=true
    // spring.web.resources.add-mappings=false
    // 를 추가하고, 이 핸들러를 활성화하세요.
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandler(NoHandlerFoundException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.NOT_FOUND;
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, code.getDefaultMessage(), request));
        }
        return view(code, code.getDefaultMessage(), request);
    }

    // import org.springframework.web.servlet.resource.NoResourceFoundException;

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        ErrorCode code = ErrorCode.NOT_FOUND; // 404
        // 리소스 404는 에러로 도배하지 않도록 warn/info 수준 권장
        // log.warn("[Static 404] {}", ex.getMessage());
        if (wantsJson(request)) {
            return ResponseEntity.status(code.getStatus())
                    .body(ErrorResponse.of(code, code.getDefaultMessage(), request));
        }
        return view(code, code.getDefaultMessage(), request); // error/404.html
    }


    // ===== Fallback =====
    @ExceptionHandler(Exception.class)
    public Object handleAny(Exception ex, jakarta.servlet.http.HttpServletRequest request) {
        // ✨ 안전망: 정적 리소스 없음은 404로 내려보냄
        if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            ErrorCode code = ErrorCode.NOT_FOUND;
            if (wantsJson(request)) {
                return org.springframework.http.ResponseEntity.status(code.getStatus())
                        .body(ErrorResponse.of(code, code.getDefaultMessage(), request));
            }
            return view(code, code.getDefaultMessage(), request);
        }

        ErrorCode code = ErrorCode.INTERNAL_ERROR; // 나머지는 500
        // log.error("[Unhandled] {}", ex.getMessage(), ex);
        if (wantsJson(request)) {
            return org.springframework.http.ResponseEntity.status(code.getStatus())
                    .body(ErrorResponse.of(code, code.getDefaultMessage(), request));
        }
        return view(code, code.getDefaultMessage(), request);
    }


    // ===== helpers =====
    private boolean wantsJson(HttpServletRequest request) {
        // 1) HandlerMethod가 있으면 @ResponseBody or @RestController 여부 확인
        Object best = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        boolean responseBody = false;
        if (best instanceof HandlerMethod hm) {
            responseBody = hm.hasMethodAnnotation(ResponseBody.class)
                    || hm.getBeanType().isAnnotationPresent(RestController.class);
        }
        // 2) 헤더 기반 힌트
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        String xhr = request.getHeader("X-Requested-With");
        String ct = request.getContentType();
        boolean acceptJson = accept != null && accept.contains("application/json");
        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(xhr);
        boolean contentJson = ct != null && ct.contains("application/json");
        return responseBody || acceptJson || ajax || contentJson;
    }

    private ModelAndView view(ErrorCode code, String message, HttpServletRequest request) {
        HttpStatus status = code.getStatus();
        ModelAndView mv = new ModelAndView("error/" + status.value());
        mv.addObject("status", status.value());
        mv.addObject("code", code.getCode()); // BasicErrorController 경로일 땐 없을 수 있음 → 렌더링 시 빈 값
        mv.addObject("error", status.getReasonPhrase());
        mv.addObject("message", (message != null && !message.isBlank()) ? message : code.getDefaultMessage());
        mv.addObject("path", request.getRequestURI());
        mv.setStatus(status);
        return mv;
    }
}
