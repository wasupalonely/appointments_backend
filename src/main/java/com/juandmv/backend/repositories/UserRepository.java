package com.juandmv.backend.repositories;

import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

    @Query("SELECT u FROM users u JOIN u.roles r WHERE r.name = :roleName")
    public List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM users u JOIN u.roles r")
    public List<User> findAllUsersWithRoles();

    public List<User> findBySpecialtyId(Long id);
}