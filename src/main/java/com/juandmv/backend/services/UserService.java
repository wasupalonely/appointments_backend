package com.juandmv.backend.services;

import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.BadRequestException;
import com.juandmv.backend.exceptions.EmailAlreadyExistsException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.mappers.UserMapper;
import com.juandmv.backend.models.dto.CreateAvailabilityDto;
import com.juandmv.backend.models.dto.CreateUserDto;
import com.juandmv.backend.models.dto.UpdateUserDto;
import com.juandmv.backend.models.entities.*;
import com.juandmv.backend.repositories.AvailabilityRepository;
import com.juandmv.backend.repositories.RoleRepository;
import com.juandmv.backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private SpecialtyService specialtyService;

    @Autowired
    private PhysicalLocationService physicalLocationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(String role) { return userRepository.findByRoles_Name(role); }

    public List<User> findBySpecialtyId(Long specialtyId) { return userRepository.findBySpecialtyId(specialtyId); }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User save(CreateUserDto createUserDto) {
        // Validar si el documento y el tipo de documento son válidos
        Optional<User> optionalDocument = userRepository.
                findByDocumentTypeAndDocumentNumber
                        (createUserDto.getDocumentType(), createUserDto.getDocumentNumber());

        if (optionalDocument.isPresent()) {
            throw new BadRequestException("El documento ya está registrado");
        }

        // Validar si el email ya existe
        Optional<User> optionalUser = this.findByEmail(createUserDto.getEmail());
        if (optionalUser.isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        List<Role> roles = new ArrayList<>();
        Optional<Role> optionalRole = createUserDto.getRole() == Roles.DOCTOR
                ? roleRepository.findByName("ROLE_DOCTOR")
                : createUserDto.getRole() == Roles.USER
                ? roleRepository.findByName("ROLE_USER")
                : roleRepository.findByName("ROLE_ADMIN");

        optionalRole.ifPresent(roles::add);
        createUserDto.setRoles(roles);

        User newUser = new User();
        newUser.setFirstName(createUserDto.getFirstName());
        newUser.setLastName(createUserDto.getLastName());
        newUser.setPhone(createUserDto.getPhone());
        newUser.setEmail(createUserDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        newUser.setDocumentType(createUserDto.getDocumentType());
        newUser.setDocumentNumber(createUserDto.getDocumentNumber());
        newUser.setRoles(createUserDto.getRoles());
        newUser.setGender(createUserDto.getGender());

        if (createUserDto.getRole() == Roles.DOCTOR) {
            if (createUserDto.getSpecialtyId() == null) {
                throw new BadRequestException("El doctor debe tener una especialidad asignada");
            }
            Specialty specialty = this.specialtyService.findById(createUserDto.getSpecialtyId());
            newUser.setSpecialty(specialty);

            if (createUserDto.getPhysicalLocationId() == null) {
                throw new BadRequestException("El doctor debe tener una ubicación asignada");
            }
            PhysicalLocation physicalLocation = this.physicalLocationService.findById(createUserDto.getPhysicalLocationId());
            newUser.setPhysicalLocation(physicalLocation);
        }

        User userSaved = userRepository.save(newUser);

        if (createUserDto.getRole() == Roles.DOCTOR && createUserDto.isDefaultSchedule()) {
            this.createDefaultAvailability(userSaved.getId());
        }

        return userSaved;
    }

    @Transactional
    public User update(Long id, @Valid UpdateUserDto updateUserDto) {
        User existingUser = this.findById(id);

        User emailValidation = this.findByEmail(updateUserDto.getEmail()).orElse(null);
        if (emailValidation != null && !emailValidation.getId().equals(id)) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        userMapper.updateUserFromDto(updateUserDto, existingUser);

        return userRepository.save(existingUser);
    }


    public void delete(Long id) {
        this.findById(id);
        userRepository.deleteById(id);
    }

    private void createDefaultAvailability(Long doctorId) {
        List<DayOfWeek> daysOfWeek = List.of(DayOfWeek.values());
        User doctor = this.findById(doctorId);

        for (DayOfWeek day : daysOfWeek) {
            if (day.name().equals("SATURDAY") || day.name().equals("SUNDAY")) {
                continue;
            } else {
                LocalDate today = LocalDate.now(); // Usar la fecha actual

                // Jornada mañana
                Availability availability = new Availability();
                availability.setDayOfWeek(day);
                availability.setStartTime(LocalDateTime.of(today, java.time.LocalTime.of(7, 0)));
                availability.setEndTime(LocalDateTime.of(today, java.time.LocalTime.of(12, 0)));
                availability.setRecurring(true);
                availability.setDoctor(doctor);

                // Jornada tarde
                Availability availability2 = new Availability();
                availability2.setDayOfWeek(day);
                availability2.setStartTime(LocalDateTime.of(today, java.time.LocalTime.of(14, 0)));
                availability2.setEndTime(LocalDateTime.of(today, java.time.LocalTime.of(18, 0)));
                availability2.setRecurring(true);
                availability2.setDoctor(doctor);

                this.availabilityRepository.save(availability);
                this.availabilityRepository.save(availability2);
            }
        }
    }
}
