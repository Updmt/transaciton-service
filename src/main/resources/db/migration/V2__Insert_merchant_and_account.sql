INSERT INTO merchants (id, secret_key, created_at, company_recognition, country)
VALUES ('4bb5752d-e861-483f-8075-a763295a9d07', 'secret', NOW(), 'Company', 'Solarion');

INSERT INTO accounts (currency, balance, merchant_id)
VALUES ('USD', 100000.00, '4bb5752d-e861-483f-8075-a763295a9d07');

INSERT INTO accounts (currency, balance, merchant_id)
VALUES ('EUR', 100000.00, '4bb5752d-e861-483f-8075-a763295a9d07');
