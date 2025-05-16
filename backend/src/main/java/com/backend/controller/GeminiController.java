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

    @PostMapping("/compare-users-answer-to-the-official-answer")
    public Double compareUsersAnswerToOfficialAnswer(@RequestParam String question, @RequestParam String officialAnswer, @RequestParam String usersAnswer) throws IOException {
        String prompt = "Given question: " + question + "\n" + "Given official answer: " + officialAnswer + "\n Compare the official answer to the user's answer and give back a procentage of how correct the user's answer is. Your answer should be only a rational number, no extra symbol (example: 87.5) \n User's answer: " + usersAnswer;

        // Get the generated content from Gemini API
        String generatedContent1 = geminiService.getGeminiResponse(prompt);
        String generatedContent2 = geminiService.getGeminiResponse(prompt);
        String generatedContent3 = geminiService.getGeminiResponse(prompt);
        String generatedContent4 = geminiService.getGeminiResponse(prompt);
        String generatedContent5 = geminiService.getGeminiResponse(prompt);

        Double percentage1 = Double.parseDouble(extractOnlyTheNumber(generatedContent1));
        Double percentage2 = Double.parseDouble(extractOnlyTheNumber(generatedContent2));
        Double percentage3 = Double.parseDouble(extractOnlyTheNumber(generatedContent3));
        Double percentage4 = Double.parseDouble(extractOnlyTheNumber(generatedContent4));
        Double percentage5 = Double.parseDouble(extractOnlyTheNumber(generatedContent5));

        if(percentage1 == 0.0 || percentage2 == 0.0 || percentage3 == 0.0 || percentage4 == 0.0 || percentage5 == 0.0) {
            return 0.0;
        }

        return (percentage1 + percentage2 + percentage3 + percentage4 + percentage5) / 5.0;
    }

    private String extractOnlyTheNumber(String json) {
        int start = json.indexOf("text=");
        if (start == -1) {
            throw new IllegalArgumentException("Could not find 'text=' in the response: " + json);
        }
        start += 5;

        int end = json.indexOf("\\n", start);
        if (end == -1) end = json.indexOf("\n", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) end = json.length(); // fallback

        return json.substring(start, end).trim();
    }
}