CREATE TABLE deck_identities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color_identity VARCHAR(10) NOT NULL,
    signature VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_deck_identities_signature UNIQUE (signature)
);

CREATE TABLE deck_identity_cards (
    id UUID PRIMARY KEY,
    deck_identity_id UUID NOT NULL,
    card_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deck_identity_cards_identity FOREIGN KEY (deck_identity_id) REFERENCES deck_identities(id) ON DELETE CASCADE,
    CONSTRAINT fk_deck_identity_cards_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE RESTRICT,
    CONSTRAINT uk_deck_identity_cards_identity_card_role UNIQUE (deck_identity_id, card_id, role)
);

CREATE INDEX idx_deck_identities_color_identity ON deck_identities(color_identity);
CREATE INDEX idx_deck_identity_cards_identity ON deck_identity_cards(deck_identity_id);
CREATE INDEX idx_deck_identity_cards_card ON deck_identity_cards(card_id);
