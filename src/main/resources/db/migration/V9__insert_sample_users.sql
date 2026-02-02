-- Insert sample users with BCrypt hashed passwords
-- Password for 'admin' user is 'admin123'
-- Password for 'user' is 'user123'
-- BCrypt hash format: $2a$10$... (60 characters)

INSERT INTO users (username, password_hash, role) VALUES
    ('admin', '$2a$10$Ww6vEj7GpuMba44Mi6Jl1uChOtSrZHkx5xvZlqDUWLmPrvq9LANUS', 'ADMIN'),
    ('user', '$2a$10$6hIoMI5aWPeG2ztfkO6DO.1fZRXGXLSa3RM1/ydcrOyhpgb.zio1C', 'USER');
