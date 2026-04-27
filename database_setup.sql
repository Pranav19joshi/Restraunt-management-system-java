CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

CREATE TABLE IF NOT EXISTS menu_items (
    itemId VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    basePrice DOUBLE NOT NULL,
    category VARCHAR(100),
    itemType VARCHAR(50),
    extra1 VARCHAR(100),
    extra2 VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS orders (
    orderId VARCHAR(50) PRIMARY KEY,
    customerName VARCHAR(255),
    status VARCHAR(50),
    total DOUBLE,
    orderTime VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderId VARCHAR(50),
    itemName VARCHAR(255),
    FOREIGN KEY (orderId) REFERENCES orders(orderId) ON DELETE CASCADE
);
