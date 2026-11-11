package cz.uhk.fim.ppro.warehouse.exception;

/**
 * Výjimka vyvolaná při pokusu o vytvoření produktu se skladovým kódem (SKU), který již v systému existuje.
 * Porušuje unikátní integritní omezení katalogu Dřevěnky s.r.o.
 * Mapuje se na HTTP stavový kód 409 CONFLICT.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public class DuplicateSkuException extends RuntimeException {

    private final String sku;

    public DuplicateSkuException(String sku) {
        super(String.format("Produkt se skladovým kódem (SKU) '%s' již v katalogu existuje. Zvolte prosím jiný kód.", sku));
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}

