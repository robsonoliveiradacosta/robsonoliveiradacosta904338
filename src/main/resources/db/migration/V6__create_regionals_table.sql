-- Create regionals table for external API synchronization
CREATE TABLE regionals (
    id INTEGER PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create index on active status for filtering
CREATE INDEX idx_regionals_active ON regionals(active);
