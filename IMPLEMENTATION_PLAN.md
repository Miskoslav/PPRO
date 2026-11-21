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
- [x] **12.1** Unit testy v `ProductServiceTest` rozšířeny o doobjednávku a naskladnění.
- [x] **12.2** Integrační testy v `ProductControllerTest` rozšířeny o testy endpointů `/order-supply` a `/receive-supply`.
- [x] **12.3** Aktualizace `README.md` a `IMPLEMENTATION_PLAN.md`.

---

## 🎯 Milestone 3: Zákazníci, objednávky a dvoufázový rezervační model (Kritický bod zadání B)

V této fázi byl realizován klíčový invariant zadání B: zamezení prodeje neexistujícího zboží pomocí atomické rezervace zásob a fixace prodejních cen.

### Fáze 13: Databázová vrstva pro Milestone 3 (Migrace V5 a V6)
- [x] **13.1** Vytvoření migrace `V5__create_customers_orders.sql`:
  - DDL tabulky `customers` (first_name, last_name, email, phone, adresa; zákazníci se nemažou).
  - DDL tabulky `orders` (order_number, customer_id, status CHECK, total_price, note).
  - DDL tabulky `order_items` s vazbou na produkt, sklad výdeje a zafixovanou cenou `unit_price_at_purchase`.
- [x] **13.2** Vytvoření seedovací migrace `V6__seed_customers_and_orders.sql`:
  - Syntetičtí zákazníci (Jana Horáčková, Tomáš Blažek, Markéta Svobodová a další).
  - Ukázkové objednávky v různých fázích životního cyklu (`CREATED`, `RESERVED`, `PROCESSING`).
  - Demonstrace navýšení `reserved_quantity` v `stock_items`.

### Fáze 14: Doménový model a repozitáře pro Milestone 3
- [x] **14.1** JPA entita `Customer` a repozitář `CustomerRepository`.
- [x] **14.2** JPA entita `Order` se stavovým automatem `OrderStatus` a repozitář `OrderRepository`.
- [x] **14.3** JPA entita `OrderItem` se zafixovanou cenou a repozitář `OrderItemRepository`.
- [x] **14.4** Obohacení `ProductCreateDto` a `ProductServiceImpl` o přiřazení kategorií (vazba M:N).

### Fáze 15: Servisní logika objednávek, skladů, split fulfillmentu a validace naskladnění
- [x] **15.1** DTO modely: `OrderCreateDto`, `OrderResponseDto`, `WarehouseSummaryDto`, `WarehouseInventoryItemDto`, `WarehouseCreateDto`.
- [x] **15.2** Byznys logika v `OrderServiceImpl`:
  - Generování čitelného čísla objednávky (`ORD-YYYY-XXXXX`).
  - **Atomická rezervace zásoby (`reserved_quantity++`)** s okamžitým snížením disponibilní zásoby.
  - **Split fulfillment (Rozdělení odběru mezi sklady):** Pokud je zboží dostatek, ale je rozděleno ve více skladech (např. 10 ks v Hradci a 5 ks v Třebechovicích), systém objednávku nezamítne, nýbrž automaticky vykryje poptávku napříč sklady a provede rezervaci v příslušných skladech.
  - **Odmítnutí objednávky (HTTP 422):** Pokud ani celková disponibilní zásoba ze všech skladů nestačí, objednávka je odmítnuta (klíčová ochrana proti prodeji neexistujícího zboží).
  - **Zafixování prodejních cen:** `unit_price_at_purchase` fixuje cenu v době vytvoření objednávky nezávisle na budoucích změnách ceníku.
  - **Stornování objednávky:** Bezpečné uvolnění rezervací zpět do disponibilního stavu na všech dotčených skladech.
- [x] **15.3** Servisní logika skladů v `WarehouseServiceImpl`:
  - Agregace fyzických, rezervovaných a volných zásob po skladech.
  - Načtení detailního inventáře skladu (`getWarehouseInventory`).
  - Možnost dynamického přidání dalšího skladu (`createWarehouse`).
- [x] **15.4** Oprava naskladnění (Validation Fix):
  - Zamezení naskladnění více kusů, než kolik bylo objednáno u dodavatele (`quantity <= incomingQuantity`), jak na backendu (HTTP 422), tak na frontendu.

### Fáze 16: Prezentační vrstva a webové rozhraní (UI)
- [x] **16.1** REST endpointy:
  - `GET /api/v1/orders`, `GET /api/v1/orders/{id}`, `POST /api/v1/orders`, `POST /api/v1/orders/{id}/cancel`.
  - `GET /api/v1/customers` (přehled zákazníků pro výběr v objednávce).
  - `GET /api/v1/warehouses/summary`, `GET /api/v1/warehouses/{id}/inventory`, `POST /api/v1/warehouses`.
  - `GET /api/v1/products/alerts/approaching-low` (položky blížící se k minimu).
