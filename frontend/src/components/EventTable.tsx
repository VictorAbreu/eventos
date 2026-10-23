import { useState } from 'react';
import { eventLabels, formatDateTime, type EventType } from '../domain/events.ts';
import type { useEvents } from '../hooks/useEvents.ts';
import { AuditDetails } from './AuditDetails.tsx';
export function EventTable({ query }: {
    query: ReturnType<typeof useEvents>;
}) {
    const [auditId, setAuditId] = useState<string>();
    const { data, loading, error, type, changeType, page, setPage, refresh } = query;
    return <section className="panel list-panel" aria-labelledby="events-title">
    <div className="section-heading"><span className="step">02</span><div><h2 id="events-title">Eventos registrados</h2><p>Histórico de ocorrências da fábrica</p></div><button className="refresh" onClick={refresh} disabled={loading} aria-label="Atualizar eventos">↻</button></div>
    <div className="list-toolbar"><div><label htmlFor="filter">Filtrar por tipo</label><select id="filter" value={type} onChange={e => { changeType(e.target.value as EventType | ''); setAuditId(undefined); }}><option value="">Todos os tipos</option><option value="PRODUCTION">Produção</option><option value="QUALITY">Qualidade</option></select></div><span className="count">{loading ? 'Consultando…' : error ? 'Consulta indisponível' : `${data?.totalElements ?? 0} evento(s)`}</span></div>
    {loading ? <div className="empty" role="status"><span className="spinner"/>Carregando eventos…</div> : error ? <div className="empty error-state"><p role="alert">{error}</p><button className="secondary" onClick={refresh}>Tentar novamente</button></div> : !data?.content.length ? <div className="empty"><span className="empty-icon" aria-hidden="true">≡</span><h3>Nenhum evento encontrado</h3><p>{type ? 'Altere o filtro para consultar outros eventos.' : 'Registre o primeiro evento no formulário ao lado.'}</p></div> : <>
      <div className="table-scroll"><table><caption className="sr-only">Eventos registrados, ordenados por data da ocorrência, do mais recente ao mais antigo</caption><thead><tr><th scope="col">Evento / ocorrência</th><th scope="col">Tipo</th><th scope="col">Descrição</th><th scope="col">Auditoria</th></tr></thead><tbody>{data.content.map(event => <tr key={event.eventId}><td><strong className="event-id">{event.eventId}</strong><time dateTime={event.timestamp}>{formatDateTime(event.timestamp)}</time></td><td><span className={`badge ${event.eventType.toLowerCase()}`}><span aria-hidden="true">●</span> {eventLabels[event.eventType]}</span></td><td className="description-cell">{event.description}</td><td>{event.eventType === 'QUALITY' ? <button className="text-button" onClick={() => setAuditId(event.eventId)} aria-label={`Ver auditoria de ${event.eventId}`}>Ver registro ↗</button> : <span className="muted">Não se aplica</span>}</td></tr>)}</tbody></table></div>
      <nav className="pagination" aria-label="Paginação dos eventos"><span>Página {page + 1} de {data.totalPages}</span><div><button className="secondary" disabled={page === 0} onClick={() => { setPage(page - 1); setAuditId(undefined); }}>Anterior</button><button className="secondary" disabled={page + 1 >= data.totalPages} onClick={() => { setPage(page + 1); setAuditId(undefined); }}>Próxima</button></div></nav>
    </>}
    {auditId && <AuditDetails eventId={auditId} onClose={() => setAuditId(undefined)}/>}
    <div className="table-note"><span aria-hidden="true">↧</span> Ordenados pela ocorrência mais recente · 10 por página</div>
  </section>;
}
