
package org.helha.aemthackatonbackend.application.notes.command.export;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.helha.aemthackatonbackend.infrastructure.note.DbNote;
import org.helha.aemthackatonbackend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExportNoteToPdfHandler {

    private final INoteRepository noteRepository;

    // détecte [[note:123]]
    private static final Pattern BRACKET_LINK = Pattern.compile("\\[\\[note:(\\d+)\\]\\]");

    // détecte URLs internes : http(s)://xxx/folders/3/notes/1
    private static final Pattern URL_LINK = Pattern.compile(
            "(?i)(?:https?://[^\\s]+)?/(?:api/)?folders/\\d+/notes/(\\d+)"
    );

    public byte[] handle(Long noteId, boolean includeMetadata) throws IOException {

        DbNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        // expand markdown links
        String expandedMarkdown = resolveLinksRecursive(note.getContent(), new HashSet<>(Set.of(noteId)));

        // convert markdown → HTML
        String htmlContent = convertMarkdownToHtml(expandedMarkdown);

        // add title
        String html = "<h1>" + escape(note.getTitle()) + "</h1>" + htmlContent;

        // generate PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baos);

        // metadata page
        if (includeMetadata) {
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(
                    new com.itextpdf.kernel.pdf.PdfReader(new java.io.ByteArrayInputStream(baos.toByteArray())),
                    new com.itextpdf.kernel.pdf.PdfWriter(baos)
            );

            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            document.add(new com.itextpdf.layout.element.Paragraph("--- Métadonnées ---"));
            document.add(new com.itextpdf.layout.element.Paragraph("Taille : " + note.getSizeBytes()));
            document.add(new com.itextpdf.layout.element.Paragraph("Lignes : " + note.getLineCount()));
            document.add(new com.itextpdf.layout.element.Paragraph("Mots : " + note.getWordCount()));
            document.add(new com.itextpdf.layout.element.Paragraph("Caractères : " + note.getCharCount()));
            document.add(new com.itextpdf.layout.element.Paragraph("Créée : " + note.getCreatedAt()));
            document.add(new com.itextpdf.layout.element.Paragraph("Modifiée : " + note.getUpdatedAt()));
            document.close();
        }

        return baos.toByteArray();
    }

    // ---------- MARKDOWN RECURSIVE LINK RESOLUTION ----------

    private String resolveLinksRecursive(String markdown, Set<Long> visited) {

        markdown = replacePattern(markdown, BRACKET_LINK, visited);
        markdown = replacePattern(markdown, URL_LINK, visited);

        return markdown;
    }

    private String replacePattern(String text, Pattern pattern, Set<Long> visited) {

        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            Long linkedId = Long.parseLong(matcher.group(1));

            if (visited.contains(linkedId)) {
                matcher.appendReplacement(sb,
                        "[⚠ boucle détectée : note " + linkedId + "]");
                continue;
            }

            visited.add(linkedId);

            DbNote linked = noteRepository.findById(linkedId)
                    .orElseThrow(() -> new IllegalArgumentException("Linked note not found: " + linkedId));

            String expanded = resolveLinksRecursive(linked.getContent(), visited);

            String replacement = expanded;

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    // ---------- MARKDOWN → HTML ----------

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

