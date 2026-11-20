import {useEffect,useState} from 'react'; import {eventApi} from '../api/eventApi';
export default function EventFeedbackPanel({eventId,canManage,canSubmit}){
const [rating,setRating]=useState(5),[comments,setComments]=useState(''),[items,setItems]=useState([]),[message,setMessage]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false);
useEffect(()=>{if(canManage)eventApi.getFeedback(eventId).then(setItems).catch(e=>setError(e.response?.data?.message||'Could not load feedback.'));},[eventId,canManage]);
const submit=async(e)=>{e.preventDefault();setBusy(true);setError('');setMessage('');try{await eventApi.submitFeedback(eventId,{rating:Number(rating),comments});setMessage('Thank you. Your feedback has been saved.');}catch(e){setError(e.response?.data?.message||'Verified attendance is required to submit feedback.');}finally{setBusy(false);}};
if(!canManage&&!canSubmit)return null;
return <section className="panel" style={{marginTop:18}}><h2>Event feedback</h2>
{canSubmit&&<form onSubmit={submit} className="feedback-form"><label htmlFor="feedback-rating">Your rating</label><select id="feedback-rating" className="field" value={rating} onChange={e=>setRating(e.target.value)}>{[5,4,3,2,1].map(n=><option value={n} key={n}>{n} / 5</option>)}</select>
<label htmlFor="feedback-comments">Comments (optional)</label><textarea id="feedback-comments" className="field full" rows="3" maxLength="2000" value={comments} onChange={e=>setComments(e.target.value)} placeholder="What did you think of the event?"/>
<div className="actions-row"><button className="btn-solid" disabled={busy}>{busy?'Saving…':'Submit feedback'}</button></div></form>}
{message&&<p className="tone good" role="status">{message}</p>}{error&&<p className="form-error-light" role="alert">{error}</p>}
{canManage&&<div className="table-wrap" style={{marginTop:14}}><p className="muted">{items.length} response(s)</p>{items.length>0&&<table><thead><tr><th>Attendee</th><th>Rating</th><th>Comments</th><th>Submitted</th></tr></thead><tbody>{items.map(i=><tr key={i.id}><td>{i.userFullName}</td><td>{'★'.repeat(i.rating)}{'☆'.repeat(5-i.rating)}</td><td>{i.comments||'—'}</td><td>{new Date(i.createdAt).toLocaleDateString()}</td></tr>)}</tbody></table>}</div>}
</section>;
}
