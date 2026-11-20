package com.societyhub.api.college;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {
    Optional<College> findByEmailDomainIgnoreCase(String emailDomain);
    boolean existsByEmailDomainIgnoreCase(String emailDomain);
}