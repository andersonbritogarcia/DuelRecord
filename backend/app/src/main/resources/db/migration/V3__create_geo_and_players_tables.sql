CREATE TABLE countries (
    code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cities (
    id UUID PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    name VARCHAR(100) NOT NULL,
    state_province VARCHAR(50) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cities_country FOREIGN KEY (country_code) REFERENCES countries(code) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_cities_country_name_state ON cities (country_code, LOWER(name), coalesce(LOWER(state_province), ''));
CREATE INDEX idx_cities_country_code ON cities(country_code);
CREATE INDEX idx_cities_name ON cities(name);

CREATE TABLE players (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE,
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    mtgo_username VARCHAR(100) NULL,
    arena_username VARCHAR(100) NULL,
    city_id UUID NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector('simple',
            coalesce(name, '') || ' ' ||
            coalesce(display_name, '') || ' ' ||
            coalesce(mtgo_username, '') || ' ' ||
            coalesce(arena_username, '')
        )
    ) STORED,
    CONSTRAINT fk_players_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_players_city FOREIGN KEY (city_id) REFERENCES cities(id) ON DELETE SET NULL
);

CREATE INDEX idx_players_search_vector ON players USING GIN (search_vector);
CREATE INDEX idx_players_user_id ON players(user_id);
CREATE INDEX idx_players_city_id ON players(city_id);
CREATE INDEX idx_players_name ON players(name);
CREATE INDEX idx_players_display_name ON players(display_name);

CREATE TABLE player_merge_logs (
    id UUID PRIMARY KEY,
    target_user_id UUID NOT NULL,
    merged_ghost_player_id UUID NOT NULL,
    original_ghost_name VARCHAR(255) NOT NULL,
    approved_by_admin_id UUID NULL,
    merged_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_merge_logs_target_user FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_merge_logs_admin FOREIGN KEY (approved_by_admin_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_player_merge_logs_target_user ON player_merge_logs(target_user_id);
CREATE INDEX idx_player_merge_logs_ghost_player ON player_merge_logs(merged_ghost_player_id);

-- Seed Countries
INSERT INTO countries (code, name) VALUES
('BR', 'Brazil'),
('US', 'United States'),
('FR', 'France'),
('DE', 'Germany'),
('IT', 'Italy'),
('ES', 'Spain'),
('JP', 'Japan'),
('CA', 'Canada'),
('GB', 'United Kingdom'),
('PT', 'Portugal'),
('CL', 'Chile'),
('AR', 'Argentina'),
('BO', 'Bolivia'),
('PH', 'Philippines'),
('CN', 'China'),
('HN', 'Honduras'),
('PE', 'Peru'),
('SE', 'Sweden'),
('CZ', 'Czech Republic'),
('TH', 'Thailand'),
('CO', 'Colombia'),
('DK', 'Denmark'),
('MY', 'Malaysia'),
('AE', 'United Arab Emirates'),
('ZA', 'South Africa'),
('PL', 'Poland'),
('SK', 'Slovakia'),
('MM', 'Myanmar'),
('MX', 'Mexico'),
('VE', 'Venezuela')
ON CONFLICT (code) DO NOTHING;

-- Seed Major Hub Cities
INSERT INTO cities (id, country_code, name, state_province) VALUES
('01a0649f-32c7-72c6-ba18-31bb8bd61abb', 'BR', 'São Paulo', 'SP'),
('01a0649f-32c7-72c6-ba18-36d8875da579', 'BR', 'Rio de Janeiro', 'RJ'),
('01a0649f-32c7-72c6-ba18-38da94784f3e', 'BR', 'Curitiba', 'PR'),
('01a0649f-32c7-72c6-ba18-3ee037a9e23a', 'BR', 'Belo Horizonte', 'MG'),
('01a0649f-32c7-72c6-ba18-43c5622a4bc6', 'BR', 'Brasília', 'DF'),
('01a0649f-f170-739e-88db-72ff391e26d2', 'BR', 'Porto Alegre', 'RS'),
('01a0649f-f170-739e-88db-74cb44b6c073', 'BR', 'Campinas', 'SP'),
('01a0649f-f170-739e-88db-7aa7fea590a5', 'BR', 'Fortaleza', 'CE'),
('01a0649f-f170-739e-88db-7dac95bbf507', 'BR', 'Recife', 'PE'),
('01a0649f-f170-739e-88db-81254fe112c2', 'BR', 'Salvador', 'BA'),
('01a064a0-666b-776f-8e5f-598bb61f88ea', 'FR', 'Paris', 'IDF'),
('01a064a0-666b-776f-8e5f-5cc4a6d7930c', 'FR', 'Lyon', 'ARA'),
('01a064a0-666b-776f-8e5f-61a1c55c08f8', 'US', 'New York', 'NY'),
('01a064a0-666b-776f-8e5f-64c043bd13a9', 'US', 'Los Angeles', 'CA'),
('01a064a0-666b-776f-8e5f-68c4f175c5c6', 'US', 'Seattle', 'WA'),
('01a064a0-666b-776f-8e5f-6c006bc16abf', 'BR', 'Maringá', 'PR')
ON CONFLICT DO NOTHING;
