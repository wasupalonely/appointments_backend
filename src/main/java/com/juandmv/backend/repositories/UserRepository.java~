package com.juandmv.backend.repositories;

import com.juandmv.backend.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByEmail(String email);

    public List<User> findByRoles_Name(String name);
}