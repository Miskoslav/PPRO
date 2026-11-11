-- ==============================================================================
-- Databázová migrace V2: Seed syntetických dat demo produktů
-- Projekt: Sklad pro malý e-shop Dřevěnka s.r.o. (Semestrální projekt PPRO)
-- ==============================================================================
--
-- DŮVOD A ÚČEL:
-- Naplnění databáze ukázkovým sortimentem dřevěných hraček pro prezentaci klientovi
-- a demonstraci fungování systému. Data jsou výhradně syntetická, neobsahují žádné
-- citlivé ani reálné osobní údaje (splnění Zásady 4 PPRO minima).
--
-- Sortiment reprezentuje 5 typických hraček z nabídky Dřevěnka s.r.o.:
-- 1. Dřevěná káča pestrobarevná (tradiční drobná hračka, vysoká obrátka)
-- 2. Dřevěný vláček s vagónky (střední kategorie)
-- 3. Bukové stavební kostky (100 ks v kyblíku - bestseller e-shopu)
-- 4. Dřevěný domeček pro panenky s nábytkem (prémiový velký artikl)
-- 5. Dřevěný tahací pejsek Bobík (hračka pro batolata)
-- ==============================================================================

INSERT INTO products (sku, name, description, purchase_price, selling_price, min_stock_level, is_active, created_at, updated_at)
VALUES
    (
        'HRA-KAC-001',
        'Dřevěná káča pestrobarevná',
        'Tradiční ručně soustružená dřevěná káča z bukového dřeva, malovaná nezávadnými ekologickými barvami. Rozvíjí jemnou motoriku.',
        45.00,
        99.00,
        15,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'HRA-VLA-001',
        'Dřevěný vláček s vagónky',
        'Masivní dřevěná mašinka se dvěma odpojitelnými nákladními vagóny a barevnými kostkami různých tvarů.',
        180.00,
        389.00,
        10,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'HRA-KOS-100',
        'Bukové stavební kostky (100 ks v kyblíku)',
        'Sada 100 kusů přírodních i pestrobarevných bukových kostek v praktickém papírovém kyblíku s víkem pro snadný úklid.',
        240.00,
        499.00,
        8,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'HRA-DOM-001',
        'Dřevěný domeček pro panenky s nábytkem',
        'Třípatrový masivní dřevěný domeček se sadou dřevěného nábytku a otevíracími okenicemi. Prémiové ruční zpracování.',
        850.00,
        1790.00,
        3,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'HRA-TAH-002',
        'Dřevěný tahací pejsek Bobík',
        'Oblíbená tahací hračka na provázku s pohyblivýma ušima a kolečky s gumovými kroužky pro tichý chod na plovoucích podlahách.',
        110.00,
        249.00,
        12,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

