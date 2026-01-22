
package org.helha.aemthackatonbackend.application.notes.command.export;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.AreaBreakType;
import lombok.RequiredArgsConstructor;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application service responsible for exporting a note to PDF.
 * The export supports recursive expansion of internal note links.
 */
@Service
@RequiredArgsConstructor
public class ExportNoteToPdfHandler {

    private final INoteRepository noteRepository;

    /**
     * Matches custom note links like [[note:123]].
     */
    private static final Pattern BRACKET_LINK =
            Pattern.compile("\\[\\[note:(\\d+)\\]\\]");

    /**
     * Matches full Markdown links pointing to internal notes.
     */
    private static final Pattern MARKDOWN_URL_LINK = Pattern.compile(
            "\\[(\\[^\\]]+)\\]\\(" +
                    "(?i)(?:https?://[^\\s)\\]]+)?" +
                    "/(?:api/)?" +
                    "folders/\\d+/notes/(\\d+)" +
                    "\\)"
    );

    /**
     * Matches raw internal URLs pointing to notes.
     */
    private static final Pattern URL_LINK = Pattern.compile(
            "(?i)(?:https?://[^\\s]+)?/(?:api/)?folders/\\d+/notes/(\\d+)"
    );

    /**
     * Exports a note as a PDF document.
     *
     * @param noteId           identifier of the note to export
     * @param includeMetadata  whether to append a metadata page
     * @return generated PDF as a byte array
     */
    public byte[] handle(Long noteId, boolean includeMetadata)
            throws IOException {

        // Load the note to export
        DbNote note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Note not found")
                );

        // Recursively expand internal note links
        String expandedMarkdown = resolveLinksRecursive(
                note.getContent(),
                new HashSet<>(Set.of(noteId))
        );

        // Convert expanded Markdown content to HTML
        String htmlContent = convertMarkdownToHtml(expandedMarkdown);

        // Prepend note title
        String html =
                "<h1>" + escape(note.getTitle()) + "</h1>" + htmlContent;

        // Generate the base PDF from HTML
        ByteArrayOutputStream baseBaos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baseBaos);
        byte[] basePdfBytes = baseBaos.toByteArray();

        // Return early if metadata page is not requested
        if (!includeMetadata) {
            return basePdfBytes;
        }

        // Append a metadata page at the end of the PDF
        ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(basePdfBytes)),
                new PdfWriter(finalBaos)
        );
        Document document = new Document(pdfDoc);

        // Move to last page and create a new one
        document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        // Write metadata values
        document.add(new Paragraph("--- Métadonnées ---"));
        document.add(new Paragraph("Taille : " + note.getSizeBytes()));
        document.add(new Paragraph("Lignes : " + note.getLineCount()));
        document.add(new Paragraph("Mots : " + note.getWordCount()));
        document.add(new Paragraph("Caractères : " + note.getCharCount()));
        document.add(new Paragraph("Créée : " + note.getCreatedAt()));
        document.add(new Paragraph("Modifiée : " + note.getUpdatedAt()));

        document.close();
        return finalBaos.toByteArray();
    }

    /**
     * Recursively resolves internal note links while preventing cycles.
     */
    private String resolveLinksRecursive(String markdown, Set<Long> visited) {

        // Process full Markdown links before raw URLs
        markdown = replaceMarkdownLink(markdown, MARKDOWN_URL_LINK, visited);

        // Process remaining bracket and raw URL links
        markdown = replacePattern(markdown, BRACKET_LINK, visited);
        markdown = replacePattern(markdown, URL_LINK, visited);

        return markdown;
    }

    /**
     * Replaces Markdown-style links with the expanded content
     * of the referenced note.
     */
    private String replaceMarkdownLink(
            String text,
            Pattern pattern,
            Set<Long> visited
    ) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String label = matcher.group(1);
            Long linkedId = Long.parseLong(matcher.group(2));

            // Prevent infinite recursion on cyclic references
            if (visited.contains(linkedId)) {
                matcher.appendReplacement(
                        sb,
                        Matcher.quoteReplacement(
                                label + " [⚠ loop detected: note " + linkedId + "]"
                        )
                );
                continue;
            }

            visited.add(linkedId);

            DbNote linked = noteRepository.findById(linkedId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Linked note not found: " + linkedId
                            )
                    );

            String expanded =
                    resolveLinksRecursive(linked.getContent(), visited);

            // Replace link with readable label + embedded content
            String replacement =
                    label + "\n\n" + expanded + "\n";

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Replaces simple link patterns with expanded note content.
     */
    private String replacePattern(
            String text,
            Pattern pattern,
            Set<Long> visited
    ) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            Long linkedId = Long.parseLong(matcher.group(1));

            // Prevent infinite recursion on cyclic references
            if (visited.contains(linkedId)) {
                matcher.appendReplacement(
                        sb,
                        "[⚠ loop detected: note " + linkedId + "]"
                );
                continue;
            }

            visited.add(linkedId);

            DbNote linked = noteRepository.findById(linkedId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Linked note not found: " + linkedId
                            )
                    );

            String expanded =
                    resolveLinksRecursive(linked.getContent(), visited);

            matcher.appendReplacement(
                    sb,
                    Matcher.quoteReplacement(expanded)
            );
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts Markdown content to HTML.
     */
    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    /**
     * Escapes basic HTML characters to prevent rendering issues.
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&")
                .replace("<", "<")
                .replace(">", ">");
    }
}
