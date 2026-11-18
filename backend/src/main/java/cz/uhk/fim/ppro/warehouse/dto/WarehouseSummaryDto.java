package cz.uhk.fim.ppro.warehouse.dto;

import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Přepravka dat (DTO) pro souhrnný přehled skladu včetně agregovaných stavů zásob.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Souhrnný přehled skladu s agregovanými zásobami")
public class WarehouseSummaryDto {

    private Long id;
    private String code;
    private String name;
    private String address;
    private Boolean isActive;
    private int totalPhysicalQuantity;
    private int totalReservedQuantity;
    private int totalAvailableQuantity;

    public WarehouseSummaryDto() {}

    public WarehouseSummaryDto(Warehouse warehouse, int totalPhysical, int totalReserved) {
        this.id = warehouse.getId();
        this.code = warehouse.getCode();
        this.name = warehouse.getName();
        this.address = warehouse.getAddress();
        this.isActive = warehouse.getIsActive();
        this.totalPhysicalQuantity = totalPhysical;
        this.totalReservedQuantity = totalReserved;
        this.totalAvailableQuantity = Math.max(0, totalPhysical - totalReserved);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public int getTotalPhysicalQuantity() { return totalPhysicalQuantity; }
    public void setTotalPhysicalQuantity(int totalPhysicalQuantity) { this.totalPhysicalQuantity = totalPhysicalQuantity; }

    public int getTotalReservedQuantity() { return totalReservedQuantity; }
    public void setTotalReservedQuantity(int totalReservedQuantity) { this.totalReservedQuantity = totalReservedQuantity; }

    public int getTotalAvailableQuantity() { return totalAvailableQuantity; }
    public void setTotalAvailableQuantity(int totalAvailableQuantity) { this.totalAvailableQuantity = totalAvailableQuantity; }
}
