-- ==============================================================================
-- Databázová migrace V6: Syntetická data zákazníků a objednávek (Milestone 3)
-- Projekt: Sklad pro malý e-shop Dřevěnka s.r.o. (Semestrální projekt PPRO)
-- ==============================================================================
--
-- DŮLEŽITÉ: Všechna data jsou SYNTETICKÁ (vymyšlená) – žádné reálné osobní údaje.
-- Jde o ukázkový sortiment pro prezentaci zákazníků e-shopu Dřevěnky.
--
-- Demonstrace dvoufázového rezervačního mechanismu:
--   - Objednávka ORD-2026-00001 je ve stavu RESERVED (zásoba je blokována pro zákazníka)
--   - Objednávka ORD-2026-00002 je ve stavu PROCESSING (sklad vychystává)
--   - Objednávka ORD-2026-00003 je ve stavu CREATED (nová, ještě bez rezervace)
-- ==============================================================================

-- Syntetičtí zákazníci (fiktivní jména a adresy)
INSERT INTO customers (first_name, last_name, email, phone, street, city, zip) VALUES
    ('Jana',   'Horáčková',  'jana.horackova@example.cz',  '+420 601 111 222', 'Palachova 12',    'Hradec Králové',          '500 02'),
    ('Tomáš',  'Blažek',     'tomas.blazek@example.cz',    '+420 602 333 444', 'Riegrovo náměstí 4', 'Hradec Králové',       '500 01'),
    ('Markéta','Svobodová',  'marketa.svobodova@example.cz','+420 603 555 666', 'Chelčického 7',   'Pardubice',               '530 02'),
    ('Pavel',  'Novák',      'pavel.novak@example.cz',      '+420 604 777 888', 'Náměstí Republiky 3', 'Pardubice',           '530 01'),
    ('Alena',  'Procházková','alena.prochazkova@example.cz','+420 605 999 000', 'Klicperova 5',    'Hradec Králové',          '500 03');

-- Syntetické objednávky
-- Objednávka 1: Jana Horáčková – káča (5 ks) + kostky (2 sety) → stav RESERVED
INSERT INTO orders (order_number, customer_id, status, total_price, note)
VALUES ('ORD-2026-00001', 1, 'RESERVED', 697.00, 'Dárek k narozeninám, prosím zabalit do dárkové krabičky.');

-- Objednávka 2: Tomáš Blažek – vláček (1 ks) → stav PROCESSING
INSERT INTO orders (order_number, customer_id, status, total_price, note)
VALUES ('ORD-2026-00002', 2, 'PROCESSING', 549.00, NULL);

-- Objednávka 3: Markéta Svobodová – pejsek (1 ks) → stav CREATED (nová, čeká na rezervaci)
INSERT INTO orders (order_number, customer_id, status, total_price, note)
VALUES ('ORD-2026-00003', 3, 'CREATED', 349.00, 'Zavolat před doručením.');

-- Položky objednávky 1 (ORD-2026-00001 – Jana Horáčková)
-- product_id 1 = Dřevěná káča přírodní (prodejní cena 99 Kč)
-- product_id 3 = Bukové stavební kostky (prodejní cena 249 Kč)
INSERT INTO order_items (order_id, product_id, warehouse_id, quantity, unit_price_at_purchase) VALUES
    (1, 1, 1, 5, 99.00),   -- 5 ks káče z Hradce Králové
    (1, 3, 1, 1, 249.00);  -- 1 set kostek z Hradce Králové

-- Rezervace zásoby pro objednávku 1 (RESERVED → reserved_quantity++ v stock_items)
-- Káča: id=1 je na skladu Hradec (warehouse_id=1) s physical_quantity=18, reserved=0
-- Přidáme 5 ks rezervace:
UPDATE stock_items SET reserved_quantity = reserved_quantity + 5
WHERE product_id = 1 AND warehouse_id = 1;

-- Kostky: id=3 je na skladu Hradec (warehouse_id=1) s physical_quantity=20, reserved=0
-- Přidáme 1 ks rezervace:
UPDATE stock_items SET reserved_quantity = reserved_quantity + 1
WHERE product_id = 3 AND warehouse_id = 1;

-- Položky objednávky 2 (ORD-2026-00002 – Tomáš Blažek)
-- product_id 2 = Dřevěný vláček s vagóny (prodejní cena 549 Kč)
INSERT INTO order_items (order_id, product_id, warehouse_id, quantity, unit_price_at_purchase) VALUES
    (2, 2, 1, 1, 549.00);  -- 1 vláček z Hradce Králové

-- Rezervace zásoby pro objednávku 2 (PROCESSING → zásoba stále rezervována, fyzický odpis proběhne při expedici)
UPDATE stock_items SET reserved_quantity = reserved_quantity + 1
WHERE product_id = 2 AND warehouse_id = 1;

-- Položky objednávky 3 (ORD-2026-00003 – Markéta Svobodová)
-- product_id 5 = Tahací pejsek Bobík (prodejní cena 349 Kč)
-- CREATED: zásoba JEŠTĚ NEBYLA REZERVOVÁNA – demonstrace stavu před rezervací
INSERT INTO order_items (order_id, product_id, warehouse_id, quantity, unit_price_at_purchase) VALUES
    (3, 5, 1, 1, 349.00);  -- 1 pejsek z Hradce Králové
