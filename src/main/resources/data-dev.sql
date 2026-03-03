INSERT INTO APP_USER (email, password_hash, name, surname, enabled, creation_date, last_update_date)
VALUES ('test@gmail.com', '$2a$10$BVifYex3bMA4qRZrPbCQweZMB2qVTYx6qwL7V7v/PxefL.iQU6PVu', 'Test', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Monedas
INSERT INTO CURRENCY (name, symbol, enabled, user_id, creation_date, last_update_date)
VALUES ('Peso Argentino', '$', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CURRENCY (name, symbol, enabled, user_id, creation_date, last_update_date)
VALUES ('Dólar Estadounidense', 'U', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Categorías (type usa EnumType.STRING)
INSERT INTO CATEGORY (name, type, enabled, user_id, creation_date, last_update_date)
VALUES ('Víveres', 'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CATEGORY (name, type, enabled, user_id, creation_date, last_update_date)
VALUES ('Educación', 'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CATEGORY (name, type, enabled, user_id, creation_date, last_update_date)
VALUES ('Salud', 'EXPENSE', true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Método de pago (payment_method_type usa EnumType.ORDINAL: CREDIT_CARD=0, DEBIT_CARD=1, CASH=2, TRANSFER=3)
INSERT INTO PAYMENT_METHOD (name, payment_method_type, enabled, user_id, creation_date, last_update_date)
VALUES ('Mercado Pago', 3, true, (SELECT id FROM APP_USER WHERE email = 'test@gmail.com'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
