'use strict';

const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const schemaPath = path.join(__dirname, '../web/app.lepotager.org/resosoin/schemas/exchange-v1.schema.json');
const schema = JSON.parse(fs.readFileSync(schemaPath, 'utf8'));

assert.equal(schema.$schema, 'https://json-schema.org/draft/2020-12/schema');
assert.equal(schema.properties.schema.const, 'resosoin.exchange.v1');
assert.equal(schema.additionalProperties, false);
assert.deepEqual(schema.required, ['schema', 'kind', 'source', 'subject', 'appointment']);
assert.equal(schema.properties.subject.additionalProperties, false);
assert.equal(schema.properties.appointment.additionalProperties, false);
assert.equal(schema.properties.appointment.properties.duration_minutes.minimum, 5);
assert.ok(schema.description.includes("n'est ni un standard médical"));
console.log('RésoSoin exchange contract checks OK.');
