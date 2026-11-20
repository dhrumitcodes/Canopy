package com.societyhub.api.society;

import com.societyhub.api.college.College;
import com.societyhub.api.common.exception.ResourceNotFoundException;
import com.societyhub.api.society.dto.CreateSocietyRequest;
import com.societyhub.api.society.dto.SocietyResponse;
import com.societyhub.api.user.User;
import com.societyhub.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocietyService {
    private final SocietyRepository societyRepository;
    private final UserRepository userRepository;

    @Transactional
    public SocietyResponse createSociety(CreateSocietyRequest request) {
        User actor = currentUser();
        College college = actor.getCollege();
        if (college == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account is not assigned to a college");
        }
        String name = request.getName().trim();
        if (societyRepository.existsByNameIgnoreCaseAndCollegeId(name, college.getId())) {
            throw new IllegalArgumentException("A society with that name already exists in your college.");
        }

        Society society = Society.builder()
                .name(name)
                .description(request.getDescription())
                .category(request.getCategory())
                .college(college)
                .build();
        return mapToResponse(societyRepository.save(society));
    }

    @Transactional(readOnly = true)
    public List<SocietyResponse> getAllSocieties() {
        User actor = currentUserOrNull();
        if (actor == null || actor.getCollege() == null) return List.of();
        return societyRepository.findAllByCollegeIdOrderByNameAsc(actor.getCollege().getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public SocietyResponse getSocietyById(Long id) {
        return mapToResponse(findInCurrentCollege(id));
    }

    @Transactional
    public SocietyResponse updateSociety(Long id, CreateSocietyRequest request) {
        Society society = findInCurrentCollege(id);
        society.setName(request.getName().trim());
        society.setDescription(request.getDescription());
        society.setCategory(request.getCategory());
        return mapToResponse(societyRepository.save(society));
    }

    @Transactional
    public void deleteSociety(Long id) {
        Society society = findInCurrentCollege(id);
        societyRepository.delete(society);
    }

    @Transactional(readOnly = true)
    public Society findInCurrentCollege(Long id) {
        User actor = currentUser();
        if (actor.getRole() == com.societyhub.api.user.Role.PLATFORM_ADMIN) {
            return societyRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        }
        if (actor.getCollege() == null) throw new ResourceNotFoundException("Society not found");
        return societyRepository.findByIdAndCollegeId(id, actor.getCollege().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
    }

    private User currentUser() {
        User user = currentUserOrNull();
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return user;
    }

    private User currentUserOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private SocietyResponse mapToResponse(Society society) {
        return SocietyResponse.builder()
                .id(society.getId())
                .name(society.getName())
                .description(society.getDescription())
                .category(society.getCategory())
                .createdAt(society.getCreatedAt())
                .build();
    }
}