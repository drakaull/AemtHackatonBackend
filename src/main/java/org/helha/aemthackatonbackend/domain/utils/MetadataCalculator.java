package org.helha.aemthackatonbackend.domain.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MetadataCalculator {
    
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final TextContentRenderer TEXT_RENDERER = TextContentRenderer.builder().build();
    
    // Supprime la destination des liens markdown : [label](url) => label
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]*)\\]\\(([^)]+)\\)");
    // Supprime la destination des images markdown : ![alt](url) => alt
    private static final Pattern MD_IMAGE = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
    
    // Mots visibles : séquences lettres/chiffres (Unicode)
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    // Caractères visibles utiles : lettres/chiffres (Unicode)
    private static final Pattern CHAR_PATTERN = Pattern.compile("[\\p{L}\\p{N}]");
    
    /**
     * Retire les URLs des syntaxes markdown non rendues telles quelles (liens, images).
     * Exemple : [lien](https://google.com) => lien
     */
    private static String stripMarkdownNonVisibleParts(String markdown) {
        if (markdown == null || markdown.isBlank()) return "";
        
        // Images d'abord (pour éviter de matcher dedans avec la regex des liens)
        String s = MD_IMAGE.matcher(markdown).replaceAll("$1");
        // Puis liens
        s = MD_LINK.matcher(s).replaceAll("$1");
        
        return s;
    }
    
    /**
     * Markdown -> texte "visible" (sans syntaxe markdown).
     * Conserve des retours à la ligne logiques (paragraphes, listes, titres, etc.).
     */
    private static String markdownToPlainTextWithNewlines(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        
        // IMPORTANT : enlever les parties non visibles (URL des liens/images)
        String sanitized = stripMarkdownNonVisibleParts(markdown);
        
        Node document = MARKDOWN_PARSER.parse(sanitized);
        String text = TEXT_RENDERER.render(document);
        
        // Normalisation des retours à la ligne
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        
        // Nettoyage léger : supprimer espaces en fin de ligne
        text = text.replaceAll("[ \t]+\n", "\n");
        
        return text;
    }
    
    /**
     * Markdown -> texte "visible" mono-ligne (retours remplacés par espaces)
     */
    private static String markdownToPlainTextSingleLine(String markdown) {
        String t = markdownToPlainTextWithNewlines(markdown);
        if (t == null || t.isBlank()) return "";
        return t.replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    /**
     * Taille en bytes en UTF-8 (basée sur le texte visible)
     */
    public static long computeSizeBytes(String content) {
        String visible = markdownToPlainTextWithNewlines(content).trim();
        return visible.getBytes(StandardCharsets.UTF_8).length;
    }
    
    /**
     * Nombre de lignes logique :
     * - Si 0 caractère visible => 1 ligne
     * - Sinon => compter les lignes sans compter une ligne vide finale artificielle
     */
    public static int computeLineCount(String content) {
        String visible = markdownToPlainTextWithNewlines(content);
        
        if (visible == null || visible.trim().isEmpty()) {
            return 1;
        }
        
        // Enlever uniquement les retours à la ligne de fin (évite +1 ligne vide)
        String withoutTrailingNewlines = visible.replaceAll("\\n+$", "");
        
        if (withoutTrailingNewlines.trim().isEmpty()) {
            return 1;
        }
        
        return withoutTrailingNewlines.split("\\n").length;
    }
    
    /**
     * Nombre de mots "visibles utiles" :
     * on compte uniquement les séquences de lettres/chiffres.
     * Exemple : "# a\nb\nc" => mots = a, b, c => 3
     * Exemple : "[lien](https://google.com)" => mots = lien => 1
     */
    public static int computeWordCount(String content) {
        String visible = markdownToPlainTextSingleLine(content);
        
        if (visible.isBlank()) {
            return 0;
        }
        
        int count = 0;
        Matcher m = WORD_PATTERN.matcher(visible);
        while (m.find()) {
            count++;
        }
        return count;
    }
    
    /**
     * Nombre de caractères "visibles utiles" :
     * on compte uniquement les lettres/chiffres (pas espaces, retours, ponctuation, syntaxe markdown, URL cachée).
     * Exemple : "# a\nb\nc" => caractères = a, b, c => 3
     * Exemple : "[lien](https://google.com)" => caractères = l,i,e,n => 4
     */
    public static int computeCharCount(String content) {
        String visible = markdownToPlainTextWithNewlines(content);
        
        if (visible == null || visible.trim().isEmpty()) {
            return 0;
        }
        
        int count = 0;
        Matcher m = CHAR_PATTERN.matcher(visible);
        while (m.find()) {
            count++;
        }
        return count;
    }
}
