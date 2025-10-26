package org.example.repository;

// Member: Asmal - Admin management
// Related database tables: admin & activity log tables

import org.example.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
