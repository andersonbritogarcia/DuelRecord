// Apply validated appearance preferences before Angular paints the first screen.
// Keep this storage key and accepted values aligned with core/ui-preferences.ts.
(() => {
  try {
    const saved = JSON.parse(localStorage.getItem('duelrecord.preferences.v1') || '{}');
    document.documentElement.dataset.theme = saved?.theme === 'dark' ? 'dark' : 'light';
    if (saved?.locale === 'en' || saved?.locale === 'fr')
      document.documentElement.lang = saved.locale;
  } catch {
    /* Default is light / pt-BR when storage cannot be read. */
  }
})();
