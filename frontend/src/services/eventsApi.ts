import type { AuditRecord, EventPage, EventType, FactoryEvent, FieldErrors } from '../domain/events.ts';
export class ApiError extends Error {
    readonly status: number;
    readonly fields: FieldErrors;
    constructor(message: string, status = 0, fields: FieldErrors = {}) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.fields = fields;
    }
}
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    let response: Response;
    try {
        response = await fetch(`/api/events${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
    }
    catch (error) {
        if (error instanceof Error && error.name === 'AbortError')
            throw error;
        throw new ApiError('Não foi possível conectar à API. Verifique sua conexão e tente novamente.');
    }
    if (!response.ok) {
        const body: unknown = await response.json().catch(() => ({}));
        const problem = (body && typeof body === 'object' ? body : {}) as {
            detail?: string;
            errors?: FieldErrors;
        };
        throw new ApiError(problem.detail || 'Não foi possível concluir a operação.', response.status, problem.errors || {});
    }
    if (response.status === 204)
        return undefined as T;
    return response.json() as Promise<T>;
}
export const eventsApi = {
    list(page = 0, eventType: EventType | '' = '', signal?: AbortSignal): Promise<EventPage> {
        const params = new URLSearchParams({ page: String(page), size: '10' });
        if (eventType)
            params.set('eventType', eventType);
        return request<EventPage>(`?${params}`, { signal });
    },
    create(event: FactoryEvent): Promise<FactoryEvent> { return request('', { method: 'POST', body: JSON.stringify(event) }); },
    audit(eventId: string, signal?: AbortSignal): Promise<AuditRecord | undefined> { return request(`/${encodeURIComponent(eventId)}/audit`, { signal }); },
};
