package pe.edu.upeu.orden.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Bad Request");
        body.put("message", "Error de validacion en los datos enviados");
        body.put("fields", exception.getBindingResult().getFieldErrors().stream()
                .collect(LinkedHashMap::new, (map, error) -> map.put(error.getField(), error.getDefaultMessage()), Map::putAll));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Error interno del servidor");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(withMessage(base(status, error), message));
    }

    private Map<String, Object> base(HttpStatus status, String error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        return body;
    }

    private Map<String, Object> withMessage(Map<String, Object> body, String message) {
        body.put("message", message);
        return body;
    }
}
