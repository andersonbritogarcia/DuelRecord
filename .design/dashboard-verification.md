# Dashboard proposals — 2026-09-08

Scope: two working visual proposals on the existing Angular dashboard. Demo data only; backend documents, match draft store, auth, sync and persistence are unchanged.

## Executed checks

- `npm run build`: production compilation passed.
- `npm test -- --watch=false`: 3 tests passed, including URL-selected theme and retaining the current route during a theme change. Replaced the obsolete Angular starter heading assertion.
- Browser: Club and Arena rendered; local commander artwork loaded without broken images. Result filter reduced four sample matches to one loss; selecting Ajani changed the summary to 60%. Returning to All restored the sample.
- Section navigation to rivalry and commanders scrolled to the intended section. Query parameter stayed selected through navigation.
- Browser viewports: default desktop, 390px, 320px. At 320px the document width was 310px (scrollbar reserved), with no horizontal overflow. Mobile composition was inspected and intro copy compacted after the first screenshot.
- Keyboard: Enter activates the direction switch. Native buttons expose their pressed state. Visible focus styling is defined globally.
- Browser console inspection returned no errors during dashboard interaction.
- Static premium audit is retained in `frontend/premium-audit.json`. Its actionless-button rule does not recognize Angular `(click)` bindings (the auditor only recognizes onclick/Vue click syntax). Four findings are false positives verified through browser interaction and Angular compilation. Do not add fake onclick handlers or disable working controls to silence this tool.

## Limits

- This is a visual exploration, not an integrated account or analytics implementation. No production loading, error or offline states are simulated for local fixtures.
- The existing match-entry prototype is not redesigned or completed by this change. It still lacks real persistence. The dashboard link preserves that existing route.
- Not a full accessibility certification: no screen-reader, physical-device or cross-browser test was performed. Reduced-motion is implemented in CSS; actual OS preference testing and 200% browser zoom remain unverified.
- Existing DESIGN.md was preserved and extended with pending proposals. The external designmd lint package is not installed; no claim of passing its schema validation is made.
- System typography is deliberate for this exploration; final cross-platform font selection remains open.
