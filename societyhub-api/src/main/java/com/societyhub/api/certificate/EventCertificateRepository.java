package com.societyhub.api.certificate;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface EventCertificateRepository extends JpaRepository<EventCertificate,Long>{Optional<EventCertificate> findByRegistrationId(Long registrationId);}