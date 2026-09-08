# DuelRecord — Implementation Plan (Task Breakdown)

Este documento detalha o backlog técnico para implementação do MVP (V1) do **DuelRecord** via agentes autônomos de IA sob supervisão sênior.

---

## 🗺️ Mapa de Dependência e Paralelização

```text
[FASE 1: Setup & Foundations]
  ├─── Task 1.1 (Docker / DB / Flyway) ──┐
  └─── Task 1.2 (Angular Base + PWA)    │
                                       ▼
                     [FASE 2: Backend Core Modules]
                       ├─── Task 2.1 (Security & Auth) 
                       ├─── Task 2.2 (Card & Scryfall Module)
                       ├─── Task 2.3 (Player & Identity Module)
                       └─── Task 2.4 (Format Rules Engine)
                                       │
                                       ▼
                     [FASE 3: Tracking & Domain Logic]
                       ├─── Task 3.1 (DeckIdentity & Mechanics)
                       ├─── Task 3.2 (Match, Game & Participant Tracking)
                       └─── Task 3.3 (Spring Modulith Event Dispatcher)
                                       │
                                       ▼
                     [FASE 4: Analytics Engine & Read Models]
                       ├─── Task 4.1 (Daily Rollups & Ledger Processing)
                       └─── Task 4.2 (Glicko-2 & Wilson Score Calculators)
                                       │
                                       ▼
                     [FASE 5: Frontend Feature Slices]
                       ├─── Task 5.1 (PWA Offline Store / IndexedDB Cache)
                       ├─── Task 5.2 (Auth & Profile UI)
                       ├─── Task 5.3 (Fast Match Entry Form / Mobile UI)
                       ├─── Task 5.4 (History & Rivalry View)
                       └─── Task 5.5 (Color & Commander Explorer)
                                       │
                                       ▼
                     [FASE 6: Integration, E2E & Readiness]
                       ├─── Task 6.1 (Sync Worker & Offline Pipeline)
                       └─── Task 6.2 (End-to-End Test Suite & Smoke Testing)
```

---

## FASE 1: Setup & Foundations (Infraestrutura & Base)

### [x] Task 1.1: Backend Project Setup, Database & Flyway
* **Stack:** Java 21/25, Spring Boot 4.x / Spring 7, Spring Modulith, PostgreSQL 16, Flyway.
* **Escopo:**
  1. Inicializar o projeto Spring Boot com Spring Modulith.
  2. Configurar `docker-compose.yml` para rodar PostgreSQL localmente.
  3. Criar scripts Flyway (`V1__initial_schema.sql`) com as tabelas base de usuários e suporte a migrations progressivas.
* **Critérios de Aceite:**
  * Build do Gradle/Maven passa sem erros.
  * O teste `ApplicationModules.of(AppApplication.class).verify()` roda com sucesso.
  * Migrations do Flyway rodam de forma idempotente.
* **Status:** Concluído com 100% dos testes verdes.

### [ ] Task 1.2: Frontend Project Setup & PWA Baseline
* **Stack:** Angular 17+ (Standalone Components), TailwindCSS / Angular Material, `@angular/pwa`.
* **Escopo:**
  1. Gerar Angular SPA com arquitetura modular por features (`features/*`, `core/*`, `shared/*`).
  2. Adicionar suporte a PWA (`ng add @angular/pwa`) configurando o `ngsw-config.json` .
  3. Configurar OpenAPI Generator para consumir os contratos da API do backend.
* **Critérios de Aceite:**
  * O app compila e instala como PWA no browser (Lighthouse PWA audit aprovado).
  * O roteamento inicial carrega páginas mockadas de Dashboard, History e Record.
* **Paralelizável com:** Task 1.1

---

## FASE 2: Backend Core Modules

### [x] Task 2.1: Identity & Google OAuth2 Resource Server
* **Módulo:** `identity`
* **Escopo:**
  1. Configurar Spring Security com OAuth2 Resource Server consumindo tokens JWT do Google.
  2. Criar serviço de resolução de usuário: ao receber um token válido, cria/recupera o registro na tabela `users` (baseado no `sub` do Google).
  3. Prover endpoint `GET /api/me` retornando dados do usuário autenticado.
  4. Suporte a internacionalização (i18n) em 3 idiomas (`en`, `pt_BR`, `fr`).
