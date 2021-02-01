
CREATE TABLE users(
user_id SERIAL,
user_email VARCHAR(255) NOT NULL UNIQUE,
user_password VARCHAR NOT NULL,
user_salt VARCHAR NOT NULL,
user_username VARCHAR(50),
user_firstname VARCHAR(100),
user_lastname VARCHAR(100),
user_register_date DATE,
user_picture BYTEA,
CONSTRAINT pk_users PRIMARY KEY (user_id, user_email)
);

INSERT INTO users(user_username, user_email, user_password, user_salt) VALUES('prueba1','prueba1@gmail.com','1234567','23234');

CREATE TABLE categories (
category_id SERIAL,
category_name VARCHAR(255) NOT NULL,
category_icon VARCHAR(1000),
CONSTRAINT pk_categories PRIMARY KEY (category_name)
);

INSERT INTO categories (category_name) VALUES('Sports');
INSERT INTO categories (category_name) VALUES('Music');

CREATE TABLE products(
product_id SERIAL PRIMARY KEY,
category_id int NOT NULL, -- solo pertenece a uno
active boolean default true,
product_title VARCHAR(255),
product_desc VARCHAR(2000),
multiscan BOOLEAN DEFAULT FALSE,
user_owned int NOT null,
user_created  int NOT null,
user_updated int,
date_created DATE DEFAULT NOW(),
date_updated DATE DEFAULT NOW(),
visit_count INT,
aprox_radius INT,
aprox_latitude SMALLINT,
aprox_longitude SMALLINT,
city VARCHAR(200),
zip INT,
CONSTRAINT fk_prod_categ FOREIGN KEY(category_id) REFERENCES categories(category_id)
);

INSERT INTO products() VALUES();

CREATE TABLE scanned(
scanned_id SERIAL,
product_id int NOT NULL,
user_id int NOT NULL,
scanned_date DATE DEFAULT NOW(),
CONSTRAINT pk_scan_prod_us PRIMARY KEY (product_id, user_id)
);

CREATE TABLE pictures_product(
picture_id SERIAL PRIMARY KEY,
product_id INT NOT NULL,
picture_data BYTEA NOT NULL,
CONSTRAINT fk_prod_categ FOREIGN KEY(product_id) REFERENCES products(product_id)
);

CREATE TABLE favourites(
fav_id SERIAL,
product_id int NOT NULL,
user_id int NOT NULL,
CONSTRAINT pk_fav_user PRIMARY KEY (product_id, user_id)
);

INSERT INTO favourites(product_id, user_id) VALUES(123 , 543);
INSERT INTO favourites(product_id, user_id) VALUES(124 , 543);
-- INSERT INTO favourites(product_id, user_id) VALUES(56 , 63);
-- INSERT INTO favourites(product_id, user_id) VALUES(1245 , 543);
