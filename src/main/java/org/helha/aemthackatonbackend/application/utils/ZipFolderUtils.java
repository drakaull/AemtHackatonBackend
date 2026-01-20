package org.helha.aemthackatonbackend.application.utils;

import org.helha.aemthackatonbackend.application.folders.query.FolderQueryProcessor;
import org.helha.aemthackatonbackend.application.folders.query.export.FolderExportNode;
import org.helha.aemthackatonbackend.application.folders.query.export.NoteExportNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFolderUtils {

    private ZipFolderUtils(){}

    public static byte[] zipFolder(FolderExportNode root)throws IOException{
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(baos)){

            addFolderRec(root,"",zip);
            zip.finish();
            return baos.toByteArray();
        }

    }
    private static void addFolderRec(FolderExportNode node, String basePath, ZipOutputStream zip)throws IOException{
        String folderName = sanitize(node.getName());
        String currentPath = basePath + folderName+"/";

        zip.putNextEntry(new ZipEntry(currentPath));
        zip.closeEntry();

        for(NoteExportNode note : node.getNotes()){
            String safeTitle = sanitize(note.getTitle());

            String filePath = currentPath + safeTitle + ".md";

            zip.putNextEntry(new ZipEntry(filePath));
            String content = note.getContent() == null ? "" : note.getContent();
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        for(FolderExportNode child : node.getChildren()){
            addFolderRec(child, currentPath, zip);
        }

    }

    //nettoyage des nom de fichier/dossier

    private static String sanitize(String raw){
        if(raw == null || raw.isBlank()){
            return "untitled";
        }
        //enlever accents
        String noAccent = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}","");
        String cleaned = noAccent.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();

        if (cleaned.isEmpty())return "untitled";

        return cleaned.length() > 120 ? cleaned.substring(0,120) : cleaned;
    }
}
