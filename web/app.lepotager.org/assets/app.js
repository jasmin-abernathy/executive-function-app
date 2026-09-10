const header = document.querySelector('[data-header]');
const toggle = document.querySelector('[data-menu-toggle]');
const nav = document.querySelector('[data-nav]');

const LANGUAGE_KEY = 'lepotager-apps-language';
const currentLanguage = document.documentElement.lang === 'en' ? 'en' : 'fr';

const preferredBrowserLanguage = () => {
  const languages = Array.isArray(navigator.languages) && navigator.languages.length
    ? navigator.languages
    : [navigator.language || 'fr'];
  return languages.some((language) => String(language).toLowerCase().startsWith('en')) ? 'en' : 'fr';
};

const languageTarget = (language) => `${language === 'en' ? '/en/' : '/'}${window.location.hash || ''}`;

// Automatic detection only until the visitor explicitly chooses FR or EN.
try {
  const savedLanguage = localStorage.getItem(LANGUAGE_KEY);
  const targetLanguage = savedLanguage === 'fr' || savedLanguage === 'en'
    ? savedLanguage
    : preferredBrowserLanguage();

  if (targetLanguage !== currentLanguage) {
    window.location.replace(languageTarget(targetLanguage));
  }
} catch (_) {
  // localStorage may be unavailable; the visible FR / EN switch still works.
}

document.querySelectorAll('[data-language]').forEach((link) => {
  link.addEventListener('click', (event) => {
    const language = link.dataset.language === 'en' ? 'en' : 'fr';
    try { localStorage.setItem(LANGUAGE_KEY, language); } catch (_) {}
    event.preventDefault();
    window.location.href = languageTarget(language);
  });
});

const setHeaderState = () => {
  header?.classList.toggle('is-scrolled', window.scrollY > 8);
};

setHeaderState();
window.addEventListener('scroll', setHeaderState, { passive: true });

toggle?.addEventListener('click', () => {
  const isOpen = toggle.getAttribute('aria-expanded') === 'true';
  toggle.setAttribute('aria-expanded', String(!isOpen));
  nav?.classList.toggle('is-open', !isOpen);
});

nav?.querySelectorAll('a').forEach((link) => {
  link.addEventListener('click', () => {
    toggle?.setAttribute('aria-expanded', 'false');
    nav.classList.remove('is-open');
  });
});

document.querySelectorAll('[data-year]').forEach((el) => {
  el.textContent = new Date().getFullYear();
});