* **Critérios de Aceite:**
  * Requisição com Bearer Token mockado do Google cria/recupera o registro no banco.
  * Chamadas sem token retornam `401 Unauthorized` .
  * Testes de integração e unitários passam com 100% de sucesso.
* **Status:** Concluído.

### [x] Task 2.2: Card & Scryfall Local Cache Engine
* **Módulo:** `card`
* **Escopo:**
  1. Implementar cliente HTTP resiliente para a API do Scryfall com rate limiting (100ms) e interceptação padronizada de erros.
  2. Criar tabela `cards` (Flyway `V2__create_cards_table.sql`) com busca Full-Text (PostgreSQL `tsvector` + GIN) e ordenação canônica WUBRG.
  3. Criar endpoint `GET /api/cards/commanders?search={query}&color={color}&limit={limit}` e `GET /api/cards/{id}` com estratégia local-first cache miss.
  4. Suporte a persistência e busca em lote (`findByScryfallIdIn`, `saveAll`).
* **Critérios de Aceite:**
  * Busca por "Tasigur" ou criaturas/planeswalkers lendários persiste e retorna com `color_identity` canônica (`UBG`).
  * Consultas locais evitam ida desnecessária à API externa.
  * Spring Modulith verification passa sem violações.
* **Status:** Concluído.

### [x] Task 2.3: Player & Ghost Player Management
* **Módulo:** `player` + `geo`
* **Escopo:**
  1. CRUD de `Player` com suporte a jogadores fantasmas (`user_id = null`) e provisionamento automático para usuários autenticados via evento de domínio desacoplado.
  2. Tabelas `countries`, `cities`, `players`, `player_merge_logs` com índices GIN full-text search (Flyway `V3__create_geo_and_players_tables.sql`).
  3. Catálogo geográfico dinâmico com auto-provisionamento de cidades on-demand.
  4. Endpoints: `GET /api/players?q={name}`, `GET /api/players/{id}`, `POST /api/players/ghost`, `GET /api/players/me`, `PUT /api/players/me`, `GET /api/geo/countries`, `GET /api/geo/cities`, `POST /api/geo/cities`.
  5. Suporte i18n em 3 idiomas (`en`, `pt_BR`, `fr`).
* **Critérios de Aceite:**
  * Usuário autenticado consegue criar oponente fantasma apenas informando o nome (nomes repetidos permitidos).
  * Perfil do jogador provisionado automaticamente no primeiro login.
  * Atualização de perfil suporta seleção de cidade existente ou criação dinâmica.
  * Busca de jogadores e cidades paginada.
  * Spring Modulith verification passa 100% sem violações de arquitetura.
* **Status:** Concluído com 77 testes passando.

### [x] Task 2.4: Formats & Validation Engine
* **Módulo:** `format` / `match.validator`
* **Escopo:**
  1. Modelar enum e regras de formato: `DUEL_COMMANDER`, `DUEL_COMMANDER_500`, `BRAWL`.
  2. Implementar `FormatRulesValidator`:
     - DC / DC500: Padrão BO3, plataformas válidas: `PAPER`, `MTGO`.
     - Brawl: Padrão BO1, plataforma válida: `ARENA`.
* **Critérios de Aceite:**
  * Teste unitário rejeita submissão de match de Brawl com plataforma `PAPER`.
  * Teste unitário rejeita submissão de match de DC500 com BO1 por padrão, a menos que explicitado.
* **Status:** Concluído com 100% de testes unitários verdes.

---

## FASE 3: Tracking & Domain Logic

### [x] Task 3.1: DeckIdentity & Mechanics Engine
* **Módulo:** `match` (base / deck identity)
* **Escopo:**
  1. Criar entidades `DeckIdentity` e `DeckIdentityCard`.
  2. Suportar papéis: `COMMANDER`, `PARTNER`, `COMPANION`, `BACKGROUND`.
  3. Implementar algoritmo de união de identidade de cor (ex: W + UB = WUB; incolor = "C").
  4. Deduplicação determinística via SHA-256 `signature` canônica.
  5. Modelar enums base do módulo `match`: `DeckCardRole`, `GameFormat`, `Platform`, `MatchStructure`.
* **Critérios de Aceite:**
  * Cadastrar Yoshimaru (W) + Kraum (UR) gera um `DeckIdentity` com `color_identity = WUR` .
  * Cadastrar Tasigur (UBG) + Lutri (UR Companion) gera identidade de cor combinada e persiste as roles corretamente.
  * Cadastrar deck incolor resulta em `color_identity = C`.
  * Submissão idêntica reutiliza a `DeckIdentity` existente.
