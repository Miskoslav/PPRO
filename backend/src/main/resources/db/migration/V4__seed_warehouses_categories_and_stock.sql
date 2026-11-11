-- ==============================================================================
-- Databázová migrace V4: Seed skladů, kategorií, vazby M:N a výchozích zásob
-- Projekt: Sklad pro malý e-shop Dřevěnka s.r.o. (Semestrální projekt PPRO)
-- ==============================================================================

-- 1. Vložení 2 reálných skladů Dřevěnky s.r.o.
INSERT INTO warehouses (code, name, address, is_active, created_at)
VALUES
    ('HRADEC', 'Hlavní sklad Hradec Králové', 'Brněnská 182/42, 500 06 Hradec Králové', TRUE, CURRENT_TIMESTAMP),
    ('TREBECHOVICE', 'Sklad Třebechovice pod Orebem (zimní garáž)', 'Pardubická 84, 503 46 Třebechovice pod Orebem', TRUE, CURRENT_TIMESTAMP);

-- 2. Vložení katalogových kategorií
INSERT INTO categories (name, slug, description, created_at)
VALUES
    ('Pro batolata', 'pro-batolata', 'Bezpečné zaoblené hračky z přírodního dřeva pro nejmenší od 1 roku.', CURRENT_TIMESTAMP),
    ('Dárky do 500 Kč', 'darky-do-500', 'Cenově dostupné a oblíbené dřevěné hračky ideální jako pozornost.', CURRENT_TIMESTAMP),
    ('Stavebnice a kostky', 'stavebnice-a-kostky', 'Bukové kostky, vláčkodráhy a stavební sady rozvíjející prostorovou představivost.', CURRENT_TIMESTAMP),
    ('Panenky a domečky', 'panenky-a-domecky', 'Tradiční masivní domečky pro panenky a detailní dřevěný nábytek.', CURRENT_TIMESTAMP),
    ('Tahací a pohybové hračky', 'tahaci-a-pohybove', 'Klasické tahací hračky s kolečky na provázku pro radost z prvních krůčků.', CURRENT_TIMESTAMP);

-- 3. Vazba M:N (Product <-> Category)
-- Káča pestrobarevná (ID 1) -> Pro batolata (ID 1) i Dárky do 500 Kč (ID 2)
INSERT INTO product_categories (product_id, category_id, assigned_at)
VALUES
    (1, 1, CURRENT_TIMESTAMP),
    (1, 2, CURRENT_TIMESTAMP),
    -- Vláček s vagónky (ID 2) -> Stavebnice a kostky (ID 3) i Dárky do 500 Kč (ID 2)
    (2, 3, CURRENT_TIMESTAMP),
    (2, 2, CURRENT_TIMESTAMP),
    -- Bukové kostky 100 ks (ID 3) -> Stavebnice a kostky (ID 3) i Dárky do 500 Kč (ID 2)
    (3, 3, CURRENT_TIMESTAMP),
    (3, 2, CURRENT_TIMESTAMP),
    -- Domeček pro panenky (ID 4) -> Panenky a domečky (ID 4)
    (4, 4, CURRENT_TIMESTAMP),
    -- Tahací pejsek Bobík (ID 5) -> Pro batolata (ID 1) i Tahací a pohybové hračky (ID 5)
    (5, 1, CURRENT_TIMESTAMP),
    (5, 5, CURRENT_TIMESTAMP);

-- 4. Výchozí skladové zásoby po skladech (StockItem)
-- Sklad 1 = Hradec Králové, Sklad 2 = Třebechovice
INSERT INTO stock_items (product_id, warehouse_id, physical_quantity, reserved_quantity, updated_at)
VALUES
    -- Káča pestrobarevná (Min zásoba = 15 ks)
    (1, 1, 18, 3, CURRENT_TIMESTAMP),
    (1, 2, 12, 0, CURRENT_TIMESTAMP),

    -- Vláček s vagónky (Min zásoba = 10 ks)
    (2, 1, 8, 1, CURRENT_TIMESTAMP),
    (2, 2, 5, 0, CURRENT_TIMESTAMP),

    -- Bukové stavební kostky (Min zásoba = 8 ks)
    (3, 1, 14, 2, CURRENT_TIMESTAMP),
    (3, 2, 20, 0, CURRENT_TIMESTAMP),

    -- Dřevěný domeček pro panenky (Min zásoba = 3 ks) -> V Hradci kritický stav (1 ks k dispozici)
    (4, 1, 2, 1, CURRENT_TIMESTAMP),
    (4, 2, 1, 0, CURRENT_TIMESTAMP),

    -- Tahací pejsek Bobík (Min zásoba = 12 ks)
    (5, 1, 15, 0, CURRENT_TIMESTAMP),
    (5, 2, 10, 0, CURRENT_TIMESTAMP);

-- 5. Nastavení zboží na cestě od dodavatele/truhláře
-- U domečku pro panenky (ID 4) je zásoba pod minimem (2 ks < 3 ks), ale 5 ks je již objednáno u truhláře (na cestě).
UPDATE products SET incoming_quantity = 5 WHERE id = 4;

