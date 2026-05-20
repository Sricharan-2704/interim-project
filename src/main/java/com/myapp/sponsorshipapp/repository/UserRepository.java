package com.myapp.sponsorshipapp.repository;

import com.myapp.sponsorshipapp.entity.User;
import com.myapp.sponsorshipapp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNameIgnoreCase(String name);
    List<User> findByRole(Role role);
}

