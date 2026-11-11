package cz.uhk.fim.ppro.warehouse.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardizovaný formát chybové odpovědi REST API.
 * Inspirováno specifikací RFC 7807 (Problem Details).
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Jednotný formát chybového hlášení systému")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Čas vzniku chyby")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP stavový kód", example = "400")
    private int status;

    @Schema(description = "Název chyby", example = "Chyba validace vstupních dat")
    private String error;

    @Schema(description = "Srozumitelný popis příčiny chyby pro klienta", example = "Pole 'name' nesmí být prázdné.")
    private String message;

    @Schema(description = "Cesta požadavku, který selhal", example = "/api/v1/products")
    private String path;

    @Schema(description = "Seznam dílčích chyb u jednotlivých formulářových polí")
    private Map<String, String> fieldErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(status, error, message, path);
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}

