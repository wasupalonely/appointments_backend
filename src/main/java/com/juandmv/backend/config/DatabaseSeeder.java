package com.juandmv.backend.config;

import com.juandmv.backend.models.entities.Role;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.RoleRepository;
import com.juandmv.backend.repositories.SpecialtyRepository;
import com.juandmv.backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoleRepository roleRepository,
                          SpecialtyRepository specialtyRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.specialtyRepository = specialtyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (specialtyRepository.count() == 0) {
            Map<String, String> specialties = Map.of(
                    "Medicina general", "Médico general, encargado de atender a pacientes de todas las edades",
                    "Pediatria", "Encargado de atender a pacientes de menos de 18 años",
                    "Odontologia", "Encargado de atender a pacientes con problemas dentales",
                    "Oftalmologia", "Encargado de atender a pacientes con problemas oculares",
                    "Cirujano", "Atiende citas especializadas de acuerdo a la necesidad"
            );

            specialties.forEach((name, description) -> {
                Specialty newSpecialty = new Specialty();
                newSpecialty.setName(name);
                newSpecialty.setDescription(description);
                specialtyRepository.save(newSpecialty);
            });
            System.out.println("Especialidades insertadas correctamente.");
        }

        if (userRepository.count() == 0) {
            Optional<Role> userRole = roleRepository.findByName("ROLE_ADMIN");

            if (userRole.isPresent()) {
                User userDto = new User();
                userDto.setRoles(List.of(userRole.get()));
                userDto.setFirstName("Admin");
                userDto.setLastName("Admin");
                userDto.setUsername("admin");
                userDto.setEmail("admin@admin.com");
                userDto.setPassword(passwordEncoder.encode("admin123"));
                userDto.setPhone("1234567890");
                this.userRepository.save(userDto);

                System.out.println("Usuario admin insertado correctamente.");
            }

        }

        // TODO: Agregar los tipos de citas y tipos de exámenes
    }
}

