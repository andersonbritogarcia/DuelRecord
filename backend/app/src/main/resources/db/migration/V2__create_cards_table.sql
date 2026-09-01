CREATE TABLE cards (
    id UUID PRIMARY KEY,
    scryfall_id VARCHAR(36) NOT NULL,
    oracle_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type_line VARCHAR(255) NOT NULL,
    mana_cost VARCHAR(100) NULL,
    cmc NUMERIC(5,1) NOT NULL DEFAULT 0.0,
    color_identity VARCHAR(10) NOT NULL DEFAULT '',
    image_uri_small TEXT NULL,
    image_uri_normal TEXT NULL,
    image_uri_art_crop TEXT NULL,
    is_commander_legal BOOLEAN NOT NULL DEFAULT FALSE,
    is_partner BOOLEAN NOT NULL DEFAULT FALSE,
    is_companion BOOLEAN NOT NULL DEFAULT FALSE,
    is_background BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(type_line, '')), 'B')
    ) STORED,
    CONSTRAINT uk_cards_scryfall_id UNIQUE (scryfall_id)
);

CREATE INDEX idx_cards_search_vector ON cards USING GIN (search_vector);
CREATE INDEX idx_cards_oracle_id ON cards(oracle_id);
CREATE INDEX idx_cards_name ON cards(name);
CREATE INDEX idx_cards_color_identity ON cards(color_identity);
CREATE INDEX idx_cards_is_commander_legal ON cards(is_commander_legal);
