import { test, before, after, beforeEach, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { chromium } from 'playwright';
import { expect } from 'playwright/test';
import { createServer } from 'vite';

let server, browser, context, page;
before(async () => {
  server = await createServer({ server: { port: 5173, host: '127.0.0.1', strictPort: true } });
  await server.listen();
  browser = await chromium.launch({ channel: process.env.BROWSER_CHANNEL, executablePath: process.env.BROWSER_EXECUTABLE, headless: true });
});
after(async () => { await browser?.close(); await server?.close(); });
beforeEach(async () => { context = await browser.newContext(); page = await context.newPage(); });
afterEach(async () => { await context?.close(); });

const event = { eventId: 'EVT-1001', eventType: 'QUALITY', description: 'Inspeção aprovada', timestamp: '2026-05-20T10:00:00' };
async function apiMock({ initial = [], failList = false, conflict = false } = {}) {
  const records = [...initial]; let submissions = 0;
  await page.route('**/api/events**', async route => {
    const request = route.request(); const url = new URL(request.url());
    if (request.method() === 'POST') {
      submissions++;
      if (conflict) return route.fulfill({ status: 409, json: { detail: "O evento 'EVT-1001' já foi registrado." } });
      const created = request.postDataJSON(); records.unshift(created);
      return route.fulfill({ status: 201, json: created });
    }
    if (url.pathname.endsWith('/audit')) return route.fulfill({ json: { eventId: event.eventId, action: 'QUALITY_EVENT_REGISTERED', recordedAt: '2026-05-20T13:00:00Z' } });
    if (failList) return route.fulfill({ status: 503, json: { detail: 'Serviço temporariamente indisponível.' } });
    const filter = url.searchParams.get('eventType'); const number = Number(url.searchParams.get('page'));
    const filtered = records.filter(record => !filter || record.eventType === filter);
    return route.fulfill({ json: { content: filtered.slice(number * 10, number * 10 + 10), totalElements: filtered.length, totalPages: Math.ceil(filtered.length / 10), number, size: 10 } });
  });
  return () => submissions;
}
async function fillForm() {
  await page.getByLabel('ID do evento').fill(event.eventId);
  await page.getByLabel('Tipo de evento', { exact: false }).selectOption('QUALITY');
  await page.getByLabel('Descrição', { exact: false }).fill(event.description);
  await page.getByLabel('Data e hora da ocorrência').fill('2026-05-20T10:00');
}
test('registra qualidade, atualiza a tabela e exibe a auditoria', async () => {
  const count = await apiMock(); await page.goto('http://127.0.0.1:5173');
  await expect(page).toHaveTitle('Eventos Industriais | Monitor de eventos');
  await expect(page.locator('html')).toHaveAttribute('lang', 'pt-BR');
  await page.getByText('Nenhum evento encontrado').waitFor();
  await fillForm(); await page.getByRole('button', { name: 'Registrar evento' }).click();
  await page.getByRole('status').filter({ hasText: 'registrado com sucesso e auditoria gerada' }).waitFor();
  await page.getByRole('button', { name: 'Ver auditoria de EVT-1001' }).click();
  await page.getByText('Registro de evento de qualidade', { exact: true }).waitFor();
  assert.equal(count(), 1);
  assert.equal(await page.getByLabel('ID do evento').inputValue(), '');
  await page.getByRole('button', { name: 'Fechar', exact: true }).click();
  await expect(page.getByRole('complementary', { name: 'Auditoria de EVT-1001', exact: true })).toHaveCount(0);
});
test('valida os campos antes de enviar', async () => {
  const count = await apiMock(); await page.goto('http://127.0.0.1:5173');
  await page.getByRole('button', { name: 'Registrar evento' }).click();
  await page.getByRole('alert').filter({ hasText: 'Revise os campos' }).waitFor();
  assert.equal(await page.getByLabel('ID do evento').getAttribute('aria-invalid'), 'true');
  assert.equal(count(), 0);
});
test('conflito preserva o formulário e exibe a mensagem do servidor', async () => {
  await apiMock({ conflict: true }); await page.goto('http://127.0.0.1:5173');
  await fillForm(); await page.getByRole('button', { name: 'Registrar evento' }).click();
  await page.getByRole('alert').filter({ hasText: 'já foi registrado' }).waitFor();
  assert.equal(await page.getByLabel('ID do evento').inputValue(), event.eventId);
});
test('filtra e pagina os eventos', async () => {
  const initial = Array.from({ length: 12 }, (_, index) => ({ ...event, eventId: `EVT-${index}`, eventType: index === 11 ? 'PRODUCTION' : 'QUALITY' }));
  await apiMock({ initial }); await page.goto('http://127.0.0.1:5173');
  await page.getByText('Página 1 de 2').waitFor();
  await expect(page.locator('tbody tr')).toHaveCount(10);
  await page.getByRole('button', { name: 'Próxima', exact: true }).click();
  await page.getByText('Página 2 de 2').waitFor();
  await expect(page.locator('tbody tr')).toHaveCount(2);
  await page.getByLabel('Filtrar por tipo').selectOption('PRODUCTION');
  await page.getByText('Página 1 de 1').waitFor();
  await expect(page.locator('tbody tr')).toHaveCount(1);
  await expect(page.getByRole('button', { name: 'Ver auditoria', exact: false })).toHaveCount(0);
});
test('exibe falha da API e ação para tentar novamente', async () => {
  await apiMock({ failList: true }); await page.goto('http://127.0.0.1:5173');
  await page.getByRole('alert').filter({ hasText: 'temporariamente indisponível' }).waitFor();
  assert.equal(await page.getByRole('button', { name: 'Tentar novamente' }).isVisible(), true);
});
test('adapta a tela ao celular e renderiza descrições como texto', async () => {
  await page.setViewportSize({ width: 390, height: 844 });
  await apiMock({ initial: [{ ...event, description: '<script>alert(1)</script>' }] });
  await page.goto('http://127.0.0.1:5173');
  await page.getByText('<script>alert(1)</script>', { exact: true }).waitFor();
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true);
  assert.equal(await page.getByRole('button', { name: 'Registrar evento' }).isVisible(), true);
});
