package com.societyhub.api.security;

import com.societyhub.api.membership.Membership;
import com.societyhub.api.membership.MembershipRepository;
import com.societyhub.api.membership.SocietyRole;
import com.societyhub.api.society.SocietyRepository;
import com.societyhub.api.user.Role;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("societySecurity")
@RequiredArgsConstructor
public class SocietySecurityEvaluator {
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final SocietyRepository societyRepository;

    private static final List<SocietyRole> OFFICER_ROLES = List.of(
            SocietyRole.PRESIDENT, SocietyRole.VICE_PRESIDENT,
            SocietyRole.SECRETARY, SocietyRole.TREASURER
    );

    public boolean isCollegeAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole() == Role.COLLEGE_ADMIN && user.getCollege() != null;
    }

    public boolean isPlatformAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole() == Role.PLATFORM_ADMIN;
    }

    public boolean canAccessSociety(Long societyId) {
        User user = getCurrentUser();
        if (user == null) return false;
        if (user.getRole() == Role.PLATFORM_ADMIN) return true;
        if (user.getCollege() == null) return false;
        return societyRepository.findByIdAndCollegeId(societyId, user.getCollege().getId()).isPresent();
    }

    public boolean isSocietyMemberOrAdmin(Long societyId) {
        User user = getCurrentUser();
        if (!canAccessSociety(societyId)) return false;
        if (user.getRole() == Role.PLATFORM_ADMIN || user.getRole() == Role.COLLEGE_ADMIN) return true;
        return membershipRepository.existsByUserIdAndSocietyId(user.getId(), societyId);
    }

    public boolean isSocietyOfficerOrAdmin(Long societyId) {
        User user = getCurrentUser();
        if (!canAccessSociety(societyId)) return false;
        if (user.getRole() == Role.PLATFORM_ADMIN || user.getRole() == Role.COLLEGE_ADMIN) return true;
        Optional<Membership> membership = membershipRepository.findByUserIdAndSocietyId(user.getId(), societyId);
        return membership.map(m -> OFFICER_ROLES.contains(m.getSocietyRole())).orElse(false);
    }

    public boolean isSocietyPresident(Long societyId) {
        User user = getCurrentUser();
        if (!canAccessSociety(societyId)) return false;
        if (user.getRole() == Role.PLATFORM_ADMIN || user.getRole() == Role.COLLEGE_ADMIN) return true;
        return membershipRepository.findByUserIdAndSocietyId(user.getId(), societyId)
                .map(m -> m.getSocietyRole() == SocietyRole.PRESIDENT).orElse(false);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        String email = principal instanceof UserDetails details ? details.getUsername()
                : principal instanceof String value ? value : null;
        return email == null ? null : userRepository.findByEmail(email).orElse(null);
    }
}