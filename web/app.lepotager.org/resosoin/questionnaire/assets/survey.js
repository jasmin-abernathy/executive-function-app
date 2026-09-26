(() => {
  "use strict";

  const app = document.getElementById("survey-app");
  const landing = document.getElementById("landing");
  if (!app || !landing) return;

  const core = window.ResoSoinSurveyCore;
  if (!core) {
    landing.innerHTML = "<p class=\"eyebrow\">Étude RésoSoin</p><h1>Questionnaire indisponible.</h1>";
    return;
  }

  const registryKey = "resosoinSurveyDraftsV2";
  const legacyTokenKey = "resosoinSurveyToken";
  const legacyAudienceKey = "resosoinSurveyAudience";
  const prefillAudience = window.RESOSOIN_PREFILL_AUDIENCE || "";
  const recruitmentSource = window.RESOSOIN_RECRUITMENT_SOURCE || "direct";

  const questionnaire = document.getElementById("questionnaire");
  const done = document.getElementById("done");
  const form = document.getElementById("question-form");
  const title = document.getElementById("question-title");
  const help = document.getElementById("question-help");
  const optionsHost = document.getElementById("question-options");
  const audienceLabel = document.getElementById("audience-label");
  const count = document.getElementById("question-count");
  const progress = document.getElementById("progress-bar");
  const backButton = document.getElementById("back-button");
  const nextButton = document.getElementById("next-button");
  const deleteButton = document.getElementById("delete-button");
  const saveStatus = document.getElementById("save-status");
  const conceptCard = document.getElementById("concept-card");
  const resumeBox = document.getElementById("resume-box");
  const resumeList = document.getElementById("resume-list");
  const pilotLink = document.getElementById("pilot-link");
  const adultCheck = document.getElementById("adult-check");
  const consentCheck = document.getElementById("consent-check");
  const landingStatus = document.getElementById("landing-status");
  const doneDeleteButton = document.getElementById("done-delete-button");
  const professionalDeclaration = document.getElementById("professional-declaration");
  const professionalFamilyName = document.getElementById("professional-family-name");
  const professionalRpps = document.getElementById("professional-rpps");
  const professionalCertification = document.getElementById("professional-certification");
  const professionalVerifyButton = document.getElementById("professional-verify-button");
  const professionalVerificationStatus = document.getElementById("professional-verification-status");
  const startButtons = [...document.querySelectorAll("[data-start]")];

  let catalog = null;
  let registry = [];
  let token = "";
  let session = null;
  let questions = [];
  let index = 0;
  let busy = false;
  let dirty = false;
  let verifyingProfessional = false;

  function now() {
    return Date.now();
  }

  function randomId() {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
      return window.crypto.randomUUID();
    }
    return String(now()) + "-" + Math.random().toString(16).slice(2);
  }

  function loadRegistry() {
    try {
      registry = core.normalizeRegistry(
        JSON.parse(localStorage.getItem(registryKey) || "[]")
      );
    } catch (_) {
      registry = [];
    }

    const legacyToken = localStorage.getItem(legacyTokenKey) || "";
    const legacyAudience = localStorage.getItem(legacyAudienceKey) || "";
    if (
      legacyToken
      && (legacyAudience === "doctor" || legacyAudience === "patient")
      && !registry.some((item) => item.token === legacyToken)
    ) {
      registry = core.upsertDraft(registry, {
        id: randomId(),
        token: legacyToken,
        audience: legacyAudience,
        status: "active",
        createdAt: now(),
        updatedAt: now(),
      });
    }

    localStorage.removeItem(legacyTokenKey);
    localStorage.removeItem(legacyAudienceKey);
    saveRegistry();
  }

  function saveRegistry() {
    localStorage.setItem(registryKey, JSON.stringify(registry));
  }

  function updateRegistry(status) {
    if (!token || !session) return;
    const existing = registry.find((item) => item.token === token);
    registry = core.upsertDraft(registry, {
      id: existing?.id || randomId(),
      token,
      audience: session.audience,
      status: status || session.status || "active",
      createdAt: existing?.createdAt || now(),
      updatedAt: now(),
    });
    saveRegistry();
    renderDrafts();
  }

  function removeRegistryToken(value) {
    registry = core.removeDraft(registry, value);
    saveRegistry();
    renderDrafts();
  }

  async function jsonFetch(url, body, authToken = token) {
    const headers = {"Content-Type": "application/json"};
    if (authToken) headers.Authorization = "Bearer " + authToken;

    const response = await fetch(url, {
      method: "POST",
      headers,
      credentials: "same-origin",
      body: JSON.stringify(body),
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok || data.ok === false) {
      const error = new Error(data.error || "Une erreur est survenue.");
      error.status = response.status;
      error.data = data;
      throw error;
    }
    return data;
  }

  async function loadCatalog() {
    const response = await fetch("questions.json", {cache: "no-store"});
    if (!response.ok) {
      throw new Error("Impossible de charger le questionnaire.");
    }
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

  function setLandingBusy(value) {
    startButtons.forEach((button) => {
      button.disabled = value;
    });
    if (professionalVerifyButton) {
      professionalVerifyButton.disabled =
        value || verifyingProfessional;
    }
  }

  function setStatus(message, isError = false) {
    saveStatus.textContent = message;
    saveStatus.style.color = isError ? "#7a3535" : "";
  }

  function setLandingStatus(message, isError = false) {
    landingStatus.textContent = message;
    landingStatus.style.color = isError ? "#7a3535" : "";
  }

  function setProfessionalVerificationStatus(
    message,
    isError = false
  ) {
    if (!professionalVerificationStatus) return;
    professionalVerificationStatus.textContent = message;
    professionalVerificationStatus.style.color =
      isError ? "#7a3535" : "";
  }

  function refreshQuestions() {
    const all = catalog?.[session?.audience]?.questions || [];
    questions = core.visibleQuestions(all, session?.answers || {});
  }

  function findStep(questionId) {
    const found = questions.findIndex((question) => question.id === questionId);
    return found >= 0 ? found : 0;
  }

  function currentQuestion() {
    return questions[index];
  }

  function makeOption(question, key, label, selected) {
    const option = document.createElement("label");
    option.className = "option";

    const input = document.createElement("input");
    input.type = question.type === "multi" ? "checkbox" : "radio";
    input.name = "answer";
    input.value = key;
    input.checked = selected;

    const span = document.createElement("span");
    span.textContent = label;

    option.append(input, span);
    return option;
  }

  function buildFieldset(question) {
    const fieldset = document.createElement("fieldset");
    fieldset.className = "question-fieldset";
    fieldset.setAttribute("aria-describedby", "question-help save-status");

    const legend = document.createElement("legend");
    legend.className = "sr-only";
    legend.textContent = question.title;
    fieldset.append(legend);

    return fieldset;
  }

  function renderScale(question, answer, fieldset) {
    const scale = document.createElement("div");
    scale.className = "scale " + (question.max === 5 ? "scale-5" : "");

    for (let value = question.min; value <= question.max; value += 1) {
      const label = document.createElement("label");
      const input = document.createElement("input");
      input.type = "radio";
      input.name = "answer";
      input.value = String(value);
      input.checked = Number(answer) === value;

      const span = document.createElement("span");
      span.textContent = String(value);
      label.append(input, span);
      scale.append(label);
    }

    fieldset.append(scale);

    if (question.allow_na) {
      fieldset.append(
        makeOption(
          question,
          "not_applicable",
          question.na_label || "Non concerné",
          answer === "not_applicable"
        )
      );
    }

    const labels = document.createElement("div");
    labels.className = "scale-labels";

    const left = document.createElement("span");
    left.textContent = question.min_label || "";
    const right = document.createElement("span");
    right.textContent = question.max_label || "";

    labels.append(left, right);
    fieldset.append(labels);
  }

  function applyMultiRules(question, list, changed) {
    const exclusive = Array.isArray(question.exclusive)
      ? question.exclusive
      : ["none", "nothing"];
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

    const selected = checkboxes.filter((input) => input.checked);
    const maxChoices = Number(question.max || checkboxes.length);
    if (selected.length > maxChoices) {
      changed.checked = false;
      setStatus(
        "Sélectionnez au maximum " + maxChoices + " réponses.",
        true
      );
      return;
    }

    if (saveStatus.textContent.startsWith("Sélectionnez au maximum")) {
      setStatus("");
    }
  }

  function renderQuestion(questionId) {
    refreshQuestions();
    if (questions.length === 0) return;

    index = findStep(questionId || session.current_step);
    const question = currentQuestion();
    if (!question) return;

    landing.hidden = true;
    done.hidden = true;
    questionnaire.hidden = false;

    audienceLabel.textContent = audienceName(session.audience);
    count.textContent = "Question " + (index + 1) + " sur " + questions.length;
    progress.style.width =
      (((index + 1) / questions.length) * 100) + "%";

    const previousQuestion = index > 0 ? questions[index - 1] : null;
    conceptCard.hidden = !(
      question.phase === "concept"
      && (!previousQuestion || previousQuestion.phase !== "concept")
    );

    title.textContent = question.title;
    help.textContent = question.help || "";
    help.hidden = !question.help;
    optionsHost.innerHTML = "";
    setStatus("");
    dirty = false;

    const answer = session.answers?.[question.id];
    const fieldset = buildFieldset(question);

    if (question.type === "scale") {
      renderScale(question, answer, fieldset);
    } else {
      const selectedValues = Array.isArray(answer) ? answer : [answer];
      const list = document.createElement("div");
      list.className = "options";

      Object.entries(question.options || {}).forEach(([key, label]) => {
        list.append(
          makeOption(
            question,
            key,
            label,
            selectedValues.includes(key)
          )
        );
      });

      if (question.type === "multi") {
        list.addEventListener("change", (event) => {
          const changed = event.target;
          if (
            changed instanceof HTMLInputElement
            && changed.type === "checkbox"
          ) {
            applyMultiRules(question, list, changed);
          }
          dirty = true;
        });
      }

      fieldset.append(list);
    }

    fieldset.addEventListener("change", () => {
      dirty = true;
    });

    optionsHost.append(fieldset);
    backButton.disabled = index === 0;
    nextButton.textContent =
      index === questions.length - 1
        ? "Envoyer mes réponses"
        : "Suivant";

    session.current_step = question.id;
    updateRegistry(session.status);
    history.replaceState(
      null,
      "",
      "?audience="
        + encodeURIComponent(session.audience)
        + "&source="
        + encodeURIComponent(recruitmentSource)
        + "#"
        + question.id
    );

    window.requestAnimationFrame(() => {
      title.focus({preventScroll: true});
      title.scrollIntoView({block: "start", behavior: "smooth"});
    });
  }

  function readAnswer(question) {
    if (question.type === "multi") {
      return [...form.querySelectorAll('input[name="answer"]:checked')]
        .map((input) => input.value);
    }

    const checked = form.querySelector('input[name="answer"]:checked');
    if (!checked) return null;

    if (
      question.type === "scale"
      && checked.value !== "not_applicable"
    ) {
      return Number(checked.value);
    }

    return checked.value;
  }

  function validAnswer(question, answer) {
    if (!question.required) return true;

    if (question.type === "multi") {
      return Array.isArray(answer)
        && answer.length > 0
        && answer.length <= Number(question.max || answer.length);
    }

    return answer !== null
      && answer !== undefined
      && answer !== "";
  }

  async function saveCurrent(direction) {
    const question = currentQuestion();
    if (!question) return false;

    const answer = readAnswer(question);
    if (!validAnswer(question, answer)) {
      setStatus("Choisissez une réponse avant de continuer.", true);
      return false;
    }

    setBusy(true);
    setStatus("Enregistrement…");

    try {
      const data = await jsonFetch("api.php", {
        action: "save",
        question_id: question.id,
        answer,
        direction,
      });

      session.answers = data.answers || {};
      session.current_step = data.current_step || question.id;
      dirty = false;
      updateRegistry(session.status);
      setStatus("Enregistré.");
      return true;
    } catch (error) {
      setStatus(error.message, true);
      return false;
    } finally {
      setBusy(false);
    }
  }

  function showDone() {
    landing.hidden = true;
    questionnaire.hidden = true;
    done.hidden = false;

    const role = session.audience === "doctor"
      ? "professionnel de santé"
      : "patient";

    pilotLink.href =
      "mailto:contact@lepotager.org?subject="
      + encodeURIComponent("Pilote RésoSoin — " + role);

    updateRegistry("submitted");
  }

  async function submitSurvey() {
    setBusy(true);
    setStatus("Envoi…");

    try {
      await jsonFetch("api.php", {action: "submit"});
      session.status = "submitted";
      dirty = false;
      showDone();
    } catch (error) {
      if (error.data?.missing?.length) {
        refreshQuestions();
        const missingIndex = questions.findIndex(
          (question) => error.data.missing.includes(question.id)
        );
        if (missingIndex >= 0) {
          index = missingIndex;
          renderQuestion(questions[missingIndex].id);
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

    refreshQuestions();
    const question = currentQuestion();
    const last = question
      && questions[questions.length - 1]?.id === question.id;

    if (!(await saveCurrent("forward"))) return;

    if (last) {
      await submitSurvey();
      return;
    }

    renderQuestion(session.current_step);
  }

  async function goBack() {
    if (busy || index === 0) return;

    const question = currentQuestion();
    const answer = readAnswer(question);
    const hasStoredAnswer = Object.prototype.hasOwnProperty.call(
      session.answers || {},
      question.id
    );

    if (
      !dirty
      && !hasStoredAnswer
      && !validAnswer(question, answer)
    ) {
      refreshQuestions();
      const previous = questions[index - 1];
      if (previous) renderQuestion(previous.id);
      return;
    }

    if (!(await saveCurrent("back"))) return;
    renderQuestion(session.current_step);
  }

  async function verifyProfessionalAndStart() {
    if (busy || verifyingProfessional) return;

    if (!adultCheck?.checked) {
      setLandingStatus(
        "Cette étude est réservée aux personnes de 18 ans ou plus.",
        true
      );
      adultCheck?.focus();
      return;
    }

    if (!consentCheck?.checked) {
      setLandingStatus(
        "Votre accord est nécessaire pour commencer.",
        true
      );
      consentCheck?.focus();
      return;
    }

    const familyName =
      professionalFamilyName?.value.trim() || "";
    const rpps =
      (professionalRpps?.value || "").replace(/\D/g, "");

    if (familyName.length < 2) {
      setProfessionalVerificationStatus(
        "Renseignez votre nom d’exercice.",
        true
      );
      professionalFamilyName?.focus();
      return;
    }

    if (!/^\d{11}$/.test(rpps)) {
      setProfessionalVerificationStatus(
        "Le numéro RPPS doit contenir 11 chiffres.",
        true
      );
      professionalRpps?.focus();
      return;
    }

    if (!professionalDeclaration?.checked) {
      setProfessionalVerificationStatus(
        "Cochez d’abord la déclaration de profession réglementée.",
        true
      );
      professionalDeclaration?.focus();
      return;
    }

    if (!professionalCertification?.checked) {
      setProfessionalVerificationStatus(
        "Confirmez que les informations correspondent à votre fiche professionnelle.",
        true
      );
      professionalCertification?.focus();
      return;
    }

    verifyingProfessional = true;
    setLandingBusy(true);
    setProfessionalVerificationStatus(
      "Recherche ponctuelle dans l’Annuaire Santé…"
    );

    try {
      const data = await jsonFetch(
        "verify-professional.php",
        {
          family_name: familyName,
          rpps,
          certification: true,
        },
        ""
      );

      setProfessionalVerificationStatus(
        (data.message || "Fiche professionnelle concordante.")
        + " Le nom et le RPPS ne sont pas enregistrés avec vos réponses."
      );

      verifyingProfessional = false;
      setLandingBusy(false);

      await start(
        "doctor",
        data.verification_token || ""
      );
    } catch (error) {
      setProfessionalVerificationStatus(
        error.message,
        true
      );
    } finally {
      verifyingProfessional = false;
      setLandingBusy(false);
    }
  }

  async function start(
    audience,
    professionalVerification = ""
  ) {
    if (busy) return;

    if (!adultCheck?.checked) {
      setLandingStatus(
        "Cette étude est réservée aux personnes de 18 ans ou plus.",
        true
      );
      return;
    }

    if (!consentCheck?.checked) {
      setLandingStatus(
        "Votre accord est nécessaire pour commencer.",
        true
      );
      return;
    }

    if (
      audience === "doctor"
      && !professionalDeclaration?.checked
    ) {
      setLandingStatus(
        "Déclarez exercer une profession de santé réglementée pour commencer.",
        true
      );
      professionalDeclaration?.focus();
      return;
    }

    busy = true;
    setLandingBusy(true);
    setLandingStatus("Création du questionnaire…");

    try {
      const data = await jsonFetch(
        "api.php",
        {
          action: "start",
          audience,
          adult: true,
          consent: true,
          source: recruitmentSource,
          professional_declaration:
            audience === "doctor"
            && Boolean(professionalDeclaration?.checked),
          professional_verification:
            professionalVerification,
        },
        ""
      );

      token = data.token;
      session = data.session;
      session.answers = session.answers || {};
      updateRegistry("active");
      setLandingStatus("");
      renderQuestion(session.current_step);
    } catch (error) {
      setLandingStatus(error.message, true);
    } finally {
      busy = false;
      setLandingBusy(false);
    }
  }

  async function resumeToken(value) {
    if (!value || busy) return false;

    token = value;
    try {
      const data = await jsonFetch(
        "api.php",
        {action: "resume"},
        value
      );
      session = data.session;
      session.answers = session.answers || {};
      updateRegistry(session.status);

      if (session.status === "submitted") {
        showDone();
        return true;
      }

      renderQuestion(session.current_step);
      return true;
    } catch (error) {
      if (error.status === 401) {
        removeRegistryToken(value);
      } else {
        setLandingStatus(error.message, true);
      }
      token = "";
      session = null;
      return false;
    }
  }

  async function deleteToken(value) {
    if (!value || busy) return;
    if (
      !window.confirm(
        "Supprimer définitivement toutes les réponses de cette session ?"
      )
    ) {
      return;
    }

    busy = true;
    try {
      await jsonFetch(
        "api.php",
        {action: "delete"},
        value
      );
      removeRegistryToken(value);

      if (token === value) {
        token = "";
        session = null;
        dirty = false;
        questionnaire.hidden = true;
        done.hidden = true;
        landing.hidden = false;
        history.replaceState(
          null,
          "",
          "./?source=" + encodeURIComponent(recruitmentSource)
        );
      }
    } catch (error) {
      setLandingStatus(error.message, true);
    } finally {
      busy = false;
    }
  }

  async function deleteAnswers() {
    if (!token) return;
    await deleteToken(token);
  }

  function renderDrafts() {
    if (!resumeBox || !resumeList) return;
    resumeList.innerHTML = "";

    const clean = core.normalizeRegistry(registry);
    resumeBox.hidden = clean.length === 0;

    clean.forEach((item) => {
      const row = document.createElement("div");
      row.className = "resume-item";

      const text = document.createElement("div");
      const strong = document.createElement("strong");
      strong.textContent = audienceName(item.audience);

      const small = document.createElement("small");
      const date = new Date(item.updatedAt);
      small.textContent =
        (item.status === "submitted" ? "Envoyé" : "Brouillon")
        + " · "
        + date.toLocaleString("fr-FR");

      text.append(strong, small);

      const actions = document.createElement("div");
      actions.className = "resume-actions";

      const resume = document.createElement("button");
      resume.type = "button";
      resume.className = "btn secondary";
      resume.textContent =
        item.status === "submitted"
          ? "Voir / supprimer"
          : "Reprendre";
      resume.addEventListener("click", () => resumeToken(item.token));

      const remove = document.createElement("button");
      remove.type = "button";
      remove.className = "btn quiet";
      remove.textContent = "Supprimer";
      remove.addEventListener("click", () => deleteToken(item.token));

      actions.append(resume, remove);
      row.append(text, actions);
      resumeList.append(row);
    });
  }

  async function boot() {
    try {
      await loadCatalog();
      loadRegistry();
      renderDrafts();

      startButtons.forEach((button) => {
        button.addEventListener(
          "click",
          () => start(button.dataset.start)
        );
      });

      professionalVerifyButton?.addEventListener(
        "click",
        verifyProfessionalAndStart
      );

      form.addEventListener("submit", next);
      backButton.addEventListener("click", goBack);
      deleteButton.addEventListener("click", deleteAnswers);
      doneDeleteButton?.addEventListener(
        "click",
        deleteAnswers
      );

      if (
        prefillAudience
        && ["doctor", "patient"].includes(prefillAudience)
      ) {
        const target = document.querySelector(
          '[data-start="' + prefillAudience + '"]'
        );
        target?.focus();
      }
    } catch (error) {
      landing.innerHTML =
        '<p class="eyebrow">Étude RésoSoin</p>'
        + "<h1>Questionnaire indisponible.</h1>"
        + '<div class="notice error">'
        + String(error.message || "Erreur technique.")
        + "</div>";
    }
  }

  window.addEventListener("beforeunload", (event) => {
    if (!dirty) return;
    event.preventDefault();
    event.returnValue = "";
  });

  boot();
})();