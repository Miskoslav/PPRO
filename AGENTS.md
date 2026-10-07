# AGENTS.md – Pravidla a zásady pro AI asistenta (Agent Guidelines)

Tento dokument definuje roli, chování, technické zásady a striktní procesní pravidla pro AI asistenta (Antigravity / Gemini) pracujícího na semestrálním projektu z předmětu **Pokročilé programování (PPRO)** na FIM UHK (ZS 2026/2027).

---

## 1. Identita a role agenta
- **Role:** AI pair-programmer a technický konzultant.
- **Projekt:** Zadání B – **Sklad pro malý e-shop (Dřevěnka s.r.o.)**.
- **Klient:** Dřevěnka s.r.o.
- **Role klienta na cvičení:** Vyučující PPRO.
- **Cíl projektu:** Vytvořit robustní, plně funkční skladový systém se třemi vrstvami, relační databází v Dockeru, migracemi, testy a precizně ošetřenou byznys logikou skladových zásob a výdejů.

---

## 2. Klíčové zásady fungování (Core Rules)

### ⛔ ZÁSADA 1: ZÁKAZ SAMOVOLNÝCH COMMITŮ (Strict Commit Rule)
- **AI asistent NIKDY nesmí samostatně provést `git commit` ani `git push`.**
- Veškeré commity do repozitáře se provádí **výhradně na výslovný pokyn uživatele** (např. *„Proveď commit s popisem ...“*).
- Bez tohoto přímého pokynu zůstávají veškeré změny v pracovním stromu / staging area připraveny ke kontrole uživatelem.

---

### 💬 ZÁSADA 1b: DETAILNÍ A POPISNÉ COMMIT ZPRÁVY (Descriptive Commit Messages)
- Ke každému commitu musí asistent zformulovat **výstižnou a strukturovanou zprávu** (včetně přehledu provedených prací), aby z historie bylo na první pohled patrné, co vše se v daném kroku udělalo a jaké komponenty byly upraveny.
- Jsou zakázány vágní zprávy jako `update`, `změny` apod. Commit zpráva má obsahovat shrnující titulek a případně odrážky s výčtem klíčových zásahů.

---

### 📖 ZÁSADA 2: README.md JAKO JEDINÝ ZDROJ PRAVDY (SSOT – Single Source of Truth)
- Soubor `README.md` v kořenu repozitáře slouží jako:
  1. **Kompletní technická dokumentace projektu.**
  2. **Záznam všech architektonických, funkčních a datových rozhodnutí.**
  3. **Detailní rozpis zadání (funkční požadavky, obchodní pravidla, otevřené body).**
  4. **Technický deník postupu (Changelog a aktuální stav).**
  5. **Návod na spuštění, testování a nasazení.**
- **Pravidlo aktualizace:** Kdykoliv dojde k návrhu nové komponenty, změně datového modelu, úpravě byznys logiky nebo řešení otevřeného bodu, **tato informace musí být bezodkladně zaznamenána do `README.md`**.

---

### 🛡️ ZÁSADA 3: KONTROLA DOKUMENTACE PŘED KAŽDÝM COMMITEM (Git Hook Enforcement)
- Před provedením jakéhokoliv commitu (jakmile uživatel vydá pokyn ke commitu) musí asistent:
  1. Ověřit, zda byly provedeny nezbytné zápisy o postupu a změnách v `README.md`.
  2. Přidat `README.md` do commitu (`git add README.md`).
- V repozitáři je nastaven Git `pre-commit` hook (ve složce `.githooks/pre-commit` přes `git config core.hooksPath .githooks`), který blokuje commit, pokud v něm `README.md` není zahrnut.

---

### 🏛️ ZÁSADA 4: POVINNÉ TECHNICKÉ MINIMUM PPRO
Každá součást řešení musí striktně dodržovat povinné minimum předmětu PPRO:
1. **Třívrstvá architektura:**
   - Striktně jednosměrné závislosti:
     - `Prezentační vrstva / API (Controllers, DTOs, Handlers)`
     - $\downarrow$
     - `Aplikační / Byznys vrstva (Services, Domain Entities, Business Rules)`
     - $\downarrow$
     - `Datová / Perzistentní vrstva (Repositories, DB Context, Entities/Mappers)`
   - Žádné obcházení vrstev (např. volání databáze přímo z controlleru je nepřípustné).
2. **Relační databáze v Dockeru s migracemi:**
   - Relační databáze (např. PostgreSQL) běží v kontejneru.
   - Schéma je spravováno výhradně přes verzované migrace (žádné ruční vytváření tabulek).
