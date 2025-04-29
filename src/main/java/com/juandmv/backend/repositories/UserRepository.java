package com.juandmv.backend.repositories;

import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

    public List<User> findByRoles_Name(String name);

    public List<User> findBySpecialtyId(Long id);
}