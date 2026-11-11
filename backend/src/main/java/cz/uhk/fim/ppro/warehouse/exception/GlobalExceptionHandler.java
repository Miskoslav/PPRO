package cz.uhk.fim.ppro.warehouse.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Globální zachytávač a překladač výjimek pro REST API.
 * <p>
 * Zajišťuje, že veškeré doménové výjimky, validační chyby a neočekávané stavy
 * jsou klientovi vráceny v jednotném a předvídatelném formátu {@link ErrorResponse}
 * s korektními HTTP stavovými kódy.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Ošetření validačních chyb vstupních DTO modelů (@Valid / Jakarta Bean Validation).
     * Vrací HTTP 400 Bad Request se slovníkem chyb pro jednotlivá pole.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validační chyba",
                "Odeslaná data obsahují neplatné hodnoty. Zkontrolujte prosím označená pole.",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Ošetření pokusu o přístup k neexistujícímu zdroji (např. neplatné ID produktu).
     * Vrací HTTP 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Zdroj nenalezen",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Ošetření kolize unikátního skladového kódu (duplicitní SKU).
     * Vrací HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSkuException(
            DuplicateSkuException ex, HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Duplicitní položka",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Ošetření porušení doménových byznys pravidel Dřevěnky s.r.o.
     * Vrací HTTP 422 Unprocessable Entity.
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(
            BusinessValidationException ex, HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Porušení obchodního pravidla",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Záchytná metoda pro veškeré neošetřené výjimky serveru.
     * Vrací HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Neočekávaná systémová chyba při zpracování požadavku: {}", request.getRequestURI(), ex);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Interní chyba serveru",
                "Došlo k neočekávané chybě na straně serveru. Podrobnosti byly zaznamenány do logu.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

