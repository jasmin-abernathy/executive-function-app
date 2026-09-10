(() => {
  'use strict';

  const Q = window.SURVEY_QUESTIONS;
  const main = document.getElementById('survey-main');
  const body = document.getElementById('survey-body');
  const top = document.getElementById('survey-top');
  const stageList = document.getElementById('stage-list');
  const saveStatus = document.getElementById('save-status');
  const pauseButton = document.getElementById('pause-button');
  const pauseModal = document.getElementById('pause-modal');
  const pauseClose = document.getElementById('pause-close');
  const pauseDeviceOnly = document.getElementById('pause-device-only');
  const pauseSend = document.getElementById('pause-send');
  const pauseEmail = document.getElementById('pause-email');
  const reminderChoice = document.getElementById('reminder-choice');
  const customReminderWrap = document.getElementById('custom-reminder-wrap');
  const customReminder = document.getElementById('custom-reminder');
  const pauseFeedback = document.getElementById('pause-feedback');
  const deleteProgress = document.getElementById('delete-progress');

  const STORAGE_KEY = 'potager_adhd_survey_v1';
  const API_URL = 'api.php';

  let state = {
    token: null,
    answers: {},
    current: 'welcome',
    completed: false,
    coreSubmitted: false,
    optionalGroup: null,
    contactSubmitted: false,
    orderSeed: null,
    personalSummary: null
  };

  let lastFocusedElement = null;
  let pauseResumeMode = 'current';

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function loadLocalState() {
    try {
      const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null');
      if (parsed && typeof parsed === 'object') {
        state = { ...state, ...parsed, answers: parsed.answers || {} };
      }
    } catch (_) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  function saveLocalState() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }

  function clearLocalState() {
    localStorage.removeItem(STORAGE_KEY);
  }

  async function api(action, payload = {}) {
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), 8000);

    try {
      const response = await fetch(`${API_URL}?action=${encodeURIComponent(action)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload),
        signal: controller.signal
      });

      const data = await response.json().catch(() => ({ ok: false, error: 'Invalid server response.' }));
      if (!response.ok || !data.ok) {
        throw new Error(data.error || 'The server could not complete this request.');
      }
      return data;
    } catch (error) {
      if (error && error.name === 'AbortError') {
        throw new Error('The server is taking too long to respond. Your browser has not been frozen.');
      }
      throw error;
    } finally {
      window.clearTimeout(timeout);
    }
  }

  function setBusy(isBusy) {
    body.setAttribute('aria-busy', isBusy ? 'true' : 'false');
  }

  function setSaveStatus(message, kind = 'normal') {
    saveStatus.textContent = message;
    saveStatus.dataset.kind = kind;
  }

  function primaryPathKey() {
    return state.answers.primary_path || null;
  }

  function secondaryPathKey() {
    const value = state.answers.secondary_path;
    return value && value !== 'none' ? value : null;
  }

  function hasSecondaryDecision() {
    return Object.prototype.hasOwnProperty.call(state.answers, 'secondary_path');
  }

  function isSecondaryQuestion(question) {
    return Boolean(question && (question.id === 'secondary_path' || question.id.startsWith('secondary_')));
  }

  function isOptionalQuestion(question) {
    return Boolean(question && (question.optional || isSecondaryQuestion(question)));
  }

  function routeStructureStable() {
    const primary = primaryPathKey();
    if (!primary || !Q.paths[primary]) return false;
    const causeQuestion = Q.paths[primary].causeQuestion;
    const causeAnswer = state.answers[causeQuestion.id];
    return !answerIsEmpty(causeQuestion, causeAnswer);
  }

  function buildFullFlow() {
    const flow = [...Q.initial];
    const primary = primaryPathKey();

    if (primary && Q.paths[primary]) {
      const path = Q.paths[primary];
      flow.push(path.causeQuestion);

      const rawCause = state.answers[path.causeQuestion.id];
      const causes = Array.isArray(rawCause) ? rawCause : (rawCause ? [rawCause] : []);
      causes.slice(0, path.causeQuestion.max || 1).forEach((cause) => {
        if (path.conditional[cause]) flow.push(path.conditional[cause]);
      });

      flow.push(...path.common);
      flow.push(Q.secondaryQuestion);

      const secondary = secondaryPathKey();
      if (secondary && secondary !== primary && Q.paths[secondary]) {
        flow.push(...Q.paths[secondary].secondary);
      }

      flow.push(...Q.shared);
    }

    return flow;
  }

  function buildCoreFlow() {
    return buildFullFlow().filter((question) => !isOptionalQuestion(question));
  }

  function buildFlow() {
    return state.coreSubmitted ? buildFullFlow() : buildCoreFlow();
  }

  function optionalGroups() {
    const full = buildFullFlow();
    const byId = new Map(full.map((question) => [question.id, question]));
    const groups = [];

    const add = (key, title, description, ids) => {
      const questions = ids.map((id) => byId.get(id)).filter(Boolean);
      if (questions.length) groups.push({ key, title, description, questions });
    };

    add(
      'strategy_tools',
      'What you already try',
      'Your current strategy and the tools or methods you already use.',
      ['current_strategy', 'existing_tools']
    );

    add(
      'context',
      'A little more context',
      'Optional background about your relationship with ADHD and your adult age range.',
      ['relationship', 'age_range']
    );

    const primary = primaryPathKey();
    if (primary && Q.paths[primary]) {
      const primaryOptional = full.filter((question) => {
        if (!question.optional) return false;
        if (!question.id.startsWith(`${primary}_`)) return false;
        return question.id !== Q.paths[primary].causeQuestion.id;
      });
      if (primaryOptional.length) {
        add(
          'main_extra',
          'Anything else about your main difficulty',
          'Optional open-text detail related to the main support area you selected.',
          primaryOptional.map((question) => question.id)
        );
      }
    }

    const secondary = secondaryPathKey();
    const secondaryIds = ['secondary_path'];
    if (secondary && Q.paths[secondary]) {
      secondaryIds.push(...Q.paths[secondary].secondary.map((question) => question.id));
    }
    add(
      'second_area',
      'Explore a second support area',
      secondary
        ? 'Continue the optional second area you selected.'
        : 'Choose whether you want to explore one additional support area.',
      secondaryIds
    );

    add(
      'drawbacks',
      'Possible drawbacks and boundaries',
      'Tell us what could make suggestions, rewards or a companion uncomfortable.',
      ['suggestions_discomfort', 'reward_discomfort', 'companion_discomfort']
    );

    add(
      'name_extra',
      'Free association with the name',
      'Add a few optional words about what your first name choice made you imagine.',
      ['name_association']
    );

    add(
      'survey_feedback',
      'Feedback about this questionnaire',
      'Tell us what felt difficult or leave one final suggestion.',
      ['survey_friction', 'survey_comment']
    );

    return groups;
  }

  function optionalQuestionAnswered(question) {
    return Object.prototype.hasOwnProperty.call(state.answers, question.id)
      && !answerIsEmpty(question, state.answers[question.id]);
  }

  function optionalGroupByKey(key) {
    return optionalGroups().find((group) => group.key === key) || null;
  }

  function optionalGroupProgress(group) {
    const answered = group.questions.filter(optionalQuestionAnswered).length;
    return { answered, total: group.questions.length };
  }

  function firstOptionalQuestion(group, preferUnanswered = true) {
    if (!group || !group.questions.length) return null;
    if (preferUnanswered) {
      const unanswered = group.questions.find((question) => !optionalQuestionAnswered(question));
      if (unanswered) return unanswered;
    }
    return group.questions[0];
  }

  function nextOptionalQuestionInGroup(groupKey, currentQuestionId) {
    const group = optionalGroupByKey(groupKey);
    if (!group) return null;

    const currentIndex = group.questions.findIndex((question) => question.id === currentQuestionId);
    for (let index = currentIndex + 1; index < group.questions.length; index += 1) {
      if (!optionalQuestionAnswered(group.questions[index])) return group.questions[index];
    }

    return null;
  }


  function questionById(id) {
    return buildFullFlow().find((question) => question.id === id) || null;
  }

  function currentQuestionIndex() {
    return buildFlow().findIndex((question) => question.id === state.current);
  }

  function stageForQuestion(questionId) {
    if (questionId === 'welcome' || questionId === 'readiness' || questionId === 'come_back_later' || questionId === 'consent' || questionId === 'ineligible' || ['age_eligibility', 'recent_difficulty', 'current_strategy', 'relationship', 'age_range', 'existing_tools'].includes(questionId)) return 'context';
    if (questionId === 'primary_path' || questionId === 'secondary_path' || questionId.startsWith('secondary_')) return 'needs';
    if (questionId.startsWith('name_')) return 'universe';
    if (questionId.startsWith('survey_') || ['review', 'core_review', 'optional_hub', 'completed'].includes(questionId)) return 'finish';
    if (primaryPathKey()) {
      const path = Q.paths[primaryPathKey()];
      const pathIds = [path.causeQuestion.id, ...Object.values(path.conditional).map((q) => q.id), ...path.common.map((q) => q.id)];
      if (pathIds.includes(questionId)) return 'needs';
    }
    return 'experience';
  }

  function renderStages() {
    const active = stageForQuestion(state.current);
    const activeIndex = Q.stages.findIndex(([key]) => key === active);
    stageList.innerHTML = Q.stages.map(([key, label], index) => {
      const classes = ['stage-item'];
      if (index < activeIndex) classes.push('is-complete');
      if (index === activeIndex) classes.push('is-current');
      return `<li class="${classes.join(' ')}"><span class="stage-track"></span><span>${escapeHtml(label)}</span></li>`;
    }).join('');
  }

  function themeClass(question) {
    const primary = primaryPathKey();
    if (question?.id === 'primary_path' || question?.id === 'secondary_path') return 'theme-neutral';
    if (question?.id?.startsWith('secondary_')) {
      const secondary = secondaryPathKey();
      return secondary ? `theme-${Q.paths[secondary].theme}` : 'theme-neutral';
    }
    if (primary && Q.paths[primary]) return `theme-${Q.paths[primary].theme}`;
    return 'theme-neutral';
  }

  function getOptionLabel(question, value) {
    if (!question || !Array.isArray(question.options)) return String(value ?? '');
    const found = question.options.find(([key]) => key === value);
    return found ? found[1] : String(value ?? '');
  }

  function summaryChoiceLabel(questionId, value) {
    if (value === null || value === undefined || value === '' || value === 'none') return null;

    if (questionId === 'primary_path' || questionId === 'secondary_path') {
      return Q.paths[value]?.label || null;
    }

    const question = Q.shared.find((item) => item.id === questionId)
      || Q.initial.find((item) => item.id === questionId);

    if (!question) return String(value);

    if (Array.isArray(question.options)) {
      return getOptionLabel(question, value);
    }

    if (Array.isArray(question.cards)) {
      return question.cards.find((card) => card.value === value)?.title || String(value);
    }

    return String(value);
  }

  function buildPersonalSummary() {
    const fields = [
      ['primary_path', 'Primary area'],
      ['secondary_path', 'Optional second area'],
      ['recent_difficulty', 'Recent difficulty'],
      ['visual_home_density', 'Home screen'],
      ['visual_focus_surface', 'Focus view'],
      ['visual_quick_capture', 'Quick capture'],
      ['visual_return_screen', 'Returning after a break'],
      ['progress_style', 'Long-term progress'],
      ['personality', 'App personality'],
      ['name_final', 'Name universe']
    ];

    return fields
      .map(([id, label]) => ({ label, value: summaryChoiceLabel(id, state.answers[id]) }))
      .filter((item) => item.value);
  }

  function deterministicShuffle(items, key) {
    const clone = [...items];
    const seedSource = `${state.orderSeed || state.token || 'seed'}:${key}`;
    let seed = 0;
    for (let i = 0; i < seedSource.length; i += 1) seed = ((seed << 5) - seed + seedSource.charCodeAt(i)) | 0;
    const random = () => {
      seed |= 0;
      seed = (seed + 0x6D2B79F5) | 0;
      let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
      t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
    for (let i = clone.length - 1; i > 0; i -= 1) {
      const j = Math.floor(random() * (i + 1));
      [clone[i], clone[j]] = [clone[j], clone[i]];
    }
    return clone;
  }

  function renderWelcome() {
    top.hidden = true;
    pauseButton.hidden = true;
    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">Help shape an ADHD-friendly app</p>
        <h1>A questionnaire that adapts to what you need</h1>
        <p class="lead">Help us pre-test a short adaptive study about a local-first, non-punitive ADHD-friendly app. We want to learn what feels useful, calm and realistic—without assuming that one support style fits everyone.</p>
        <div class="theme-banner theme-green">
          <p><strong>About 8–12 minutes for most people.</strong> Your route changes according to your answers, and you can pause, save your progress, skip optional parts later, or stop whenever you need to.</p>
        </div>
        <div class="notice">
          <strong>This is participatory product research, not a medical test.</strong><br>
          It does not diagnose ADHD, assess treatment, or provide health advice. This pre-test version is open to adults aged 18 or older, and ADHD-related questions remain optional.
        </div>
        <div class="eligibility-banner" role="note" aria-label="Age eligibility">
          <span class="eligibility-badge">18+ PRE-TEST</span>
          <div>
            <strong>Adults 18+ only for this pre-test.</strong>
            <p>If you are under 18, please do not begin this round. A later research round may use an adapted process for younger participants.</p>
          </div>
        </div>
        <div class="action-row">
          <a class="btn btn-quiet" href="privacy.php">Read the privacy information</a>
          <button class="btn btn-primary" id="begin-survey" type="button">Continue</button>
        </div>
      </div>`;
    document.getElementById('begin-survey').addEventListener('click', () => {
      state.current = 'readiness';
      saveLocalState();
      render();
    });
  }

  function renderReadiness() {
    top.hidden = true;
    pauseButton.hidden = true;
    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">Before you begin</p>
        <h2>Check in with yourself first</h2>
        <p class="lead">Some questions refer to everyday frustration, overwhelm and support needs. For some people, that can feel tiring or emotionally activating.</p>

        <section class="readiness-card">
          <div class="readiness-item">
            <strong>Only continue if this feels manageable today.</strong>
            <p>If today is a low-energy or emotionally difficult day, you do not have to do this now.</p>
          </div>
          <div class="readiness-item">
            <strong>You do not need to think about your worst moments.</strong>
            <p>Just answer based on what feels generally true for you, or on what comes to mind most easily.</p>
          </div>
          <div class="readiness-item">
            <strong>You do not need to finish everything in one go.</strong>
            <p>You can pause, save your progress, skip optional parts later, or stop partway through.</p>
          </div>
        </section>

        <div class="notice">
          <strong>If this is not a good time right now, that is completely okay.</strong><br>
          You can come back later when you feel more available.
        </div>

        <div class="action-row">
          <button class="btn btn-secondary" id="readiness-later" type="button">I’d rather come back later</button>
          <button class="btn btn-primary" id="readiness-continue" type="button">I feel okay to continue</button>
        </div>
      </div>`;

    document.getElementById('readiness-later').addEventListener('click', () => {
      state.current = 'come_back_later';
      saveLocalState();
      render();
    });

    document.getElementById('readiness-continue').addEventListener('click', () => {
      state.current = 'consent';
      saveLocalState();
      render();
    });
  }

  function renderComeBackLater() {
    top.hidden = true;
    pauseButton.hidden = true;
    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">That’s completely okay</p>
        <h2>You can come back later</h2>
        <p class="lead">If this does not feel manageable right now, you do not need to push through it.</p>

        <div class="theme-banner theme-peach">
          <p><strong>The questionnaire will still be here later.</strong> Come back when you feel more rested, more focused, or simply more available.</p>
        </div>

        <div class="notice">
          You are not “failing” the questionnaire by stopping here. Waiting for a better moment is valid.
        </div>

        <div class="action-row">
          <button class="btn btn-secondary" id="later-back" type="button">Back</button>
          <a class="btn btn-quiet" href="https://www.lepotager.org/">Leave for now</a>
        </div>
      </div>`;

    document.getElementById('later-back').addEventListener('click', () => {
      state.current = 'readiness';
      saveLocalState();
      render();
    });
  }

  function renderConsent() {
    top.hidden = true;
    pauseButton.hidden = true;
    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">Before we begin</p>
        <h2>Your choice and your data</h2>
        <p class="lead">Participation is voluntary. You can skip optional questions, pause, return later, stop partway through, or delete your unfinished answers.</p>
        <div class="consent-list">
          <label class="consent-row">
            <input type="checkbox" id="consent-research">
            <span>I agree that my answers may be used to design and evaluate this app concept.</span>
          </label>
          <label class="consent-row">
            <input type="checkbox" id="consent-sensitive">
            <span>I explicitly agree to the processing of any optional ADHD-related information I choose to provide for this participatory product research.</span>
          </label>
          <label class="consent-row">
            <input type="checkbox" id="consent-privacy">
            <span>I have read the <a href="privacy.php" target="_blank" rel="noopener">privacy information</a>, including retention periods and my rights.</span>
          </label>
        </div>
        <div id="consent-feedback" aria-live="polite"></div>
        <div class="action-row">
          <button class="btn btn-secondary" id="consent-back" type="button">Back</button>
          <button class="btn btn-primary" id="consent-continue" type="button">Agree and continue</button>
        </div>
      </div>`;

    document.getElementById('consent-back').addEventListener('click', () => {
      state.current = 'readiness';
      render();
    });
    document.getElementById('consent-continue').addEventListener('click', startSurvey);
  }

  async function startSurvey() {
    const checks = ['consent-research', 'consent-sensitive', 'consent-privacy'].map((id) => document.getElementById(id).checked);
    const feedback = document.getElementById('consent-feedback');
    if (!checks.every(Boolean)) {
      feedback.innerHTML = '<div class="notice is-error">Please confirm all three points before continuing.</div>';
      return;
    }

    setBusy(true);
    feedback.innerHTML = '';
    try {
      const result = await api('start', { locale: 'en' });
      state.token = result.token;
      state.orderSeed = result.session_id;
      state.current = 'age_eligibility';
      state.answers = {};
      state.completed = false;
      state.coreSubmitted = false;
      state.optionalGroup = null;
      saveLocalState();
      render();
    } catch (error) {
      feedback.innerHTML = `<div class="notice is-error">${escapeHtml(error.message)}</div>`;
    } finally {
      setBusy(false);
    }
  }


  function getRouteProgress(question) {
    const flow = state.coreSubmitted ? buildFullFlow() : buildCoreFlow();
    const index = flow.findIndex((item) => item.id === question.id);
    const stable = state.coreSubmitted ? false : routeStructureStable();
    const total = flow.length;
    const page = index >= 0 ? index + 1 : 0;
    const percentBefore = stable && total ? Math.round((Math.max(0, index) / total) * 100) : null;

    let optionalLabel = '';
    if (state.coreSubmitted) {
      optionalLabel = 'Optional topic · you can return to the topic menu or finish at any time';
    } else if (question.id === 'secondary_path') {
      optionalLabel = 'Optional choice · you can continue without a second area';
    } else if (question.id.startsWith('secondary_')) {
      optionalLabel = 'Optional second area';
    } else if (question.optional) {
      optionalLabel = 'Optional · skip anytime';
    }

    return { flow, index, stable, total, page, percentBefore, optionalLabel };
  }


  function renderQuestionMeta(question) {
    const progress = getRouteProgress(question);
    const chips = [];

    if (progress.stable && progress.index >= 0) {
      chips.push(`<span class="question-meta-chip progress-chip" aria-label="Page ${progress.page} of ${progress.total} in your chosen route">Route · ${progress.page}/${progress.total}</span>`);
    } else if (question.id === 'secondary_path') {
      chips.push('<span class="question-meta-chip progress-chip is-pending">Your route length is set after this choice</span>');
    }

    if (progress.optionalLabel) {
      chips.push(`<span class="question-meta-chip optional-chip">${escapeHtml(progress.optionalLabel)}</span>`);
    }

    if (!chips.length) return '';
    return `<div class="question-meta" aria-label="Question status">${chips.join('')}</div>`;
  }

  function getMilestones(question) {
    const progress = getRouteProgress(question);
    const { flow, index } = progress;
    if (index === -1) return [];

    // A section-completion message is shown on the NEXT page, after the
    // participant has actually completed the section. This avoids congratulating
    // them before they have answered its final question.
    if (question.id === 'secondary_path') {
      const primary = primaryPathKey();
      return [{
        kind: 'path-complete',
        eyebrow: 'Adaptive path complete',
        title: 'Your main area is mapped 🌿',
        text: `You’ve finished the adaptive questions about ${Q.paths[primary]?.label || 'your main need'}. This next choice is optional.`,
        motion: 'quiet',
        confetti: false
      }];
    }

    const secondary = secondaryPathKey();
    if (secondary && Q.paths[secondary]) {
      const secondaryIds = Q.paths[secondary].secondary.map((item) => item.id);
      const lastSecondaryIndex = flow.findIndex((item) => item.id === secondaryIds[secondaryIds.length - 1]);
      if (lastSecondaryIndex >= 0 && index === lastSecondaryIndex + 1) {
        return [{
          kind: 'optional-complete',
          eyebrow: 'Optional section complete',
          title: 'Second area explored ✨',
          text: 'That optional detour is finished. You’re back to the shared questions now.',
          motion: 'quiet',
          confetti: false
        }];
      }
    }

    // The exact halfway marker only becomes available once the participant has
    // chosen their main path, the cause-specific branch, and whether to include
    // a second optional area. At that point buildFlow() is final for this route.
    if (progress.stable && flow.length) {
      const secondaryDecisionIndex = flow.findIndex((item) => item.id === 'secondary_path');
      const firstStableIndex = Math.max(0, secondaryDecisionIndex + 1);
      const exactHalfIndex = Math.ceil(flow.length / 2);
      const halfTriggerIndex = Math.max(firstStableIndex, exactHalfIndex);
      if (index === halfTriggerIndex) {
        const completed = index;
        return [{
          kind: 'half',
          eyebrow: 'Chosen route progress',
          title: 'You’re halfway through the required part! 🌱',
          text: `${completed} of ${flow.length} required questionnaire pages are behind you. Optional topics will be offered separately after submission.`,
          cta: true,
          motion: 'celebrate',
          confetti: true
        }];
      }
    }

    // Show completion of the three equal name-universe cards only after the
    // third card has actually been answered, on the final name-choice page.
    if (question.id === 'name_final') {
      return [{
        kind: 'name-complete',
        eyebrow: 'Name study',
        title: 'All three universes explored ✨',
        text: 'Now you can make one final choice with the same amount of context for each name.',
        motion: 'soft',
        confetti: true
      }];
    }

    // The last required/core question is followed by two optional feedback
    // questions. Celebrate completion of the core on the first of those pages,
    // rather than calling an optional page “the last question”.
    const coreFlow = flow.filter((item) => !isOptionalQuestion(item));
    const lastCore = coreFlow.at(-1);
    if (lastCore) {
      const lastCoreIndex = flow.findIndex((item) => item.id === lastCore.id);
      if (index === lastCoreIndex + 1) {
        return [{
          kind: 'core-complete',
          eyebrow: 'Core questionnaire complete',
          title: 'You’ve completed everything required! 🌿✨',
          text: 'Only optional feedback remains before review. You can answer it, skip it, or save and come back later.',
          motion: 'celebrate',
          confetti: true,
          grown: true
        }];
      }
    }

    return [];
  }

  function renderMilestones(question) {
    const milestones = getMilestones(question);
    if (!milestones.length) return '';
    return milestones.map((milestone) => {
      const confetti = milestone.confetti ? `
          <span class="confetti confetti-a"></span>
          <span class="confetti confetti-b"></span>
          <span class="confetti confetti-c"></span>
          <span class="confetti confetti-d"></span>` : '';
      const grown = milestone.grown || milestone.kind === 'core-complete';
      return `
      <div class="milestone-card milestone-${milestone.kind} milestone-motion-${milestone.motion || 'quiet'}">
        <div class="milestone-decoration" aria-hidden="true">
          ${confetti}
          <span class="milestone-plant ${grown ? 'is-grown' : ''}">
            <span class="plant-pot"></span>
            <span class="plant-stem"></span>
            <span class="plant-leaf plant-leaf-left"></span>
            <span class="plant-leaf plant-leaf-right"></span>
            <span class="plant-leaf plant-leaf-top"></span>
          </span>
        </div>
        <div class="milestone-copy">
          <p class="eyebrow">${escapeHtml(milestone.eyebrow || 'Progress marker')}</p>
          <h3>${escapeHtml(milestone.title)}</h3>
          <p>${escapeHtml(milestone.text)}</p>
        </div>
        ${milestone.cta ? `<div class="milestone-actions"><button class="btn btn-primary btn-large milestone-save" type="button">Save and finish later</button></div>` : ''}
      </div>`;
    }).join('');
  }

  function visualPreview(kind) {
    const previews = {
      home_one: `<div class="preview-shell"><div class="preview-line short"></div><div class="preview-card big"></div><div class="preview-line tiny"></div></div>`,
      home_few: `<div class="preview-shell"><div class="preview-card big"></div><div class="preview-row"><span class="preview-chip"></span><span class="preview-chip"></span></div><div class="preview-row"><span class="preview-card mini"></span><span class="preview-card mini"></span></div></div>`,
      home_list: `<div class="preview-shell"><div class="preview-line short"></div><div class="preview-card mini"></div><div class="preview-card mini"></div><div class="preview-card mini"></div></div>`,
      home_custom: `<div class="preview-shell"><div class="preview-grid"></div><div class="preview-line"></div><div class="preview-line short"></div></div>`,
      focus_timer: `<div class="preview-shell center"><div class="preview-circle"></div><div class="preview-line short"></div></div>`,
      focus_task: `<div class="preview-shell"><div class="preview-line short"></div><div class="preview-circle small"></div><div class="preview-card mini"></div></div>`,
      focus_veil: `<div class="preview-shell veil"><div class="preview-circle small"></div><div class="preview-line short"></div><div class="preview-button"></div></div>`,
      focus_hidden: `<div class="preview-shell center"><div class="preview-dot-row"></div><div class="preview-line tiny"></div></div>`,
      capture_text: `<div class="preview-shell"><div class="preview-button"></div><div class="preview-line"></div></div>`,
      capture_choice: `<div class="preview-shell"><div class="preview-row"><span class="preview-button small"></span><span class="preview-button small"></span></div><div class="preview-line short"></div></div>`,
      capture_inbox: `<div class="preview-shell"><div class="preview-button"></div><div class="preview-card mini"></div><div class="preview-line tiny"></div></div>`,
      capture_form: `<div class="preview-shell"><div class="preview-line"></div><div class="preview-line"></div><div class="preview-line short"></div></div>`,
      return_resume: `<div class="preview-shell"><div class="preview-card big"></div><div class="preview-button"></div></div>`,
      return_choice: `<div class="preview-shell"><div class="preview-row"><span class="preview-card mini"></span><span class="preview-card mini"></span></div><div class="preview-line tiny"></div></div>`,
      return_summary: `<div class="preview-shell"><div class="preview-line"></div><div class="preview-card mini"></div><div class="preview-card mini"></div></div>`,
      return_question: `<div class="preview-shell"><div class="preview-line short"></div><div class="preview-button"></div><div class="preview-button small"></div></div>`
    };
    return previews[kind] || `<div class="preview-shell"><div class="preview-line"></div></div>`;
  }

  function renderQuestion(question) {
    top.hidden = false;
    pauseButton.hidden = true;
    renderStages();

    const theme = themeClass(question);
    let controlHtml = '';
    const milestonesHtml = renderMilestones(question);
    const questionMetaHtml = renderQuestionMeta(question);
    const optionalQuestion = isOptionalQuestion(question);

    if (question.type === 'single' || question.type === 'multi' || question.type === 'visual_single') controlHtml = renderChoiceControl(question);
    if (question.type === 'path') controlHtml = renderPathControl(question, false);
    if (question.type === 'secondary_path') controlHtml = renderPathControl(question, true);
    if (question.type === 'text') controlHtml = renderTextControl(question);
    if (question.type === 'universe') controlHtml = renderUniverseControl(question);

    body.innerHTML = `
      <div class="question-wrap ${theme}">
        ${milestonesHtml}
        <div class="theme-banner ${theme} ${optionalQuestion ? 'is-optional-question' : ''}">
          ${questionMetaHtml}
          <p class="eyebrow">${escapeHtml(question.eyebrow || '')}</p>
          <h2>${escapeHtml(question.title)}</h2>
          ${question.description ? `<p>${escapeHtml(question.description)}</p>` : ''}
        </div>
        ${question.help ? `<p class="help-text">${escapeHtml(question.help)}</p>` : ''}
        <div id="question-control">${controlHtml}</div>
        <div id="question-feedback" aria-live="polite"></div>
        <div class="action-row">
          <button class="btn btn-secondary" id="question-back" type="button">${state.coreSubmitted ? 'Optional topics' : 'Back'}</button>
          <div class="action-group question-actions">
            ${optionalQuestion ? '<button class="btn btn-quiet" id="question-skip" type="button">Skip</button>' : ''}
            <button class="btn btn-save-later" id="question-save-later" type="button">Save and finish later</button>
            ${state.coreSubmitted ? '<button class="btn btn-secondary" id="question-finish" type="button">Finish questionnaire</button>' : ''}
            <button class="btn btn-primary" id="question-continue" type="button">${state.coreSubmitted ? 'Save & continue' : 'Continue'}</button>
          </div>
        </div>
      </div>`;

    wireQuestionInputs(question);
    document.getElementById('question-back').addEventListener('click', () => {
      if (state.coreSubmitted) saveOptionalDraftAndReturn(question);
      else goBack();
    });
    const finish = document.getElementById('question-finish');
    if (finish) finish.addEventListener('click', () => finishFromOptionalQuestion(question));
    const skip = document.getElementById('question-skip');
    if (skip) skip.addEventListener('click', () => {
      // Treat skipping the optional second-area chooser as an explicit “none”.
      // This makes the remaining route length final and lets progress messages
      // become exact immediately afterwards.
      if (question.id === 'secondary_path') advance(question, 'none', false);
      else advance(question, null, true);
    });
    document.getElementById('question-save-later').addEventListener('click', () => saveQuestionAndFinishLater(question));
    document.getElementById('question-continue').addEventListener('click', () => submitQuestion(question));
    updateChoiceVisuals();
    document.querySelectorAll('.milestone-save').forEach((button) => button.addEventListener('click', () => saveQuestionAndFinishLater(question)));
  }

  function renderChoiceControl(question) {
    if (question.type === 'visual_single') {
      const current = state.answers[question.id];
      return `<div class="options-grid visual-grid">${(question.cards || []).map((card) => `
        <label class="option-card visual-card ${current === card.value ? 'is-selected' : ''}">
          <input type="radio" name="answer" value="${escapeHtml(card.value)}" ${current === card.value ? 'checked' : ''}>
          <span class="option-copy">
            <strong>${escapeHtml(card.title)}</strong>
            <small>${escapeHtml(card.caption || '')}</small>
            <span class="wireframe-preview">${visualPreview(card.preview)}</span>
          </span>
        </label>`).join('')}</div>`;
    }
    let options = question.options || [];
    if (question.randomize) options = deterministicShuffle(options, question.id);
    const current = state.answers[question.id];
    const selected = Array.isArray(current) ? current : [current];
    const inputType = question.type === 'multi' ? 'checkbox' : 'radio';
    return `<div class="options-grid">${options.map(([value, label]) => `
      <label class="option-card ${selected.includes(value) ? 'is-selected' : ''}">
        <input type="${inputType}" name="answer" value="${escapeHtml(value)}" ${selected.includes(value) ? 'checked' : ''}>
        <span class="option-copy">${escapeHtml(label)}</span>
      </label>`).join('')}</div>`;
  }

  function renderPathControl(question, secondary) {
    const current = state.answers[question.id];
    const primary = primaryPathKey();
    const entries = Object.entries(Q.paths).filter(([key]) => !secondary || key !== primary);
    const cards = entries.map(([key, path]) => `
      <label class="option-card path-card theme-${path.theme} ${current === key ? 'is-selected' : ''}">
        <input type="radio" name="answer" value="${escapeHtml(key)}" ${current === key ? 'checked' : ''}>
        <span class="option-copy"><strong>${escapeHtml(path.short)}</strong><small>${escapeHtml(path.description)}</small></span>
      </label>`).join('');
    const noSecond = secondary ? `
      <label class="option-card ${current === 'none' ? 'is-selected' : ''}">
        <input type="radio" name="answer" value="none" ${current === 'none' ? 'checked' : ''}>
        <span class="option-copy"><strong>No second area</strong><small>Continue with the shared questions.</small></span>
      </label>` : '';
    return `<div class="options-grid path-grid">${cards}${noSecond}</div>`;
  }

  function renderTextControl(question) {
    const value = state.answers[question.id] || '';
    return `<label>
      <span class="field-label">Your answer</span>
      <textarea class="text-field" id="text-answer" maxlength="${question.maxLength || 1000}" placeholder="${escapeHtml(question.placeholder || '')}">${escapeHtml(value)}</textarea>
      <span class="field-help"><span id="character-count">${String(value).length}</span> / ${question.maxLength || 1000}</span>
    </label>`;
  }

  function renderUniverseControl(question) {
    const current = state.answers[question.id] || { fit: null, words: [] };
    const words = Array.isArray(current.words) ? current.words : [];
    return `
      <div>
        <p class="field-label">Overall fit</p>
        <div class="rating-row" role="group" aria-label="Overall fit from 1 to 5">
          ${[1,2,3,4,5].map((value) => `<button type="button" class="rating-button ${Number(current.fit) === value ? 'is-selected' : ''}" data-rating="${value}" aria-pressed="${Number(current.fit) === value}">${value}</button>`).join('')}
        </div>
        <div class="rating-labels"><span>Not for me</span><span>Strong fit</span></div>
        <p class="field-label" style="margin-top:22px">Which words fit this universe?</p>
        <p class="small-text">Choose up to three.</p>
        <div class="word-grid">
          ${Q.universeWords.map(([value, label]) => `<button type="button" class="word-chip ${words.includes(value) ? 'is-selected' : ''}" data-word="${escapeHtml(value)}" aria-pressed="${words.includes(value)}">${escapeHtml(label)}</button>`).join('')}
        </div>
      </div>`;
  }

  function wireQuestionInputs(question) {
    document.querySelectorAll('input[name="answer"]').forEach((input) => {
      input.addEventListener('change', () => {
        if (question.type === 'multi') {
          const checked = [...document.querySelectorAll('input[name="answer"]:checked')];
          if (checked.length > (question.max || 99)) {
            input.checked = false;
            showQuestionError(`Choose no more than ${question.max} answers.`);
          } else {
            clearQuestionError();
          }
        }
        updateChoiceVisuals();
      });
    });

    const text = document.getElementById('text-answer');
    if (text) {
      text.addEventListener('input', () => {
        document.getElementById('character-count').textContent = String(text.value.length);
      });
    }

    document.querySelectorAll('[data-rating]').forEach((button) => {
      button.addEventListener('click', () => {
        document.querySelectorAll('[data-rating]').forEach((item) => {
          item.classList.toggle('is-selected', item === button);
          item.setAttribute('aria-pressed', item === button ? 'true' : 'false');
        });
      });
    });

    document.querySelectorAll('[data-word]').forEach((button) => {
      button.addEventListener('click', () => {
        const selected = [...document.querySelectorAll('[data-word].is-selected')];
        const becomingSelected = !button.classList.contains('is-selected');
        if (becomingSelected && selected.length >= 3) {
          showQuestionError('Choose no more than three words.');
          return;
        }
        button.classList.toggle('is-selected');
        button.setAttribute('aria-pressed', button.classList.contains('is-selected') ? 'true' : 'false');
        clearQuestionError();
      });
    });
  }

  function updateChoiceVisuals() {
    document.querySelectorAll('.option-card').forEach((card) => {
      const input = card.querySelector('input');
      card.classList.toggle('is-selected', Boolean(input?.checked));
    });
  }

  function showQuestionError(message) {
    document.getElementById('question-feedback').innerHTML = `<div class="notice is-error">${escapeHtml(message)}</div>`;
  }

  function clearQuestionError() {
    const feedback = document.getElementById('question-feedback');
    if (feedback) feedback.innerHTML = '';
  }

  function collectAnswer(question) {
    if (question.type === 'single' || question.type === 'visual_single' || question.type === 'path' || question.type === 'secondary_path') {
      return document.querySelector('input[name="answer"]:checked')?.value || null;
    }
    if (question.type === 'multi') {
      return [...document.querySelectorAll('input[name="answer"]:checked')].map((input) => input.value);
    }
    if (question.type === 'text') {
      return document.getElementById('text-answer').value.trim();
    }
    if (question.type === 'universe') {
      const rating = document.querySelector('[data-rating].is-selected');
      const words = [...document.querySelectorAll('[data-word].is-selected')].map((button) => button.dataset.word);
      return { fit: rating ? Number(rating.dataset.rating) : null, words };
    }
    return null;
  }

  function answerIsEmpty(question, answer) {
    if (question.type === 'multi') return !Array.isArray(answer) || answer.length === 0;
    if (question.type === 'universe') return !answer || !answer.fit;
    return answer === null || answer === undefined || answer === '';
  }

  function submitQuestion(question) {
    const answer = collectAnswer(question);
    const optionalQuestion = isOptionalQuestion(question);
    if (!optionalQuestion && answerIsEmpty(question, answer)) {
      showQuestionError('Choose an answer before continuing.');
      return;
    }
    if (optionalQuestion && answerIsEmpty(question, answer)) {
      if (question.id === 'secondary_path') {
        advance(question, 'none', false);
      } else {
        advance(question, null, true);
      }
      return;
    }
    if (question.type === 'multi' && Array.isArray(answer) && answer.length > (question.max || 99)) {
      showQuestionError(`Choose no more than ${question.max} answers.`);
      return;
    }
    advance(question, answer, false);
  }

  function answerHasAnyInput(question, answer) {
    if (question.type === 'multi') return Array.isArray(answer) && answer.length > 0;
    if (question.type === 'universe') {
      return Boolean(answer && (answer.fit || (Array.isArray(answer.words) && answer.words.length > 0)));
    }
    return answer !== null && answer !== undefined && answer !== '';
  }

  async function saveQuestionAndFinishLater(question) {
    const answer = collectAnswer(question);

    if (question.type === 'multi' && Array.isArray(answer) && answer.length > (question.max || 99)) {
      showQuestionError(`Choose no more than ${question.max} answers.`);
      return;
    }

    clearQuestionError();

    // A complete answer behaves like Continue: save it and resume at the next question.
    if (!answerIsEmpty(question, answer)) {
      await advance(question, answer, false, true);
      return;
    }

    // No complete answer: preserve any partial input, keep this question as the
    // resume target, and never force a response just because the participant
    // needs to stop now.
    if (answerHasAnyInput(question, answer)) state.answers[question.id] = answer;
    else delete state.answers[question.id];

    state.current = question.id;
    saveLocalState();
    setSaveStatus(answerHasAnyInput(question, answer) ? 'Saving your progress…' : 'Saving your place…');
    await syncLocalAnswers();
    openPauseModal({ advanced: false, partial: answerHasAnyInput(question, answer) });
  }

  async function advance(question, answer, skipped, pauseAfter = false) {
    if (skipped) delete state.answers[question.id];
    else state.answers[question.id] = answer;

    if (
      (question.id === 'age_eligibility' && answer === 'no')
      || (question.id === 'age_range' && answer === 'under_18')
    ) {
      try { if (state.token) await api('delete', { token: state.token }); } catch (_) {}
      clearLocalState();
      state = {
        token: null,
        answers: {},
        current: 'ineligible',
        completed: false,
        coreSubmitted: false,
        optionalGroup: null,
        contactSubmitted: false,
        orderSeed: null,
        personalSummary: null
      };
      render();
      return;
    }

    if (question.id === 'primary_path') {
      Object.keys(state.answers).forEach((id) => {
        if (id !== 'primary_path' && !Q.initial.some((q) => q.id === id)) delete state.answers[id];
      });
    }

    if (question.id === 'secondary_path') {
      Object.keys(state.answers)
        .filter((id) => id.startsWith('secondary_') && id !== 'secondary_path')
        .forEach((id) => delete state.answers[id]);
    }

    if (state.coreSubmitted && state.optionalGroup) {
      const next = nextOptionalQuestionInGroup(state.optionalGroup, question.id);
      state.current = next ? next.id : 'optional_hub';
      if (!next) state.optionalGroup = null;

      saveLocalState();

      if (pauseAfter) {
        setSaveStatus('Saving your answer…');
        await syncLocalAnswers();
        render();
        openPauseModal({ advanced: true });
        return;
      }

      render();
      await syncLocalAnswers();
      return;
    }

    const flow = buildCoreFlow();
    const index = flow.findIndex((item) => item.id === question.id);
    const next = flow[index + 1];
    state.current = next ? next.id : 'core_review';
    saveLocalState();

    if (pauseAfter) {
      setSaveStatus('Saving your answer…');
      await syncLocalAnswers();
      render();
      openPauseModal({ advanced: true });
      return;
    }

    render();
    await syncLocalAnswers();
  }

  async function saveOptionalDraftAndReturn(question) {
    const answer = collectAnswer(question);

    if (question.type === 'multi' && Array.isArray(answer) && answer.length > (question.max || 99)) {
      showQuestionError(`Choose no more than ${question.max} answers.`);
      return;
    }

    if (answerHasAnyInput(question, answer)) {
      state.answers[question.id] = answer;
    }

    state.current = 'optional_hub';
    state.optionalGroup = null;
    saveLocalState();
    await syncLocalAnswers();
    render();
  }

  async function finishFromOptionalQuestion(question) {
    const answer = collectAnswer(question);

    if (question.type === 'multi' && Array.isArray(answer) && answer.length > (question.max || 99)) {
      showQuestionError(`Choose no more than ${question.max} answers.`);
      return;
    }

    if (answerHasAnyInput(question, answer)) {
      state.answers[question.id] = answer;
    }

    await syncLocalAnswers();
    await finishQuestionnaire();
  }


  function goBack() {
    const flow = buildFlow();
    const index = flow.findIndex((question) => question.id === state.current);
    if (index > 0) state.current = flow[index - 1].id;
    else state.current = 'consent';
    saveLocalState();
    render();
    if (state.token) {
      api('set_step', { token: state.token, current_step: state.current }).catch(() => {});
    }
  }


  function renderIneligible() {
    top.hidden = true;
    pauseButton.hidden = true;
    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">Thank you for your interest</p>
        <h2>This first pre-test is limited to adults aged 18 or older</h2>
        <p class="lead">Your unfinished questionnaire session and its answers have been deleted. You cannot take part in this round.</p>

        <section class="future-youth-card">
          <p class="eyebrow">Future research</p>
          <h3>Want to hear if a future age-appropriate round opens?</h3>
          <p>You can leave only an email address below. It will be stored separately from questionnaire responses. We will not keep your exact age, your discarded answers, your name or any questionnaire identifier for this notification.</p>

          <label class="field-label" for="future-youth-email">Email address</label>
          <input
            class="text-field"
            id="future-youth-email"
            type="email"
            inputmode="email"
            autocomplete="email"
            placeholder="you@example.org">

          <label class="consent-row future-youth-consent">
            <input type="checkbox" id="future-youth-consent">
            <span>I want to receive an email only if a future age-appropriate research round becomes available. I understand that I can unsubscribe at any time.</span>
          </label>

          <div id="future-youth-feedback" aria-live="polite"></div>

          <div class="action-row">
            <button class="btn btn-primary" id="future-youth-submit" type="button">Notify me about a future round</button>
          </div>

          <p class="small-text">A confirmation email will be sent first. If you do not confirm it, this request remains inactive and is later removed under the existing retention rules.</p>
          <p class="small-text future-direct-link">
            Need to come back later? This notification form also has its own permanent page:
            <a href="future-research.php"><strong>future-research.php</strong></a>.
          </p>
        </section>

        <div class="action-row">
          <a class="btn btn-secondary" href="future-research.php">Open the standalone notification page</a>
          <a class="btn btn-secondary" href="https://www.lepotager.org/">Return to Le Potager du Web</a>
        </div>
      </div>`;

    const submit = document.getElementById('future-youth-submit');
    submit.addEventListener('click', submitFutureYouthInterest);
  }

  async function submitFutureYouthInterest() {
    const emailField = document.getElementById('future-youth-email');
    const consentField = document.getElementById('future-youth-consent');
    const feedback = document.getElementById('future-youth-feedback');
    const button = document.getElementById('future-youth-submit');

    const email = emailField.value.trim();

    if (!email || !consentField.checked) {
      feedback.innerHTML = '<div class="notice is-error">Enter your email address and confirm that you want this future-round notification.</div>';
      return;
    }

    button.disabled = true;
    feedback.innerHTML = '';

    try {
      await api('participate', {
        email,
        preferred_name: '',
        preferred_language: 'en',
        interests: ['future_youth'],
        details: {},
        link_to_response: false
      });

      emailField.value = '';
      consentField.checked = false;
      feedback.innerHTML = '<div class="notice is-success"><strong>Check your inbox.</strong> We sent a confirmation link. No questionnaire response is attached to this request.</div>';
      button.textContent = 'Confirmation email sent';
    } catch (error) {
      feedback.innerHTML = `<div class="notice is-error">${escapeHtml(error.message)}</div>`;
      button.disabled = false;
    }
  }


  function renderCoreReview() {
    top.hidden = false;
    pauseButton.hidden = true;
    renderStages();

    const primary = Q.paths[primaryPathKey()];
    const requiredCount = buildCoreFlow().length;

    body.innerHTML = `
      <div class="question-wrap">
        <section class="core-submit-card">
          <div class="milestone-decoration" aria-hidden="true">
            <span class="confetti confetti-a"></span>
            <span class="confetti confetti-b"></span>
            <span class="confetti confetti-c"></span>
            <span class="milestone-plant is-grown"><span class="plant-stem"></span><span class="plant-leaf leaf-left"></span><span class="plant-leaf leaf-right"></span></span>
          </div>
          <p class="eyebrow">Required part complete</p>
          <h2>You’ve completed everything required 🌿✨</h2>
          <p class="lead">You can submit the required part now. Once submitted, it already counts as a completed pre-test response — everything after that is optional.</p>
        </section>

        <div class="summary-grid">
          <div class="summary-card"><small>Main support area</small><strong>${escapeHtml(primary?.label || 'Selected')}</strong></div>
          <div class="summary-card"><small>Required pages completed</small><strong>${requiredCount}</strong></div>
        </div>

        <div class="notice">
          <strong>What happens next?</strong> After submitting, you will get a menu of optional topics. You can answer none, one, several or all of them, come back later, and finish whenever you want.
        </div>

        <div id="core-submit-feedback" aria-live="polite"></div>

        <div class="action-row">
          <button class="btn btn-secondary" id="core-back" type="button">Back to the last required question</button>
          <button class="btn btn-primary btn-large" id="core-submit" type="button">Submit required answers</button>
        </div>
      </div>`;

    document.getElementById('core-back').addEventListener('click', () => {
      state.current = buildCoreFlow().at(-1)?.id || 'primary_path';
      render();
    });

    document.getElementById('core-submit').addEventListener('click', submitCoreQuestionnaire);
  }

  async function submitCoreQuestionnaire() {
    const button = document.getElementById('core-submit');
    const feedback = document.getElementById('core-submit-feedback');

    button.disabled = true;
    feedback.innerHTML = '';
    setSaveStatus('Submitting required answers…');

    try {
      const synced = await syncLocalAnswers();
      if (!synced) {
        throw new Error('The required answers could not be synced with the server. Please try again.');
      }

      await api('submit', {
        token: state.token,
        phase: 'core'
      });

      state.coreSubmitted = true;
      state.completed = false;
      state.optionalGroup = null;
      state.current = 'optional_hub';
      saveLocalState();

      setSaveStatus('Required answers submitted');
      render();
    } catch (error) {
      feedback.innerHTML = `<div class="notice is-error">${escapeHtml(error.message)}</div>`;
      button.disabled = false;
      setSaveStatus('Submission failed');
    }
  }

  function renderOptionalHub() {
    top.hidden = false;
    pauseButton.hidden = true;
    renderStages();

    const groups = optionalGroups();
    const totalQuestions = groups.reduce((sum, group) => sum + group.questions.length, 0);
    const answeredQuestions = groups.reduce((sum, group) => sum + optionalGroupProgress(group).answered, 0);
    const remaining = Math.max(0, totalQuestions - answeredQuestions);

    body.innerHTML = `
      <div class="question-wrap optional-hub">
        <section class="optional-hub-intro">
          <p class="eyebrow">Required answers submitted ✓</p>
          <h2>The rest is entirely up to you</h2>
          <p class="lead">Your required response already counts. Choose any optional topic below, come back to this page after each topic, save for later, or finish now.</p>

          <div class="optional-hub-status">
            <strong>${answeredQuestions} optional question${answeredQuestions === 1 ? '' : 's'} answered</strong>
            <span>${remaining} still available · none are required</span>
          </div>
        </section>

        <div class="optional-topic-grid">
          ${groups.map((group) => {
            const progress = optionalGroupProgress(group);
            const done = progress.answered === progress.total;
            const partial = progress.answered > 0 && !done;
            const status = done ? 'Completed' : partial ? `${progress.answered}/${progress.total} answered` : 'Not answered';
            const action = done ? 'Review' : partial ? 'Continue' : 'Explore';
            return `
              <article class="optional-topic-card ${done ? 'is-complete' : ''}">
                <div>
                  <span class="optional-topic-status">${status}</span>
                  <h3>${escapeHtml(group.title)}</h3>
                  <p>${escapeHtml(group.description)}</p>
                </div>
                <button class="btn ${done ? 'btn-secondary' : 'btn-primary'} optional-topic-open" type="button" data-group="${escapeHtml(group.key)}">${action}</button>
              </article>`;
          }).join('')}
        </div>

        <div class="notice">
          <strong>You can stop whenever you want.</strong> Finishing now keeps every answer you have already submitted. Leaving an optional topic unanswered does not make your response incomplete.
        </div>

        <div class="action-row optional-hub-actions">
          <button class="btn btn-save-later" id="optional-save-later" type="button">Save and come back later</button>
          <button class="btn btn-primary btn-large" id="optional-finish" type="button">Finish questionnaire</button>
        </div>
      </div>`;

    document.querySelectorAll('.optional-topic-open').forEach((button) => {
      button.addEventListener('click', () => enterOptionalGroup(button.dataset.group));
    });

    document.getElementById('optional-save-later').addEventListener('click', async () => {
      state.current = 'optional_hub';
      state.optionalGroup = null;
      saveLocalState();
      await syncLocalAnswers();
      openPauseModal({ advanced: false });
    });

    document.getElementById('optional-finish').addEventListener('click', finishQuestionnaire);
  }

  async function enterOptionalGroup(groupKey) {
    const group = optionalGroupByKey(groupKey);
    if (!group) return;

    const question = firstOptionalQuestion(group, true) || firstOptionalQuestion(group, false);
    if (!question) return;

    state.optionalGroup = group.key;
    state.current = question.id;
    saveLocalState();

    try {
      await api('set_step', { token: state.token, current_step: state.current });
    } catch (_) {}

    render();
  }

  async function finishQuestionnaire() {
    setSaveStatus('Finishing…');

    try {
      await syncLocalAnswers();
      const personalSummary = buildPersonalSummary();

      await api('submit', {
        token: state.token,
        phase: 'final'
      });

      state.coreSubmitted = true;
      state.completed = true;
      state.current = 'completed';
      state.optionalGroup = null;
      state.personalSummary = personalSummary;
      state.answers = {};
      saveLocalState();

      setSaveStatus('Submitted');
      render();
    } catch (error) {
      setSaveStatus('Could not finish — your submitted required answers are still safe', 'warning');
      const target = document.getElementById('question-feedback') || document.getElementById('core-submit-feedback');
      if (target) target.innerHTML = `<div class="notice is-error">${escapeHtml(error.message)}</div>`;
    }
  }

  function renderReview() {
    top.hidden = false;
    pauseButton.hidden = true;
    renderStages();
    const primary = Q.paths[primaryPathKey()];
    const secondary = secondaryPathKey() ? Q.paths[secondaryPathKey()] : null;
    const firstName = getOptionLabel(Q.shared.find((q) => q.id === 'name_first'), state.answers.name_first);
    const finalName = getOptionLabel(Q.shared.find((q) => q.id === 'name_final'), state.answers.name_final);

    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">Review and submit</p>
        <h2>Your questionnaire is ready</h2>
        <p class="lead">You can submit now, or go back to change the most recent answer. Contact details are not required to submit the questionnaire.</p>
        <div class="summary-grid">
          <div class="summary-card"><small>Primary area</small><strong>${escapeHtml(primary?.label || 'Not selected')}</strong></div>
          <div class="summary-card"><small>Optional second area</small><strong>${escapeHtml(secondary?.label || 'None')}</strong></div>
          <div class="summary-card"><small>First name impression</small><strong>${escapeHtml(firstName || 'Skipped')}</strong></div>
          <div class="summary-card"><small>Final name choice</small><strong>${escapeHtml(finalName || 'Skipped')}</strong></div>
        </div>
        <div class="notice">Your response is stored under a random identifier. The optional participation form appears only after submission and is stored separately.</div>
        <div id="submit-feedback" aria-live="polite"></div>
        <div class="action-row">
          <button class="btn btn-secondary" id="review-back" type="button">Back</button>
          <button class="btn btn-primary" id="submit-survey" type="button">Submit my answers</button>
        </div>
      </div>`;
    document.getElementById('review-back').addEventListener('click', () => {
      const flow = buildFlow();
      state.current = flow.at(-1)?.id || 'primary_path';
      render();
    });
    document.getElementById('submit-survey').addEventListener('click', submitSurvey);
  }

  async function submitSurvey() {
    if (!state.coreSubmitted) {
      state.current = 'core_review';
      render();
      return;
    }
    await finishQuestionnaire();
  }


  function renderCompleted() {
    top.hidden = false;
    pauseButton.hidden = true;
    renderStages();
    const summary = Array.isArray(state.personalSummary) ? state.personalSummary : [];
    const summaryHtml = summary.length ? `
      <section class="participant-summary">
        <p class="eyebrow">Your answer snapshot</p>
        <h3>A quick recap of what you chose</h3>
        <p class="small-text">This is not a diagnosis or an ADHD profile — just a recap of a few product preferences from your own answers. It contains no date, time or response identifier.</p>
        <div class="participant-summary-grid">
          ${summary.map((item) => `<div class="participant-summary-item"><small>${escapeHtml(item.label)}</small><strong>${escapeHtml(item.value)}</strong></div>`).join('')}
        </div>
      </section>` : '';

    body.innerHTML = `
      <div class="question-wrap">
        <p class="eyebrow">Answers submitted</p>
        <h2>Thank you for helping shape the project 🌱</h2>
        <p class="lead">Your questionnaire is finished. Your required response was already secured before the optional topics; any optional answers you added were saved with it. The contact form below is separate.</p>
        ${summaryHtml}
        <div class="theme-banner theme-green">
          <p><strong>No contact details are needed for your questionnaire response.</strong> Nothing below is preselected.</p>
          <p style="margin-top:10px"><a href="results.php">See what the pre-test is learning so far →</a></p>
        </div>
        <div id="participation-root"></div>
      </div>`;
    renderParticipationForm();
  }

  function renderParticipationForm() {
    const root = document.getElementById('participation-root');
    if (state.contactSubmitted) {
      root.innerHTML = `
        <div class="notice is-success"><strong>Check your inbox.</strong> We sent a confirmation link. Your participation preferences become active only after you confirm them.</div>
        <div class="action-row"><a class="btn btn-secondary" href="https://www.lepotager.org/">Return to Le Potager du Web</a></div>`;
      return;
    }

    const interests = [
      ['research', 'User research panel', 'Short surveys, interviews or prototype evaluations.'],
      ['codesign', 'Co-design group', 'More active discussions about features, accessibility and language.'],
      ['beta', 'Beta-testing group', 'Try early functional versions of the app.'],
      ['accessibility', 'Accessibility testing', 'Readability, cognitive accessibility and assistive technology.'],
      ['opensource', 'Open-source contributions', 'Development, design, documentation, testing or organisation.'],
      ['translation', 'Translation and localisation', 'Help adapt the app to another language or cultural context.'],
      ['professional', 'Professional advisory group', 'Contribute relevant professional or research expertise.'],
      ['crowdfunding', 'Crowdfunding updates', 'Be notified when crowdfunding or pre-orders open.'],
      ['updates', 'Occasional project updates', 'Receive a low-frequency project newsletter.']
    ];

    root.innerHTML = `
      <h3>Would you like to stay involved?</h3>
      <p class="help-text">Choose as many options as you like. You will be contacted only about the categories you select.</p>
      <div class="participation-grid">
        ${interests.map(([value, title, description]) => `
          <label class="option-card participation-option">
            <input type="checkbox" name="interest" value="${value}">
            <span class="option-copy"><strong>${title}</strong><small>${description}</small></span>
          </label>`).join('')}
      </div>

      <div id="participation-fields" hidden>
        <div class="conditional-panel">
          <label class="field-label" for="participation-email">Email address</label>
          <input class="text-field" id="participation-email" type="email" inputmode="email" autocomplete="email" placeholder="you@example.org">
          <label class="field-label" for="participation-name" style="margin-top:14px">Name or preferred name <span class="small-text">(optional)</span></label>
          <input class="text-field" id="participation-name" type="text" autocomplete="name" maxlength="120">
          <label class="field-label" for="participation-language" style="margin-top:14px">Preferred language</label>
          <select class="select-field" id="participation-language"><option value="en">English</option><option value="fr">French</option><option value="other">Another language</option></select>
        </div>

        <div class="conditional-panel" id="research-details" hidden>
          <p class="field-label">Which research activities would you consider?</p>
          ${['Short questionnaires','Individual interviews','Group discussions','Prototype testing','Diary studies','I am not sure yet'].map((label, index) => `<label class="consent-row"><input type="checkbox" name="research_activity" value="${index}"><span>${label}</span></label>`).join('')}
        </div>

        <div class="conditional-panel" id="beta-details" hidden>
          <p class="field-label">Which platforms could you test?</p>
          ${['Android','iPhone or iPad','Windows','macOS','Linux','Web browser'].map((label) => `<label class="consent-row"><input type="checkbox" name="platform" value="${label}"><span>${label}</span></label>`).join('')}
        </div>

        <div class="conditional-panel" id="contributor-details" hidden>
          <p class="field-label">What might you like to contribute?</p>
          ${['Lived experience','User research','Accessibility','Development','UX or interface design','Illustration or visual design','Documentation','Communication','Translation','Community moderation','Professional or academic expertise'].map((label) => `<label class="consent-row"><input type="checkbox" name="contribution" value="${label}"><span>${label}</span></label>`).join('')}
        </div>

        <div class="conditional-panel">
          <label class="field-label" for="participation-note">Anything you would like us to know? <span class="small-text">(optional)</span></label>
          <textarea class="text-field" id="participation-note" maxlength="1200" placeholder="A short answer is enough…"></textarea>
          <label class="consent-row">
            <input type="checkbox" id="participation-consent">
            <span>I agree to receive messages about the categories I selected. I understand that I can unsubscribe at any time.</span>
          </label>
        </div>

        <div id="participation-feedback" aria-live="polite"></div>
        <div class="action-row">
          <button class="btn btn-quiet" id="participation-skip" type="button">No thanks — finish</button>
          <button class="btn btn-primary" id="participation-submit" type="button">Save my participation preferences</button>
        </div>
      </div>
      <div id="no-interest-actions" class="action-row"><a class="btn btn-secondary" href="https://www.lepotager.org/">Finish without joining</a></div>`;

    document.querySelectorAll('input[name="interest"]').forEach((input) => input.addEventListener('change', updateParticipationVisibility));
    document.querySelectorAll('.participation-option input').forEach((input) => input.addEventListener('change', updateChoiceVisuals));
    document.getElementById('participation-submit').addEventListener('click', submitParticipation);
    document.getElementById('participation-skip').addEventListener('click', () => {
      document.getElementById('participation-fields').hidden = true;
      document.getElementById('no-interest-actions').hidden = false;
    });
  }

  function updateParticipationVisibility() {
    const selected = [...document.querySelectorAll('input[name="interest"]:checked')].map((input) => input.value);
    const fields = document.getElementById('participation-fields');
    const noInterest = document.getElementById('no-interest-actions');
    fields.hidden = selected.length === 0;
    noInterest.hidden = selected.length > 0;
    document.getElementById('research-details').hidden = !selected.some((value) => ['research', 'codesign'].includes(value));
    document.getElementById('beta-details').hidden = !selected.includes('beta');
    document.getElementById('contributor-details').hidden = !selected.some((value) => ['opensource', 'accessibility', 'translation', 'professional'].includes(value));
  }

  async function submitParticipation() {
    const feedback = document.getElementById('participation-feedback');
    const button = document.getElementById('participation-submit');
    const interests = [...document.querySelectorAll('input[name="interest"]:checked')].map((input) => input.value);
    const email = document.getElementById('participation-email').value.trim();
    const consent = document.getElementById('participation-consent').checked;

    if (interests.length === 0 || !email || !consent) {
      feedback.innerHTML = '<div class="notice is-error">Choose at least one category, enter your email address, and confirm your consent.</div>';
      return;
    }

    const details = {
      research_activities: [...document.querySelectorAll('input[name="research_activity"]:checked')].map((input) => input.value),
      platforms: [...document.querySelectorAll('input[name="platform"]:checked')].map((input) => input.value),
      contributions: [...document.querySelectorAll('input[name="contribution"]:checked')].map((input) => input.value),
      note: document.getElementById('participation-note').value.trim()
    };

    button.disabled = true;
    feedback.innerHTML = '';
    try {
      await api('participate', {
        token: state.token,
        email,
        preferred_name: document.getElementById('participation-name').value.trim(),
        preferred_language: document.getElementById('participation-language').value,
        interests,
        details,
        link_to_response: false
      });
      state.contactSubmitted = true;
      saveLocalState();
      renderParticipationForm();
    } catch (error) {
      feedback.innerHTML = `<div class="notice is-error">${escapeHtml(error.message)}</div>`;
      button.disabled = false;
    }
  }

  function openPauseModal(options = {}) {
    lastFocusedElement = document.activeElement;
    pauseFeedback.innerHTML = '';
    pauseResumeMode = options.advanced ? 'next' : 'current';
    const note = document.getElementById('device-save-note');
    if (note) {
      if (pauseResumeMode === 'next') {
        note.textContent = 'Your answer has been saved. When you return, you will continue with the next question.';
      } else if (options.partial) {
        note.textContent = 'Your partial input and your place have been saved. When you return, this question will be waiting for you.';
      } else {
        note.textContent = 'Your place has been saved. When you return, you will come back to this question — no answer is required before taking a break.';
      }
    }
    pauseModal.hidden = false;
    pauseClose.focus();
  }

  function closePauseModal() {
    pauseModal.hidden = true;
    if (lastFocusedElement) lastFocusedElement.focus();
  }

  function reminderDateFromChoice() {
    const choice = reminderChoice.value;
    if (choice === 'none') return null;
    if (choice === 'custom') {
      if (!customReminder.value) return null;
      return new Date(customReminder.value).toISOString();
    }
    const date = new Date();
    if (choice === 'tomorrow') date.setDate(date.getDate() + 1);
    if (choice === 'three_days') date.setDate(date.getDate() + 3);
    if (choice === 'week') date.setDate(date.getDate() + 7);
    date.setHours(10, 0, 0, 0);
    return date.toISOString();
  }

  async function sendPauseEmail() {
    const email = pauseEmail.value.trim();
    if (!email) {
      pauseFeedback.innerHTML = '<div class="notice is-error">Enter an email address for the private resume link.</div>';
      return;
    }
    if (reminderChoice.value === 'custom' && !customReminder.value) {
      pauseFeedback.innerHTML = '<div class="notice is-error">Choose a reminder date and time.</div>';
      return;
    }
    pauseSend.disabled = true;
    pauseFeedback.innerHTML = '';
    try {
      const synced = await syncLocalAnswers();
      if (!synced) {
        throw new Error('Your progress is safe on this device, but the server could not be reached. Try again before requesting a cross-device resume link.');
      }
      await api('pause', { token: state.token, email, reminder_at: reminderDateFromChoice() });
      const resumeMessage = pauseResumeMode === 'next'
        ? 'Your private resume link has been sent. You will resume with the next question.'
        : 'Your private resume link has been sent. You will resume on the question where you stopped.';
      pauseFeedback.innerHTML = `<div class="notice is-success">${resumeMessage}</div>`;
    } catch (error) {
      pauseFeedback.innerHTML = `<div class="notice is-error">${escapeHtml(error.message)}</div>`;
    } finally {
      pauseSend.disabled = false;
    }
  }

  async function deleteSavedProgress() {
    const confirmed = window.confirm('Delete this unfinished questionnaire and all of its saved answers? This cannot be undone.');
    if (!confirmed) return;
    try {
      if (state.token) await api('delete', { token: state.token });
    } catch (_) {
      // Local deletion still proceeds so the participant regains control immediately.
    }
    clearLocalState();
    state = { token: null, answers: {}, current: 'welcome', completed: false, coreSubmitted: false, optionalGroup: null, contactSubmitted: false, orderSeed: null, personalSummary: null };
    closePauseModal();
    render();
  }

  async function resumeFromServer(token) {
    try {
      setBusy(true);
      const result = await api('load', { token });
      state.token = token;
      state.answers = result.answers || state.answers || {};
      state.current = result.session.current_step || state.current || 'age_eligibility';
      state.coreSubmitted = result.session.status === 'completed';
      state.completed = result.session.status === 'completed' && result.session.current_step === 'completed';
      state.optionalGroup = state.current === 'optional_hub' ? null : state.optionalGroup;
      state.orderSeed = result.session.id;
      if (state.completed) state.current = 'completed';
      saveLocalState();
      const url = new URL(window.location.href);
      url.searchParams.delete('resume');
      history.replaceState({}, '', url);
    } catch (_) {
      setSaveStatus('Could not load the server copy. Using the saved copy on this device when available.', 'warning');
      if (!state.current) state.current = 'welcome';
    } finally {
      setBusy(false);
    }
  }

  async function syncLocalAnswers() {
    if (!state.token || state.completed) return false;
    try {
      await api('sync', {
        token: state.token,
        answers: state.answers,
        current_step: state.current,
        primary_path: primaryPathKey(),
        secondary_path: secondaryPathKey()
      });
      setSaveStatus('Saved securely');
      return true;
    } catch (_) {
      setSaveStatus('Saved on this device — server sync pending', 'warning');
      return false;
    }
  }

  function ensureValidCurrent() {
    if (['welcome', 'readiness', 'come_back_later', 'consent', 'review', 'core_review', 'optional_hub', 'completed', 'ineligible'].includes(state.current)) return;

    const flow = buildFlow();
    if (flow.some((question) => question.id === state.current)) return;

    if (!state.coreSubmitted) {
      const core = buildCoreFlow();
      const firstUnanswered = core.find((question) => !optionalQuestionAnswered(question));
      state.current = firstUnanswered?.id || 'core_review';
      return;
    }

    state.current = 'optional_hub';
    state.optionalGroup = null;
  }

  function render() {
    ensureValidCurrent();
    window.scrollTo({ top: 0, behavior: 'smooth' });
    if (state.current === 'welcome') return renderWelcome();
    if (state.current === 'readiness') return renderReadiness();
    if (state.current === 'come_back_later') return renderComeBackLater();
    if (state.current === 'consent') return renderConsent();
    if (state.current === 'ineligible') return renderIneligible();
    if (state.current === 'core_review') return renderCoreReview();
    if (state.current === 'optional_hub') return renderOptionalHub();
    if (state.current === 'review') return state.coreSubmitted ? renderOptionalHub() : renderCoreReview();
    if (state.current === 'completed' || state.completed) return renderCompleted();
    const question = questionById(state.current);
    if (question) return renderQuestion(question);
    state.current = 'welcome';
    renderWelcome();
  }

  if (pauseButton) pauseButton.addEventListener('click', () => openPauseModal());
  pauseClose.addEventListener('click', closePauseModal);
  pauseDeviceOnly.addEventListener('click', closePauseModal);
  pauseSend.addEventListener('click', sendPauseEmail);
  deleteProgress.addEventListener('click', deleteSavedProgress);
  reminderChoice.addEventListener('change', () => { customReminderWrap.hidden = reminderChoice.value !== 'custom'; });
  pauseModal.addEventListener('click', (event) => { if (event.target === pauseModal) closePauseModal(); });
  document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && !pauseModal.hidden) closePauseModal(); });
  window.addEventListener('online', syncLocalAnswers);

  async function init() {
    const params = new URLSearchParams(window.location.search);

    if (params.get('fresh') === '1') {
      clearLocalState();
      state = {
        token: null,
        answers: {},
        current: 'welcome',
        completed: false,
        coreSubmitted: false,
        optionalGroup: null,
        contactSubmitted: false,
        orderSeed: null,
        personalSummary: null
      };
      params.delete('fresh');
      const freshUrl = new URL(window.location.href);
      freshUrl.searchParams.delete('fresh');
      history.replaceState({}, '', freshUrl);
    } else {
      loadLocalState();
    }

    const resumeToken = params.get('resume');
    const token = resumeToken || state.token;

    if (resumeToken && resumeToken !== state.token) {
      state.contactSubmitted = false;
    }

    // Render immediately. A slow or unavailable database must never leave the
    // questionnaire as a blank page while a saved session is being checked.
    render();

    if (token) {
      await resumeFromServer(token);
      render();
    }

    if (state.token && !state.completed) {
      syncLocalAnswers();
    }
  }

  init().catch((error) => {
    console.error('[ADHD questionnaire boot]', error);
    try {
      state.current = state.current || 'welcome';
      render();
      setSaveStatus('The server could not be reached. The questionnaire is still available on this device.', 'warning');
    } catch (_) {
      body.innerHTML = `
        <div class="question-wrap">
          <p class="eyebrow">Temporary loading problem</p>
          <h1>The questionnaire could not initialise</h1>
          <p class="lead">Please reload this page. If the problem continues, open <a href="?fresh=1">a fresh local session</a>.</p>
        </div>`;
    }
  });
})();
