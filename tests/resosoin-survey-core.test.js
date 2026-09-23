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
