import { useRef, useState, type FormEvent } from 'react';
import { eventLabels, localDateTime, validateEvent, type FactoryEvent, type FieldErrors, type EventType } from '../domain/events.ts';
import { ApiError, eventsApi } from '../services/eventsApi.ts';
const emptyEvent = (): FactoryEvent => ({ eventId: '', eventType: 'PRODUCTION', description: '', timestamp: localDateTime() });
export function EventForm({ onCreated }: {
    onCreated: () => void;
}) {
    const [event, setEvent] = useState(emptyEvent);
    const [errors, setErrors] = useState<FieldErrors>({});
    const [pending, setPending] = useState(false);
    const [feedback, setFeedback] = useState<{
        text: string;
        error: boolean;
    }>();
    const submitting = useRef(false);
    const update = (key: keyof FactoryEvent, value: string) => {
        setEvent(previous => ({ ...previous, [key]: value }));
        setErrors(previous => ({ ...previous, [key]: undefined }));
        setFeedback(undefined);
    };
    async function submit(e: FormEvent) {
        e.preventDefault();
        if (submitting.current)
            return;
        const validation = validateEvent(event);
        setErrors(validation);
        setFeedback(undefined);
        if (Object.keys(validation).length) {
            setFeedback({ text: 'Revise os campos destacados.', error: true });
            return;
        }
        submitting.current = true;
        setPending(true);
        try {
            const created = await eventsApi.create({ ...event, description: event.description.trim(), timestamp: event.timestamp.length === 16 ? `${event.timestamp}:00` : event.timestamp });
            setFeedback({ text: `Evento ${created.eventId} registrado com sucesso${created.eventType === 'QUALITY' ? ' e auditoria gerada' : ''}.`, error: false });
            setEvent(emptyEvent());
            onCreated();
        }
        catch (cause) {
            if (cause instanceof ApiError)
                setErrors(cause.fields);
            setFeedback({ text: cause instanceof Error ? cause.message : 'Não foi possível registrar o evento.', error: true });
        }
        finally {
            submitting.current = false;
            setPending(false);
        }
    }
    const errorProps = (key: keyof FactoryEvent) => ({ 'aria-invalid': Boolean(errors[key]), 'aria-describedby': errors[key] ? `${key}-error` : undefined });
    return <section className="panel form-panel" aria-labelledby="register-title">
    <div className="section-heading"><span className="step">01</span><div><h2 id="register-title">Registrar evento</h2><p>Recebimento de dados do equipamento</p></div></div>
    <form onSubmit={submit} noValidate aria-busy={pending}>
      <fieldset disabled={pending}>
        <label htmlFor="eventId">ID do evento <span aria-hidden="true">*</span></label>
        <input id="eventId" required maxLength={100} placeholder="Ex.: EVT-1001" autoComplete="off" value={event.eventId} onChange={e => update('eventId', e.target.value)} {...errorProps('eventId')}/>
        <small>Identificador único enviado pelo equipamento.</small>
        {errors.eventId && <span className="field-error" id="eventId-error">{errors.eventId}</span>}
        <label htmlFor="eventType">Tipo de evento <span aria-hidden="true">*</span></label>
        <select id="eventType" required value={event.eventType} onChange={e => update('eventType', e.target.value as EventType)} {...errorProps('eventType')}>
          <option value="PRODUCTION">{eventLabels.PRODUCTION}</option><option value="QUALITY">{eventLabels.QUALITY}</option>
        </select>
        {errors.eventType && <span className="field-error" id="eventType-error">{errors.eventType}</span>}
        <div className={`policy ${event.eventType === 'QUALITY' ? 'quality' : ''}`}><span aria-hidden="true">↳</span> {event.eventType === 'QUALITY' ? 'Este evento também gera um registro de auditoria.' : 'Este evento será registrado sem auditoria adicional.'}</div>
        <label htmlFor="timestamp">Data e hora da ocorrência <span aria-hidden="true">*</span></label>
        <input id="timestamp" type="datetime-local" required value={event.timestamp} onChange={e => update('timestamp', e.target.value)} {...errorProps('timestamp')}/>
        <small>Horário local da fábrica, sem conversão de fuso.</small>
        {errors.timestamp && <span className="field-error" id="timestamp-error">{errors.timestamp}</span>}
        <label htmlFor="description">Descrição <span aria-hidden="true">*</span></label>
        <textarea id="description" required maxLength={2000} rows={4} placeholder="Descreva o que aconteceu no equipamento…" value={event.description} onChange={e => update('description', e.target.value)} {...errorProps('description')}/>
        <div className="counter">{event.description.length.toLocaleString('pt-BR')} / 2.000</div>
        {errors.description && <span className="field-error" id="description-error">{errors.description}</span>}
        <button className="primary" type="submit">{pending ? 'Registrando…' : 'Registrar evento'} <span aria-hidden="true">→</span></button>
      </fieldset>
      {feedback && <div className={`feedback ${feedback.error ? 'error' : 'success'}`} role={feedback.error ? 'alert' : 'status'}>{feedback.text}</div>}
    </form>
    <p className="form-footnote">* Todos os campos são obrigatórios. IDs já registrados não podem ser reutilizados.</p>
  </section>;
}
