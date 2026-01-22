package org.helha.aemthackatonbackend.domain.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MetadataCalculator {

    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final TextContentRenderer TEXT_RENDERER = TextContentRenderer.builder().build();

    /**
     * Markdown -> texte "visible" (sans syntaxe markdown).
     * Conserve des retours à la ligne logiques (paragraphes, listes, titres, etc.).
     */
    private static String markdownToPlainTextWithNewlines(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node document = MARKDOWN_PARSER.parse(markdown);
        String text = TEXT_RENDERER.render(document);

        // Normalisation des retours à la ligne
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // Nettoyage léger : supprimer espaces en fin de ligne
        text = text.replaceAll("[ \t]+\n", "\n");

        return text;
    }

    /**
     * Markdown -> texte "visible" mono-ligne pour mots/caractères (retours remplacés par espaces)
     */
    private static String markdownToPlainTextSingleLine(String markdown) {
        String t = markdownToPlainTextWithNewlines(markdown);
        if (t == null || t.isBlank()) return "";
        return t.replace('\n', ' ').replaceAll("\\s+", " ").trim();
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
     * - Si 0 caractère visible => 0 ligne
     * - Sinon => compter les lignes sans compter une ligne vide finale artificielle
     */
    public static int computeLineCount(String content) {
        String visible = markdownToPlainTextWithNewlines(content);

        // Si aucune char visible (après trim) => 1 ligne
        if (visible == null || visible.trim().isEmpty()) {
            return 0;
        }

        // IMPORTANT :
        // On enlève uniquement les retours à la ligne de fin, car ils créent une "ligne vide" en trop.
        // On ne touche pas aux retours à la ligne internes (qui comptent comme des lignes).
        String withoutTrailingNewlines = visible.replaceAll("\\n+$", "");

        // Après suppression, si jamais ça devient vide, on retombe sur 1 ligne (cas ultra-limite).
        if (withoutTrailingNewlines.trim().isEmpty()) {
            return 1;
        }

        // Comptage "normal" : une ligne = segments séparés par '\n'
        // (pas de -1 ici, car on a supprimé les \n finaux et on ne veut pas compter de ligne vide finale)
        return withoutTrailingNewlines.split("\\n").length;
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

        if (visible == null) {
            return 0;
        }

        // Ne pas compter les sauts de ligne
        String noNewlines = visible.replace("\n", "").trim();

        if (noNewlines.isEmpty()) {
            return 0;
        }

        return noNewlines.length();
    }
}
