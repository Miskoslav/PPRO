-- ==============================================================================
-- Databázová migrace V3: Sklady, kategorie, vazba M:N a skladové zásoby
-- Projekt: Sklad pro malý e-shop Dřevěnka s.r.o. (Semestrální projekt PPRO)
-- ==============================================================================
--
-- DŮVOD A ÚČEL:
-- Realizace klíčové doménové logiky zadání B a splnění povinného minima PPRO:
-- 1. Evidence zásob na konkrétních skladech (Hradec Králové vs. garáž Třebechovice).
-- 2. Zařazení produktů do více kategorií současně (vazba M:N přes tabulku product_categories).
-- 3. Striktní zamezení záporného stavu na úrovni DB integritních omezení:
--    - physical_quantity >= 0 (fyzická zásoba na regále)
--    - reserved_quantity >= 0 (rezervováno pro rozpracované objednávky)
--    - reserved_quantity <= physical_quantity (nelze rezervovat víc, než máme)
-- ==============================================================================

-- 1. Sklady Dřevěnky (Hlavní sklad Hradec Králové + sklad v Třebechovicích pod Orebem)
CREATE TABLE warehouses (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(32) NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    address             VARCHAR(255),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_warehouses_code ON warehouses (code);

-- 2. Kategorie hraček (např. "Pro batolata", "Dárky do 500 Kč", "Stavebnice a kostky")
CREATE TABLE categories (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(128) NOT NULL,
    slug                VARCHAR(128) NOT NULL UNIQUE,
    description         TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_slug ON categories (slug);

-- 3. Vazební tabulka M:N (Product <-> Category)
CREATE TABLE product_categories (
    product_id          BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    category_id         BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    assigned_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id, category_id)
);

CREATE INDEX idx_prod_cat_product ON product_categories (product_id);
CREATE INDEX idx_prod_cat_category ON product_categories (category_id);

-- 4. Zásoby jednotlivých produktů na konkrétních skladech (StockItem)
CREATE TABLE stock_items (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    warehouse_id        BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    physical_quantity   INTEGER NOT NULL DEFAULT 0,
    reserved_quantity   INTEGER NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_stock_items_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT chk_stock_physical_non_negative  CHECK (physical_quantity >= 0),
    CONSTRAINT chk_stock_reserved_non_negative  CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_stock_reserved_le_physical   CHECK (reserved_quantity <= physical_quantity)
);

CREATE INDEX idx_stock_items_product ON stock_items (product_id);
-- 5. Rozšíření produktů o evidenci doobjednaného zboží u výrobce/dodavatele (zboží na cestě)
ALTER TABLE products ADD COLUMN incoming_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE products ADD CONSTRAINT chk_products_incoming_quantity CHECK (incoming_quantity >= 0);

