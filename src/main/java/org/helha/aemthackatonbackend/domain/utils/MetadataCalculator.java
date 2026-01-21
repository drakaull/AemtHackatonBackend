package org.helha.aemthackatonbackend.domain.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MetadataCalculator {
    
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();
    
    /**
     * Transforme le markdown en texte "visible"
     * (sans syntaxe markdown, sans HTML)
     */
    private static String markdownToPlainText(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        
        Node document = MARKDOWN_PARSER.parse(markdown);
        String html = HTML_RENDERER.render(document);
        
        // Jsoup enlève toutes les balises HTML et normalise les espaces
        return Jsoup.parse(html).text();
    }
    
    /**
     * Taille en bytes UTF-8 (basée sur le texte visible)
     */
    public static long computeSizeBytes(String content) {
        String visibleText = markdownToPlainText(content);
        return visibleText.getBytes(StandardCharsets.UTF_8).length;
    }
    
    /**
     * Nombre de lignes (texte visible)
     * Une note vide = 1 ligne
     */
    public static int computeLineCount(String content) {
        String visibleText = markdownToPlainText(content);
        
        if (visibleText.isEmpty()) {
            return 1;
        }
        
        return visibleText.split("\r?\n", -1).length;
    }
    
    /**
     * Nombre de mots (texte visible)
     */
    public static int computeWordCount(String content) {
        String visibleText = markdownToPlainText(content);
        
        if (visibleText.isBlank()) {
            return 0;
        }
        
        return visibleText.trim().split("\\s+").length;
    }
    
    /**
     * Nombre de caractères (texte visible)
     */
    public static int computeCharCount(String content) {
        String visibleText = markdownToPlainText(content);
        return visibleText.length();
    }
}
