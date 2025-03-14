package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSpecialtyDto {

    @Size(min = 5, max = 50)
    private String name;

    @Size(min = 10, max = 100)
    private String description;
}
