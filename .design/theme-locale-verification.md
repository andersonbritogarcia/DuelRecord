# Approved themes and localization — 2026-09-08

The prior dashboard exploration report is historical. Both proposals are now approved themes; light is default. The studio bar was removed and replaced by shared theme/language preferences. Future public pages, player search and commander search are recorded in DESIGN.md / PRODUCT.md, not fabricated as implemented routes.

## Changes and scope

- Preferences are local, independent and validated. No OS-based dark default; pt-BR fallback. Old proposal links are migrated once without losing route/query/fragment.
- Shared typed pt-BR/en/fr catalogs cover shell, dashboard and existing match-entry prototype. Intl handles numeric, percentage, calendar-date and match-count formatting. Names and canonical card titles remain source data.
- The form now uses the same tokens and translated, associated labels. Its previous Save button called only console.log. It now honestly reviews a draft and states that nothing was saved. Backend/offline persistence remains out of scope.
- Dark art treatment remains; content order no longer changes with theme.

## Verification

- Production build passed.
- Seven tests passed: corrupt preferences fallback; restore/save/invalid locale; blocked storage; locale data formatting; catalog keys/placeholders parity; filters and commander state across preference changes; legacy-link migration and preservation of draft/route with translated document title/lang.
- Browser: light and dark dashboard, Portuguese/English/French text, French numeric separators and dates, HTML lang and document title checked.
- Browser reload retained English and light after changing from French/dark. Legacy proposal query was removed.
- At 320px, French dashboard document width was 310px including reserved scrollbar space: no global horizontal overflow. Inspected default desktop and narrow layout.
- Filled a demo opponent, selected 2–1, switched English to French, and reviewed: value and score stayed, localized status explicitly confirmed no save.
- Native language select exposes all three autonyms and supports programmatic selection; its OS popup was not reliably captured by the browser tool. Keyboard Enter on the theme button worked. Physical-device, screen-reader, OS reduced-motion and actual 200% zoom tests remain unperformed.
- Static premium audit remains limited by its failure to recognize Angular `(click)` bindings. Its findings must be read alongside executed browser/tests rather than treated as a production certification.
- DESIGN.md documents the accepted identity and runtime token mapping. External designmd schema lint is not installed.

Remaining product work: API/auth, offline persistence, public routes and SEO language strategy, full opponent/commander lookup flows, additional feature translations and native-language editorial review.