- [x] **16.2** Webové rozhraní (`index.html`):
  - **Dashboard:** Reálná data v boxech skladů (fyzicky v regálech, rezervováno pro objednávky, volně k prodeji) + nová sekce pro zásoby blížící se k minimu + tlačítka pro zobrazení inventáře.
  - **Produkty:** Výběr kategorií pomocí checkboxů při zakládání nové dřevěné hračky (vazba M:N).
  - **Upozornění zásob:** Rozdělení na dva oddíly: kritické zásoby (pod minimem) a zásoby blížící se k minimu (doporučeno sledovat).
  - **Sklady:** Možnost rozkliknout libovolný sklad a zobrazit kompletní inventář + tlačítko a formulář pro přidání nového skladu.
  - **Objednávky:** Plně funkční správa zákaznických objednávek (přehled, storno, modální okno pro vytvoření objednávky s kalkulací ceny, podporou split fulfillmentu a okamžitou rezervací).
  - **Naskladnění:** Vizuální limit a validace proti překročení objednaného množství.

### Fáze 17: Automatizované testy a předělání kontejnerizace
- [x] **17.1** Testy pro objednávky v `OrderServiceTest`:
  - Test rezervace zásoby a fixace ceny.
  - Test odmítnutí při nedostatku zásob napříč sklady.
  - Test **Split fulfillmentu** – automatické rozdělení objednávky a rezervací mezi Hradec Králové a Třebechovice.
  - Test uvolnění rezervací při stornu objednávky.
- [x] **17.2** Unit testy v `ProductServiceTest`:
  - Test zákazu naskladnění vyššího počtu kusů než bylo objednáno.
  - Test vyfiltrování zásob blížících se k minimu (`getApproachingLowStockAlerts`).
- [x] **17.3** Všech **28 automatizovaných testů** úspěšně prochází (`BUILD SUCCESS`).
- [x] **17.4** Předělání Dockeru (`Dockerfile` a `docker-compose.yml`):
  - Multi-stage build s efektivním cachováním závislostí `mvn dependency:resolve`.
  - Doplněn `curl` a kontejnerový healthcheck pro backend (`/api/v1/health`) i databázi (`pg_isready`).
  - Zavedena dedikovaná síť `drevenka-network` (bridge).
  - Parametry JVM optimalizované pro kontejner (`MaxRAMPercentage=75.0`).

### Fáze 18: Oficiální projektová dokumentace a ergonomie UI
- [x] **18.1** Vyplnění oficiálního dokumentu `PPRO dokumentace.docx`:
  - Vypracován oddíl *1. Popis řešení* (kontext firmy, řešení původních problémů, třívrstvá architektura, REST API, JPA, migrace a Docker).
  - Vyplněna tabulka *2. Rozhodnutí* se všemi 11 architektonickými rozhodnutími (R1 až R11).
  - Vyplněna tabulka *3. Změny požadované klientem* s autentickými požadavky klienta ze cvičení (Z1 a Z2 ve stavu Hotovo).
  - Aktualizována hlavička metadat (datum poslední úpravy: 21. 11. 2026).
- [x] **18.2** Klientská úprava formuláře objednávky (Změna Z2):
  - Přesun dropdownu volby skladu expedice (`order-warehouse`) v `index.html` z 2-sloupcového gridu na celou šířku formuláře pod výběr dřevěné hračky.
  - Eliminace nutnosti horizontálního posunu a zajištění čisté ergonomie.
- [x] **18.3** Finální technická dokumentace v PDF (`docs/technicka-dokumentace.pdf`):
  - Vygenerován oficiální dokument ve složce `docs/` v rozsahu 14 stran (doporučeno 8–15 stran dle oddílu 5 směrnice).
  - Obsahuje všech 9 povinných kapitol včetně vektorového ER diagramu (7 entit a M:N vazba), návodu na spuštění, přehledu 28 testů, **Protokolu o zacyklení na 1 stranu** (chyba naskladnění nad meze) a Přiznání práce s AI.

---

## 🔮 Výhled na další milestony (Rozšíření systému)
- **Milestone 4:** Kniha skladových pohybů (`StockMovement`), převody mezi Hradcem a Třebechovicemi, modul Inventura.
- **Milestone 5:** Měsíční statistiky obratu po kategoriích, exporty a optimalizace pro obhajobu.
