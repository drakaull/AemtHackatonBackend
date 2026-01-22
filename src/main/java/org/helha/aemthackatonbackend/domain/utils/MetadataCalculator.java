
package org.helha.aemthackatonbackend.domain.utils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class responsible for computing metadata related to note content.
 * All calculations are based on the "visible" text rendered from Markdown,
 * not on the raw Markdown syntax.
 */
@Component
public class MetadataCalculator {

    private static final Parser MARKDOWN_PARSER =
            Parser.builder().build();

    private static final TextContentRenderer TEXT_RENDERER =
            TextContentRenderer.builder().build();

    /**
     * Removes Markdown link destinations: url -> label.
     */
    private static final Pattern MD_LINK =
            Pattern.compile("\\[(\\[^\\]]*)\\]\\(([^)]+)\\)");

    /**
     * Removes Markdown image destinations: ![alt](url) -> alt.
     */
    private static final Pattern MD_IMAGE =
            Pattern.compile("!\\[(\\[^\\]]*)\\]\\(([^)]+)\\)");

    /**
     * Matches visible words (letter and digit sequences, Unicode aware).
     */
    private static final Pattern WORD_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}]+");

    /**
     * Matches visible characters (letters and digits only).
     */
    private static final Pattern CHAR_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}]");

    /**
     * Removes non-visible parts of Markdown syntax (URLs in links and images).
     * Example: [link](https://example.com) -> link
     */
    private static String stripMarkdownNonVisibleParts(String markdown) {

        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        // Remove image URLs first to avoid matching them with link regex
        String s = MD_IMAGE.matcher(markdown).replaceAll("$1");

        // Then remove link URLs
        s = MD_LINK.matcher(s).replaceAll("$1");

        return s;
    }

    /**
     * Converts Markdown content to visible plain text while preserving
     * logical line breaks (paragraphs, lists, headings).
     */
    private static String markdownToPlainTextWithNewlines(String markdown) {

        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        // Important: remove non-visible Markdown parts first
        String sanitized = stripMarkdownNonVisibleParts(markdown);

        Node document = MARKDOWN_PARSER.parse(sanitized);
        String text = TEXT_RENDERER.render(document);

        // Normalize line endings
        text = text.replace("\r\n", "\n")
                .replace("\r", "\n");

        // Remove trailing spaces before line breaks
        text = text.replaceAll("[ \t]+\\n", "\n");

        return text;
    }

    /**
     * Converts Markdown to a single-line visible text representation.
     * Line breaks are replaced by spaces.
     */
    private static String markdownToPlainTextSingleLine(String markdown) {

        String t = markdownToPlainTextWithNewlines(markdown);
        if (t == null || t.isBlank()) {
            return "";
        }

        return t.replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Computes the size in bytes (UTF-8) of the visible text.
     */
    public static long computeSizeBytes(String content) {

        String visible =
                markdownToPlainTextWithNewlines(content).trim();

        return visible
                .getBytes(StandardCharsets.UTF_8)
                .length;
    }

    /**
     * Computes the logical line count.
     * - Empty visible content counts as 1 line
     * - Trailing artificial empty lines are ignored
     */
    public static int computeLineCount(String content) {

        String visible = markdownToPlainTextWithNewlines(content);

        if (visible == null || visible.trim().isEmpty()) {
            return 1;
        }

        // Remove only trailing line breaks to avoid counting empty lines
        String withoutTrailingNewlines =
                visible.replaceAll("\\n+$", "");

        if (withoutTrailingNewlines.trim().isEmpty()) {
            return 1;
        }

        return withoutTrailingNewlines.split("\\n").length;
    }

    /**
     * Computes the number of visible words.
     * Only sequences of letters and digits are counted.
     */
    public static int computeWordCount(String content) {

        String visible =
                markdownToPlainTextSingleLine(content);

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
     * Computes the number of visible characters.
     * Only letters and digits are counted (no whitespace or punctuation).
     */
    public static int computeCharCount(String content) {

        String visible =
                markdownToPlainTextWithNewlines(content);

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
