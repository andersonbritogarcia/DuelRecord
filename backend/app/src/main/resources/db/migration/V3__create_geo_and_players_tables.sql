CREATE TABLE countries (
    code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cities (
    id UUID PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cities_country FOREIGN KEY (country_code) REFERENCES countries(code) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_cities_country_name ON cities (country_code, LOWER(name));
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
INSERT INTO cities (id, country_code, name) VALUES
('019544e3-3f62-7200-8000-000000000001', 'BR', 'São Paulo'),
('019544e3-3f62-7200-8000-000000000002', 'BR', 'Rio de Janeiro'),
('019544e3-3f62-7200-8000-000000000003', 'BR', 'Curitiba'),
('019544e3-3f62-7200-8000-000000000004', 'BR', 'Belo Horizonte'),
('019544e3-3f62-7200-8000-000000000005', 'BR', 'Brasília'),
('019544e3-3f62-7200-8000-000000000006', 'BR', 'Porto Alegre'),
('019544e3-3f62-7200-8000-000000000007', 'BR', 'Campinas'),
('019544e3-3f62-7200-8000-000000000008', 'BR', 'Fortaleza'),
('019544e3-3f62-7200-8000-000000000009', 'BR', 'Recife'),
('019544e3-3f62-7200-8000-00000000000a', 'BR', 'Salvador'),
('019544e3-3f62-7200-8000-00000000000b', 'FR', 'Paris'),
('019544e3-3f62-7200-8000-00000000000c', 'FR', 'Lyon'),
('019544e3-3f62-7200-8000-00000000000d', 'US', 'New York'),
('019544e3-3f62-7200-8000-00000000000e', 'US', 'Los Angeles'),
('019544e3-3f62-7200-8000-00000000000f', 'US', 'Seattle'),
('019544e3-3f62-7200-8000-000000000010', 'BR', 'Maringá')
ON CONFLICT DO NOTHING;
