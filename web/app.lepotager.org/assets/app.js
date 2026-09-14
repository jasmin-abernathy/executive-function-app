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

const mobileMenuQuery = window.matchMedia('(max-width: 720px)');

const setMenuState = (open) => {
  if (!toggle || !nav) return;

  const mobile = mobileMenuQuery.matches;
  const effectiveOpen = mobile && Boolean(open);
  toggle.setAttribute('aria-expanded', String(effectiveOpen));
  toggle.setAttribute('aria-label', effectiveOpen
    ? (currentLanguage === 'en' ? 'Close menu' : 'Fermer le menu')
    : (currentLanguage === 'en' ? 'Open menu' : 'Ouvrir le menu'));

  const srLabel = toggle.querySelector('.sr-only');
  if (srLabel) {
    srLabel.textContent = effectiveOpen
      ? (currentLanguage === 'en' ? 'Close menu' : 'Fermer le menu')
      : (currentLanguage === 'en' ? 'Open menu' : 'Ouvrir le menu');
  }

  nav.dataset.open = String(effectiveOpen);
  nav.classList.toggle('is-open', effectiveOpen);
  nav.hidden = mobile ? !effectiveOpen : false;
  document.body.classList.toggle('mobile-menu-open', effectiveOpen);
};

const closeMenu = () => setMenuState(false);

if (toggle && nav) {
  setMenuState(false);

  toggle.addEventListener('click', (event) => {
    event.preventDefault();
    event.stopPropagation();
    setMenuState(toggle.getAttribute('aria-expanded') !== 'true');
  });

  nav.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', closeMenu);
  });

  document.addEventListener('click', (event) => {
    if (toggle.getAttribute('aria-expanded') !== 'true') return;
    if (nav.contains(event.target) || toggle.contains(event.target)) return;
    closeMenu();
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && toggle.getAttribute('aria-expanded') === 'true') {
      closeMenu();
      toggle.focus();
    }
  });

  const syncMenuToViewport = () => setMenuState(false);
  if (typeof mobileMenuQuery.addEventListener === 'function') {
    mobileMenuQuery.addEventListener('change', syncMenuToViewport);
  } else if (typeof mobileMenuQuery.addListener === 'function') {
    mobileMenuQuery.addListener(syncMenuToViewport);
  }
}

document.querySelectorAll('[data-year]').forEach((el) => {
  el.textContent = new Date().getFullYear();
});

// Research panel form feedback
(() => {
  const params = new URLSearchParams(window.location.search);
  const state = params.get('research');

  if (!state) return;

  const status = document.querySelector('[data-research-status]');
  if (!status) return;

  const lang = document.documentElement.lang === 'en' ? 'en' : 'fr';
  const messages = {
    fr: {
      'check-email': 'Merci ! Regarde maintenant ta boîte mail pour confirmer ton inscription.',
      'already': 'Cette adresse est déjà inscrite au panel de recherche.',
      'invalid': 'Vérifie les champs obligatoires puis réessaie.',
      'invalid-email': 'L’adresse e-mail indiquée ne semble pas valide.',
      'error': 'Une erreur technique est survenue. Tu peux réessayer dans quelques instants.'
    },
    en: {
      'check-email': 'Thanks! Check your inbox now to confirm your signup.',
      'already': 'This email address is already registered for the research panel.',
      'invalid': 'Please check the required fields and try again.',
      'invalid-email': 'The email address does not appear to be valid.',
      'error': 'A technical error occurred. Please try again shortly.'
    }
  };

  status.textContent = messages[lang][state] || messages[lang].error;
  status.style.display = 'block';
  status.classList.add('is-visible');
  status.classList.add(state === 'check-email' || state === 'already' ? 'is-success' : 'is-error');

  const cleanUrl = new URL(window.location.href);
  cleanUrl.searchParams.delete('research');
  history.replaceState(null, '', cleanUrl.pathname + cleanUrl.search + cleanUrl.hash);
})();

// Keep the voluntary support page visible from the French site without duplicating
// payment details or secrets in the navigation markup.
(() => {
  if (currentLanguage !== 'fr') return;

  const supportHref = '/soutenir/';
  if (nav && !nav.querySelector(`a[href="${supportHref}"]`)) {
    const supportLink = document.createElement('a');
    supportLink.href = supportHref;
    supportLink.textContent = 'Soutenir';
    supportLink.addEventListener('click', closeMenu);
    nav.appendChild(supportLink);
  }

  const footerLinks = document.querySelector('.footer-links');
  if (footerLinks && !footerLinks.querySelector(`a[href="${supportHref}"]`)) {
    const supportLink = document.createElement('a');
    supportLink.href = supportHref;
    supportLink.textContent = 'Soutenir';
    footerLinks.prepend(supportLink);
  }
})();
