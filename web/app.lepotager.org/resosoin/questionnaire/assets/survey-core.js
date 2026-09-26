(function (root, factory) {
  const api = factory();
  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  } else {
    root.ResoSoinSurveyCore = api;
  }
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  function conditionMatches(condition, answers) {
    if (!condition || typeof condition !== 'object') return true;
    const questionId = String(condition.question || '');
    if (!questionId) return true;
    const answer = answers ? answers[questionId] : undefined;
    const op = String(condition.op || 'equals');

    if (op === 'contains') {
      return Array.isArray(answer) && answer.includes(condition.value);
    }

    if (op === 'in') {
      const values = Array.isArray(condition.values) ? condition.values : [];
      return values.includes(answer);
    }

    if (op === 'equals') {
      return answer === condition.value;
    }

    if (op === 'not_equals') {
      return answer !== condition.value;
    }

    return false;
  }

  function visibleQuestions(list, answers) {
    return (Array.isArray(list) ? list : []).filter(
      (question) => conditionMatches(question.show_if, answers || {})
    );
  }

  function normalizeRegistry(value) {
    if (!Array.isArray(value)) return [];
    const seen = new Set();
    const clean = [];
    for (const item of value) {
      if (!item || typeof item !== 'object') continue;
      const token = typeof item.token === 'string' ? item.token.trim() : '';
      const audience = item.audience === 'doctor' || item.audience === 'patient'
        ? item.audience
        : '';
      if (!token || !audience || seen.has(token)) continue;
      seen.add(token);
      clean.push({
        id: typeof item.id === 'string' && item.id ? item.id : token.slice(0, 12),
        token,
        audience,
        status: item.status === 'submitted' ? 'submitted' : 'active',
        createdAt: Number(item.createdAt || Date.now()),
        updatedAt: Number(item.updatedAt || Date.now()),
      });
    }
    return clean.sort((a, b) => b.updatedAt - a.updatedAt);
  }

  function upsertDraft(registry, draft) {
    const clean = normalizeRegistry(registry);
    const item = normalizeRegistry([draft])[0];
    if (!item) return clean;
    const next = clean.filter((entry) => entry.token !== item.token);
    next.unshift(item);
    return normalizeRegistry(next);
  }

  function removeDraft(registry, token) {
    return normalizeRegistry(registry).filter((entry) => entry.token !== token);
  }

  return {
    conditionMatches,
    visibleQuestions,
    normalizeRegistry,
    upsertDraft,
    removeDraft,
  };
});
