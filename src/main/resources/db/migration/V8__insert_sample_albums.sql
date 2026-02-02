-- Insert sample albums
INSERT INTO albums (title, year) VALUES
    ('A Night at the Opera', 1975),
    ('Thriller', 1982);

-- Link albums to artists
-- A Night at the Opera by Queen (album_id=1, artist_id=1)
INSERT INTO album_artist (album_id, artist_id) VALUES
    (1, 1);

-- Thriller by Michael Jackson (album_id=2, artist_id=3)
INSERT INTO album_artist (album_id, artist_id) VALUES
    (2, 3);
