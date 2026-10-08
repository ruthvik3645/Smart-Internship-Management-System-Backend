package com.internship.util;

import com.internship.web.ApiException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Dates cross the API as plain yyyy-MM-dd strings, so JSON date modules are not needed. */
public final class Dates {
    private Dates() {}

    public static LocalDate parse(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw ApiException.bad("Dates must look like 2026-10-31.");
        }
    }

    public static String format(LocalDate d) {
        return d == null ? null : d.toString();
    }
}