* **Status:** Concluído com 102 testes passando e verificação Modulith 100% verde.

### [x] Task 3.2: Match & Game Recording Engine
* **Módulo:** `match`
* **Escopo:**
  1. Criar entidades `TournamentParticipation`, `Match`, `MatchParticipant` e `Game`.
  2. Implementar cálculo automático de resultado da partida no backend (WIN/LOSS/DRAW) baseado nos `Game` registrados.
  3. Suporte a empates 0-0 (Intentional Draw sem games e empates por tempo com games disputados).
  4. Suportar flag `verification_status` (`UNVERIFIED` vs `VERIFIED`).
  5. Garantir que `display_name_snapshot` seja gravado no momento do registro.
* **Critérios de Aceite:**
  * Submeter games [G1: P1 win, G2: P1 win] grava a match como vitória de P1 (2-0).
  * Submeter match rápida (preset 2-1) gera os 3 games no banco automaticamente.
  * Submeter empate 0-0 registra resultado `DRAW` e suporta lista de games vazia (ID).
* **Status:** Concluído com 131 testes passando e verificação Modulith 100% verde.

### [x] Task 3.3: Spring Modulith Domain Event Publisher
* **Módulo:** `tracking` -> `events` (`com.duelrecord.app.match`)
* **Escopo:**
  1. Definir eventos imutáveis de domínio: `MatchCreatedEvent`, `MatchUpdatedEvent`, `MatchDeletedEvent`.
  2. Disparar eventos dentro da transação do serviço de registro de partidas.
* **Critérios de Aceite:**
  * Teste com `@ApplicationModuleTest` confirma que o `MatchCreatedEvent` é publicado e recebido pelo listener assíncrono.
* **Status:** Concluído com testes `@ApplicationModuleTest` e verificação arquitetural 100% verde (132 testes passando).
* **Depende de:** Task 3.2

---

## FASE 4: Analytics Engine & Read Models

### [ ] Task 4.1: Daily Rollups & Ledger Processing Engine
* **Módulo:** `statistics`
* **Escopo:**
  1. Criar tabelas agregadas de leitura: `commander_daily_stats`, `player_daily_stats`, `matchup_daily_stats`.
  2. Criar `DailyRollupListener` (Spring Modulith Event Listener) para atualizar as linhas do dia referente à data da partida (`played_at`).
  3. Criar tabela de Ledger para reprocessamento diário em caso de edição/deleção retroativa.
* **Critérios de Aceite:**
  * Inserção de uma nova partida atualiza as estatísticas diárias daquele dia sem necessidade de full scan na tabela `matches`.
  * Endpoint `GET /api/players/{id}/statistics?from={date}&to={date}` soma apenas as linhas do rollup do período.
* **Depende de:** Task 3.3 | **Paralelizável com:** Task 4.2

### [ ] Task 4.2: Rating Calculators (Glicko-2 & Wilson Score)
* **Módulo:** `rating`
* **Escopo:**
  1. Implementar motor de cálculo Glicko-2 (`rating`, `rating_deviation`, `volatility`) isolado por formato.
  2. Criar serviço de cálculo do Wilson Lower Bound para ordenação de performance de comandantes.
  3. Garantir que apenas partidas `VERIFIED` modifiquem os ratings oficiais da tabela `player_ratings`.
* **Critérios de Aceite:**
  * Comandante com 1 vitória e 0 derrotas (100% WR) fica ranqueado abaixo de um com 50 vitórias e 5 derrotas (90.9% WR) via Wilson Score.
  * Partidas de DC 500 não afetam o rating de Duel Commander.
* **Depende de:** Task 3.3 | **Paralelizável com:** Task 4.1

---

## FASE 5: Frontend Feature Slices

### [ ] Task 5.1: IndexedDB Client & Offline Sync Queue
* **Feature:** `core/offline`
* **Escopo:**
  1. Configurar `idb` (IndexedDB wrapper) no Angular para armazenar cache local de cartas de comandantes.
  2. Criar fila de sincronização `match_sync_queue` no IndexedDB para partidas criadas offline (`PENDING_SYNC`).
  3. Adicionar listener de conectividade (`navigator.onLine`) para disparar a sincronização quando a internet voltar.
