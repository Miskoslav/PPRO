-- ==============================================================================
-- Databázová migrace V1: Vytvoření tabulky produktů (katalog dřevěných hraček)
-- Projekt: Sklad pro malý e-shop Dřevěnka s.r.o. (Semestrální projekt PPRO)
-- ==============================================================================
--
-- DŮVOD A ÚČEL:
-- Klient (Dřevěnka s.r.o.) dosud evidoval sortiment v Excelu, kde docházelo ke kolizím
-- a nekonzistentním zápisům kódů a cen.
-- Tato tabulka definuje primární katalogové záznamy pro všechny prodávané položky.
--
-- OBCHODNÍ PRAVIDLA A INVARIANTY (dle zadání a AGENTS.md):
-- 1. Každý produkt má unikátní skladový kód (SKU) pro jednoznačnou identifikaci v regálu.
-- 2. Nákupní i prodejní cena musí být nezáporná (ošetřeno CHECK omezením na úrovni DB).
-- 3. Minimální bezpečná zásoba musí být nezáporná (CHECK omezení).
-- 4. Produkty se fyzicky nemažou, pouze deaktivují (příznak is_active),
--    aby zůstala zachována integrita historických objednávek.
-- ==============================================================================

CREATE TABLE products (
    id                  BIGSERIAL PRIMARY KEY,
    sku                 VARCHAR(64) NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    purchase_price      NUMERIC(10, 2) NOT NULL,
    selling_price       NUMERIC(10, 2) NOT NULL,
    min_stock_level     INTEGER NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_products_purchase_price   CHECK (purchase_price >= 0),
    CONSTRAINT chk_products_selling_price    CHECK (selling_price >= 0),
    CONSTRAINT chk_products_min_stock_level  CHECK (min_stock_level >= 0)
);

-- Index pro rychlé vyhledávání podle skladového kódu (SKU)
CREATE INDEX idx_products_sku ON products (sku);

-- Index pro filtrování aktivních položek v katalogu
CREATE INDEX idx_products_is_active ON products (is_active);
