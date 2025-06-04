package com.backend.utils;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class FlashcardBatchGenerator {    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${app.backend.base-url:http://localhost:3000}")
    private String backendBaseUrl;
    
    private final String BASE_DIRECTORY = "courses";

    // Progress tracking
    private final AtomicInteger totalFiles = new AtomicInteger(0);
    private final AtomicInteger processedFiles = new AtomicInteger(0);
    
    private String getGenerateUrl() {
        return backendBaseUrl + "/gemini/generate-with-prompt";
    }

    //@PostConstruct
    public void generateAllFlashcards() {
        File coursesFolder = new File(BASE_DIRECTORY);

        if (!coursesFolder.exists() || !coursesFolder.isDirectory()) {
            System.out.println("❌ Directorul '" + BASE_DIRECTORY + "' nu există!");
            return;
        }

        // First pass: count total files
        totalFiles.set(0);
        processedFiles.set(0);
        countFiles(coursesFolder);

        System.out.println("🚀 Încep procesarea din directorul: " + BASE_DIRECTORY);
        System.out.println("📊 Total fișiere de procesat: " + totalFiles.get());

        processDirectory(coursesFolder, "");

        System.out.println("✨ Procesarea completă! Procesate: " + processedFiles.get() + "/" + totalFiles.get());
    }

    private void countFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt") && !file.getName().contains("_flashcards")) {
                totalFiles.incrementAndGet();
            } else if (file.isDirectory()) {
                countFiles(file);
            }
        }
    }

    private void processDirectory(File directory, String relativePath) {
        File[] files = directory.listFiles();

        if (files == null) {
            System.out.println("⚠️ Nu pot accesa conținutul din: " + directory.getPath());
            return;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt") && !file.getName().contains("_flashcards")) {
                String fileRelativePath = relativePath.isEmpty() ?
                        file.getName() :
                        relativePath + "/" + file.getName();

                processTextFile(fileRelativePath);
            }
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String newRelativePath = relativePath.isEmpty() ?
                        file.getName() :
                        relativePath + "/" + file.getName();

                System.out.println("📁 Intru în directorul: " + newRelativePath);
                processDirectory(file, newRelativePath);
            }
        }
    }

    public void processTextFile(String relativePath) {
        try {
            int current = processedFiles.incrementAndGet();
            System.out.println("📄 Procesez fișierul (" + current + "/" + totalFiles.get() + "): " + relativePath);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("inputFilePath", relativePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(getGenerateUrl(), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Flashcard-uri generate pentru: " + relativePath + " (" + current + "/" + totalFiles.get() + " complete)");
                System.out.println("📝 Răspuns: " + response.getBody());
            } else {
                System.out.println("❌ Eroare HTTP " + response.getStatusCode() + " pentru " + relativePath);
            }

            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("❌ Eroare la procesarea fișierului " + relativePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
        public int getTotalFiles() {
            return totalFiles.get();
        }
        public int getProcessedFiles() {
            return processedFiles.get();
        }

    public void generateFlashcardsForSpecificCourse(String courseName) {
        String courseDirectory = BASE_DIRECTORY + "/" + courseName;
        File courseFolder = new File(courseDirectory);

        if (!courseFolder.exists() || !courseFolder.isDirectory()) {
            System.out.println("❌ Cursul '" + courseName + "' nu există în " + BASE_DIRECTORY);
            return;
        }

            // Count files for this course
            totalFiles.set(0);
            processedFiles.set(0);
            countFiles(courseFolder);

            System.out.println("🚀 Procesez cursul: " + courseName);
            System.out.println("📊 Total fișiere de procesat: " + totalFiles.get());

            processDirectory(courseFolder, courseName);

            System.out.println("✨ Procesarea cursului " + courseName + " completă! Procesate: " + processedFiles.get() + "/" + totalFiles.get());
    }
}