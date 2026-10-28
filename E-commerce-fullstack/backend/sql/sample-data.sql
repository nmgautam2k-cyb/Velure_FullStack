-- Optional sample data: the 12 products from the original db.json, 50 units of stock each.
-- Run AFTER the backend has started once (it creates the tables and the 3 categories).
USE ecommerce_db;

INSERT IGNORE INTO categories (name, slug) VALUES ('T-Shirts','tshirts'),('Shirts','shirts'),('Pants','pants');

INSERT INTO products (name, description, price, stock_quantity, image_url, category_id, created_at)
SELECT v.name, NULL, v.price, 50, v.image, c.id, NOW(6)
FROM (
  SELECT 'Beige pant' AS name, 2499.00 AS price, '/pant1.jpg' AS image, 'pants' AS slug
  UNION ALL
  SELECT 'Brown Trousers' AS name, 1899.00 AS price, '/pant2.jpg' AS image, 'pants' AS slug
  UNION ALL
  SELECT 'Pink pant' AS name, 1599.00 AS price, '/pant3.jpg' AS image, 'pants' AS slug
  UNION ALL
  SELECT 'Green Shirt' AS name, 1299.00 AS price, '/shirt1.webp' AS image, 'shirts' AS slug
  UNION ALL
  SELECT 'Navy Blue Formal Shirt' AS name, 1499.00 AS price, '/shirt2.webp' AS image, 'shirts' AS slug
  UNION ALL
  SELECT 'Patterened Shirt' AS name, 1199.00 AS price, '/shirt3.webp' AS image, 'shirts' AS slug
  UNION ALL
  SELECT 'Green T-Shirt' AS name, 799.00 AS price, '/tshirt1.webp' AS image, 'tshirts' AS slug
  UNION ALL
  SELECT 'Black Striped T-Shirt' AS name, 599.00 AS price, '/tshirt2.webp' AS image, 'tshirts' AS slug
  UNION ALL
  SELECT 'Red T-Shirt' AS name, 699.00 AS price, '/tshirt3.webp' AS image, 'tshirts' AS slug
  UNION ALL
  SELECT 'white Tshirt' AS name, 297.00 AS price, '/tshirt4.webp' AS image, 'tshirts' AS slug
  UNION ALL
  SELECT 'cream' AS name, 19999.00 AS price, '/tshirt5.webp' AS image, 'tshirts' AS slug
  UNION ALL
  SELECT 'velvet' AS name, 123.00 AS price, '/tshirt6.webp' AS image, 'tshirts' AS slug
) v JOIN categories c ON c.slug = v.slug
WHERE NOT EXISTS (SELECT 1 FROM products x WHERE x.name = v.name);

-- Users are NOT seeded here: passwords must be BCrypt-hashed by the application.
-- Create the admin with ADMIN_EMAIL / ADMIN_PASSWORD in backend/.env and register customers through the UI.
