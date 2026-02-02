-- V10: Alter album_images table to add metadata columns and change structure

-- Drop the existing album_images table since we're changing from ElementCollection to Entity
DROP TABLE IF EXISTS album_images;

-- Recreate album_images table with new structure
CREATE TABLE album_images (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    hash VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size INTEGER NOT NULL,
    CONSTRAINT fk_album_images_album FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- Create indexes for efficient lookups
CREATE INDEX idx_album_images_album_id ON album_images(album_id);
CREATE INDEX idx_album_images_hash ON album_images(hash);