* **Critérios de Aceite:**
  * Em modo offline (via DevTools), a criação de partida salva no IndexedDB e retorna feedback de "Salvo localmente".
  * Ao religar a rede, a partida é enviada à API e o status muda para sincronizado.
* **Depende de:** Task 1.2 | **Paralelizável com:** Task 5.2, 5.3

### [ ] Task 5.2: Auth Integration & Player Profile UI
* **Feature:** `features/auth` + `features/players`
* **Escopo:**
  1. Implementar botão e fluxo de login com Google Identity Services SDK no Angular.
  2. Armazenar JWT e injetar via `HttpInterceptor`.
  3. Criar página de perfil público do jogador exibindo rating, volume de partidas e comandantes favoritos.
* **Critérios de Aceite:**
  * Fluxo de login autentica com sucesso e redireciona para o Dashboard.
  * Perfil público exibe os dados consumindo `GET /api/players/{id}`.
* **Depende de:** Task 2.1, Task 2.3 | **Paralelizável com:** Task 5.3, 5.4, 5.5

### [ ] Task 5.3: Rapid Match Recording Form (Mobile-First UX)
* **Feature:** `features/matches`
* **Escopo:**
  1. Criar formulário mobile-first para registro em menos de 20 segundos.
  2. Suportar seleção rápida de modo: **Presets Rápidos** (2-0, 2-1, 1-2, 0-2) vs **Modo Detalhado** (game por game com Play/Draw).
  3. Componente de autocomplete de comandante consumindo o cache do IndexedDB / API.
* **Critérios de Aceite:**
  * O fluxo completo de preenchimento e salvamento é realizável em menos de 5 cliques/taps no celular.
* **Depende de:** Task 3.2, Task 5.1 | **Paralelizável com:** Task 5.4, 5.5

### [ ] Task 5.4: History & Head-to-Head Rivalry Views
* **Feature:** `features/history` + `features/rivalries`
* **Escopo:**
  1. Criar tela de histórico de partidas com paginação e filtros (Formato, Plataforma, Comandante, Oponente, Período).
  2. Exibir badge com o nome original do snapshot caso o oponente tenha passado por merge.
  3. Criar tela de rivalidade direta (Head-to-Head: Jogador A vs Jogador B).
* **Critérios de Aceite:**
  * Tela de histórico renderiza corretamente partidas rápidas e partidas de torneio.
  * Tela de rivalidade calcula e exibe streak atual, recorde lifetime e histórico de jogos.
* **Depende de:** Task 4.1 | **Paralelizável com:** Task 5.3, 5.5

### [ ] Task 5.5: Color Identity & Commander Explorer UI
* **Feature:** `features/explore`
* **Escopo:**
  1. Criar tela `/explore` com seletores de cores canônicas (W, U, B, R, G) nos modos `EXACT` e `CONTAINS`.
  2. Exibir tabela de comandantes ordenados por Wilson Performance Score, Win Rate e total de partidas.
  3. Permitir filtrar por piloto específico.
* **Critérios de Aceite:**
  * Selecionar W + U + B no modo `EXACT` lista apenas comandantes Esper.
  * A busca responde instantaneamente consumindo os endpoints de agregados diários.
* **Depende de:** Task 4.1, Task 4.2 | **Paralelizável com:** Task 5.3, 5.4

---

## FASE 6: Integration, E2E & Readiness

### [ ] Task 6.1: Full Offline-to-Online Pipeline Validation
* **Escopo:**
  1. Testar ciclo de vida completo: App offline -> Registro de 3 rodadas de torneio -> Reconexão -> Processamento assíncrono -> Atualização dos Daily Rollups.
  2. Ajustar tratamento de conflitos e idempotência nas requisições da API.
* **Critérios de Aceite:**
  * Nenhuma partida enviada da fila offline é duplicada no backend.
  * Todos os Rollups Diários refletem o estado correto pós-sincronização.
* **Depende de:** Task 5.1, Task 5.3, Task 4.1

### [ ] Task 6.2: End-to-End Test Suite (Cypress/Playwright)
* **Escopo:**
  1. Criar suíte de testes E2E cobrindo:\
     - Login com Google (mockado).\
     - Registro de Quick Match (DC 500).\
     - Validação de atualização de estatísticas na tela de Explorer.\
     - Navegação e visualização de Rivalidade.
* **Critérios de Aceite:**
  * Todos os fluxos críticos passam no pipeline de CI sem falhas intermitentes (flakiness).
* **Depende de:** Todas as tarefas anteriores
