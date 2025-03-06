package com.juandmv.backend.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "exam_types")
@Getter
@Setter
public class ExamType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
