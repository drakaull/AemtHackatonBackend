
package org.helha.aemthackatonbackend.domain.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MetadataCalculator {

    /**
     * Taille en bytes en UTF-8
     */
    public long computeSizeBytes(String content) {
        if (content == null) return 0;
        return content.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Nombre de lignes
     * Une note vide = 1 ligne
     */
    public int computeLineCount(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }
        return content.split("\r?\n", -1).length;
    }

    /**
     * Nombre de mots
     */
    public int computeWordCount(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }

    /**
     * Nombre de caractères (UTF-16, version Java)
     * Pour UTF-8 → sizeBytes
     */
    public int computeCharCount(String content) {
        if (content == null) return 0;
        return content.length();
    }
}
