# DuelRecord — Design system

## Overview

**Approved 2026-09-08:** both dashboard proposals are the product identity. The light theme is the default; the second proposal is the optional dark theme. This decision supersedes the earlier exploratory design notes.

DuelRecord is a personal competitive history, analytics and rivalry platform for 1v1 Commander formats. Its character is elegant, modern and enjoyable: the feeling of a competitive club, with commanders and rivalries giving the data personality. The application must remain quick to use between rounds and useful for analysis at home.

Signature: the opposing-chevron wordmark, expressive match scores, carefully cropped commander artwork and personal head-to-head records. Brand expression comes from Magic and the player's journey, with restrained chrome around it.

This identity applies to future public pages, player/opponent search and profiles, commander exploration, match history and registration. These future screens are not implemented by this decision.

## Colors

The login and account screens share these tokens. Login uses commander art and a single Google entry point; missing configuration and failed connection have translated messages. `/preview` preserves the approved sample dashboard. Authenticated account screens display API data only, with explicit unfilled profile fields and no fabricated statistics.

Runtime authority: `frontend/src/styles.css`. `:root` owns the light semantic tokens; `:root[data-theme='dark']` overrides them. Shared chrome, dashboard and match-entry consume the same tokens. The `.theme-dark` class only supplies the approved dark art treatment.

| Role | Token | Light | Dark |
| --- | --- | --- | --- |
| Canvas | `--canvas` | #f3f6f6 | #131b2b |
| Surface | `--surface` | #ffffff | #1d283b |
| Main text | `--ink` | #16383e | #eef2f9 |
| Secondary text | `--muted` | #62777c | #a7b4c8 |
| Borders | `--line` | #dce5e6 | #344055 |
| Brand | `--brand` | #126568 | #bdd6f4 |
| Selected surface | `--soft` | #e7f1ef | #2b3b52 |
| Warm accent / focus | `--accent`, `--focus` | #b45129 | #ffc39d |
| Hero surface | `--hero` | #e3eeeb | #1a273b |
| Positive result | `--positive` | #17674f | #ade2cf |
| Negative result | `--negative` | #98534e | #f1b8af |

Use result labels as well as color. Keep the restrained green/rose result surfaces. Artwork can be expressive; navigation and data stay calm. Dark mode retains the blue atmosphere and warm primary action, without neon. Theme changes must not reorder information or alter functionality.

## Typography

- Display: Bahnschrift, with Segoe UI/sans-serif fallback, used for short headlines, commanders and scores.
- Body: Segoe UI, Arial, sans-serif. No external font request is needed.
- Numeric values use tabular figures; dates, percentages and numbers use the selected locale through `Intl`.
- Small uppercase eyebrows are secondary. Names, actions and data remain readable and wrap naturally.
- System fonts vary by OS. Any later bundled-font decision must preserve this hierarchy and support Portuguese, English and French diacritics.

## Layout

- Shared top navigation; 1324px maximum dashboard width.
- Desktop: paired personal summary and commander artwork, four-column stats, history alongside commanders, rivalry below. Both themes share this content order.
- Below 760px: stacked content/art, two-column stats, single-column sections. Introductory copy becomes compact. Document scrolling remains the owner.
- Language and theme controls remain available on narrow screens. Accommodate French text expansion without clipped actions or global horizontal scrolling.
- Public pages may use a different content composition, but must use these tokens, mark, type hierarchy, preferences and localization boundaries.

## Elevation & Depth

Flat surfaces and fine borders; minimal shadows. Use dark overlays only to make text over commander artwork legible. Avoid decorative glass, excessive gradients and floating UI.

## Shapes

6–8px control corners, 12px panels and up to 16px featured surfaces. Round avatars and small color-identity markers. The opposing-chevron mark remains the product-owned symbol.

## Components

- Shared theme/language: `core/ui-preferences.ts`, `core/i18n/i18n.ts`, `app.html`.
- Theme defaults to light regardless of OS preference. User selection persists locally, with safe in-memory fallback when storage is unavailable.
- Locale defaults to Portuguese (`pt-BR`), with English (`en`) and French (`fr`). A native select uses the languages' own names, never flags; platform popup geometry is intentional.
- Typed catalogs live in `core/i18n/{pt-BR,en,fr}.ts`. Every new key must exist in all three catalogs with matching interpolation variables. Never use translated labels as business-state IDs.
- Shell, dashboard and existing match prototype use translated copy, labels, accessibility text and document titles. Match scores, player names, official format names and canonical card names are not translated as UI strings. Localized card names can later come from API card data.
- Civil-date fixtures use UTC for display to avoid shifting calendar dates. Future timestamp-bearing API records need an explicit event/user timezone contract.
- Buttons expose selected/focus/disabled states. Motion is limited to feedback and navigation; honor reduced-motion preferences.
- The demo-data notice stays until real integration. Never imply demo statistics or a reviewed draft were saved to an account.

## Do's and Don'ts

- Build future public, opponent and commander screens on the shared theme and locale services.
- Lead with commander art, match outcomes and rivalry context where they help the user's task.
- Preserve filters, drafts and navigation when changing preferences.
- Do not reintroduce the A/B studio comparison strip, independent screen palettes, generic dashboard metrics, or untranslated UI copy.
- Keep theme selection out of route state. Legacy `design=club|arena` links are migrated once and cleaned without losing the current route or other parameters.

## Assets and implementation scope

Local commander crops live in `frontend/public/art`; `credits.json` preserves sources and artists. The frontend makes no runtime Scryfall requests.

The dashboard remains a bounded sample. Registration remains a draft prototype with an explicitly labeled review action, not persistence or offline sync. New public/search screens, authentication, API integration, catalogs for additional features and localized public SEO are future work.
