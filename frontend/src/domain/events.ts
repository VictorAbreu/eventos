export type EventType = 'PRODUCTION' | 'QUALITY';
export interface FactoryEvent {
    eventId: string;
    eventType: EventType;
    description: string;
    timestamp: string;
}
export interface EventPage {
    content: FactoryEvent[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}
export interface AuditRecord {
    eventId: string;
    action: string;
    recordedAt: string;
}
export type FieldErrors = Partial<Record<keyof FactoryEvent, string>>;
export const eventLabels: Record<EventType, string> = { PRODUCTION: 'Produção', QUALITY: 'Qualidade' };
export function validateEvent(event: FactoryEvent): FieldErrors {
    const errors: FieldErrors = {};
    if (!/^[A-Za-z0-9][A-Za-z0-9._-]{0,99}$/.test(event.eventId))
        errors.eventId = 'Use até 100 letras, números, pontos, hífens ou sublinhados.';
    if (!['PRODUCTION', 'QUALITY'].includes(event.eventType))
        errors.eventType = 'Selecione um tipo válido.';
    if (!event.description.trim() || event.description.length > 2000)
        errors.description = 'Informe uma descrição de até 2.000 caracteres.';
    if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?$/.test(event.timestamp) || Number.isNaN(Date.parse(event.timestamp))) {
        errors.timestamp = 'Informe uma data e hora válidas.';
    }
    return errors;
}
export function localDateTime(now = new Date()): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
}
export function formatDateTime(value: string): string {
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'medium' }).format(new Date(value));
}
