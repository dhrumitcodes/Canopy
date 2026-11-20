import { useCallback, useEffect, useState } from 'react';
import { eventApi } from '../../api/eventApi';
import { formatDateTime } from '../../utils/formatDate';
import Loader from '../../components/common/Loader';
import ErrorMessage from '../../components/common/ErrorMessage';
import { useAuth } from '../../context/AuthContext';
import { Navigate } from 'react-router-dom';
export default function EventApprovalsPage() {
    const { user } = useAuth();
    const [items, setItems] = useState(null); const [error, setError] = useState(null); const [busy, setBusy] = useState(null);
    const load = useCallback(() => { setError(null); eventApi.getPendingApprovals().then(setItems).catch(setError); }, []);
    useEffect(() => { load(); }, [load]);
    const decide = async (event, approve) => {
        let reason = '';
        if (!approve) { reason = window.prompt('Enter a reason for rejecting this event:') || ''; if (!reason.trim()) return; }
        setBusy(event.id);
        try { if (approve) await eventApi.approveEvent(event.id); else await eventApi.rejectEvent(event.id, reason.trim()); load(); }
        catch (e) { setError(e); } finally { setBusy(null); }
    };
    if (user.role !== 'COLLEGE_ADMIN') return <Navigate to="/discover" replace />;
    if (error) return <ErrorMessage error={error} onRetry={load} />;
    if (!items) return <Loader />;
    return <><h1 className="greet">Event approvals</h1><p className="muted greet-sub">Review events submitted by societies in your college.</p>
        <section className="panel">{items.length === 0 ? <p className="muted empty">No events are waiting for approval.</p> : items.map((e) => <article className="notice" key={e.id}>
            <div className="notice-head"><h3>{e.title}</h3><span className="tag">Pending</span></div>
            <small>{e.societyName} · {formatDateTime(e.startTime)} · {e.location || 'Location not set'}</small>
            <p>{e.description || 'No description provided.'}</p><p className="muted">Capacity: {e.capacity}</p>
            <div className="actions-row"><button className="btn-solid" type="button" disabled={busy === e.id} onClick={() => decide(e, true)}>Approve</button><button className="btn-danger" type="button" disabled={busy === e.id} onClick={() => decide(e, false)}>Reject</button></div>
        </article>)}</section></>;
}