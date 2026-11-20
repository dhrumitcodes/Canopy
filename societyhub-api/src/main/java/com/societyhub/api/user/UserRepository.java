package com.societyhub.api.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository public interface UserRepository extends JpaRepository<User,Long>{
 Optional<User> findByEmail(String email); boolean existsByEmail(String email);
 List<User> findByCollegeIdAndRole(Long collegeId,Role role);
}