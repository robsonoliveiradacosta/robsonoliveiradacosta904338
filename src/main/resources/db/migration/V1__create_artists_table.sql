-- Create artists table
CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('SINGER', 'BAND'))
);

-- Create index on name for faster searches
CREATE INDEX idx_artists_name ON artists(name);
