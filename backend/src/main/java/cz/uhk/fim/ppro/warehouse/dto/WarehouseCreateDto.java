package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Přepravka dat pro vytvoření nového skladu.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Požadavek na vytvoření nového skladu")
public class WarehouseCreateDto {

    @Schema(description = "Jednoznačný kód skladu (např. PARDUBICE)", example = "PARDUBICE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Kód skladu je povinný údaj")
    @Size(max = 32, message = "Kód skladu smí mít maximálně 32 znaků")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Kód skladu smí obsahovat pouze velká písmena, číslice a pomlčky")
    private String code;

    @Schema(description = "Název skladu", example = "Pobočka Pardubice", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Název skladu je povinný údaj")
    @Size(max = 255, message = "Název skladu smí mít maximálně 255 znaků")
    private String name;

    @Schema(description = "Adresa skladu", example = "Jiráskova 145, Pardubice")
    @Size(max = 255, message = "Adresa smí mít maximálně 255 znaků")
    private String address;

    public WarehouseCreateDto() {}

    public WarehouseCreateDto(String code, String name, String address) {
        this.code = code;
        this.name = name;
        this.address = address;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
