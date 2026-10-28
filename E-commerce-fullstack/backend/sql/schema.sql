-- Reference schema for MySQL 8+ (utf8mb4). It mirrors the JPA entities.
-- You do NOT need to run this for development: with spring.jpa.hibernate.ddl-auto=update the app creates
-- the tables itself. Use this file for review, for DBAs, or as the starting point of a Flyway V1 migration
-- (then run the app with DDL_AUTO=validate).
CREATE DATABASE IF NOT EXISTS ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_db;

CREATE TABLE IF NOT EXISTS users (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  full_name   VARCHAR(100) NOT NULL,
  email       VARCHAR(150) NOT NULL,
  password    VARCHAR(100) NOT NULL,            -- BCrypt hash
  phone       VARCHAR(20)  NULL,
  role        VARCHAR(20)  NOT NULL,            -- ADMIN | USER
  created_at  DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  slug        VARCHAR(100) NOT NULL,
  description VARCHAR(500) NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_categories_name UNIQUE (name),
  CONSTRAINT uk_categories_slug UNIQUE (slug)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
  id             BIGINT         NOT NULL AUTO_INCREMENT,
  name           VARCHAR(150)   NOT NULL,
  description    VARCHAR(2000)  NULL,
  price          DECIMAL(12,2)  NOT NULL,
  stock_quantity INT            NOT NULL,
  image_url      VARCHAR(500)   NULL,
  category_id    BIGINT         NOT NULL,
  created_at     DATETIME(6)    NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT chk_products_price CHECK (price >= 0),
  CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0),
  INDEX idx_products_category (category_id),
  INDEX idx_products_name (name),
  INDEX idx_products_created_at (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS carts (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  user_id     BIGINT      NOT NULL,
  created_at  DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_carts_user UNIQUE (user_id),
  CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cart_items (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  cart_id     BIGINT        NOT NULL,
  product_id  BIGINT        NOT NULL,
  quantity    INT           NOT NULL,
  unit_price  DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
  CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
  CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT chk_cart_items_qty CHECK (quantity > 0),
  INDEX idx_cart_items_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
  id               BIGINT        NOT NULL AUTO_INCREMENT,
  user_id          BIGINT        NOT NULL,
  total_amount     DECIMAL(12,2) NOT NULL,
  order_status     VARCHAR(20)   NOT NULL,   -- PENDING | PROCESSING | SHIPPED | DELIVERED | CANCELLED
  payment_status   VARCHAR(20)   NOT NULL,   -- PENDING | TEST_SIMULATED | PAID
  payment_method   VARCHAR(20)   NOT NULL,   -- COD | GOOGLE_PAY | PHONEPE
  shipping_address VARCHAR(1000) NOT NULL,
  created_at       DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
  INDEX idx_orders_user (user_id),
  INDEX idx_orders_status (order_status),
  INDEX idx_orders_created_at (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  order_id      BIGINT        NOT NULL,
  product_id    BIGINT        NOT NULL,
  product_name  VARCHAR(150)  NOT NULL,
  product_image VARCHAR(500)  NULL,
  quantity      INT           NOT NULL,
  unit_price    DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
  INDEX idx_order_items_order (order_id),
  INDEX idx_order_items_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS wishlist_items (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  user_id     BIGINT      NOT NULL,
  product_id  BIGINT      NOT NULL,
  created_at  DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_wishlist_user_product UNIQUE (user_id, product_id),
  CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES products (id),
  INDEX idx_wishlist_product (product_id)
) ENGINE=InnoDB;
