-- Create album_images table for storing image keys from MinIO
CREATE TABLE album_images (
    album_id BIGINT NOT NULL,
    image_key VARCHAR(500) NOT NULL,
    CONSTRAINT fk_album_images_album FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

-- Create index on album_id for faster lookups
CREATE INDEX idx_album_images_album_id ON album_images(album_id);
