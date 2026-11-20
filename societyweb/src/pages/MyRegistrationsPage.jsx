import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { eventApi } from '../api/eventApi';
import { dayOf, formatDateTime, monthOf } from '../utils/formatDate';
import Loader from '../components/common/Loader';
import ErrorMessage from '../components/common/ErrorMessage';
export default function MyRegistrationsPage() {
 const [items,setItems]=useState(null),[error,setError]=useState(null),[busyId,setBusyId]=useState(null),[ticketId,setTicketId]=useState(null);
 const load=useCallback(()=>{setError(null);eventApi.getMyRegistrations().then(setItems).catch(setError);},[]);
 useEffect(load,[load]);
 const cancel=async(eventId)=>{if(!window.confirm('Cancel this registration?'))return;setBusyId(eventId);try{await eventApi.cancelRegistration(eventId);load();}catch(e){setError(e);}finally{setBusyId(null);}};
 if(error)return <ErrorMessage error={error} onRetry={load}/>;if(!items)return <Loader/>;
 const now=new Date(),sorted=[...items].sort((a,b)=>new Date(a.eventStartTime)-new Date(b.eventStartTime));
 return <><h1 className="greet">My registrations</h1><p className="muted greet-sub">Your event tickets and attendance details.</p>
 <section className="panel">{sorted.length===0?<p className="muted empty">You have not registered for anything yet. <Link className="link" to="/discover">Browse events</Link></p>:sorted.map(r=>{
  const ended=new Date(r.eventEndTime)<now;
  return <article className="ticket-row" key={r.id}>
   <div className="event-row"><span className="date-tile"><b>{dayOf(r.eventStartTime)}</b><small>{monthOf(r.eventStartTime)}</small></span>
    <Link to={`/discover/societies/${r.societyId}/events/${r.eventId}`} style={{flex:1,color:'inherit',textDecoration:'none'}}><strong>{r.eventTitle}</strong><small>{r.societyName} · {formatDateTime(r.eventStartTime)}</small></Link>
    {r.checkedInAt?<span className="tone good">Checked in</span>:ended?<span className="tone muted">Ended</span>:<button type="button" className="btn-danger" disabled={busyId===r.eventId} onClick={()=>cancel(r.eventId)}>Cancel</button>}
    <button type="button" className="btn-outline" onClick={()=>setTicketId(ticketId===r.id?null:r.id)}>{ticketId===r.id?'Hide ticket':'View QR ticket'}</button>
   </div>
   {ticketId===r.id&&<div className="qr-ticket"><QRCodeSVG value={r.qrToken} size={190} level="M" includeMargin/><div><strong>Entry ticket</strong><p>{r.eventTitle}</p><small>Show this QR code to the event organizer at the entrance. Keep it private.</small><p className="ticket-status">{r.checkedInAt? `Checked in at ${new Date(r.checkedInAt).toLocaleString()}`:'Not checked in yet'}</p></div></div>}
  </article>;
 })}</section></>;
}