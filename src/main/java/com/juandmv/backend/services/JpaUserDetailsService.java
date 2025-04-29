package com.juandmv.backend.services;

import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.models.entities.CustomUserDetails;
import com.juandmv.backend.models.entities.User;
import com.juandmv.backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional()
    public UserDetails loadUserByUsername(String authUsername) throws UsernameNotFoundException {
        // El authUsername ahora tiene el formato "documentType:documentNumber"
        String[] parts = authUsername.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Formato de autenticación inválido");
        }

        DocumentType documentType = DocumentType.valueOf(parts[0]);
        String documentNumber = parts[1];

        // Buscar usuario por tipo de documento y número de documento
        User user = userRepository.findByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con documento " + documentType + ":" + documentNumber + " no encontrado"));

        System.out.println(user.getFullName());

        List<GrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Devolvemos un CustomUserDetails con la información necesaria
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(), // documentType:documentNumber como username
                user.getPassword(), // Usando documentNumber como password
                user.getEmail(),
                documentType,
                documentNumber,
                authorities,
                true,
                true,
                true,
                user.isEnabled()
        );
    }
}