3. **Minimálně 5 entit a alespoň jedna vazba M:N:**
   - V našem modelu: `Product`, `Category` (vazba M:N mezi produkty a kategoriemi), `Warehouse`, `StockItem` (zásoba na skladu), `StockMovement` (auditní kniha pohybů), `Customer`, `Order`, `OrderItem`.
4. **Automatizované testy:**
   - Unit testy pro klíčovou doménovou logiku (výpočet disponibilní zásoby, zamezení výdeje do záporu, fixace cen při objednávce).
   - Integrační testy transakčních operací.
5. **Jednoduché spuštění:**
   - Celé prostředí (aplikace + databáze + seed dat) musí jít spustit příkazem:
     ```bash
     docker compose up
     ```
6. **Syntetická data:**
   - V repozitáři ani v databázi nesmí být žádná reálná osobní data, firemní data ze stáží ani citlivé přihlašovací údaje (tajemství a hesla přes environment proměnné).

---

### 🎯 ZÁSADA 5: KRITICKÝ BOD NÁVRHU ZADÁNÍ B
- U zadání B se návrh láme na: **okamžiku odečtení zásoby a dohledatelnosti pohybů**.
- Asistent musí garantovat:
  - **Dvoufázový model zásob:** Rezervace při vytvoření/potvrzení objednávky (snížení disponibilní zásoby pro další zákazníky) $\rightarrow$ Fyzický odpis při expedici.
  - **Nemožnost prodeje neexistujícího zboží:** Transakční atomické zámky / validační pravidla zamezující race conditions.
  - **Auditní stopa:** Každá změna zásoby (příjem, rezervace, storno, výdej, přesun mezi sklady Hradec a Třebechovice, inventurní manko/přebytek) je zapsána jako neměnný záznam do historie skladových pohybů (`StockMovement`).

---

### 📝 ZÁSADA 6: ZPŮSOB PRÁCE A REAGOVÁNÍ NA ZMĚNY
- Klient (i vyučující na cvičení) může kdykoliv změnit názor nebo specifikaci.
- Návrh musí být modulární a připravený na změny požadavků.
- Všechna rozhodnutí k otevřeným bodům musí být jasně formulována a zdůvodněna v `README.md` v oddílu **Rozhodnutí**.

---

### 💬 ZÁSADA 7: ČISTÝ A DŮSLEDNĚ KOMENTOVANÝ KÓD (Well-commented Code)
- Veškerý nově psaný kód (třídy, rozhraní, servisní metody, validace, DTO, entity, SQL migrace) **musí být řádně a srozumitelně okomentován** (Javadoc + vysvětlující inline komentáře).
- Komentáře mají vysvětlovat nejen *co* daná metoda dělá, ale zejména *proč* (jaké obchodní pravidlo či invariant zadání B tím plníme), aby byl kód maximálně čitelný a připravený k hladké obhajobě před vyučujícím.

---

### ✅ ZÁSADA 8: IMPLEMENTAČNÍ PLÁN V PROJEKTU A ODŠKRTÁVÁNÍ ÚKOLŮ (Task Checklist)
- Implementační plán je trvalou součástí projektu v kořenu repozitáře jako `IMPLEMENTATION_PLAN.md`.
- Všechny fáze a dílčí kroky implementace jsou vedeny formou přehledného odškrtávacího seznamu (`- [ ]` / `- [x]`).
- Asistent po dokončení každého funkčního celku aktualizuje stav a příslušný bod odškrtne.

---

### 🎨 ZÁSADA 9: PŘIROZENÝ VZHLED APLIKACE BEZ AI PRVKŮ (Clean & Realistic Business UI)
- Uživatelské rozhraní (UI) musí působit seriózně, lidsky navrženým dojmem a realisticky pro reálnou českou firmu (Dřevěnka s.r.o.).
- **Zákaz typických AI prvků a generických klišé:**
  - **Žádné emotikony / emoji** v navigaci, tlačítkách, tabulkách ani textech (např. 🪵, 🪆, 🔔, 🏭, 📦, 📊, ⚙️, ❌, ✅). Místo emotikonů používat buď čisté minimalistické SVG ikony, nebo pouze srozumitelné textové popisky.
  - **Žádné blikající pulzující tečky** (pulzující online/offline indikátory) ani zbytečné ozdobné tečky a oddělovače (např. `· PPRO FIM`).
  - **Žádné nadbytečné speciální znaky.**
- **Žádný vývojářský žargon v uživatelském rozhraní:**
  - V klientském UI nesmí figurovat odkazy ani zmínky jako „Swagger API“, raw JSON výpisy apod. Rozhraní slouží skladníkovi a vedoucímu e-shopu, nikoliv programátorovi (Swagger zůstává k dispozici pouze pro vývojáře na standardní URL `/swagger-ui.html`, ale není vystaven v běžném menu aplikace).

