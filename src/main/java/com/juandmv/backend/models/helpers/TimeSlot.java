package com.juandmv.backend.models.helpers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Verifica si este slot se solapa con el rango de tiempo especificado
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return !start.isAfter(otherEnd) && !end.isBefore(otherStart);
    }

    /**
     * Elimina el solapamiento con el rango de tiempo especificado
     * y devuelve los slots resultantes (puede ser 0, 1 o 2 slots)
     */
    public List<TimeSlot> removeOverlap(LocalDateTime overlapStart, LocalDateTime overlapEnd) {
        List<TimeSlot> result = new ArrayList<>();

        // Caso 1: No hay solapamiento
        if (!overlaps(overlapStart, overlapEnd)) {
            result.add(this);
            return result;
        }

        // Caso 2: Solapamiento inicial
        if (start.isBefore(overlapStart) && end.isAfter(overlapStart)) {
            result.add(new TimeSlot(start, overlapStart));
        }

        // Caso 3: Solapamiento final
        if (start.isBefore(overlapEnd) && end.isAfter(overlapEnd)) {
            result.add(new TimeSlot(overlapEnd, end));
        }

        return result;
    }
}
