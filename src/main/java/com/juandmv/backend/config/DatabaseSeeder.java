package com.juandmv.backend.config;

import com.juandmv.backend.models.entities.Role;
import com.juandmv.backend.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DatabaseSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {  // Evita duplicados
            Role admin = new Role("ROLE_ADMIN");
            Role user = new Role("ROLE_USER");
            Role doctor = new Role("ROLE_DOCTOR");

            roleRepository.saveAll(List.of(admin, user, doctor));
            System.out.println("Roles insertados correctamente.");
        }

        // TODO: Agregar los tipos de citas y tipos de exámenes
    }
}

