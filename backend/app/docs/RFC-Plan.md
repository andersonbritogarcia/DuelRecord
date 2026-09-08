# RFC — DuelRecord

**Status:** Plano consolidado para implementação (V1 Enxuta)
**Produto:** DuelRecord
**Backend:** Java + Spring Boot + Spring Modulith
**Banco:** PostgreSQL
**Frontend:** Angular (PWA Offline-first)
**Escopo:** formatos MTG 1x1 baseados em comandante
**MVP (V1):** Duel Commander, Duel Commander 500 + Brawl
**Autenticação V1:** Google Identity exclusivo (Passwordless / Social Only)
**Integrações V1:** Google Identity + Scryfall
**V2:** Discord SSO, Curadoria de Claim/Merge, Rankings Regionais Avançados, importação MTGO + Arena Logs

---

## 1. Visão

DuelRecord será uma plataforma de histórico competitivo, analytics e rivalidades para formatos 1x1 de Magic baseados em comandante.

O produto não é um gerenciador de torneios nem um deck builder. O valor central é transformar partidas registradas em respostas:
- Qual é meu record e win rate?
- Qual comandante funciona melhor para mim?
- Contra qual jogador ou comandante tenho melhor/pior desempenho?
- Como um jogador específico performa pilotando determinado comandante?
- Quais são os melhores comandantes de uma identidade de cor?
- Eu performo melhor no Play ou Draw?
- Paper, MTGO ou Arena?
- Como meu rating evoluiu?
- Quem é meu maior rival, carrasco ou "freguês"?

> **DuelRecord — Your competitive MTG record.**

---

## 2. Escopo de formatos

```text
GameFormat
- DUEL_COMMANDER
- DUEL_COMMANDER_500
- BRAWL
```

A plataforma continuará estritamente 1x1. cEDH e Commander multiplayer ficam fora do escopo.

- **Duel Commander 500:** Formato de primeira classe com isolamento total de `PlayerRating`, win rate e rankings.
- **Regras por formato:**
```text
FormatRules
- format
- default_match_type
- max_games
- allowed_platforms
- uses_color_identity
- supports_multiple_commanders
```

---

## 3. Plataformas

```text
Platform
- PAPER
- MTGO
- ARENA
```

---

## 4. Arquitetura

```text
Angular SPA (PWA Offline-First)
    |
Spring Boot Modular Monolith (Spring Modulith Events)
    |
PostgreSQL (Transacional + Read Models Diários)
    |
+ Scryfall (Cache local)
+ Google Identity (SSO Exclusivo V1)

V2:
+ Discord SSO
+ MTGO Import Adapter
+ Arena Log Import Adapter
```

---

## 5. User, Player e Identidade

```text
User
- id
- email
- auth_provider_id (Google Sub ID)
- status
- created_at
- updated_at
```

```text
Player
- id
- user_id nullable (se null, é Ghost Player)
- name
- display_name
- mtgo_username nullable
- arena_username nullable
- city_id nullable
- created_at
- updated_at
```

```text
PlayerMergeLog (Preparação V1 / Operação V2)
- id
- target_user_id
- merged_ghost_player_id
- original_ghost_name
- approved_by_admin_id
- merged_at
```

*Nota V1:* Autenticação 100% via Google Identity (sem senhas locais). A funcionalidade de Claim/Merge com fluxo de curadoria fica preparada no schema e entra operacional na V2 (na V1, o admin pode rodar merges pontuais via script/log).

---

## 6. Cards e Scryfall

Cache local de cartas no PostgreSQL sincronizado via Scryfall. O frontend consome apenas a API do DuelRecord.

---

## 7. Color Identity

Identidade de cor canônica (`W U B R G`). O backend processa canonicamente e a interface apresenta labels amigáveis ("Esper", "Sultai", "Mono Red").

---

## 8. DeckIdentity e Mecânicas Adjacentes

```text
DeckIdentity
- id
- format
- display_name nullable
- color_identity
- created_at
```

```text
DeckIdentityCard
- deck_identity_id
- card_id
- role (COMMANDER, PARTNER, COMPANION, BACKGROUND)
- position
```

---

## 9. Deck (Opcional)

Cadastro de lista completa (100 cartas) é opcional. O registro rápido aceita apenas os comandantes.

---

## 10. TournamentParticipation

Registra a participação do jogador no torneio (colocação, loja, notas), não administra chaves.

---

## 11. Quick Match / Standalone Match

Partidas fora de torneio utilizam `Match.tournament_participation_id = null`.

---

## 12. Match e Status de Verificação

```text
Match
- id
- format
- tournament_participation_id nullable
- played_at
- platform
- round nullable
- notes nullable
- source (MANUAL, MTGO_IMPORT, ARENA_IMPORT)
- verification_status (UNVERIFIED, VERIFIED)
- created_by
- created_at
- updated_at
```

- **UNVERIFIED:** Registradas unilateralmente (contam para estatísticas gerais, perfil, rivalidades e Ranking Comunitário/Geral).
- **VERIFIED:** Validadas mutuamente ou importadas (alimentam o Ranking Oficial/Verificado e o Ranking Comunitário/Geral).

---

## 13. MatchParticipant

```text
MatchParticipant
- id
- match_id
- player_id
- deck_identity_id
- display_name_snapshot
- seat
```

Preserva `display_name_snapshot` imutável para que, mesmo após merges, o histórico mostre o nome utilizado no dia da partida.

---

## 14. Games, Play e Draw

Play/Draw registrado por game. Backend calcula win/loss da match.

---

## 15. Níveis de Completude de Dados

- **BASIC:** Score + comandantes
- **STANDARD:** + Oponente + games
- **DETAILED:** + Play/Draw por game + notas

---

## 16. Métricas Centrais

- Match Record & Match WR
- Game Record & Game WR
- Play/Draw WR
- Player Rating (Glicko-2 por formato com visão dupla: Oficial/Verificado e Comunitário/Geral)
- Commander Performance Score (Wilson Lower Bound)

---

## 17. Read Models (Rollups Diários)

Eventos assíncronos (`Spring Modulith`) alimentam tabelas agregadas diárias:
- `CommanderDailyStats`
- `PlayerDailyStats`
- `MatchupDailyStats`

Um Ledger de Reprocessamento gerencia edições retroativas sem varrer o banco todo.

---

## 18. Frontend PWA Offline-First

- Angular SPA configurado como PWA.
- Catálogo de comandantes em cache via IndexedDB.
- Registro de partidas offline em lojas com fila de sincronização (`PENDING_SYNC`).

---

## 19. Escopo V1 (MVP) vs Futuro

### O que entra na V1 (MVP):
- **Auth:** Google SSO exclusivo.
- **Formatos:** Duel Commander, DC 500, Brawl.
- **Tracking:** Torneios e Quick Matches (Paper, MTGO, Arena).
- **Comandantes:** Partners, Companions, Backgrounds.
- **Analytics Pessoais & Rivalidades:** Match/Game WR, Play/Draw, Head-to-Head direto, Color Explorer.
- **Performance:** Rollups diários assíncronos.
- **UX:** PWA Offline-first.

### O que fica para V2 / MVP+1:
- **Auth:** Discord SSO / Magic Link.
- **Social/Curadoria:** Interface completa de solicitação de Claim/Merge pelo usuário.
- **Rankings Públicos Avançados:** Filtros granulares por Cidade/Loja/Estado e Trending.
- **Mulligan Tracking:** Registro de mulligans por game.
- **Importadores:** MTGO (.dat/logs) e MTG Arena logs.
- **Decklists detalhadas:** Versionamento de decks (DeckVersion) e integrações externas.
