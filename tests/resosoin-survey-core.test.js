'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const core = require('../web/app.lepotager.org/resosoin/questionnaire/assets/survey-core.js');

const catalogPath = path.join(
  __dirname,
  '../web/app.lepotager.org/resosoin/questionnaire/questions.json'
);
const catalog = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));

const patient = catalog.patient.questions;
const doctor = catalog.doctor.questions;
const surveyJs = fs.readFileSync(
  path.join(
    __dirname,
    '../web/app.lepotager.org/resosoin/questionnaire/assets/survey.js'
  ),
  'utf8'
);

const questionnaireIndex = fs.readFileSync(
  path.join(
    __dirname,
    '../web/app.lepotager.org/resosoin/questionnaire/index.php'
  ),
  'utf8'
);
const apiPhp = fs.readFileSync(
  path.join(
    __dirname,
    '../web/app.lepotager.org/resosoin/questionnaire/api.php'
  ),
  'utf8'
);

assert.equal(
  doctor[0]?.id,
  'doctor_profession',
  'Professional survey must begin with the declared regulated profession'
);
assert.ok(
  doctor.findIndex((q) => q.id === 'doctor_automation')
    < doctor.findIndex((q) => q.id === 'doctor_ai_preference'),
  'Automation usefulness must be asked before AI preference'
);
assert.ok(
  doctor.some((q) => q.id === 'doctor_concept_blocker'),
  'Professional concept must measure the primary adoption blocker'
);
assert.equal(
  patient[0]?.id,
  'patient_booking_now',
  'Patient survey must begin with current booking channels'
);
assert.ok(
  patient.some((q) => q.id === 'patient_availability_difficulty'),
  'Patient survey must measure difficulty finding an available professional'
);
assert.ok(
  patient.some((q) => q.id === 'patient_concept_blocker'),
  'Patient concept must measure the primary blocker'
);
const patientAge = patient.find((q) => q.id === 'patient_age');
assert.equal(
  patientAge?.required,
  false,
  'Patient age band must be optional'
);
assert.match(
  questionnaireIndex,
  /data-start="doctor"/,
  'Professionals must be able to start without directory lookup'
);
assert.match(
  questionnaireIndex,
  /déclaration n’authentifie pas votre identité/i,
  'Self-declaration limitation must be visible'
);
assert.match(
  apiPhp,
  /professionalVerificationMethod = 'self_declared'/,
  'API must record self-declared professional sessions explicitly'
);
assert.doesNotMatch(
  apiPhp,
  /\$professionalVerified\s*=\s*true/,
  'Self-declaration or directory matching must never set identity verified'
);
assert.match(
  apiPhp,
  /survey_version_changed/,
  'Old active drafts must be rejected without silent rewriting'
);

let visible = core.visibleQuestions(patient, {
  patient_booking_now: ['phone'],
});
assert.equal(
  visible.some((q) => q.id === 'patient_doctolib_frequency'),
  false,
  'Doctolib frequency must be hidden when Doctolib is not used'
);

visible = core.visibleQuestions(patient, {
  patient_booking_now: ['doctolib', 'phone'],
});
assert.equal(
  visible.some((q) => q.id === 'patient_doctolib_frequency'),
  true,
  'Doctolib frequency must be visible when Doctolib is used'
);

visible = core.visibleQuestions(patient, {
  patient_booking_now: ['phone'],
  patient_app_preference: 'no',
});
assert.equal(
  visible.some((q) => q.id === 'patient_features'),
  false,
  'App features must be hidden after refusing the app'
);

visible = core.visibleQuestions(patient, {
  patient_booking_now: ['phone'],
  patient_app_preference: 'optional',
});
assert.equal(
  visible.some((q) => q.id === 'patient_features'),
  true,
  'App features must be visible when an optional app is acceptable'
);

let registry = [];
registry = core.upsertDraft(registry, {
  id: 'a',
  token: 'token-doctor-a-1234567890',
  audience: 'doctor',
  createdAt: 1,
  updatedAt: 1,
});
registry = core.upsertDraft(registry, {
  id: 'b',
  token: 'token-doctor-b-1234567890',
  audience: 'doctor',
  createdAt: 2,
  updatedAt: 2,
});
registry = core.upsertDraft(registry, {
  id: 'c',
  token: 'token-patient-c-123456789',
  audience: 'patient',
  createdAt: 3,
  updatedAt: 3,
});
assert.equal(registry.length, 3, 'Multiple drafts must coexist in one browser');
assert.equal(
  registry.filter((item) => item.audience === 'doctor').length,
  2,
  'Two doctor drafts must coexist'
);
registry = core.removeDraft(registry, 'token-doctor-a-1234567890');
assert.equal(registry.length, 2, 'Deleting one draft must preserve the others');

const doctorSatisfaction = doctor.find(
  (q) => q.id === 'doctor_satisfaction'
);
assert.equal(
  doctorSatisfaction.allow_na,
  true,
  'Doctor satisfaction must allow a non-applicable answer'
);

const doctorPilot = doctor.find((q) => q.id === 'doctor_pilot');
const patientPilot = patient.find((q) => q.id === 'patient_pilot');
assert.equal(doctorPilot.required, false, 'Doctor pilot interest must be optional');
assert.equal(patientPilot.required, false, 'Patient pilot interest must be optional');

assert.match(
  surveyJs,
  /saveCurrent\("back"\)/,
  'Back navigation must save a modified valid answer before leaving'
);
assert.match(
  surveyJs,
  /setLandingBusy\(true\)/,
  'Starting the survey must immediately lock start buttons'
);
assert.match(
  surveyJs,
  /beforeunload/,
  'Unsaved visible changes must trigger a navigation warning'
);

console.log('RésoSoin survey core tests OK.');
