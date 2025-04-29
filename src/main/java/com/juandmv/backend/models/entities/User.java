package com.juandmv.backend.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.juandmv.backend.enums.DocumentType;
import com.juandmv.backend.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(unique = true, nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private boolean enabled = true;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    // TODO: Implementar roles de usuario (estudiante, ciudadano)

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToMany
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
    )
    private List<Role> roles;

    @ManyToOne(optional = true)
    @JoinColumn(name = "physicalLocationId", referencedColumnName = "id")
    private PhysicalLocation physicalLocation;

    @ManyToOne(optional = true)
    @JoinColumn(name = "specialtyId", referencedColumnName = "id")
    private Specialty specialty;

    public User() {
        this.roles = new ArrayList<>();
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getPhysicalLocationAddress() {
        if (this.physicalLocation == null) return "";
        return this.physicalLocation.getAddress() + ", " + this.physicalLocation.getName();
    }
}
