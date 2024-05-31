ALTER TABLE customers
ADD CONSTRAINT unique_customer_identity UNIQUE (first_name, last_name, country);

ALTER TABLE cards
ADD CONSTRAINT unique_card_number_currency UNIQUE (card_number, currency);