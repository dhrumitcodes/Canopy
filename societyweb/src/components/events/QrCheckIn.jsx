import { useEffect, useRef, useState } from 'react';
import { eventApi } from '../../api/eventApi';
export default function QrCheckIn() {
 const video=useRef(null),stream=useRef(null),frame=useRef(null),[manual,setManual]=useState(''),[busy,setBusy]=useState(false),[message,setMessage]=useState(''),[scanning,setScanning]=useState(false);
 const stop=()=>{if(frame.current)cancelAnimationFrame(frame.current);stream.current?.getTracks().forEach(t=>t.stop());stream.current=null;if(video.current)video.current.srcObject=null;setScanning(false);};
 useEffect(()=>()=>{if(frame.current)cancelAnimationFrame(frame.current);stream.current?.getTracks().forEach(t=>t.stop());},[]);
 const submit=async(token)=>{if(!token?.trim())return false;setBusy(true);setMessage('');try{const result=await eventApi.checkIn(token.trim());setMessage(`Checked in: ${result.userFullName || result.userEmail}`);setManual('');stop();return true;}catch(e){setMessage(e.response?.data?.message||'Ticket could not be checked in.');return false;}finally{setBusy(false);}};
 const start=async()=>{
  setMessage('');
  if(!('BarcodeDetector' in window)){setMessage('Camera QR scanning is not supported in this browser. Enter the ticket code below.');return;}
  try{const detector=new window.BarcodeDetector({formats:['qr_code']});stream.current=await navigator.mediaDevices.getUserMedia({video:{facingMode:'environment'}});video.current.srcObject=stream.current;await video.current.play();setScanning(true);
   const scan=async()=>{if(!video.current||!stream.current)return;try{const codes=await detector.detect(video.current);if(codes.length){const success=await submit(codes[0].rawValue);if(success)return;}}catch{/* keep scanning while camera frames settle */}frame.current=requestAnimationFrame(scan);};scan();
  }catch{stop();setMessage('Camera access failed. Check browser permissions or enter the ticket code manually.');}
 };
 return <section className="panel checkin-panel"><h2>QR attendance check-in</h2><p className="muted">Scan a registered attendee's ticket. Each ticket can only be checked in once.</p>
  <video ref={video} className="qr-video" muted playsInline hidden={!scanning}/>
  <div className="actions-row"><button type="button" className="btn-solid" disabled={busy||scanning} onClick={start}>Start camera scanner</button>{scanning&&<button type="button" className="btn-outline" onClick={stop}>Stop scanner</button>}</div>
  <label htmlFor="ticket-code">Or enter QR ticket code</label><div className="actions-row"><input id="ticket-code" className="field full" value={manual} onChange={e=>setManual(e.target.value)} placeholder="Paste ticket code"/><button type="button" className="btn-solid" disabled={busy||!manual.trim()} onClick={()=>submit(manual)}>Check in</button></div>
  {message&&<p className="form-error-light" role="status">{message}</p>}
 </section>;
}