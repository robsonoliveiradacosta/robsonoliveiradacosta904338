-- Insert sample users with BCrypt hashed passwords
-- Password for 'admin' user is 'admin123'
-- Password for 'user' is 'user123'
-- BCrypt hash format: $2a$10$... (60 characters)

INSERT INTO users (username, password_hash, role) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
    ('user', '$2a$10$UZLCfq5j9eVhFMGEQKpYFeM6rcI4pVVGx0J6OvXKQW1LYvZ8VqXnS', 'USER');
