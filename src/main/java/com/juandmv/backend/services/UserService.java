package com.juandmv.backend.services;

import com.juandmv.backend.enums.Roles;
import com.juandmv.backend.exceptions.EmailAlreadyExistsException;
import com.juandmv.backend.exceptions.ResourceNotFoundException;
import com.juandmv.backend.models.dto.CreateUserDto;
import com.juandmv.backend.models.entities.Role;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.RoleRepository;
import com.juandmv.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(CreateUserDto createUserDto) {
        // Email validation
        Optional<User> optionalUser = this.findByEmail(createUserDto.getEmail());
        if (optionalUser.isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        List<Role> roles = new ArrayList<>();
        // Revisar la asignación de un rol
        Optional<Role> optionalRole = createUserDto.getRole() == Roles.DOCTOR ?
                roleRepository.findByName("ROLE_DOCTOR") :
                roleRepository.findByName("ROLE_USER");
        optionalRole.ifPresent(roles::add);
        createUserDto.setRoles(roles);

        createUserDto.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        User newUser = new User();
        newUser.setName(createUserDto.getName());
        newUser.setEmail(createUserDto.getEmail());
        newUser.setPassword(createUserDto.getPassword());
        newUser.setRoles(createUserDto.getRoles());

        return userRepository.save(newUser);
    }

    public void delete(Long id) {
        this.findById(id);
        userRepository.deleteById(id);
    }
}
