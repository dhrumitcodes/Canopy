package com.societyhub.api.security;

import com.societyhub.api.college.College;
import com.societyhub.api.membership.Membership;
import com.societyhub.api.membership.MembershipRepository;
import com.societyhub.api.membership.SocietyRole;
import com.societyhub.api.society.Society;
import com.societyhub.api.society.SocietyRepository;
import com.societyhub.api.user.Role;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SocietySecurityEvaluatorTest {
    private static final String EMAIL = "someone@college.edu";
    @Mock private MembershipRepository membershipRepository;
    @Mock private UserRepository userRepository;
    @Mock private SocietyRepository societyRepository;
    @InjectMocks private SocietySecurityEvaluator evaluator;
    private College college;

    @BeforeEach
    void clear() {
        SecurityContextHolder.clearContext();
        college = mock(College.class);
        when(college.getId()).thenReturn(10L);
        Society society = mock(Society.class);
        when(societyRepository.findByIdAndCollegeId(1L, 10L)).thenReturn(Optional.of(society));
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void signInAs(Role globalRole) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(user.getRole()).thenReturn(globalRole);
        when(user.getCollege()).thenReturn(globalRole == Role.PLATFORM_ADMIN ? null : college);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    private void memberOf(long societyId, SocietyRole role) {
        Membership m = Membership.builder().societyRole(role).build();
        when(membershipRepository.findByUserIdAndSocietyId(7L, societyId)).thenReturn(Optional.of(m));
    }

    @Test
    void everyOfficerRoleCanManageTheirSociety() {
        for (SocietyRole role : List.of(SocietyRole.PRESIDENT, SocietyRole.VICE_PRESIDENT,
                SocietyRole.SECRETARY, SocietyRole.TREASURER)) {
            signInAs(Role.STUDENT);
            memberOf(1L, role);
            assertTrue(evaluator.isSocietyOfficerOrAdmin(1L), role + " should be able to manage");
        }
    }

    @Test
    void aPlainMemberCannotManage() {
        signInAs(Role.STUDENT);
        memberOf(1L, SocietyRole.MEMBER);
        assertFalse(evaluator.isSocietyOfficerOrAdmin(1L));
    }

    @Test
    void anOfficerOfOneSocietyCannotManageAnother() {
        signInAs(Role.STUDENT);
        memberOf(1L, SocietyRole.PRESIDENT);
        when(societyRepository.findByIdAndCollegeId(2L, 10L)).thenReturn(Optional.empty());
        assertFalse(evaluator.isSocietyOfficerOrAdmin(2L));
    }

    @Test
    void aNonMemberCannotManage() {
        signInAs(Role.STUDENT);
        when(membershipRepository.findByUserIdAndSocietyId(7L, 1L)).thenReturn(Optional.empty());
        assertFalse(evaluator.isSocietyOfficerOrAdmin(1L));
    }

    @Test
    void collegeAdminCanManageOnlyTheirOwnCollegeSocieties() {
        signInAs(Role.COLLEGE_ADMIN);
        assertTrue(evaluator.isSocietyOfficerOrAdmin(1L));
        assertTrue(evaluator.isSocietyPresident(1L));
        assertTrue(evaluator.isCollegeAdmin());
        when(societyRepository.findByIdAndCollegeId(2L, 10L)).thenReturn(Optional.empty());
        assertFalse(evaluator.isSocietyOfficerOrAdmin(2L));
    }

    @Test
    void platformAdminCanAccessSocietiesAcrossColleges() {
        signInAs(Role.PLATFORM_ADMIN);
        assertTrue(evaluator.isSocietyOfficerOrAdmin(1L));
        assertTrue(evaluator.isSocietyPresident(999L));
        assertTrue(evaluator.isPlatformAdmin());
    }

    @Test
    void onlyThePresidentPassesThePresidentCheck() {
        signInAs(Role.STUDENT);
        memberOf(1L, SocietyRole.SECRETARY);
        assertFalse(evaluator.isSocietyPresident(1L));
        memberOf(1L, SocietyRole.PRESIDENT);
        assertTrue(evaluator.isSocietyPresident(1L));
    }

    @Test
    void aSocietyPresidentIsNotACollegeAdmin() {
        signInAs(Role.STUDENT);
        memberOf(1L, SocietyRole.PRESIDENT);
        assertFalse(evaluator.isCollegeAdmin());
    }

    @Test
    void anonymousVisitorsCanDoNothing() {
        assertFalse(evaluator.isSocietyOfficerOrAdmin(1L));
        assertFalse(evaluator.isSocietyPresident(1L));
        assertFalse(evaluator.isCollegeAdmin());
    }
}