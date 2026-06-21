package br.com.infnet.msmatches.exception;

import br.com.infnet.msmatches.mapper.ErrorResponseMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;

    @ExceptionHandler(MatchNotFoundException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(MatchNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponseMapper.toResponse(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(InvalidStatusChangeException.class)
    ResponseEntity<Map<String, Object>> handleInvalidStatusChange(InvalidStatusChangeException exception) {
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.toResponse(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(CoreDataUnavailableException.class)
    ResponseEntity<Map<String, Object>> handleCoreDataUnavailable(CoreDataUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponseMapper.toResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.toResponse(HttpStatus.BAD_REQUEST, "Invalid request payload"));
    }
}
