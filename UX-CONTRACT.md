# DuelRecord — Shared interface contract

## Current scope

Routes: `/login` (Google entry), `/` and `/profile` (authenticated account), `/preview` (demo dashboard), and `/fast-match` (public draft prototype without API writes). Public discovery and player/commander search remain planned. Both themes share navigation and data order. Real account screens never substitute sample statistics for API data.

## Canonical UI Map

Authentication is owned by `frontend/src/app/core/auth.ts`; deployment configuration by `frontend/scripts/runtime-config.mjs`. Tokens and account data stay in memory. Logout and expiry clear drafts as well. See `frontend/AUTH-INTEGRATION.md` for setup and limitations.

| Capability | Canonical owner | Source of truth | Allowed variants | Verification |
| --- | --- | --- | --- | --- |
| Theme | `frontend/src/app/core/ui-preferences.ts` | DESIGN.md | light default / dark | preference + browser tests |
| Locale | `frontend/src/app/core/i18n/i18n.ts` | DESIGN.md | pt-BR / en / fr | catalog parity, format, browser tests |
| Select/Listbox | Native select in `frontend/src/app/app.html` | DESIGN.md | platform-owned language picker | keyboard + popup + mobile |
| Scrollbar | `frontend/src/styles.css` | DESIGN.md | document scrolling | narrow viewport |
| Form | `match-entry.component.ts` and existing draft store | Existing prototype | draft review, no persistence | field labels + state preservation |

Preferences use `duelrecord.preferences.v1` in localStorage, containing only theme and locale. Invalid or inaccessible storage falls back to light/pt-BR. The small public preference-init script applies saved appearance before the first Angular render; it does not own token values. No account data or authentication secrets are stored here.

Changing language or theme is immediate and does not navigate, clear a filter, change a commander or reset a draft. Language updates page text, the document title and HTML `lang`. Explicit choices persist across reloads. A legacy proposal query is consumed once, with the rest of the URL retained.

Dashboard filters use stable IDs and synchronous sample data. Native buttons expose pressed states. Commander names stay as canonical source data. Sample format and date range are labels, not unavailable dropdown controls.

The existing match draft stays in the singleton store during route changes; a full page reload does not persist it. The review action is enabled when an opponent and score are supplied and explicitly reports that nothing has been saved. Backend submission, validation and offline persistence require their own implementation, not a success toast over this prototype.

New routes must supply all three language catalogs, shared preference controls and locale-aware data formatting. Before public-page work, define localized routing and SEO separately; this client-side catalog is not a public indexing strategy.
