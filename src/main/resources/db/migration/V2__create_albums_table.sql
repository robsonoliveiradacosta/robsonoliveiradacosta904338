-- Create albums table
CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    year INTEGER NOT NULL
);

-- Create index on title for faster searches
CREATE INDEX idx_albums_title ON albums(title);

-- Create index on year for filtering
CREATE INDEX idx_albums_year ON albums(year);
