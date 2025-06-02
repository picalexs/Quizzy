package com.backend.controller;

import com.backend.utils.FlashcardBatchGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardGeneratorController {

    private final FlashcardBatchGenerator flashcardBatchGenerator;
    
    // Track the generation status
    private final AtomicBoolean isGenerating = new AtomicBoolean(false);
    private final AtomicReference<String> lastStatus = new AtomicReference<>("idle");
    private final AtomicReference<String> lastError = new AtomicReference<>(null);

    @PostMapping("/generate-all")
    public ResponseEntity<Map<String, Object>> generateFlashcards() {
        Map<String, Object> response = new HashMap<>();
        
        // Check if already running
        if (isGenerating.get()) {
            response.put("status", "already_running");
            response.put("message", "Generarea flashcard-urilor este deja în desfășurare.");
            response.put("progress", getProgressInfo());
            return ResponseEntity.ok(response);
        }
        
        // Start the async operation
        isGenerating.set(true);
        lastStatus.set("started");
        lastError.set(null);
        
        CompletableFuture.runAsync(() -> {
            try {
                lastStatus.set("processing");
                flashcardBatchGenerator.generateAllFlashcards();
                lastStatus.set("completed");
            } catch (Exception e) {
                lastStatus.set("error");
                lastError.set(e.getMessage());
                System.err.println("❌ Eroare la generarea flashcard-urilor: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isGenerating.set(false);
            }
        });
        
        response.put("status", "started");
        response.put("message", "🚀 Generarea flashcard-urilor a început în background. Folosiți /api/flashcards/generation-status pentru a verifica progresul.");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/generation-status")
    public ResponseEntity<Map<String, Object>> getGenerationStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("isGenerating", isGenerating.get());
        response.put("status", lastStatus.get());
        response.put("progress", getProgressInfo());
        
        if (lastError.get() != null) {
            response.put("error", lastError.get());
        }
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> getProgressInfo() {
        Map<String, Object> progress = new HashMap<>();
        progress.put("totalFiles", flashcardBatchGenerator.getTotalFiles());
        progress.put("processedFiles", flashcardBatchGenerator.getProcessedFiles());
        
        int total = flashcardBatchGenerator.getTotalFiles();
        int processed = flashcardBatchGenerator.getProcessedFiles();
        
        if (total > 0) {
            double percentage = (double) processed / total * 100;
            progress.put("percentage", Math.round(percentage * 100.0) / 100.0);
        } else {
            progress.put("percentage", 0.0);
        }
        
        return progress;
    }
}

