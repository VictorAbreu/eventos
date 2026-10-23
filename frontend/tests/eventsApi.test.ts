import { test, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { ApiError, eventsApi } from '../src/services/eventsApi.ts';

const originalFetch = globalThis.fetch;
afterEach(() => { globalThis.fetch = originalFetch; });

test('consulta com filtro codificado, paginação e sinal de cancelamento', async () => {
  const controller = new AbortController();
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/events?page=2&size=10&eventType=QUALITY');
    assert.equal(options?.signal, controller.signal);
    return Response.json({ content: [], totalElements: 0 });
  };
  assert.deepEqual(await eventsApi.list(2, 'QUALITY', controller.signal), { content: [], totalElements: 0 });
});
test('consulta padrão não adiciona filtro vazio', async () => {
  globalThis.fetch = async url => { assert.equal(url, '/api/events?page=0&size=10'); return Response.json({ content: [] }); };
  await eventsApi.list();
});
test('envia os dados exatos no cadastro', async () => {
  const event = { eventId: 'id', eventType: 'PRODUCTION' as const, description: 'ok', timestamp: '2026-05-20T10:00:00' };
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/events'); assert.equal(options?.method, 'POST');
    assert.deepEqual(JSON.parse(String(options?.body)), event);
    return Response.json(event, { status: 201 });
  };
  assert.deepEqual(await eventsApi.create(event), event);
});
test('consulta de auditoria trata retorno 204 e codifica os IDs', async () => {
  globalThis.fetch = async url => { assert.equal(url, '/api/events/a%2Fb/audit'); return new Response(null, { status: 204 }); };
  assert.equal(await eventsApi.audit('a/b'), undefined);
});
test('preserva os erros estruturados de validação e conflito da API', async () => {
  globalThis.fetch = async () => Response.json({ detail: 'Duplicado', errors: { eventId: 'Inválido' } }, { status: 409 });
  await assert.rejects(eventsApi.list(), error => error instanceof ApiError && error.status === 409 && error.message === 'Duplicado' && error.fields.eventId === 'Inválido');
});
test('apresenta mensagem segura quando o erro não contém JSON válido', async () => {
  for (const response of [new Response('falha no serviço', { status: 502 }), Response.json({}, { status: 500 }), Response.json(null, { status: 500 })]) {
    globalThis.fetch = async () => response;
    await assert.rejects(eventsApi.list(), error => error instanceof ApiError && error.status >= 500 && error.message.includes('concluir'));
  }
});
test('trata falha de rede sem expor exceção e preserva o cancelamento', async () => {
  globalThis.fetch = async () => { throw new Error('falha interna'); };
  await assert.rejects(eventsApi.list(), error => error instanceof ApiError && error.status === 0 && error.message.includes('conectar'));
  globalThis.fetch = async () => { throw 'rede'; };
  await assert.rejects(eventsApi.list(), ApiError);
  const aborted = new DOMException('cancelado', 'AbortError');
  globalThis.fetch = async () => { throw aborted; };
  await assert.rejects(eventsApi.list(), error => error === aborted);
});
