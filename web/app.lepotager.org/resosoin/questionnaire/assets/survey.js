(() => {
  const app = document.getElementById('survey-app');
  if (!app || !document.getElementById('landing')) return;

  const storageKey = 'resosoinSurveyToken';
  const audienceKey = 'resosoinSurveyAudience';
  const prefillAudience = window.RESOSOIN_PREFILL_AUDIENCE || '';
  const landing = document.getElementById('landing');
  const questionnaire = document.getElementById('questionnaire');
  const done = document.getElementById('done');
  const form = document.getElementById('question-form');
  const title = document.getElementById('question-title');
  const help = document.getElementById('question-help');
  const optionsHost = document.getElementById('question-options');
  const audienceLabel = document.getElementById('audience-label');
  const count = document.getElementById('question-count');
  const progress = document.getElementById('progress-bar');
  const backButton = document.getElementById('back-button');
  const nextButton = document.getElementById('next-button');
  const deleteButton = document.getElementById('delete-button');
  const saveStatus = document.getElementById('save-status');
  const conceptCard = document.getElementById('concept-card');
  const resumeBox = document.getElementById('resume-box');
  const resumeButton = document.getElementById('resume-button');
  const resumeLabel = document.getElementById('resume-label');
  const pilotLink = document.getElementById('pilot-link');

  let catalog = null;
  let token = localStorage.getItem(storageKey) || '';
  let session = null;
  let questions = [];
  let index = 0;
  let busy = false;

  async function jsonFetch(url, body, authToken = token) {
    const headers = {'Content-Type': 'application/json'};
    if (authToken) headers.Authorization = `Bearer ${authToken}`;
    const response = await fetch(url, {
      method: 'POST',
      headers,
      credentials: 'same-origin',
      body: JSON.stringify(body),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok || data.ok === false) {
      const error = new Error(data.error || 'Une erreur est survenue.');
      error.status = response.status;
      error.data = data;
      throw error;
    }
    return data;
  }

  async function loadCatalog() {
    const response = await fetch('questions.json', {cache: 'no-store'});
    if (!response.ok) throw new Error('Impossible de charger le questionnaire.');
    catalog = await response.json();
  }

  function audienceName(audience) {
    return catalog?.[audience]?.label || audience;
  }

  function setBusy(value) {
    busy = value;
    nextButton.disabled = value;
    backButton.disabled = value;
    deleteButton.disabled = value;
  }

  function setStatus(message, isError = false) {
    saveStatus.textContent = message;
    saveStatus.style.color = isError ? '#7a3535' : '';
  }

  function chooseQuestions(audience) {
    questions = catalog?.[audience]?.questions || [];
  }

  function findStep(questionId) {
    const found = questions.findIndex((question) => question.id === questionId);
    return found >= 0 ? found : 0;
  }

  function currentQuestion() {
    return questions[index];
  }

  function makeOption(question, key, label, selected) {
    const option = document.createElement('label');
    option.className = 'option';
    const input = document.createElement('input');
    input.type = question.type === 'multi' ? 'checkbox' : 'radio';
    input.name = 'answer';
    input.value = key;
    input.checked = selected;
    const span = document.createElement('span');
    span.textContent = label;
    option.append(input, span);
    return option;
  }

  function renderScale(question, answer) {
    const scale = document.createElement('div');
    scale.className = `scale ${question.max === 5 ? 'scale-5' : ''}`;
    for (let value = question.min; value <= question.max; value += 1) {
      const label = document.createElement('label');
      const input = document.createElement('input');
      input.type = 'radio';
      input.name = 'answer';
      input.value = String(value);
      input.checked = Number(answer) === value;
      const span = document.createElement('span');
      span.textContent = String(value);
      label.append(input, span);
      scale.append(label);
    }
    const labels = document.createElement('div');
    labels.className = 'scale-labels';
    labels.innerHTML = `<span>${question.min_label || ''}</span><span>${question.max_label || ''}</span>`;
    optionsHost.append(scale, labels);
  }

  function renderQuestion() {
    const question = currentQuestion();
    if (!question) return;
    landing.hidden = true;
    done.hidden = true;
    questionnaire.hidden = false;
    audienceLabel.textContent = audienceName(session.audience);
    count.textContent = `${index + 1} / ${questions.length}`;
    progress.style.width = `${((index + 1) / questions.length) * 100}%`;
    conceptCard.hidden = question.phase !== 'concept';
    title.textContent = question.title;
    help.textContent = question.help || '';
    help.hidden = !question.help;
    optionsHost.innerHTML = '';
    setStatus('');

    const answer = session.answers?.[question.id];
    if (question.type === 'scale') {
      renderScale(question, answer);
    } else {
      const selectedValues = Array.isArray(answer) ? answer : [answer];
      const list = document.createElement('div');
      list.className = 'options';
      Object.entries(question.options || {}).forEach(([key, label]) => {
        list.append(makeOption(question, key, label, selectedValues.includes(key)));
      });
      if (question.type === 'multi') {
        list.addEventListener('change', (event) => {
          const changed = event.target;
          if (!(changed instanceof HTMLInputElement) || changed.type !== 'checkbox') return;
          const exclusive = ['none', 'nothing'];
          const checkboxes = [...list.querySelectorAll('input[type="checkbox"]')];
          if (changed.checked && exclusive.includes(changed.value)) {
            checkboxes.forEach((input) => {
              if (input !== changed) input.checked = false;
            });
          } else if (changed.checked) {
            checkboxes.forEach((input) => {
              if (exclusive.includes(input.value)) input.checked = false;
            });
          }
        });
      }
      optionsHost.append(list);
    }

    backButton.disabled = index === 0;
    nextButton.textContent = index === questions.length - 1 ? 'Envoyer mes réponses' : 'Suivant';
    session.current_step = question.id;
    history.replaceState(null, '', `?audience=${encodeURIComponent(session.audience)}#${question.id}`);
  }

  function readAnswer(question) {
    if (question.type === 'multi') {
      return [...form.querySelectorAll('input[name="answer"]:checked')].map((input) => input.value);
    }
    const checked = form.querySelector('input[name="answer"]:checked');
    if (!checked) return null;
    return question.type === 'scale' ? Number(checked.value) : checked.value;
  }

  function validAnswer(question, answer) {
    if (!question.required) return true;
    if (question.type === 'multi') return Array.isArray(answer) && answer.length > 0;
    return answer !== null && answer !== undefined && answer !== '';
  }

  async function saveCurrent(nextStepId) {
    const question = currentQuestion();
    const answer = readAnswer(question);
    if (!validAnswer(question, answer)) {
      setStatus('Choisissez une réponse avant de continuer.', true);
      return false;
    }
    setBusy(true);
    setStatus('Enregistrement…');
    try {
      await jsonFetch('api.php', {
        action: 'save',
        question_id: question.id,
        answer,
        current_step: nextStepId || question.id,
      });
      session.answers[question.id] = answer;
      setStatus('Enregistré.');
      return true;
    } catch (error) {
      setStatus(error.message, true);
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function submitSurvey() {
    setBusy(true);
    setStatus('Envoi…');
    try {
      await jsonFetch('api.php', {action: 'submit'});
      session.status = 'submitted';
      localStorage.removeItem(storageKey);
      localStorage.removeItem(audienceKey);
      questionnaire.hidden = true;
      done.hidden = false;
      const role = session.audience === 'doctor' ? 'professionnel de santé' : 'patient';
      pilotLink.href = `mailto:contact@lepotager.org?subject=${encodeURIComponent(`Pilote RésoSoin — ${role}`)}`;
    } catch (error) {
      if (error.data?.missing?.length) {
        const missingIndex = questions.findIndex((q) => error.data.missing.includes(q.id));
        if (missingIndex >= 0) {
          index = missingIndex;
          renderQuestion();
        }
      }
      setStatus(error.message, true);
    } finally {
      setBusy(false);
    }
  }

  async function next(event) {
    event.preventDefault();
    if (busy) return;
    const last = index === questions.length - 1;
    const nextId = last ? currentQuestion().id : questions[index + 1].id;
    if (!(await saveCurrent(nextId))) return;
    if (last) {
      await submitSurvey();
      return;
    }
    index += 1;
    renderQuestion();
  }

  async function start(audience) {
    if (busy) return;
    const data = await jsonFetch('api.php', {action: 'start', audience}, '');
    token = data.token;
    session = data.session;
    localStorage.setItem(storageKey, token);
    localStorage.setItem(audienceKey, audience);
    chooseQuestions(audience);
    index = findStep(session.current_step);
    renderQuestion();
  }

  async function resume() {
    if (!token) return false;
    try {
      const data = await jsonFetch('api.php', {action: 'resume'});
      session = data.session;
      chooseQuestions(session.audience);
      if (session.status === 'submitted') {
        localStorage.removeItem(storageKey);
        localStorage.removeItem(audienceKey);
        return false;
      }
      index = findStep(session.current_step);
      resumeLabel.textContent = ` ${audienceName(session.audience)}.`;
      resumeBox.hidden = false;
      return true;
    } catch (error) {
      if (error.status === 401) {
        localStorage.removeItem(storageKey);
        localStorage.removeItem(audienceKey);
        token = '';
      }
      return false;
    }
  }

  async function deleteAnswers() {
    if (!token || busy) return;
    if (!window.confirm('Supprimer définitivement toutes les réponses de cette session ?')) return;
    setBusy(true);
    try {
      await jsonFetch('api.php', {action: 'delete'});
      localStorage.removeItem(storageKey);
      localStorage.removeItem(audienceKey);
      token = '';
      session = null;
      questionnaire.hidden = true;
      done.hidden = true;
      landing.hidden = false;
      resumeBox.hidden = true;
      history.replaceState(null, '', './');
    } catch (error) {
      setStatus(error.message, true);
    } finally {
      setBusy(false);
    }
  }

  async function boot() {
    try {
      await loadCatalog();
      document.querySelectorAll('[data-start]').forEach((button) => {
        button.addEventListener('click', () => start(button.dataset.start));
      });
      form.addEventListener('submit', next);
      backButton.addEventListener('click', () => {
        if (index > 0 && !busy) {
          index -= 1;
          renderQuestion();
        }
      });
      deleteButton.addEventListener('click', deleteAnswers);
      resumeButton.addEventListener('click', () => renderQuestion());

      const hasResume = await resume();
      if (!hasResume && prefillAudience && ['doctor', 'patient'].includes(prefillAudience)) {
        const target = document.querySelector(`[data-start="${prefillAudience}"]`);
        target?.focus();
      }
    } catch (error) {
      landing.innerHTML = `<p class="eyebrow">Étude RésoSoin</p><h1>Questionnaire indisponible.</h1><div class="notice error">${error.message}</div>`;
    }
  }

  boot();
})();
