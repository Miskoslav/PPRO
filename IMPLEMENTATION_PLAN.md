# IMPLEMENTATION_PLAN.md – Implementační plán projektu

> **Projekt:** Zadání B – Sklad pro malý e-shop (Dřevěnka s.r.o.)  
> **Předmět:** Pokročilé programování (PPRO), FIM UHK (ZS 2026/2027)  
> **Cíl:** Postupná realizace robustního třívrstvého skladového systému.  
> **Milestone 1:** První funkční demo jediné klíčové entity (`Product`) ve Spring Boot pro prezentaci klientovi / vyučujícímu.

---

## 🎯 Milestone 1: Demo první entity (`Product`) ve Spring Boot

V této fázi budujeme plně funkční vertikálu (od databáze až po webové UI a Swagger) pro správu dřevěných hraček e-shopu Dřevěnka s.r.o.

### Fáze 1: Inicializace Spring Boot projektu a Maven konfigurace
- [ ] **1.1** Vytvoření adresářové struktury projektu (`backend/` se standardní Maven layout strukturou).
- [ ] **1.2** Konfigurace `pom.xml` se všemi potřebnými závislostmi:
  - Spring Boot 3.x, Java 21+
  - Spring Data JPA + PostgreSQL driver + Flyway (migrace)
  - Spring Boot Validation (Jakarta)
  - SpringDoc OpenAPI (Swagger UI)
  - Spring Boot Test + H2 (pro testovací účely)
- [ ] **1.3** Zajištění Maven wrapperu (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`).
- [ ] **1.4** Konfigurace aplikačních profilů v `application.yml` (profily `local` a `docker`, datasource, JPA nastavení, Flyway).

### Fáze 2: Databázová vrstva a verzované Flyway migrace
- [ ] **2.1** Vytvoření migračního skriptu `V1__create_products_table.sql`:
  - DDL tabulky `products` (id, sku, name, description, purchase_price, selling_price, min_stock_level, is_active, created_at, updated_at).
  - Databázová integritní omezení (UNIQUE na SKU, CHECK na nezáporné ceny a minimální zásobu).
- [ ] **2.2** Vytvoření seedovací migrace `V2__seed_demo_products.sql`:
  - Syntetická data reprezentující reálný sortiment Dřevěnky (káča, vláček, bukové kostky, domeček pro panenky).
- [ ] **2.3** Vytvoření JPA entity `Product`:
  - Mapování tabulky, anotace `@Entity`, `@Table`, validace, timestampy.
  - **Důsledné Javadoc komentáře** vysvětlující účel a vazby každého pole.
- [ ] **2.4** Vytvoření Spring Data JPA rozhraní `ProductRepository`:
  - Metody pro vyhledání podle SKU, podle aktivity a dotaz na produkty pod minimální bezpečnou zásobou (`findByMinStockLevel`).

### Fáze 3: Byznys logika (Service vrstva) a validace
- [ ] **3.1** Vytvoření DTO modelů pro přenos dat:
  - `ProductCreateDto` – validační anotace (`@NotBlank`, `@Positive`, unikátní SKU).
  - `ProductUpdateDto` – bezpečné úpravy parametrů a cen.
  - `ProductResponseDto` – výstupní model s formátovanými údaji a marží.
- [ ] **3.2** Globální zpracování výjimek a doménové chyby:
  - `ResourceNotFoundException`, `DuplicateSkuException`, `BusinessValidationException`.
  - `GlobalExceptionHandler` pro jednotné a srozumitelné chybové odpovědi ve formátu RFC 7807 (Problem Details).
- [ ] **3.3** Rozhraní `ProductService` a jeho implementace `ProductServiceImpl`:
  - Byznys logika: ověření unikátnosti SKU, validace kladné marže, výpočet produktů vyžadujících doobjednání.
  - **Podrobné komentáře** u každé metody vysvětlující plněná obchodní pravidla.

### Fáze 4: Prezentační vrstva (REST Controller, Swagger & Web Demo UI)
- [ ] **4.1** Implementace `ProductController`:
  - REST endpointy: `GET /api/v1/products`, `GET /api/v1/products/{id}`, `POST /api/v1/products`, `PUT /api/v1/products/{id}`, `DELETE /api/v1/products/{id}`, `GET /api/v1/products/alerts/low-stock`.
  - HTTP stavové kódy a OpenAPI anotace (`@Operation`, `@ApiResponse`).
- [ ] **4.2** Konfigurace SpringDoc OpenAPI (Swagger UI):
  - Dostupné na `/swagger-ui.html` pro interaktivní testování endpointů.
- [ ] **4.3** Vytvoření jednoduchého a reprezentativního webového rozhraní (`index.html` v `src/main/resources/static`):
  - Přehledná tabulka produktů s vizuální indikací stavu zásoby (*„co hoří“*).
  - Dialog / formulář pro přidání nové hračky s okamžitým zobrazením validačních hlášek.
  - Responzivní vzhled optimalizovaný i pro mobil/tablet.

### Fáze 5: Kontejnerizace a snadné spuštění (`docker compose up`)
- [ ] **5.1** Vytvoření multi-stage `Dockerfile`:
  - Build fáze (kompilace přes Maven) a runtime fáze s Eclipse Temurin JRE pro minimální velikost image.
- [ ] **5.2** Vytvoření `docker-compose.yml`:
  - Služba `db`: PostgreSQL 16 s automatickým vytvořením databáze a perzistentním volume.
  - Služba `backend`: Spring Boot aplikace napojená na `db` s healthcheckem.
- [ ] **5.3** Ověření běhu a funkčnosti aplikace v kontejneru.

### Fáze 6: Automatizované testy
- [ ] **6.1** Unit testy `ProductServiceTest`:
  - Test úspěšného vytvoření hračky.
  - Test odmítnutí duplicitního SKU kódu.
  - Test detekce položek pod minimální zásobou.
- [ ] **6.2** Integrační testy `ProductControllerTest`:
  - Testování REST rozhraní pomocí `MockMvc`.
  - Ověření HTTP kódů 200, 201, 400, 404 a chybových validačních hlášení.

### Fáze 7: Dokumentace a příprava na commit
- [ ] **7.1** Aktualizace technické dokumentace [README.md](file:///u:/PPRO/PPRO/README.md) (záznam v technickém deníku, návod ke spuštění dema).
- [ ] **7.2** Kontrola dodržení všech zásad v [AGENTS.md](file:///u:/PPRO/PPRO/AGENTS.md) a otestování pre-commit hooku.
- [ ] **7.3** Příprava strukturované zprávy pro commit po schválení uživatelem.

---

## 🔮 Výhled na další milestony (Rozšíření systému)
- **Milestone 2:** Implementace zbývajících skladových entit (`Warehouse`, `StockItem`, `Category`, `ProductCategory` vazba M:N).
- **Milestone 3:** Kritický bod zadání B – Rezervační mechanismus, nákupní objednávky (`Order`, `OrderItem`), odmítnutí při nedostatku zásob.
- **Milestone 4:** Kniha skladových pohybů (`StockMovement`), převody mezi Hradcem a Třebechovicemi, modul Inventura.
- **Milestone 5:** Měsíční statistiky obratu po kategoriích, exporty a optimalizace pro obhajobu.
