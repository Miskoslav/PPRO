-- ==============================================================================
-- Databázová migrace V5: Zákazníci a zákaznické objednávky (Milestone 3)
-- Projekt: Sklad pro malý e-shop Dřevěnka s.r.o. (Semestrální projekt PPRO)
-- ==============================================================================
--
-- DŮVOD A ÚČEL:
-- Realizace kritického bodu zadání B: Rezervační mechanismus, který zabrání prodeji
-- neexistujícího zboží. Jde o hlavní bolest klienta (2× měsíčně prodá zboží, které
-- fyzicky nemá). Každá objednávka při přijetí okamžitě rezervuje disponibilní zásobu.
--
-- DVOUFÁZOVÝ MODEL ZÁSOB (Rozhodnutí č. 2 z README.md):
--   1. Fáze RESERVED: Vytvoření objednávky → reserved_quantity++ v stock_items.
--      Disponibilní zásoba okamžitě klesne pro ostatní zákazníky.
--   2. Fáze DISPATCH: Expedice → physical_quantity-- a reserved_quantity--.
--      Fyzická zásoba v regálu se snižuje až při skutečném zabalení a odeslání.
--
-- BUSINESS RULE (klientova podmínka č. 1):
--   "Vydat víc, než je na skladě, nesmí jít za žádných okolností."
--   → DB integritní omezení: reserved_quantity <= physical_quantity (již existuje z V3).
--   → Kontrola v servisní vrstvě před rezervací.
--
-- BUSINESS RULE (klientova podmínka č. 2):
--   "Cena v odeslané objednávce se nesmí zpětně změnit."
--   → unit_price_at_purchase: Zafixovaná prodejní cena v okamžiku objednávky.
--
-- BUSINESS RULE (klientova podmínka č. 4):
--   "Zákazník se z databáze nikdy nemaže."
--   → Tabulka customers nemá soft-delete; DELETE je na úrovni aplikace blokován.
-- ==============================================================================

-- 1. Zákazníci e-shopu Dřevěnky
--    Zákazníci se nikdy nemažou – jsou potřeba pro historii objednávek a reklamace.
CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(128) NOT NULL,
    last_name       VARCHAR(128) NOT NULL,
    email           VARCHAR(255) NOT NULL,  -- Nemusí být unikátní (jeden zákazník může nakupovat z více e-mailů)
    phone           VARCHAR(32),
    street          VARCHAR(255),
    city            VARCHAR(128),
    zip             VARCHAR(16),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_email ON customers (email);
CREATE INDEX idx_customers_name  ON customers (last_name, first_name);

-- 2. Zákaznické objednávky
--    Stavy objednávky (order_status):
--      CREATED     – Nová objednávka, zásoba JEŠTĚ NEBYLA REZERVOVÁNA
--      RESERVED    – Zásoba byla atomicky rezervována (reserved_quantity++ v stock_items)
--      PROCESSING  – Ve vychystávání (sklad fyzicky připravuje zásilku)
--      DISPATCHED  – Expedováno (physical_quantity-- a reserved_quantity-- proběhly)
--      CANCELLED   – Stornováno (rezervace byla uvolněna zpět)
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    order_number    VARCHAR(32) NOT NULL UNIQUE,  -- Čitelný kód objednávky (např. ORD-2026-00001)
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    status          VARCHAR(32) NOT NULL DEFAULT 'CREATED'
                        CHECK (status IN ('CREATED','RESERVED','PROCESSING','DISPATCHED','CANCELLED')),
    total_price     NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    note            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_customer   ON orders (customer_id);
CREATE INDEX idx_orders_status     ON orders (status);
CREATE INDEX idx_orders_number     ON orders (order_number);

-- 3. Položky objednávky
--    Každá položka eviduje, kolik kusů bylo objednáno a Z JAKÉHO SKLADU bude expedováno.
--    unit_price_at_purchase: ZAFIXOVANÁ cena v okamžiku objednávky – nesmí se měnit se ceníkem!
--    Splnění klientovy podmínky: "Cena v odeslané objednávce se nesmí zpětně změnit."
CREATE TABLE order_items (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id              BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    warehouse_id            BIGINT REFERENCES warehouses(id),    -- Sklad, z nějž bude zboží vydáno
    quantity                INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_at_purchase  NUMERIC(12, 2) NOT NULL,              -- Zafixovaná prodejní cena

    CONSTRAINT uk_order_items_order_prod_wh UNIQUE (order_id, product_id, warehouse_id)
);

CREATE INDEX idx_order_items_order   ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);
