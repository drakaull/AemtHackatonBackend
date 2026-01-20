package org.helha.aemthackatonbackend.application.notes.command.export;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExportNoteToPdfHandler {
    
    private final INoteRepository noteRepository;
    
    
    public byte[] handle(Long noteId, boolean includeMetadata) throws IOException {
        DbNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        // convertir Markdown en HTML
        String htmlContent = convertMarkdownToHtml(resolveInternalLinks(note.getContent()));
        
        // ajouter le titre
        String html = "<h1>" + note.getTitle() + "</h1>" + htmlContent;
        
        // générer le PDF avec HtmlConverter
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baos);
        
        // si métadonnées activées, rouvrir le PDF et les ajouter
        if (includeMetadata) {
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(
                    new com.itextpdf.kernel.pdf.PdfReader(new java.io.ByteArrayInputStream(baos.toByteArray())),
                    new com.itextpdf.kernel.pdf.PdfWriter(baos)
            );
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE)); // saut de page
            document.add(new com.itextpdf.layout.element.Paragraph("\n--- Métadonnées ---"));
            document.add(new com.itextpdf.layout.element.Paragraph("Taille (bytes) : " + note.getSizeBytes()));
            document.add(new com.itextpdf.layout.element.Paragraph("Nombre de lignes : " + note.getLineCount()));
            document.add(new com.itextpdf.layout.element.Paragraph("Nombre de mots : " + note.getWordCount()));
            document.add(new com.itextpdf.layout.element.Paragraph("Nombre de caractères : " + note.getCharCount()));
            document.add(new com.itextpdf.layout.element.Paragraph("Créée le : " + note.getCreatedAt()));
            document.add(new com.itextpdf.layout.element.Paragraph("Dernière modification : " + note.getUpdatedAt()));
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    // convertir Markdown en HTML
    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
    
    // remplacer les liens internes par le contenu des notes liées
    private String resolveInternalLinks(String content) {
        Pattern pattern = Pattern.compile("\\[\\[note:(\\d+)\\]\\]");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            Long linkedNoteId = Long.parseLong(matcher.group(1));
            DbNote linkedNote = noteRepository.findById(linkedNoteId)
                    .orElseThrow(() -> new IllegalArgumentException("Linked note not found"));
            matcher.appendReplacement(sb, linkedNote.getContent()); // insère le Markdown brut
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
