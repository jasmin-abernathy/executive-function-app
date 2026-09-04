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

(() => {
  const menuToggle = document.querySelector('[data-menu-toggle]');
  const nav = document.querySelector('[data-nav]');
  if (menuToggle && nav) {
    const close = () => {
      menuToggle.setAttribute('aria-expanded', 'false');
      nav.dataset.open = 'false';
      nav.classList.remove('is-open');
    };
    menuToggle.addEventListener('click', () => {
      const open = menuToggle.getAttribute('aria-expanded') !== 'true';
      menuToggle.setAttribute('aria-expanded', String(open));
      nav.dataset.open = String(open);
      nav.classList.toggle('is-open', open);
    });
    nav.addEventListener('click', (event) => { if (event.target.closest('a')) close(); });
    document.addEventListener('keydown', (event) => { if (event.key === 'Escape') close(); });
  }

  const value = new URLSearchParams(location.search).get('ref');
  const ref = value === 'potager' || value === 'prestadmin' ? value : 'direct';
  document.documentElement.dataset.siteRef = ref;
  document.querySelectorAll('[data-site-context]').forEach((label) => {
    label.textContent = ref === 'prestadmin' ? 'Prestadmin × Le Potager' : label.dataset.contextDefault;
  });
  if (ref !== 'direct') {
    document.querySelectorAll('a[href]').forEach((link) => {
      const raw = link.getAttribute('href');
      if (!raw || raw.startsWith('#') || /^(mailto:|tel:|javascript:)/i.test(raw)) return;
      const url = new URL(raw, location.href);
      if (url.origin !== location.origin || url.searchParams.has('ref')) return;
      url.searchParams.set('ref', ref);
      link.href = url.pathname + url.search + url.hash;
    });
  }
})();
