-- FoodieGo MySQL Database Configuration Script
-- Target Database: foodiego_db

CREATE DATABASE IF NOT EXISTS foodiego_db;
USE foodiego_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Products Table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    price VARCHAR(20) NOT NULL,
    rating VARCHAR(10) NOT NULL,
    delivery_time VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Cart Table
CREATE TABLE IF NOT EXISTS cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_price VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price VARCHAR(20) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seeding Premium Culinary Food Products
INSERT INTO products (name, description, image_url, price, rating, delivery_time) VALUES
('Pizza Margherita', 'Classic mozzarella cheese, fresh basil leaves, extra virgin olive oil, and house marinara sauce.', 'https://images.unsplash.com/photo-1604382355076-af4b0eb60143?q=80&w=600&auto=format&fit=crop', '₹199', '4.7', '25 min'),
('Cheese Burger', 'Double-layered prime beef patty, cheddar cheese, fresh veggies, and secret house burger sauce.', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=600&auto=format&fit=crop', '₹149', '4.5', '15 min'),
('White Sauce Pasta', 'Penne pasta tossed in rich, creamy garlic parmesan white sauce with buttered mushrooms.', 'https://images.unsplash.com/photo-1645112411341-6c4fd023714a?q=80&w=600&auto=format&fit=crop', '₹249', '4.6', '30 min'),
('Veggie Club Sandwich', 'Triple decker toasted sandwich loaded with fresh cucumber, tomatoes, lettuce, and premium cheddar.', 'https://images.unsplash.com/photo-1525351484163-7529414344d8?q=80&w=600&auto=format&fit=crop', '₹119', '4.3', '20 min'),
('Caramel Macchiato', 'Rich espresso combined with milk and sweet vanilla syrup, finished with a caramel drizzle.', 'https://images.unsplash.com/photo-1572286258217-40142c1c6a70?q=80&w=600&auto=format&fit=crop', '₹179', '4.8', '10 min'),
('Double Chocolate Muffin', 'Rich, moist chocolate muffin filled with premium Belgian dark chocolate chips.', 'https://images.unsplash.com/photo-1607958996333-41aef7caefaa?q=80&w=600&auto=format&fit=crop', '₹99', '4.4', '12 min'),
('Paneer Tikka Wrap', 'Grilled cottage cheese cubes wrapped in soft tortilla wrap with crisp onions and mint mayo.', 'https://images.unsplash.com/photo-1626700051175-6518c4793f4f?q=80&w=600&auto=format&fit=crop', '₹159', '4.5', '25 min'),
('Garlic Bread with Cheese', 'Crispy freshly baked artisanal baguette slices topped with garlic herb butter and melted mozzarella.', 'https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?q=80&w=600&auto=format&fit=crop', '₹129', '4.6', '18 min'),
('Chocolate Lava Cake', 'Hot chocolate soufflé cake with a rich and gooey molten Belgian chocolate center.', 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?q=80&w=600&auto=format&fit=crop', '₹139', '4.9', '15 min'),
('Tropical Mango Smoothie', 'A creamy and refreshing tropical blend of sweet Alphonso mangoes, fresh banana, and Greek yogurt.', 'https://images.unsplash.com/photo-1553530666-ba11a7da3888?q=80&w=600&auto=format&fit=crop', '₹149', '4.7', '10 min');
