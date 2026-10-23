import { test } from 'node:test';
import assert from 'node:assert/strict';
import { validateEvent, localDateTime, formatDateTime, type FactoryEvent } from '../src/domain/events.ts';

const valid: FactoryEvent = { eventId: 'EVT-1', eventType: 'PRODUCTION', description: 'Linha iniciada', timestamp: '2026-05-20T10:00' };
test('aceita os dois tipos de evento e precisão de segundos', () => {
  assert.deepEqual(validateEvent(valid), {});
  assert.deepEqual(validateEvent({ ...valid, eventType: 'QUALITY', timestamp: '2026-05-20T10:00:00' }), {});
});
test('valida o formato do ID e os limites de tamanho', () => {
  for (const id of ['', ' ', 'a/b', '../id', 'x'.repeat(101)]) assert.ok(validateEvent({ ...valid, eventId: id }).eventId);
  assert.deepEqual(validateEvent({ ...valid, eventId: 'x'.repeat(100) }), {});
  assert.deepEqual(validateEvent({ ...valid, eventId: 'a.b_c-1' }), {});
});
test('valida descrição, data e tipo', () => {
  for (const description of ['', ' ', 'x'.repeat(2001)]) assert.ok(validateEvent({ ...valid, description }).description);
  assert.deepEqual(validateEvent({ ...valid, description: 'x'.repeat(2000) }), {});
  for (const timestamp of ['', 'invalid', '2026-99-20T10:00']) assert.ok(validateEvent({ ...valid, timestamp }).timestamp);
  assert.ok(validateEvent({ ...valid, eventType: 'UNKNOWN' as never }).eventType);
});
test('utiliza o horário local da ocorrência sem conversão para UTC', () => {
  assert.equal(localDateTime(new Date(2026, 0, 2, 3, 4)), '2026-01-02T03:04');
  assert.match(localDateTime(), /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
  assert.match(formatDateTime('2026-05-20T10:00:00'), /20\/05\/2026.*10:00:00/);
});
