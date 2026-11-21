package com.societyhub.api.certificate;

import com.societyhub.api.event.*;
import com.societyhub.api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCertificateServiceTest {
 @Mock EventRegistrationRepository registrations;
 @Mock EventCertificateRepository certificates;
 @InjectMocks EventCertificateService service;

 private User user(){return User.builder().id(12L).fullName("Attendee").email("a@college.edu").build();}
 private EventRegistration checkedInRegistration(User user){
  Event event=Event.builder().id(4L).title("Design Sprint").status(EventStatus.APPROVED).startTime(LocalDateTime.of(2026,11,20,10,0)).build();
  return EventRegistration.builder().id(2L).event(event).user(user).checkedInAt(LocalDateTime.now()).build();
 }
 @Test void refusesCertificateWithoutVerifiedAttendance(){
  User user=user(); Event event=Event.builder().id(4L).title("Design Sprint").status(EventStatus.APPROVED).build();
  when(registrations.findByEventIdAndUserId(4L,12L)).thenReturn(Optional.of(EventRegistration.builder().id(2L).event(event).user(user).build()));
  ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.issuePdf(4L,user));
  assertEquals(403,ex.getStatusCode().value()); verifyNoInteractions(certificates);
 }
 @Test void issuesAReadablePdfWithTheExistingPersistentCertificateNumber(){
  User user=user(); EventRegistration registration=checkedInRegistration(user);
  EventCertificate certificate=EventCertificate.builder().id(3L).registration(registration).certificateNumber("CAN-TEST-1234567890").build();
  when(registrations.findByEventIdAndUserId(4L,12L)).thenReturn(Optional.of(registration));
  when(certificates.findByRegistrationId(2L)).thenReturn(Optional.of(certificate));
  byte[] pdf=service.issuePdf(4L,user);
  String content=new String(pdf,java.nio.charset.StandardCharsets.US_ASCII);
  assertTrue(content.startsWith("%PDF-1.4")); assertTrue(content.contains("CERTIFICATE OF PARTICIPATION"));
  assertTrue(content.contains("CAN-TEST-1234567890")); assertTrue(content.contains("Attendee"));
 }
}
