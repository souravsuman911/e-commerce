https://docs.google.com/document/d/1VWpix7j2Y3Qrsgm2_CpMiNEs_rWGKnUGfnawsBqK8FA/edit?usp=sharing

-- auth_db
-- Tables :-
-- users : stores user details
-- roles : stores available roles (USER, ADMIN)
-- user_roles : mapping between users & roles (many-to-many)

CREATE DATABASE auth_db;
USE auth_db;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- User ↔ Role mapping
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN');










-- product_db
-- Tables :-
-- products – list of available products

CREATE DATABASE product_db;
USE product_db;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Example product
INSERT INTO products (name, description, price, stock) 
VALUES ('iPhone 15', 'Latest Apple iPhone', 79999.99, 50);









-- cart_db
-- Tables
-- carts – one cart per user
-- cart_items – items inside the cart (linked to product)

CREATE DATABASE cart_db;
USE cart_db;

-- Cart linked to a user (from auth-service via user_id)
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Items inside the cart
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id)
    -- product_id is from product-service (validated via API, not FK here)
);

-- Example cart
INSERT INTO carts (user_id) VALUES (1);

-- Example cart item
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (1, 1, 2);










-- order_db
-- Tables
-- orders – order info
-- order_items – items in an order

CREATE DATABASE order_db;
USE order_db;

-- Orders table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Example order
INSERT INTO orders (user_id, total_amount, status) 
VALUES (1, 159999.98, 'PENDING');

INSERT INTO order_items (order_id, product_id, quantity, price) 
VALUES (1, 1, 2, 79999.99);
