package com.backend.controller;

import com.backend.service.AWSS3Service;
import com.backend.utils.PDFtoText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/pdf")
public class PDFController {

    private final AWSS3Service awss3Service;

    @Autowired
    public PDFController(AWSS3Service awss3Service) {
        this.awss3Service = awss3Service;
    }

    @PostMapping("/pdf-to-image")
    public ResponseEntity<String> convertPdfToImage(
            @RequestParam String pdfPath,
            @RequestParam String textPath) {

        String tempPdfPath = null;

        try {
            String bucketName = "quizzy-s3-bucket";
            System.out.println("Step 1: Starting PDF processing for bucket: " + bucketName + ", path: " + pdfPath);

            Resource pdfResource = awss3Service.getPdfResourceFromS3(bucketName, pdfPath);

            if (pdfResource == null || !pdfResource.exists()) {
                System.err.println("Step 1 FAILED: PDF not found in S3");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("PDF not found in S3 at path: " + pdfPath);
            }
            System.out.println("Step 1 SUCCESS: PDF resource obtained from S3");

            Path tempDir = Paths.get("/tmp");
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                System.out.println("Step 2: Created /tmp directory");
            } else {
                System.out.println("Step 2: /tmp directory already exists");
            }

            tempPdfPath = "/tmp/temp.pdf";
            Path tempPdfFile = Paths.get(tempPdfPath);

            try (InputStream inputStream = pdfResource.getInputStream()) {
                Files.copy(inputStream, tempPdfFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Step 3 SUCCESS: PDF saved to temporary file: " + tempPdfPath);
                System.out.println("Temp file size: " + Files.size(tempPdfFile) + " bytes");
            } catch (IOException e) {
                System.err.println("Step 3 FAILED: Could not save PDF to temp file - " + e.getMessage());
                throw e;
            }

            // Obține calea directorului proiectului și combină cu textPath
            String projectDir = System.getProperty("user.dir");
            String fullTextPath = Paths.get(projectDir, textPath).toString();

            // Verifică și creează toate directoarele necesare pentru fullTextPath
            Path outputPath = Paths.get(fullTextPath);
            Path parentDir = outputPath.getParent();
            if (parentDir != null) {
                try {
                    Files.createDirectories(parentDir);
                    System.out.println("Created directories: " + parentDir.toString());
                } catch (IOException e) {
                    System.err.println("Failed to create directories: " + parentDir.toString() + " - " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create output directory: " + e.getMessage());
                }
            }

            // Verifică permisiunile de scriere
            if (parentDir != null && !Files.isWritable(parentDir)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No write permission for directory: " + parentDir.toString());
            }

            // Apelează metoda de conversie cu calea completă
            boolean conversionSuccess = PDFtoText.pdfToImage(tempPdfPath, fullTextPath);

            if (conversionSuccess) {
                return ResponseEntity.ok("PDF converted to image successfully at: " + fullTextPath);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Conversion failed");
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("IO Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Conversion failed: " + e.getMessage());
        } finally {
            if (tempPdfPath != null) {
                try {
                    Path tempFile = Paths.get(tempPdfPath);
                    if (Files.exists(tempFile)) {
                        Files.delete(tempFile);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to delete temporary file: " + tempPdfPath +
                            " - " + e.getMessage());
                }
            }
        }
    }
}