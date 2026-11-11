# IMPLEMENTATION_PLAN.md – Implementační plán projektu

> **Projekt:** Zadání B – Sklad pro malý e-shop (Dřevěnka s.r.o.)  
> **Předmět:** Pokročilé programování (PPRO), FIM UHK (ZS 2026/2027)  
> **Cíl:** Postupná realizace robustního třívrstvého skladového systému.  
> **Milestone 1:** První funkční demo jediné klíčové entity (`Product`) ve Spring Boot pro prezentaci klientovi / vyučujícímu.

---

## 🎯 Milestone 1: Demo první entity (`Product`) ve Spring Boot

V této fázi budujeme plně funkční vertikálu (od databáze až po webové UI a Swagger) pro správu dřevěných hraček e-shopu Dřevěnka s.r.o.

### Fáze 1: Inicializace Spring Boot projektu a Maven konfigurace
- [x] **1.1** Vytvoření adresářové struktury projektu (`backend/` se standardní Maven layout strukturou).
- [x] **1.2** Konfigurace `pom.xml` se všemi potřebnými závislostmi:
  - Spring Boot 3.x, Java 21+
  - Spring Data JPA + PostgreSQL driver + Flyway (migrace)
  - Spring Boot Validation (Jakarta)
  - SpringDoc OpenAPI (Swagger UI)
  - Spring Boot Test + H2 (pro testovací účely)
