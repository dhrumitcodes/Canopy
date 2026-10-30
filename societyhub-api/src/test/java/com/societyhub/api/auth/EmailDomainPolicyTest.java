package com.societyhub.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailDomainPolicyTest {

    @Test
    void anEmptyListAllowsEveryone() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of());

        assertDoesNotThrow(() -> policy.check("anyone@gmail.com"));
    }

    @Test
    void aBlankSettingAllowsEveryone() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of(""));

        assertDoesNotThrow(() -> policy.check("anyone@gmail.com"));
    }

    @Test
    void acceptsAnAllowedDomain() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of("college.edu"));

        assertDoesNotThrow(() -> policy.check("student@college.edu"));
    }

    @Test
    void ignoresCaseAndSpacesAndALeadingAt() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of(" @College.EDU "));

        assertDoesNotThrow(() -> policy.check("Student@COLLEGE.edu"));
    }

    @Test
    void acceptsAnyOfSeveralDomains() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of("college.edu", "staff.college.edu"));

        assertDoesNotThrow(() -> policy.check("teacher@staff.college.edu"));
    }

    @Test
    void rejectsOtherDomainsAndExplainsWhy() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of("college.edu"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> policy.check("me@gmail.com"));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("@college.edu"));
    }

    @Test
    void rejectsLookAlikeDomains() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of("college.edu"));

        assertThrows(ResponseStatusException.class, () -> policy.check("me@college.edu.evil.com"));
        assertThrows(ResponseStatusException.class, () -> policy.check("me@notcollege.edu"));
    }

    @Test
    void rejectsAnAddressWithoutADomain() {
        EmailDomainPolicy policy = new EmailDomainPolicy(List.of("college.edu"));

        assertThrows(ResponseStatusException.class, () -> policy.check("not-an-email"));
        assertThrows(ResponseStatusException.class, () -> policy.check(null));
    }
}