package net.miarma.api.util;

/**
 * Clase de utilidad para censurar nombres.
 * Censura los nombres dejando las primeras 3 letras visibles y el resto con asteriscos.
 * Si el nombre es muy largo, lo acorta a 16 caracteres y añade "..." al final.
 * @author José Manuel Amador Gallardo
 */
public class NameCensorer {

    public static String censor(String nombre) {
        if (nombre == null || nombre.isBlank()) return "";

        String[] palabras = nombre.trim().split("\\s+");

        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i];
            int len = palabra.length();

            if (len > 3) {
                palabras[i] = palabra.substring(0, 3) + "*".repeat(len - 3);
            } else if (len > 0) {
                palabras[i] = palabra.charAt(0) + "*".repeat(len - 1);
            }
        }

        String censurado = String.join(" ", palabras);

        if (censurado.length() > 16) {
            censurado = censurado.substring(0, 16) + "...";
        }

        return censurado;
    }
}
