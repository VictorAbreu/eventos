import { useCallback, useEffect, useState } from 'react';
import type { EventPage, EventType } from '../domain/events.ts';
import { eventsApi } from '../services/eventsApi.ts';
export function useEvents() {
    const [page, setPage] = useState(0);
    const [type, setType] = useState<EventType | ''>('');
    const [revision, setRevision] = useState(0);
    const [data, setData] = useState<EventPage>();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    useEffect(() => {
        const controller = new AbortController();
        setLoading(true);
        setError('');
        eventsApi.list(page, type, controller.signal)
            .then(result => { if (!controller.signal.aborted)
            setData(result); })
            .catch((cause: unknown) => { if (!controller.signal.aborted)
            setError(cause instanceof Error ? cause.message : 'Falha ao carregar eventos.'); })
            .finally(() => { if (!controller.signal.aborted)
            setLoading(false); });
        return () => controller.abort();
    }, [page, type, revision]);
    const refresh = useCallback(() => setRevision(value => value + 1), []);
    const changeType = (value: EventType | '') => { setType(value); setPage(0); };
    const afterCreate = () => { setPage(0); setType(''); refresh(); };
    return { page, setPage, type, changeType, data, loading, error, refresh, afterCreate };
}
