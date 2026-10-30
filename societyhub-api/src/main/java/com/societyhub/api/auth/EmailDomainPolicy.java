package com.societyhub.api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class EmailDomainPolicy {

    private final List<String> allowedDomains;

    public EmailDomainPolicy(@Value("${app.registration.allowed-email-domains:}") List<String> domains) {
        this.allowedDomains = domains.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .map(d -> d.startsWith("@") ? d.substring(1) : d)
                .filter(d -> !d.isEmpty())
                .toList();
    }

    public void check(String email) {
        if (allowedDomains.isEmpty()) return;

        String domain = email == null || !email.contains("@")
                ? ""
                : email.substring(email.lastIndexOf('@') + 1).trim().toLowerCase();

        if (!allowedDomains.contains(domain)) {
            String shown = String.join(", ", allowedDomains.stream().map(d -> "@" + d).toList());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please sign up with your college email address (" + shown + ").");
        }
    }
}