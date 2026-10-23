import { useEffect, useState } from 'react';
import { eventsApi } from '../services/eventsApi.ts';
import { formatDateTime, type AuditRecord } from '../domain/events.ts';
export function AuditDetails({ eventId, onClose }: {
    eventId: string;
    onClose: () => void;
}) {
    const [record, setRecord] = useState<AuditRecord>();
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);
    useEffect(() => {
        const controller = new AbortController();
        setLoading(true);
        setError('');
        setRecord(undefined);
        eventsApi.audit(eventId, controller.signal)
            .then(value => { if (!controller.signal.aborted)
            setRecord(value); })
            .catch((cause: unknown) => { if (!controller.signal.aborted)
            setError(cause instanceof Error ? cause.message : 'Falha ao consultar auditoria.'); })
            .finally(() => { if (!controller.signal.aborted)
            setLoading(false); });
        return () => controller.abort();
    }, [eventId]);
    return <aside className="audit-details" aria-label={`Auditoria de ${eventId}`}>
    <div className="audit-heading"><strong>Auditoria · {eventId}</strong><button className="text-button" onClick={onClose}>Fechar</button></div>
    {loading ? <p role="status">Carregando auditoria…</p> : error ? <p role="alert">{error}</p> : record ?
            <dl><dt>Ação</dt><dd>Registro de evento de qualidade</dd><dt>Registrada em</dt><dd>{formatDateTime(record.recordedAt)} <small>(seu fuso local)</small></dd></dl> : <p>Nenhuma auditoria encontrada para este evento.</p>}
  </aside>;
}
