package interview.identity.api;

import interview.identity.exception.LegacyEntitlementsUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(LegacyEntitlementsUnavailableException.class)
    public ResponseEntity<ApiError> legacyDown(LegacyEntitlementsUnavailableException ex) {
        ApiError body = new ApiError(
                "LEGACY_UNAVAILABLE",
                "Legacy entitlements service is unavailable"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
