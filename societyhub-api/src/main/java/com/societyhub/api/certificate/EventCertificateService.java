package com.societyhub.api.certificate;
import com.societyhub.api.event.*; import com.societyhub.api.user.User; import lombok.RequiredArgsConstructor; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException; import java.nio.charset.StandardCharsets; import java.util.UUID;
@Service @RequiredArgsConstructor
public class EventCertificateService{
private final EventRegistrationRepository registrations; private final EventCertificateRepository certificates;
@Transactional public byte[] issuePdf(Long eventId,User user){
EventRegistration r=registrations.findByEventIdAndUserId(eventId,user.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Registration not found"));
if(r.getCheckedInAt()==null||r.getEvent().getStatus()!=EventStatus.APPROVED)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Certificates are issued only to verified attendees");
EventCertificate c=certificates.findByRegistrationId(r.getId()).orElseGet(()->certificates.save(EventCertificate.builder().registration(r).certificateNumber("CAN-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase()).build()));
String body="BT /F1 24 Tf 72 700 Td (CERTIFICATE OF PARTICIPATION) Tj /F1 16 Tf 0 -55 Td ("+safe(user.getFullName())+") Tj /F1 12 Tf 0 -30 Td (has successfully participated in) Tj /F1 18 Tf 0 -35 Td ("+safe(r.getEvent().getTitle())+") Tj /F1 12 Tf 0 -30 Td (Event date: "+r.getEvent().getStartTime().toLocalDate()+") Tj 0 -35 Td (Certificate ID: "+c.getCertificateNumber()+") Tj ET";
String[] obj={"1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n","2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n","3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n","4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n","5 0 obj << /Length "+body.getBytes(StandardCharsets.US_ASCII).length+" >> stream\n"+body+"\nendstream endobj\n"};
StringBuilder pdf=new StringBuilder("%PDF-1.4\n"); int[] offsets=new int[6];
for(int i=0;i<obj.length;i++){offsets[i+1]=pdf.toString().getBytes(StandardCharsets.US_ASCII).length;pdf.append(obj[i]);}
int xref=pdf.toString().getBytes(StandardCharsets.US_ASCII).length; pdf.append("xref\n0 6\n0000000000 65535 f \n");
for(int i=1;i<=5;i++)pdf.append(String.format("%010d 00000 n \n",offsets[i]));
pdf.append("trailer << /Size 6 /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF"); return pdf.toString().getBytes(StandardCharsets.US_ASCII);
}
private String safe(String s){return s==null?"":s.replaceAll("[^\\x20-\\x7E]"," ").replace("\\","\\\\").replace("(","\\(").replace(")","\\)");}
}