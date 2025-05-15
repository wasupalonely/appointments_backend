package com.juandmv.backend.config;

import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.enums.Gender;
import com.juandmv.backend.models.entities.AppointmentType;
import com.juandmv.backend.models.entities.Role;
import com.juandmv.backend.models.entities.Specialty;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.AppointmentTypeRepository;
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
import java.util.stream.Collectors;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoleRepository roleRepository,
                          SpecialtyRepository specialtyRepository,
                          UserRepository userRepository,
                          AppointmentTypeRepository appointmentTypeRepository,
                          PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.specialtyRepository = specialtyRepository;
        this.userRepository = userRepository;
        this.appointmentTypeRepository = appointmentTypeRepository;
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
                userDto.setEmail("admin@admin.com");
                userDto.setDocumentType(DocumentType.CITIZENSHIP_CARD);
                userDto.setDocumentNumber("1234567890");
                userDto.setPassword(passwordEncoder.encode("admin123"));
                userDto.setPhone("1234567890");
                userDto.setGender(Gender.MALE);
                this.userRepository.save(userDto);

                System.out.println("Usuario admin insertado correctamente.");
            }

        }

        if (appointmentTypeRepository.count() == 0) {
            Map<String, Specialty> specialties = specialtyRepository.findAll()
                    .stream()
                    .map(specialty -> Map.entry(specialty.getName(), specialty))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            List<Map<String, Object>> types = List.of(
                    Map.of("name", "Consulta odontológica general",
                            "specialty", specialties.get("Odontologia"),
                            "isGeneral", true,
                            "durationInMinutes", 40),
                    Map.of("name", "Consulta general",
                            "specialty", specialties.get("Medicina general"),
                            "isGeneral", true,
                            "durationInMinutes", 30),
                    Map.of("name", "Consulta oftalmológica general",
                            "specialty", specialties.get("Oftalmologia"),
                            "isGeneral", true,
                            "durationInMinutes", 60),
                    Map.of("name", "Examen de sangre",
                            "specialty", specialties.get("Medicina general"),
                            "isGeneral", false,
                            "durationInMinutes", 15),
                    Map.of("name", "Oftalmoscopia",
                            "specialty", specialties.get("Oftalmologia"),
                            "isGeneral", false,
                            "durationInMinutes", 30),
                    Map.of("name", "Cirugía de cordales",
                            "specialty", specialties.get("Odontologia"),
                            "isGeneral", false,
                            "durationInMinutes", 60)
            );

            types.forEach(type -> {
                Specialty specialty = (Specialty) type.get("specialty");
                if (specialty == null) {
                    throw new RuntimeException("No se encontró la especialidad para: " + type.get("name"));
                }

                AppointmentType newType = new AppointmentType();
                newType.setName((String) type.get("name"));
                newType.setSpecialty(specialty);
                newType.setIsGeneral((Boolean) type.get("isGeneral"));
                newType.setDurationInMinutes((Integer) type.get("durationInMinutes"));
                appointmentTypeRepository.save(newType);
            });
            System.out.println("Tipos de citas insertados correctamente.");

        }

        // TODO: Agregar los tipos de citas y tipos de exámenes
    }
}

