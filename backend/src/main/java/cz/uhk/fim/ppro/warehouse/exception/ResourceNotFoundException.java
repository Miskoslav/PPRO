package cz.uhk.fim.ppro.warehouse.exception;

/**
 * Výjimka vyvolaná při pokusu o přístup k neexistujícímu doménovému zdroji (např. produkt nenalezen).
 * Mapuje se na HTTP stavový kód 404 NOT FOUND.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s nebyl nalezen pro %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

