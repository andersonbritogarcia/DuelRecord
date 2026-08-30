# DuelRecord ⚔️

> **Your competitive MTG record.**

DuelRecord is a specialized match tracker, analytics engine, and rivalry platform tailored for **1v1 commander-based Magic: The Gathering formats** (Duel Commander, Duel Commander 500, and Brawl).

Unlike generic deckbuilders or bracket managers, DuelRecord focuses exclusively on delivering actionable competitive intelligence: win rates, Play/Draw performance, head-to-head rivalries, commander performance scores, and format-isolated ratings.

---

## ✨ Key Features

- **Format-Specific Isolation:** Dedicated tracking, ratings, and analytics for **Duel Commander**, **Duel Commander 500**, and **Brawl**.
- **Deep Performance Analytics:**
  - Distinct **Match Win Rate (Match WR)** and **Game Win Rate (Game WR)**.
  - Granular **Play vs. Draw** tracking per game.
  - **Wilson Lower Bound Score** for commander rankings to prevent small-sample bias.
  - **Glicko-2 Rating** system isolated per format.
- **Rivalry & Matchup Engine:** Head-to-Head breakdowns (Player vs. Player, Commander vs. Commander, and Pilot + Commander matchups).
- **Fast Match Entry (< 20s):** Mobile-optimized quick recording with score presets (2-0, 2-1, 1-2, 0-2) and detailed game-by-game modes.
- **Offline-First (PWA):** Register matches on local game store (LGS) tables even without cell service; data syncs automatically upon reconnection.
- **Pre-aggregated Daily Rollups:** Sub-millisecond analytics powered by asynchronous domain events and daily rollup tables.

---

## 🏗️ Architecture & Tech Stack

```text
┌──────────────────────────────────────────────────┐
│             Angular SPA (PWA Offline)            │
│       IndexedDB Local Cache & Sync Worker        │
└────────────────────────┬─────────────────────────┘
                         │ REST / JSON (OAuth2 JWT)
┌────────────────────────▼─────────────────────────┐
│       Spring Boot Modular Monolith (Java 25)     │
│   Spring Modulith (Domain Events & Async Rollups)│
└──────────┬─────────────────────────────┬─────────┘
           │                             │
┌──────────▼──────────┐       ┌──────────▼──────────┐
│    PostgreSQL 16    │       │ External Services   │
│ (Transactional +    │       │ - Google Identity   │
│  Daily Read Models) │       │ - Scryfall (Cards)  │
└─────────────────────┘       └─────────────────────┘
```

- **Backend:** Java 25, Spring Boot 3.x, Spring Modulith, Spring Security (OAuth2 Resource Server), Flyway.
- **Database:** PostgreSQL 16.
- **Frontend:** Angular 17+ (Standalone Components, PWA, TailwindCSS / Material, IndexedDB via `idb`).
- **External Integrations:** Google Identity Services, Scryfall API (with local caching).

---

## 🚀 Getting Started

### Prerequisites

- **Java 25** (JDK 25)
- **Node.js** (v20+ LTS) & **npm**
- **Docker** & **Docker Compose**

### 1. Clone the repository

```bash
git clone https://github.com/your-username/duel-record.git
cd duel-record
```

### 2. Start Local Database

```bash
docker-compose up -d postgres
```

### 3. Backend Setup

Configure your Google OAuth2 credentials in `application-local.yml` or export environment variables:

```bash
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
```

Run database migrations and start the Spring Boot server:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# or with Gradle:
# ./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. Frontend Setup

```bash
cd frontend
npm install
npm start
```

Open [http://localhost:4200](http://localhost:4200) in your browser.

---

## 📋 Project Roadmap

- [x] **V1 (MVP):**
  - Google SSO authentication.
  - Duel Commander, Duel Commander 500, and Brawl support.
  - Standalone (Quick Match) and Tournament match logging.
  - Commander, Partner, Companion, and Background mechanics.
  - Glicko-2 ratings and Wilson Performance Scores.
  - Daily rollups for instant `/explore` and profile analytics.
  - Angular PWA offline-first support.
- [ ] **V2:**
  - Discord SSO integration.
  - Player claim & merge self-service workflow.
  - MTG Arena log parser and MTGO match importer.
  - Mulligan count tracking per game.
  - Granular store/city leaderboard discovery.

---

## 📄 License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)** — see the [LICENSE](LICENSE) file for details.

---

## ⚖️ Legal & Disclaimer

**DuelRecord** is unofficial Fan Content permitted under the Wizards of the Coast Fan Content Policy. Portions of the materials used are property of Wizards of the Coast LLC. © Wizards of the Coast LLC.

Card data, mana symbols, and card imagery are provided by [Scryfall](https://scryfall.com/). DuelRecord is not affiliated with, endorsed, sponsored, or specifically approved by Wizards of the Coast or Scryfall.
