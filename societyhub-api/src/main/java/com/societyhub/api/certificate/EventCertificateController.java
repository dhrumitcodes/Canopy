package com.societyhub.api.certificate;
import com.societyhub.api.user.*; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/v1/events") @RequiredArgsConstructor
public class EventCertificateController{
private final EventCertificateService service; private final UserRepository users;
@GetMapping(value="/{eventId}/certificate",produces=MediaType.APPLICATION_PDF_VALUE)
public ResponseEntity<byte[]> certificate(@PathVariable Long eventId,Authentication auth){
User u=users.findByEmail(auth.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found"));
return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=Canopy-certificate-"+eventId+".pdf").body(service.issuePdf(eventId,u));
}
}