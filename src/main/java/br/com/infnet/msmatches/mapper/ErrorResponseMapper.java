package br.com.infnet.msmatches.mapper;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseMapper {

    public Map<String, Object> toResponse(HttpStatus status, String message) {
        return Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
