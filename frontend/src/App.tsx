import { EventForm } from './components/EventForm.tsx';
import { EventTable } from './components/EventTable.tsx';
import { useEvents } from './hooks/useEvents.ts';
export default function App() {
    const query = useEvents();
    return <><header className="topbar"><div className="brand"><span className="brand-icon" aria-hidden="true">▥</span><span>eventos<span className="brand-light"> industriais</span><small>OPERAÇÕES INDUSTRIAIS</small></span></div><div className="workspace"><span className="status-dot"/>Central de eventos</div></header>
    <main><div className="page-intro"><div><div className="eyebrow">CONTROLE E RASTREABILIDADE</div><h1>Cada evento, <span>um registro.</span></h1><p>Receba ocorrências dos equipamentos e acompanhe o histórico da sua operação.</p></div><span className="environment">AMBIENTE LOCAL</span></div>
      <div className="principles" aria-label="Regras de processamento"><div><span className="principle-icon" aria-hidden="true">01</span><p><strong>Identidade única</strong><span>Proteção contra duplicidade</span></p></div><div><span className="principle-icon" aria-hidden="true">02</span><p><strong>Produção registrada</strong><span>Histórico de cada ocorrência</span></p></div><div><span className="principle-icon" aria-hidden="true">03</span><p><strong>Qualidade auditada</strong><span>Evento e auditoria na mesma transação</span></p></div></div>
      <div className="dashboard"><EventForm onCreated={query.afterCreate}/><EventTable query={query}/></div>
      <footer>Eventos Industriais <span>·</span> Registro de eventos industriais <span className="footer-right">Java + Spring Boot · React + TypeScript</span></footer>
    </main></>;
}
