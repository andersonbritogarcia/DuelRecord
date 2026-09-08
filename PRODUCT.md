# Product

<!-- impeccable:product-schema 1 -->

## Platform
web

## Stack
Angular 17+ (Standalone Components), TailwindCSS, Angular Material. PWA Offline-First.

## Users
Competitive Magic The Gathering players who focus on 1x1 Commander formats (Duel Commander, DC 500, Brawl). They want to track their performance, identify their best decks, and understand their local rivalries and matchups.

## Product Purpose
DuelRecord is a competitive history, analytics, and rivalry platform. The core value is transforming recorded matches into actionable insights (win rate, best commander, worst matchups, play/draw advantage). Success means players have a flawless and rapid way to log matches at the store counter and deep analytics at home.

## Positioning
It is strictly an analytics and tracking platform, NOT a tournament manager or deck builder.

## Operating Context
Players will use the app mostly on mobile devices while at local game stores (FNM, physical tournaments) to log matches between rounds, often offline. They will also use the dashboard on desktop to analyze their Glicko-2 ratings and deep stats.

## Capabilities and Constraints
- Quick match logging (<20s constraint).
- Offline-first mutation support (IndexedDB queuing).
- Dual rating system (Verified vs Global Glicko-2).
- Purely 1v1 Commander (no cEDH/multiplayer mechanics).
- Temporal 7-day edit lock.

## Brand Commitments
- Light theme is the default; an optional dark blue theme is approved.
- Minimalist, e-sports aesthetic.
- Petroleum/mint on light surfaces; deep blue, ice-blue and warm peach accents in dark mode. Commander artwork and rivalry scores bring personality without neon.
- Name: DuelRecord.
- UI languages: Portuguese (pt-BR, default), English (en), French (fr). Language and theme are independent user preferences.
- Planned surfaces include the public platform view, opponent/player search and profiles, and commander exploration, using the same identity and localization foundation.
