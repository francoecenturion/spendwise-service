-- ─────────────────────────────────────────────────────────────────────────────
-- USUARIO DE PRUEBA
-- password: test1234
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO APP_USER (email, password_hash, name, surname, enabled, role, creation_date, last_update_date)
VALUES ('test@gmail.com', '$2a$10$LTDozBa/.MSCvCqve9TvJe1xLkXhuBMQWEFOybp/zrpzvp1W4ucJ2', 'Franco', 'Test', true, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- MONEDAS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO CURRENCY (name, symbol, icon, enabled, is_default, user_id, creation_date, last_update_date)
VALUES ('Peso Argentino', 'ARS', '🇦🇷', true, true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CURRENCY (name, symbol, icon, enabled, is_default, user_id, creation_date, last_update_date)
VALUES ('Dólar Estadounidense', 'USD', '🇺🇸', true, false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- CATEGORÍAS — GASTOS  (type = STRING)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO CATEGORY (name, icon, type, enabled, user_id, creation_date, last_update_date) VALUES
  ('Víveres',         'ShoppingCart',  'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Restaurantes',    'Utensils',      'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Transporte',      'Car',           'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Hogar',           'Home',          'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Servicios',       'Zap',           'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Salud',           'Pill',          'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Entretenimiento', 'Gamepad2',      'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Ropa',            'Shirt',         'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Tecnología',      'Laptop',        'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Educación',       'BookOpen',      'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Café / Salidas',  'Coffee',        'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Mascotas',        'PawPrint',      'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CATEGORÍAS — INGRESOS
INSERT INTO CATEGORY (name, icon, type, enabled, user_id, creation_date, last_update_date) VALUES
  ('Sueldo',          'Wallet',        'INCOME', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Freelance',       'Globe',         'INCOME', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Alquiler',        'Building2',     'INCOME', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CATEGORÍAS — AHORROS / INVERSIONES
INSERT INTO CATEGORY (name, icon, type, enabled, user_id, creation_date, last_update_date) VALUES
  ('Ahorro personal', 'Star',          'SAVING',     true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Inversiones',     'TrendingUp',    'INVESTMENT', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- ENTIDADES FINANCIERAS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO ISSUING_ENTITY (description, enabled, user_id, creation_date, last_update_date) VALUES
  ('Banco Santander',  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Banco Galicia',    true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Banco Nación',     true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('BBVA',             true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Mercado Pago',     true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- MÉTODOS DE PAGO
-- payment_method_type ordinal: CREDIT_CARD=0, DEBIT_CARD=1, CASH=2, TRANSFER=3
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, creation_date, last_update_date)
VALUES ('Efectivo', 2, true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, creation_date, last_update_date)
VALUES ('Transferencia', 3, true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, issuing_entity_id, creation_date, last_update_date)
VALUES ('Débito Santander', 1, true,
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'),
  (SELECT id FROM ISSUING_ENTITY WHERE description = 'Banco Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, issuing_entity_id, creation_date, last_update_date)
VALUES ('Visa Crédito Santander', 0, true,
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'),
  (SELECT id FROM ISSUING_ENTITY WHERE description = 'Banco Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, issuing_entity_id, creation_date, last_update_date)
VALUES ('Mastercard Galicia', 0, true,
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'),
  (SELECT id FROM ISSUING_ENTITY WHERE description = 'Banco Galicia' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, issuing_entity_id, creation_date, last_update_date)
VALUES ('Mercado Pago', 3, true,
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'),
  (SELECT id FROM ISSUING_ENTITY WHERE description = 'Mercado Pago' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- BILLETERAS / CUENTAS DE AHORRO
-- savings_wallet_type ordinal: BANK_ACCOUNT=0, VIRTUAL_WALLET=1, MUTUAL_FUND=2, FIXED_TERM=3, CASH=4
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO SAVINGS_WALLET (name, savings_wallet_type, enabled, issuing_entity_id, user_id, creation_date, last_update_date) VALUES
  ('Caja de Ahorro Santander', 0, true,
    (SELECT id FROM ISSUING_ENTITY WHERE description = 'Banco Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
    (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Cuenta Mercado Pago', 1, true,
    (SELECT id FROM ISSUING_ENTITY WHERE description = 'Mercado Pago' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
    (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Efectivo en mano', 4, true, null,
    (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- GASTOS RECURRENTES
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO RECURRENT_EXPENSE (description, icon, amount_ars, day_of_month, category_id, currency_id, enabled, user_id, creation_date, last_update_date)
VALUES ('Netflix', '📺', 8990, 1,
  (SELECT id FROM CATEGORY WHERE name = 'Entretenimiento' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO RECURRENT_EXPENSE (description, icon, amount_ars, day_of_month, category_id, currency_id, enabled, user_id, creation_date, last_update_date)
VALUES ('Spotify', '🎵', 3490, 15,
  (SELECT id FROM CATEGORY WHERE name = 'Entretenimiento' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO RECURRENT_EXPENSE (description, icon, amount_ars, day_of_month, category_id, currency_id, enabled, user_id, creation_date, last_update_date)
VALUES ('Gym', '🏋️', 25000, 5,
  (SELECT id FROM CATEGORY WHERE name = 'Salud' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO RECURRENT_EXPENSE (description, icon, amount_ars, day_of_month, category_id, currency_id, enabled, user_id, creation_date, last_update_date)
VALUES ('Alquiler', '🏠', 350000, 10,
  (SELECT id FROM CATEGORY WHERE name = 'Hogar' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO RECURRENT_EXPENSE (description, icon, amount_usd, day_of_month, category_id, currency_id, enabled, user_id, creation_date, last_update_date)
VALUES ('Adobe Creative Cloud', '🎨', 59.99, 20,
  (SELECT id FROM CATEGORY WHERE name = 'Tecnología' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Dólar Estadounidense' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- INGRESOS (marzo 2026)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO INCOME (description, amount_ars, amount_usd, source_id, currency_id, date, user_id, creation_date, last_update_date)
VALUES ('Sueldo marzo', 850000, 680,
  (SELECT id FROM CATEGORY WHERE name = 'Sueldo' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE symbol = 'ARS' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  '2026-03-05',
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO INCOME (description, amount_ars, amount_usd, source_id, currency_id, date, user_id, creation_date, last_update_date)
VALUES ('Proyecto freelance', 120000, 96,
  (SELECT id FROM CATEGORY WHERE name = 'Freelance' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE symbol = 'ARS' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  '2026-03-12',
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- GASTOS (marzo 2026)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Supermercado Coto', 45000, 36,    '2026-03-01',
  (SELECT id FROM CATEGORY WHERE name = 'Víveres' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Débito Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Almuerzo con compañeros', 12500, 10,  '2026-03-03',
  (SELECT id FROM CATEGORY WHERE name = 'Restaurantes' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Efectivo' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('SUBE', 2400, 2,    '2026-03-04',
  (SELECT id FROM CATEGORY WHERE name = 'Transporte' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Efectivo' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Netflix', 8990, 7.19,  '2026-03-01',
  (SELECT id FROM CATEGORY WHERE name = 'Entretenimiento' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Visa Crédito Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Farmacity', 18600, 14.88,  '2026-03-07',
  (SELECT id FROM CATEGORY WHERE name = 'Salud' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Mastercard Galicia' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Café La Biela', 3800, 3.04,  '2026-03-08',
  (SELECT id FROM CATEGORY WHERE name = 'Café / Salidas' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Mercado Pago' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Rappi - almuerzo', 9200, 7.36,  '2026-03-10',
  (SELECT id FROM CATEGORY WHERE name = 'Restaurantes' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Mercado Pago' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Expensas', 85000, 68,  '2026-03-10',
  (SELECT id FROM CATEGORY WHERE name = 'Hogar' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Transferencia' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Supermercado Carrefour', 62000, 49.6,  '2026-03-14',
  (SELECT id FROM CATEGORY WHERE name = 'Víveres' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Débito Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Edesur', 22000, 17.6,  '2026-03-15',
  (SELECT id FROM CATEGORY WHERE name = 'Servicios' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Débito Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Auriculares Sony', 89000, 71.2,  '2026-03-16',
  (SELECT id FROM CATEGORY WHERE name = 'Tecnología' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Visa Crédito Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO EXPENSE (description, amount_ars, amount_usd, date, category_id, payment_method_id, currency_id, micro_expense, user_id, creation_date, last_update_date)
VALUES ('Spotify', 3490, 2.79,  '2026-03-15',
  (SELECT id FROM CATEGORY WHERE name = 'Entretenimiento' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM PAYMENT_METHOD WHERE name = 'Visa Crédito Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  false, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- AHORROS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO SAVING (description, currency_id, savings_wallet_id, amount_ars, amount_usd, date, user_id, creation_date, last_update_date)
VALUES ('Ahorro mensual marzo',
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM SAVINGS_WALLET WHERE name = 'Caja de Ahorro Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  100000, 80, '2026-03-05',
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO SAVING (description, currency_id, savings_wallet_id, amount_ars, amount_usd, date, user_id, creation_date, last_update_date)
VALUES ('Dólares guardados',
  (SELECT id FROM CURRENCY WHERE name = 'Dólar Estadounidense' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM SAVINGS_WALLET WHERE name = 'Efectivo en mano' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  250000, 200, '2026-03-10',
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ─────────────────────────────────────────────────────────────────────────────
-- DEUDAS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO DEBT (description, amount_ars, amount_usd, date, cancelled, personal, creditor, currency_id, user_id, creation_date, last_update_date)
VALUES ('Le debo a Juan por la cena', 15000, 12, '2026-03-08', false, true, 'Juan García',
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO DEBT (description, amount_ars, amount_usd, date, due_date, cancelled, personal, issuing_entity, currency_id, user_id, creation_date, last_update_date)
VALUES ('Cuota tarjeta Santander febrero', 120000, 96, '2026-02-28', '2026-03-15', true, false,
  (SELECT id FROM ISSUING_ENTITY WHERE description = 'Banco Santander' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO DEBT (description, amount_ars, amount_usd, date, due_date, cancelled, personal, issuing_entity, currency_id, user_id, creation_date, last_update_date)
VALUES ('Cuota tarjeta Galicia marzo', 95000, 76, '2026-03-01', '2026-04-10', false, false,
  (SELECT id FROM ISSUING_ENTITY WHERE description = 'Banco Galicia' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM CURRENCY WHERE name = 'Peso Argentino' AND user_id = (SELECT id FROM APP_USER WHERE email = 'test@gmail.com')),
  (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
