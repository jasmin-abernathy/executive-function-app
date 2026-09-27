/* Synthetic prototype only. No patient data or vendor integration. */
'use strict';
const { randomUUID } = require('node:crypto');

const VERSION = 'resosoin.exchange.v1';
const MAX_BYTES = 8192;
const FIELDS = {
  '': ['schema', 'kind', 'source', 'subject', 'appointment'],
  subject: ['external_reference'],
  appointment: ['starts_at', 'duration_minutes', 'status']
};

function validate(raw) {
  const errors = [];
  let value = raw;
  if (typeof raw === 'string') {
    if (Buffer.byteLength(raw, 'utf8') > MAX_BYTES) return { valid: false, errors: [{ field: '$', code: 'too_large' }] };
    try { value = JSON.parse(raw); } catch { return { valid: false, errors: [{ field: '$', code: 'invalid_json' }] }; }
  }
  if (!value || typeof value !== 'object' || Array.isArray(value)) return { valid: false, errors: [{ field: '$', code: 'invalid_object' }] };
  try {
    if (Buffer.byteLength(JSON.stringify(value), 'utf8') > MAX_BYTES) return { valid: false, errors: [{ field: '$', code: 'too_large' }] };
  } catch { return { valid: false, errors: [{ field: '$', code: 'invalid_object' }] }; }
  const issue = (field, code) => errors.push({ field, code });
  function fields(object, prefix) {
    if (!object || typeof object !== 'object' || Array.isArray(object)) { issue(prefix, 'invalid_object'); return false; }
    for (const key of Object.keys(object)) if (!FIELDS[prefix].includes(key)) issue(prefix ? `${prefix}.${key}` : key, 'unknown_field');
    for (const key of FIELDS[prefix]) if (!Object.hasOwn(object, key)) issue(prefix ? `${prefix}.${key}` : key, 'required');
    return true;
  }
  fields(value, '');
  if (value.schema !== VERSION) issue('schema', 'unknown_version');
  if (value.kind !== 'appointment-summary') issue('kind', 'invalid_value');
  if (typeof value.source !== 'string' || value.source.trim().length < 1 || value.source.length > 80) issue('source', 'invalid_value');
  if (fields(value.subject, 'subject')) {
    const ref = value.subject.external_reference;
    if (typeof ref !== 'string' || ref.trim().length < 1 || ref.length > 100) issue('subject.external_reference', 'invalid_value');
  }
  if (fields(value.appointment, 'appointment')) {
    const a = value.appointment;
    if (typeof a.starts_at !== 'string' || !/^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(?:\.\d+)?(?:Z|[+-]\d\d:\d\d)$/.test(a.starts_at) || !Number.isFinite(Date.parse(a.starts_at)) || !validCalendarDate(a.starts_at)) issue('appointment.starts_at', 'invalid_datetime');
    if (!Number.isInteger(a.duration_minutes) || a.duration_minutes < 5 || a.duration_minutes > 1440) issue('appointment.duration_minutes', 'invalid_duration');
    if (!['planned', 'confirmed', 'cancelled'].includes(a.status)) issue('appointment.status', 'invalid_value');
  }
  return errors.length ? { valid: false, errors } : { valid: true, value };
}

function validCalendarDate(s) {
  const [, year, month, day, hour, minute, second, zone] = s.match(/^(\d{4})-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:\.\d+)?(Z|[+-]\d\d:\d\d)$/) || [];
  if (!year) return false;
  const maxDay = new Date(Date.UTC(Number(year), Number(month), 0)).getUTCDate();
  return Number(month) >= 1 && Number(month) <= 12 && Number(day) >= 1 && Number(day) <= maxDay && Number(hour) <= 23 && Number(minute) <= 59 && Number(second) <= 59 && (zone === 'Z' || (Number(zone.slice(1, 3)) <= 23 && Number(zone.slice(4)) <= 59));
}

// In-memory dry-run: the caller must provide a fresh expected key at commit time.
function createMockAdapter() {
  const records = new Map();
  const previews = new Map();
  function preview(payload, key) {
    const result = validate(payload);
    if (!result.valid) return { status: 'rejected', errors: result.errors };
    if (typeof key !== 'string' || !/^[a-zA-Z0-9_-]{8,128}$/.test(key)) return { status: 'rejected', errors: [{ field: 'idempotency_key', code: 'invalid_value' }] };
    const serialized = JSON.stringify(result.value);
    if (records.has(key)) return records.get(key) === serialized
      ? { status: 'duplicate', schema: VERSION, idempotency_key: key }
      : { status: 'rejected', errors: [{ field: 'idempotency_key', code: 'conflict' }] };
    const review_token = randomUUID();
    previews.set(key, { serialized, review_token });
    return { status: 'valid', schema: VERSION, idempotency_key: key, review_token };
  }
  function commit(payload, key, confirmation, review_token) {
    const result = validate(payload);
    if (!result.valid) return { status: 'rejected', errors: result.errors };
    if (typeof key !== 'string' || !/^[a-zA-Z0-9_-]{8,128}$/.test(key)) return { status: 'rejected', errors: [{ field: 'idempotency_key', code: 'invalid_value' }] };
    const serialized = JSON.stringify(result.value);
    if (records.has(key)) return records.get(key) === serialized
      ? { status: 'duplicate', schema: VERSION, idempotency_key: key }
      : { status: 'rejected', errors: [{ field: 'idempotency_key', code: 'conflict' }] };
    if (confirmation !== true || !previews.has(key) || previews.get(key).review_token !== review_token || previews.get(key).serialized !== serialized) return { status: 'rejected', errors: [{ field: 'confirmation', code: 'preview_required' }] };
    previews.delete(key);
    records.set(key, serialized);
    return { status: 'accepted', schema: VERSION, idempotency_key: key };
  }
  return { preview, commit };
}

module.exports = { validate, createMockAdapter, VERSION, MAX_BYTES };
