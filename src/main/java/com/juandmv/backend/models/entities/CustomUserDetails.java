package com.juandmv.backend.models.entities;

import com.juandmv.backend.enums.DocumentType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private final Long id;
    private final String email;
    private final DocumentType documentType;
    private final String documentNumber;

    public CustomUserDetails(Long id, String username, String password, String email,
                             DocumentType documentType, String documentNumber,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean accountNonExpired, boolean accountNonLocked,
                             boolean credentialsNonExpired, boolean enabled) {
        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.email = email;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
}
