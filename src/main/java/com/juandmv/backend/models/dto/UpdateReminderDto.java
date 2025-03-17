package com.juandmv.backend.models.dto;

import com.juandmv.backend.enums.ReminderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReminderDto {


    private String name;
    private String message;
    private ReminderType reminderType;
}
