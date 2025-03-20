package com.juandmv.backend.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {
    public static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'del' yyyy", new Locale("es", "ES"));
    public static ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors()
                .forEach(err -> errors.put(err.getField(), "Campo " + err.getField() + " " + err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
}
