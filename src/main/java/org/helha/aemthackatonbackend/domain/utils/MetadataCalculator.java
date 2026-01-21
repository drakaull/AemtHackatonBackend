package org.helha.aemthackatonbackend.domain.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MetadataCalculator {
    
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();
    
    /**
     * Markdown -> HTML (CommonMark)
     */
    private static String markdownToHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) return "";
        Node document = MARKDOWN_PARSER.parse(markdown);
        return HTML_RENDERER.render(document);
    }
    
    /**
     * Markdown -> texte "visible" (sans syntaxe markdown) en conservant des retours à la ligne
     * pour les séparations de blocs (titres, paragraphes, items, etc.).
     */
    private static String markdownToPlainTextWithNewlines(String markdown) {
        String html = markdownToHtml(markdown);
        if (html.isBlank()) return "";
        
        Document doc = Jsoup.parse(html);
        
        // Conserver les retours à la ligne : <br> et fins de blocs
        doc.select("br").append("\\n");
        doc.select("p, div, li, h1, h2, h3, h4, h5, h6, blockquote, pre, tr")
                .append("\\n");
        
        // wholeText() conserve les \n injectés (contrairement à text())
        String raw = doc.body().wholeText();
        
        // Normalisation minimale :
        // - convertir les "\n" littéraux injectés en vrais retours à la ligne
        // - supprimer les espaces inutiles autour des retours
        String withNewlines = raw.replace("\\n", "\n")
                .replace("\r\n", "\n")
                .replace("\r", "\n");
        
        // Optionnel: éviter des lignes vides multiples excessives
        withNewlines = withNewlines.replaceAll("[ \t]+\n", "\n");
        
        return withNewlines.trim();
    }
    
    /**
     * Markdown -> texte "visible" mono-ligne pour mots/caractères (retours remplacés par espaces)
     */
    private static String markdownToPlainTextSingleLine(String markdown) {
        String t = markdownToPlainTextWithNewlines(markdown);
        if (t.isBlank()) return "";
        return t.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Taille en bytes UTF-8 (basée sur le texte visible)
     */
    public static long computeSizeBytes(String content) {
        String visible = markdownToPlainTextWithNewlines(content);
        return visible.getBytes(StandardCharsets.UTF_8).length;
    }
    
    /**
     * Nombre de lignes (basé sur le rendu "texte" avec retours à la ligne conservés)
     * Une note vide = 1 ligne
     */
    public static int computeLineCount(String content) {
        String visible = markdownToPlainTextWithNewlines(content);
        
        if (visible.isBlank()) {
            return 1;
        }
        
        // split avec -1 pour compter aussi une éventuelle dernière ligne vide si vous le souhaitez
        // Ici on compte les lignes visibles ; après trim(), pas de ligne vide finale.
        return visible.split("\n", -1).length;
    }
    
    /**
     * Nombre de mots (texte visible)
     */
    public static int computeWordCount(String content) {
        String visible = markdownToPlainTextSingleLine(content);
        
        if (visible.isBlank()) {
            return 0;
        }
        
        return visible.split("\\s+").length;
    }
    
    /**
     * Nombre de caractères (texte visible).
     * On ne compte pas les retours à la ligne comme caractères visibles.
     */
    public static int computeCharCount(String content) {
        String visible = markdownToPlainTextWithNewlines(content);
        
        if (visible.isBlank()) {
            return 0;
        }
        
        // Ne pas compter les sauts de ligne
        return visible.replace("\n", "").length();
    }
}
