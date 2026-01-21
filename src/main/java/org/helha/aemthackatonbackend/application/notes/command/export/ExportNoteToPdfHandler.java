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

@Service
@RequiredArgsConstructor
public class ExportNoteToPdfHandler {
    
    private final INoteRepository noteRepository;
    
    // détecte [[note:123]]
    private static final Pattern BRACKET_LINK = Pattern.compile("\\[\\[note:(\\d+)\\]\\]");
    
    // détecte lien Markdown complet : [Label](http://.../folders/x/notes/ID) ou [Label](/folders/x/notes/ID)
    private static final Pattern MARKDOWN_URL_LINK = Pattern.compile(
            "\\[([^\\]]+)]\\(" +                     // label (group 1)
                    "(?i)(?:https?://[^\\s)]+)?" +    // domaine optionnel
                    "/(?:api/)?" +
                    "folders/\\d+/notes/(\\d+)" +     // id (group 2)
                    "\\)"
    );
    
    // détecte URLs internes "brutes" : http(s)://xxx/folders/3/notes/1 ou /folders/3/notes/1
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
        
        // generate base PDF (HTML -> PDF)
        ByteArrayOutputStream baseBaos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baseBaos);
        byte[] basePdfBytes = baseBaos.toByteArray();
        
        if (!includeMetadata) {
            return basePdfBytes;
        }
        
        // append metadata page at the end (page n+1)
        ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
        
        PdfDocument pdfDoc = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(basePdfBytes)),
                new PdfWriter(finalBaos)
        );
        
        Document document = new Document(pdfDoc);
        
        // go to last existing page, then create the next one => n+1
        document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        
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
    
    // ---------- MARKDOWN RECURSIVE LINK RESOLUTION ----------
    private String resolveLinksRecursive(String markdown, Set<Long> visited) {
        // IMPORTANT : traiter le lien Markdown complet AVANT de traiter l'URL "brute"
        markdown = replaceMarkdownLink(markdown, MARKDOWN_URL_LINK, visited);
        // puis les syntaxes restantes
        markdown = replacePattern(markdown, BRACKET_LINK, visited);
        markdown = replacePattern(markdown, URL_LINK, visited);
        return markdown;
    }
    
    private String replaceMarkdownLink(String text, Pattern pattern, Set<Long> visited) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String label = matcher.group(1);
            Long linkedId = Long.parseLong(matcher.group(2));
            if (visited.contains(linkedId)) {
                matcher.appendReplacement(sb,
                        Matcher.quoteReplacement(label + " [⚠ boucle détectée : note " + linkedId + "]"));
                continue;
            }
            visited.add(linkedId);
            DbNote linked = noteRepository.findById(linkedId)
                    .orElseThrow(() -> new IllegalArgumentException("Linked note not found: " + linkedId));
            String expanded = resolveLinksRecursive(linked.getContent(), visited);
            // Remplacement : label en clair + contenu intégré
            String replacement = label + "\n\n" + expanded + "\n";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
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
            matcher.appendReplacement(sb, Matcher.quoteReplacement(expanded));
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
