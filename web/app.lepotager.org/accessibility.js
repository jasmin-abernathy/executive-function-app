(() => {
  const toggle = document.querySelector('[data-app-accessibility-toggle]');
  if (!toggle) return;
  const key = 'app-lepotager-accessible-mode';
  const apply = enabled => {
    document.documentElement.dataset.appAccessible = String(enabled);
    toggle.setAttribute('aria-pressed', String(enabled));
    toggle.textContent = enabled ? 'Version standard' : 'Version accessible';
  };
  let saved = false;
  try { saved = localStorage.getItem(key) === 'true'; } catch (_) {}
  apply(saved);
  toggle.addEventListener('click', () => {
    const enabled = document.documentElement.dataset.appAccessible !== 'true';
    apply(enabled);
    try { localStorage.setItem(key, String(enabled)); } catch (_) {}
  });
})();
