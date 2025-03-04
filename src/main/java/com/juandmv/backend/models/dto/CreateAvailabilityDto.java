package com.juandmv.backend.models.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.Date;

@Getter
@Setter
public class CreateAvailabilityDto {

    private Long doctorId;
    private DayOfWeek dayOfWeek;
    private Date startTime;
    private Date endTime;
    private boolean isRecurring;
}
