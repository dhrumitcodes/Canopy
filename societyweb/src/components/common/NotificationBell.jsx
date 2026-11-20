import { useEffect, useState } from 'react';
import { notificationApi } from '../../api/notificationApi';
export default function NotificationBell() {
    const [data, setData] = useState({ items: [], unreadCount: 0 });
    const [open, setOpen] = useState(false);
    const [error, setError] = useState('');
    const load = () => notificationApi.list().then(setData).catch(() => setError('Notifications could not be loaded.'));
    useEffect(() => { load(); }, []);
    const read = async (item) => { if (!item.readAt) { await notificationApi.markRead(item.id); await load(); } };
    const readAll = async () => { await notificationApi.markAllRead(); await load(); };
    return <div className="notification-wrap">
        <button type="button" className="notification-trigger" aria-label={`Notifications, ${data.unreadCount} unread`} onClick={() => { setOpen(!open); if (!open) load(); }}>
            ♧ <span>Notifications</span>{data.unreadCount > 0 && <b>{data.unreadCount}</b>}
        </button>
        {open && <section className="notification-popover">
            <header><strong>Notifications</strong><button type="button" className="link-button" onClick={readAll} disabled={!data.unreadCount}>Mark all read</button></header>
            {error && <p role="alert">{error}</p>}
            {data.items.length === 0 ? <p className="muted">You are all caught up.</p> : data.items.map((item) => <button type="button" className={`notification-item ${item.readAt ? '' : 'unread'}`} key={item.id} onClick={() => read(item)}>
                <strong>{item.title}</strong><span>{item.message}</span><small>{new Date(item.createdAt).toLocaleString()}</small>
            </button>)}
        </section>}
    </div>;
}