-- Insert sample users with BCrypt hashed passwords (Quarkus Elytron BcryptUtil)
-- Password for 'admin' user is 'admin123'
-- Password for 'user' is 'user123'
-- BCrypt hash format: $2a$10$... (60 characters)

INSERT INTO users (username, password_hash, role) VALUES
    ('admin', '$2a$10$wp/KY6ZL1/8fsSE1YeOiMuRI1JaAX/5BuCl2lzkuHgqbXtAozTeoC', 'ADMIN'),
    ('user', '$2a$10$zCEFQe/MxZAOqpvbf2v0q.yiWYymQeZ5llT0NFzAavCK9hEpKYY2.', 'USER');
