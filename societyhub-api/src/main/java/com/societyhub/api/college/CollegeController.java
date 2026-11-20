package com.societyhub.api.college;

import com.societyhub.api.college.dto.CreateCollegeRequest;
import com.societyhub.api.user.Role;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform/colleges")
@RequiredArgsConstructor
public class CollegeController {
    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> createCollege(@Valid @RequestBody CreateCollegeRequest request) {
        String domain = request.getEmailDomain().trim().toLowerCase(Locale.ROOT);
        String adminEmail = request.getAdminEmail().trim().toLowerCase(Locale.ROOT);

        if (collegeRepository.existsByEmailDomainIgnoreCase(domain)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A college is already registered for this email domain");
        }
        if (!adminEmail.substring(adminEmail.lastIndexOf('@') + 1).equals(domain)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The college admin email must use the college email domain");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "The admin must create a normal Canopy account before college onboarding"));
        if (admin.getCollege() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This account is already assigned to a college");
        }
        if (admin.getRole() == Role.PLATFORM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A platform admin cannot be assigned as a college admin");
        }

        College college = collegeRepository.save(College.builder()
                .name(request.getName().trim()).emailDomain(domain).build());
        admin.setCollege(college);
        admin.setRole(Role.COLLEGE_ADMIN);
        userRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", college.getId(), "name", college.getName(),
                "emailDomain", college.getEmailDomain(), "adminEmail", admin.getEmail()));
    }
}