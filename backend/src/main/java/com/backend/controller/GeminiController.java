package com.backend.controller;

import com.backend.service.GeminiService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.File;
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

        String prompt = "Create 35 flashcards with essential information from the material provided. Format single choice answers as:\n\n--FlashCardSeparator--\nSingle\n--InteriorSeparator--\ndetailed question\n--InteriorSeparator--\nanswer\n--InteriorSeparator--\ndifficulty\n--InteriorSeparator--\npageIndex\n--FlashCardSeparator--\n\nFormat multiple choice answers as:\n\n--FlashCardSeparator--\nMultiple\n--InteriorSeparator--\ndetailed question\n--InteriorSeparator--\n(right) answer option\n(right/wrong) answer option\n(wrong) answer option\n(wrong) answer option\n--InteriorSeparator--\ndifficulty\n--InteriorSeparator--\npageIndex\n--FlashCardSeparator--\n\nEnsure:\n- Mix of single and multiple choice questions\n- Difficulties: easy, medium, hard\n- Minimum 10 hard questions\n- 1-2 correct answers for multiple choice\n- No emojis or additional commentary";

        // Get the generated content from Gemini API
        String generatedContent = geminiService.processFileWithPrompt(inputFilePath, prompt);

        // Get the base path to the courses directory
        String coursesBasePath = System.getProperty("user.dir") + File.separator + "courses";

        // Parse the relative file path to determine output directory and filename
        Path relativePath = Paths.get(inputFilePath);
        String filename = relativePath.getFileName().toString();
        String filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
        String outputFilename = filenameWithoutExtension + "_flashcards.txt";

        // Create the output path in the same directory as the input file
        Path outputPath = Paths.get(coursesBasePath).resolve(relativePath.getParent()).resolve(outputFilename);

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