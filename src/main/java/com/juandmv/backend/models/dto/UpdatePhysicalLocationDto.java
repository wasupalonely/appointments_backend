package com.juandmv.backend.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePhysicalLocationDto {

    @Size(min = 5, max = 50)
    private String name;

    @Size(min = 10, max = 50)
    private String address;
}
