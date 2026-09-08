# Domain Glossary (DuelRecord)

- **Ghost Player**: A player profile registered by a third-party (opponents) without an associated User account. They exist so matches can be recorded against non-registered opponents. Can later be converted to a fully registered user through a *Claim/Merge* process.
- **Match Verification Status**:
  - UNVERIFIED: A match registered unilaterally (e.g. against a Ghost Player). Impacts personal stats, but handling in global rankings is subject to restrictions to prevent abuse. Remains UNVERIFIED forever even after a Ghost Player merge to prevent legacy conflicts.
  - VERIFIED: A match either mutually confirmed by both authenticated users, or imported from a cryptographic/trusted source (like MTGO logs).
  - DISPUTED: If mutually requested validation is explicitly rejected, the match enters a neutral DISPUTED limbo. It contributes 0 points anywhere until an admin resolves or the match is deleted.
- **Temporal Edit Lock**: To avoid catastrophic cascading recalculations in the Glicko/ELO algorithms, matches are hard-locked against any result edits after **7 days**. Retrospective imports from sources like MTGO process rating points at the time of import calculation.
- **Tournament**: An event happening on a specific date, hosted at a specific location/store, restricted to a specific format. It groups together TournamentParticipation records to avoid deduplication.
  - *Deduplication Constraint*: The system strictly prevents duplicate tournaments by enforcing uniqueness on Location/Store + Date + Format. If a user attempts to create one, they are prompted to join the existing event.
- **Match Deduplication Window**: The engine will halt and prompt the user if they submit a match that overlaps with a pending/existing match on the same date between the same two players to prevent accidental double-submits. Manual matches will also be conflict-checked against batch imports.
- **Player Rating**:
  - Verified_Rating: A Glicko-2 score attached strictly to Player + Format and calculated ONLY from VERIFIED matches ("Official Ranking").
  - Global_Rating: A Glicko-2 score attached strictly to Player + Format and calculated from ALL matches (VERIFIED + UNVERIFIED) ("Community Ranking").
- **Game Outcomes**: A match is constructed iteratively via sub-components called Games, which can end in WIN, LOSS, or DRAW. An Intentional Draw registers 0 games but creates a DRAW outcome on the match.
- **Unknown Deck / Commander**: If a player cannot remember an opponent's commander, a global fallback Unknown Commander Deck Identity is utilized so the match can gracefully be recorded for personal analytics.
