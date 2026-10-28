package ravzeno.droppers_service.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.Instant;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ravzeno.droppers_service.common.exception.core.FileConflictException;
import ravzeno.droppers_service.common.exception.core.InvalidRequestException;
import ravzeno.droppers_service.common.exception.core.ResourceNotFoundException;
import ravzeno.droppers_service.common.exception.core.SecurityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException e, HttpServletRequest r) {
        return error(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", e, r);
    }

    @ExceptionHandler(SecurityViolationException.class)
    ResponseEntity<ApiError> security(SecurityViolationException e, HttpServletRequest r) {
        return error(HttpStatus.FORBIDDEN, "PATH_OUTSIDE_STORAGE_ROOT", e, r);
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> invalid(RuntimeException e, HttpServletRequest r) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e, r);
    }

    @ExceptionHandler({FileConflictException.class, FileAlreadyExistsException.class})
    ResponseEntity<ApiError> conflict(Exception e, HttpServletRequest r) {
        return error(HttpStatus.CONFLICT, "FILE_ALREADY_EXISTS", e, r);
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<ApiError> io(IOException e, HttpServletRequest r) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_SYSTEM_ERROR", e, r);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, Exception e, HttpServletRequest r) {
        return ResponseEntity.status(status)
                .body(
                        new ApiError(
                                Instant.now(),
                                status.value(),
                                code,
                                e.getMessage(),
                                r.getRequestURI()));
    }
}
