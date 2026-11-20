package com.societyhub.api.certificate;
import com.societyhub.api.event.EventRegistration; import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="event_certificates") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventCertificate{
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="registration_id",nullable=false,unique=true) private EventRegistration registration;
@Column(name="certificate_number",nullable=false,unique=true,length=48) private String certificateNumber;
@Column(name="issued_at",nullable=false,updatable=false) @Builder.Default private LocalDateTime issuedAt=LocalDateTime.now();
}