- [x] **1.3** Zajištění Maven wrapperu (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`).
- [x] **1.4** Konfigurace aplikačních profilů v `application.yml` (profily `local` a `docker`, datasource, JPA nastavení, Flyway).

### Fáze 2: Databázová vrstva a verzované Flyway migrace
- [x] **2.1** Vytvoření migračního skriptu `V1__create_products_table.sql`:
  - DDL tabulky `products` (id, sku, name, description, purchase_price, selling_price, min_stock_level, is_active, created_at, updated_at).
  - Databázová integritní omezení (UNIQUE na SKU, CHECK na nezáporné ceny a minimální zásobu).
- [x] **2.2** Vytvoření seedovací migrace `V2__seed_demo_products.sql`:
  - Syntetická data reprezentující reálný sortiment Dřevěnky (káča, vláček, bukové kostky, domeček pro panenky, tahací pejsek).
- [x] **2.3** Vytvoření JPA entity `Product`:
  - Mapování tabulky, anotace `@Entity`, `@Table`, validace, timestampy.
  - **Důsledné Javadoc komentáře** vysvětlující účel a vazby každého pole.
- [x] **2.4** Vytvoření Spring Data JPA rozhraní `ProductRepository`:
  - Metody pro vyhledání podle SKU, podle aktivity a dotaz na produkty pod minimální bezpečnou zásobou (`findActiveWithMinimumStockConfigured`).

### Fáze 3: Byznys logika (Service vrstva) a validace
- [x] **3.1** Vytvoření DTO modelů pro přenos dat:
  - `ProductCreateDto` – validační anotace (`@NotBlank`, `@Positive`, unikátní SKU).
  - `ProductUpdateDto` – bezpečné úpravy parametrů a cen bez možnosti změny SKU.
  - `ProductResponseDto` – výstupní model s formátovanými údaji, marží v CZK i v % a aliasem `skuCode`.
- [x] **3.2** Globální zpracování výjimek a doménové chyby:
  - `ResourceNotFoundException` (404), `DuplicateSkuException` (409), `BusinessValidationException` (422).
  - `GlobalExceptionHandler` pro jednotné a srozumitelné chybové odpovědi ve formátu RFC 7807 (Problem Details).
- [x] **3.3** Rozhraní `ProductService` a jeho implementace `ProductServiceImpl`:
  - Byznys logika: ověření unikátnosti SKU, validace nezáporné marže, soft-delete, výpočet produktů vyžadujících doobjednání.
  - **Podrobné komentáře** u každé metody vysvětlující plněná obchodní pravidla.

### Fáze 4: Prezentační vrstva (REST Controller, Swagger & Web Demo UI)
- [x] **4.1** Implementace `ProductController`:
  - REST endpointy: `GET /api/v1/products`, `GET /api/v1/products/{id}`, `GET /api/v1/products/sku/{sku}`, `POST /api/v1/products`, `PUT /api/v1/products/{id}`, `DELETE /api/v1/products/{id}`, `GET /api/v1/products/alerts/low-stock`.
  - HTTP stavové kódy a OpenAPI anotace (`@Operation`, `@ApiResponse`).
- [x] **4.2** Konfigurace SpringDoc OpenAPI (Swagger UI):
  - Konfigurační třída `OpenApiConfig` s metadaty systému, dostupné na `/swagger-ui.html`.
- [x] **4.3** Vytvoření reprezentativního webového rozhraní (`index.html` v `src/main/resources/static`):
  - Přehledná tabulka produktů s vizuální indikací stavu zásoby (*„co hoří“*).
  - Dialog / formulář pro přidání nové hračky s okamžitým zobrazením validačních chyb přímo u polí.
  - Responzivní vzhled optimalizovaný pro desktop i mobil/tablet bez AI klišé.

### Fáze 5: Kontejnerizace a snadné spuštění (`docker compose up`)
- [x] **5.1** Vytvoření multi-stage `Dockerfile`:
  - Build fáze (kompilace přes Maven) a runtime fáze s Eclipse Temurin JRE 21 a neprivilegovaným uživatelem.
- [x] **5.2** Vytvoření `docker-compose.yml`:
  - Služba `db`: PostgreSQL 16 s automatickým vytvořením databáze a perzistentním volume.
  - Služba `backend`: Spring Boot aplikace napojená na `db` s healthcheckem.
- [x] **5.3** Ověření sestavení spustitelného JAR a běhu aplikace.

### Fáze 6: Automatizované testy
- [x] **6.1** Unit testy `ProductServiceTest`:
  - Test úspěšného vytvoření hračky.
  - Test odmítnutí duplicitního SKU kódu (`DuplicateSkuException`).
  - Test odmítnutí prodejní ceny pod nákupní cenou (`BusinessValidationException`).
  - Test vyhledání podle ID (úspěch i 404).
  - Test aktualizace parametrů a soft-deaktivace.
  - Test detekce položek s minimální zásobou.
- [x] **6.2** Integrační testy `ProductControllerTest`:
  - Testování REST rozhraní pomocí `MockMvc`.
  - Ověření HTTP kódů 200, 201 (včetně hlavičky `Location`), 400, 404, 409, 422.
  - Test kontextu aplikace `WarehouseApplicationTests` s Flyway migracemi.

### Fáze 7: Dokumentace a příprava na commit
- [x] **7.1** Aktualizace technické dokumentace [README.md](file:///c:/Dokumenty/Škola/Vysoká/PPRO/PPRO/README.md) (záznam v technickém deníku, návod ke spuštění dema, aktualizace stavu checklistu povinného minima PPRO).
- [x] **7.2** Kontrola dodržení všech zásad v [AGENTS.md](file:///c:/Dokumenty/Škola/Vysoká/PPRO/PPRO/AGENTS.md) a otestování pre-commit hooku.
- [ ] **7.3** Příprava strukturované zprávy pro commit po schválení uživatelem (Zásada 1 & 1b: NIKDY neprovádět commit samovolně!).

---

## 🎯 Milestone 2: Skladová struktura, kategorie (vazba M:N), reálné zásoby a doobjednávky

V této fázi rozšiřujeme systém o evidenci skladů (Hradec Králové vs. garáž v Třebechovicích), zařazení do kategorií (vazba M:N), sledování fyzické vs. disponibilní zásoby a doobjednávky nového zboží u dodavatele.

### Fáze 8: Databázová vrstva pro Milestone 2 (Migrace V3 a V4)
- [x] **8.1** Vytvoření migračního skriptu `V3__create_warehouses_categories_and_stock.sql`:
  - DDL tabulky `warehouses` (id, code, name, address, is_active, created_at).
  - DDL tabulky `categories` (id, name, slug, description, created_at).
  - DDL vazební tabulky M:N `product_categories` (product_id, category_id, assigned_at).
  - DDL tabulky `stock_items` (id, product_id, warehouse_id, physical_quantity, reserved_quantity, updated_at) s integritními CHECK omezeními.
  - Rozšíření tabulky `products` o sloupec `incoming_quantity` (zboží objednané u dodavatele / na cestě).
- [x] **8.2** Vytvoření seedovací migrace `V4__seed_warehouses_categories_and_stock.sql`:
  - Sklady: Hlavní sklad Hradec Králové (`HRADEC`) a Sklad Třebechovice pod Orebem (`TREBECHOVICE`).
  - Kategorie: Pro batolata, Dárky do 500 Kč, Stavebnice a kostky, Panenky a domečky, Tahací a pohybové hračky.
  - Vazba M:N: Přiřazení hraček do více kategorií současně (např. káča je v batolatech i dárcích do 500 Kč).
  - Reálné počty skladových zásob: Káča (30 ks celkem), Kostky (34 ks celkem), Pejsek (25 ks celkem), Vláček (13 ks celkem), Domeček (3 ks celkem + 5 ks na cestě).

### Fáze 9: Doménové entity a repozitáře (Data vrstva)
- [x] **9.1** JPA entita `Warehouse` a repozitář `WarehouseRepository`.
- [x] **9.2** JPA entita `Category` a repozitář `CategoryRepository`.
- [x] **9.3** Aktualizace entity `Product` o vazbu `@ManyToMany` na `Category` a pole `incomingQuantity`.
- [x] **9.4** JPA entita `StockItem` a repozitář `StockItemRepository` (agregace fyzické i volné zásoby, vyhledání dle skladu a produktu).

### Fáze 10: Byznys logika, doobjednávky a rozpad zásob (Service vrstva)
- [x] **10.1** DTO modely:
  - `ProductResponseDto` obohacené o `availableQuantity`, `incomingQuantity`, `stockStatus`, `stockStatusLabel`, `categories` a `warehouseStock`.
  - `SupplyOrderDto` (doobjednání u truhláře) a `SupplyReceiptDto` (naskladnění doručené zásilky).
- [x] **10.2** Servisní logika v `ProductServiceImpl`:
  - Dynamický výpočet celkové fyzické i volné disponibilní zásoby ze všech skladů.
  - Vyhodnocení stavu: Pokud je zásoba pod minimem, ale `incomingQuantity > 0`, stav se nastaví na `ON_ORDER` (*„Na cestě“*) místo červeného poplachu.
  - Metoda `orderSupply()`: Zvýšení množství zboží na cestě.
  - Metoda `receiveSupply()`: Naskladnění fyzických kusů do vybraného skladu a snížení množství na cestě.

### Fáze 11: Prezentační vrstva a interaktivní webové rozhraní
- [x] **11.1** REST endpointy:
  - `POST /api/v1/products/{id}/order-supply` – zadání doobjednávky.
  - `POST /api/v1/products/{id}/receive-supply` – naskladnění doručeného zboží.
  - `GET /api/v1/warehouses` a `GET /api/v1/categories`.
- [x] **11.2** Webové rozhraní (`index.html`):
  - Přehledná tabulka s rozpadem zásob na Hradec a Třebechovice a zobrazením kategorií.
  - Modrý klidový badge `Na cestě (X ks)` pro doobjednané zboží.
  - Interaktivní dialog pro doobjednání zboží u dodavatele.
  - Interaktivní dialog pro naskladnění doručené zásilky na zvolený sklad.

### Fáze 12: Automatizované testy a dokumentace
- [x] **12.1** Unit testy v `ProductServiceTest` rozšířeny o doobjednávku a naskladnění (10 unit testů).
- [x] **12.2** Integrační testy v `ProductControllerTest` rozšířeny o testy endpointů `/order-supply` a `/receive-supply` (12 integračních testů).
- [x] **12.3** Aktualizace `README.md` a `IMPLEMENTATION_PLAN.md` (zaznamenáno nové byznys pravidlo č. 6 a stav).

---

## 🔮 Výhled na další milestony (Rozšíření systému)
- **Milestone 3:** Kritický bod zadání B – Rezervační mechanismus, nákupní objednávky (`Order`, `OrderItem`), odmítnutí při nedostatku zásob.
- **Milestone 4:** Kniha skladových pohybů (`StockMovement`), převody mezi Hradcem a Třebechovicemi, modul Inventura.
- **Milestone 5:** Měsíční statistiky obratu po kategoriích, exporty a optimalizace pro obhajobu.
