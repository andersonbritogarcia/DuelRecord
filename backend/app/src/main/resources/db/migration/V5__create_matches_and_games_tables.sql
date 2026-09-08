CREATE TABLE tournament_participations (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    tournament_name VARCHAR(150) NOT NULL,
    placement INT NULL,
    store_name VARCHAR(150) NULL,
    swiss_rounds INT NULL,
    notes TEXT NULL,
    played_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_participations_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE RESTRICT
);

CREATE INDEX idx_tournament_participations_player ON tournament_participations(player_id);
CREATE INDEX idx_tournament_participations_played_at ON tournament_participations(played_at);

CREATE TABLE matches (
    id UUID PRIMARY KEY,
    format VARCHAR(50) NOT NULL,
    tournament_participation_id UUID NULL,
    played_at TIMESTAMP WITH TIME ZONE NOT NULL,
    platform VARCHAR(30) NOT NULL,
    match_structure VARCHAR(30) NOT NULL,
    round INT NULL,
    notes TEXT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    winner_player_id UUID NULL,
    is_draw BOOLEAN NOT NULL DEFAULT FALSE,
    is_intentional_draw BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_tournament_participation FOREIGN KEY (tournament_participation_id) REFERENCES tournament_participations(id) ON DELETE SET NULL,
    CONSTRAINT fk_matches_winner_player FOREIGN KEY (winner_player_id) REFERENCES players(id) ON DELETE SET NULL,
    CONSTRAINT fk_matches_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_matches_format ON matches(format);
CREATE INDEX idx_matches_played_at ON matches(played_at);
CREATE INDEX idx_matches_created_by ON matches(created_by);
CREATE INDEX idx_matches_winner_player ON matches(winner_player_id);
CREATE INDEX idx_matches_tournament_participation ON matches(tournament_participation_id);

CREATE TABLE match_participants (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL,
    player_id UUID NOT NULL,
    deck_identity_id UUID NOT NULL,
    seat INT NOT NULL,
    display_name_snapshot VARCHAR(255) NOT NULL,
    is_winner BOOLEAN NOT NULL DEFAULT FALSE,
    game_wins INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_participants_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_participants_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE RESTRICT,
    CONSTRAINT fk_match_participants_deck_identity FOREIGN KEY (deck_identity_id) REFERENCES deck_identities(id) ON DELETE RESTRICT,
    CONSTRAINT uk_match_participants_match_seat UNIQUE (match_id, seat),
    CONSTRAINT uk_match_participants_match_player UNIQUE (match_id, player_id)
);

CREATE INDEX idx_match_participants_match ON match_participants(match_id);
CREATE INDEX idx_match_participants_player ON match_participants(player_id);
CREATE INDEX idx_match_participants_deck_identity ON match_participants(deck_identity_id);

CREATE TABLE games (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL,
    game_number INT NOT NULL,
    winner_player_id UUID NULL,
    starting_player_id UUID NULL,
    is_draw BOOLEAN NOT NULL DEFAULT FALSE,
    duration_seconds INT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_games_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_games_winner_player FOREIGN KEY (winner_player_id) REFERENCES players(id) ON DELETE SET NULL,
    CONSTRAINT fk_games_starting_player FOREIGN KEY (starting_player_id) REFERENCES players(id) ON DELETE SET NULL,
    CONSTRAINT uk_games_match_number UNIQUE (match_id, game_number)
);

CREATE INDEX idx_games_match ON games(match_id);
CREATE INDEX idx_games_winner_player ON games(winner_player_id);
CREATE INDEX idx_games_starting_player ON games(starting_player_id);
