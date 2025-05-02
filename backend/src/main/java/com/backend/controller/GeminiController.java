package com.backend.controller;

import com.backend.service.GeminiService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/generate")
    public String generateResponse(@RequestParam String inputFilePath) throws IOException {
        return geminiService.processFile(inputFilePath);
    }

    @PostMapping("/generate-with-prompt")
    public String generateResponseWithPrompt(
            @RequestParam String inputFilePath) throws IOException {

        String prompt = "Generate 30 flashcards based on the content of this file. Each flashcard should follow this format: --FlashCardSeparator-- Single [Question] --InteriorSeparator-- [Answer] --InteriorSeparator--[Difficulty: easy/medium/hard] --FlashCardSeparator--OR  --FlashCardSeparator-- Multiple [Question] --InteriorSeparator-- 1.(right/wrong) [Option 1] 2.(right/wrong) [Option 2] 3.(right/wrong) [Option 3] 4.(right/wrong) [Option 4] --InteriorSeparator--[Difficulty: easy/medium/hard] --FlashCardSeparator--  Mix both single and multiple choice flashcards. Ensure questions cover key concepts from the text.";

        // Get the generated content from Gemini API
        String generatedContent = geminiService.processFileWithPrompt(inputFilePath, prompt);

        // Parse the input file path to determine output directory and filename
        Path inputPath = Paths.get(inputFilePath);
        String filename = inputPath.getFileName().toString();
        String filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
        String outputFilename = filenameWithoutExtension + "_flashcards.txt";

        // Create the output path in the same directory as the input file
        Path outputPath = inputPath.getParent().resolve(outputFilename);

        try {
            // Ensure the directory exists
            Files.createDirectories(outputPath.getParent());

            // Write the content to the file
            Files.writeString(outputPath, generatedContent);

            return "ok - Saved to " + outputPath;
        } catch (Exception e) {
            return "Error saving flashcards: " + e.getMessage();
        }
    }
}