package com.andesstay.msreservations.security;

import java.text.Normalizer;
import java.util.Locale;

public final class RoleUtils {

    private RoleUtils() {
    }

    // normaliza los roles para comparar mayúsculas y tildes de la misma forma.
    public static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        return Normalizer.normalize(role.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }
}
