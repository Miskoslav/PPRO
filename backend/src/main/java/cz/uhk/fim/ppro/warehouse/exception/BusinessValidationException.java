package cz.uhk.fim.ppro.warehouse.exception;

/**
 * Výjimka vyvolaná při porušení doménového byznys pravidla (např. záporná marže,
 * pokus o výdej víc, než je na skladě, rozpor v cenách apod.).
 * Mapuje se na HTTP stavový kód 422 UNPROCESSABLE ENTITY.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}

