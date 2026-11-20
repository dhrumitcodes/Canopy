package com.societyhub.api.society;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyRepository extends JpaRepository<Society, Long> {
    boolean existsByNameIgnoreCaseAndCollegeId(String name, Long collegeId);
    List<Society> findAllByCollegeIdOrderByNameAsc(Long collegeId);
    Optional<Society> findByIdAndCollegeId(Long id, Long collegeId);
}