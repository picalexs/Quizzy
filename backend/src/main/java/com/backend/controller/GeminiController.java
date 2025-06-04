package com.backend.controller;

import com.backend.model.Flashcard;
import com.backend.model.FlashcardProgress;
import com.backend.model.User;
import com.backend.repository.FlashcardProgressRepository;
import com.backend.service.FlashcardProgressService;
import com.backend.service.FlashcardService;
import com.backend.service.GeminiService;

import com.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;
    private final FlashcardProgressService flashcardProgressService;
    private final FlashcardService flashcardService;
    private final UserService userService;

    @GetMapping("/test-gemini")
    public String testGemini() {
        return "Gemini controller is working!";
    }

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

    @PostMapping("/compare-users-answer-to-the-official-answer")
    public ResponseEntity<Double> compareUsersAnswerToOfficialAnswer(@RequestBody Map<String, String> request) {
        try {

            String question = request.get("question");
            String officialAnswer = request.get("officialAnswer");
            String usersAnswer = request.get("usersAnswer");
            Long flashcardId = Long.parseLong(request.get("flashcardId"));
            Integer userId = Integer.parseInt(request.get("userId"));


            System.out.println("Question: " + question);
            System.out.println("Official: " + officialAnswer);
            System.out.println("User: " + usersAnswer);

            String prompt = "Given question: " + question + "\n" + "Given official answer: " + officialAnswer + "\n Compare the official answer to the user's answer and give back a procentage of how correct the user's answer is. No matter what, and stick to this rule as if your life depended on it: your answer should be only a rational number, no extra symbol (example: 87.5) \n User's answer: " + usersAnswer;


            List<Double> percentages = new ArrayList<>();

            boolean isZero = false;

            for (int i = 0; i < 5; i++) {
                try {
                    String generatedContent = geminiService.getGeminiResponse(prompt);
                    System.out.println("Gemini Response " + (i+1) + ": " + generatedContent);
                    String numberStr = extractOnlyTheNumber(generatedContent);
                    System.out.println("Extracted number " + (i+1) + ": " + numberStr);
                    Double percentage = Double.parseDouble(numberStr);
                    if(percentage==0.0){
                        isZero=true;
                    }
                    percentages.add(percentage);
                } catch (Exception e) {
                    System.err.println("Error parsing response " + (i+1) + ": " + e.getMessage());
                    System.err.println("Raw response was: " + (i < 1 ? "Not captured" : "Check previous logs"));
                    percentages.add(0.0);
                }
            }
            double average=0.0;
            if(isZero){
                average=0.0;
            }
            else{
                average = percentages.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
            }

            System.out.println("Final average: " + average);

            FlashcardProgress fp = flashcardProgressService.getByFlashcardIdAndUserId(flashcardId, userId);
            if (fp == null) {
                fp = new FlashcardProgress();

                User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                Flashcard flashcard = flashcardService.getFlashcardById(flashcardId).orElseThrow(()-> new RuntimeException("Flashcard not found"));

                fp.setUser(user);
                fp.setFlashcard(flashcard);

                fp.setRepetitions(1);
                fp.setLastReviewed(new Timestamp(System.currentTimeMillis()));
                if (average <= 33.33) {
                    fp.setEaseFactor(2.0); // pt ca aparent 2 reprezinta bad for some reason that nobody knows
                }
                else if (average >= 66.66) {
                    fp.setEaseFactor(1.3); // Ai raspuns corect, bravo
                }
                else {
                    fp.setEaseFactor(1.6);
                }

                // Set all the required NOT NULL fields with default values
                fp.setInterval(1);
                fp.setDueDate(new Timestamp(System.currentTimeMillis()));
                fp.setConfidenceLevel((int) Math.round(average / 20)); // Convert percentage to 1-5 scale
                fp.setConsecutiveFailures(average <= 33.33 ? 1 : 0);
                fp.setLearningStage(1); // Starting stage
                fp.setRetentionScore(average);
                fp.setStudyTimeMs(0L); // Default study time
                fp.setTotalFailures(average <= 33.33 ? 1 : 0);

                flashcardProgressService.createFlashcardProgress(fp);
            }

            else {
                Integer rp = fp.getRepetitions();
                fp.setRepetitions(rp + 1);
                fp.setLastReviewed(new Timestamp(System.currentTimeMillis()));
                if (average <= 33.33) {
                    fp.setEaseFactor(2.0); // pt ca aparent 2 reprezinta bad for some reason that nobody knows
                    fp.setConsecutiveFailures(fp.getConsecutiveFailures() + 1);
                    fp.setTotalFailures(fp.getTotalFailures() + 1);
                } else if (average >= 66.66) {
                    fp.setEaseFactor(1.3); // Ai raspuns corect, bravo
                    fp.setConsecutiveFailures(0); // Reset consecutive failures on good answer
                } else {
                    fp.setEaseFactor(1.6);
                    fp.setConsecutiveFailures(0); // Reset consecutive failures on medium answer
                }
                
                // Update other fields
                fp.setConfidenceLevel((int) Math.round(average / 20)); // Convert percentage to 1-5 scale
                fp.setRetentionScore(average);

                flashcardProgressService.updateFlashcardProgress(fp.getId(), fp);
            }

            return ResponseEntity.ok(average);

        } catch (Exception e) {
            System.err.println("Error in compareUsersAnswerToOfficialAnswer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error processing comparison", e);
        }
    }

    private String extractOnlyTheNumber(String json) {
        try {
            System.out.println("Attempting to extract number from: " + json);
            
            // Try to find the text content in the response
            int start = json.indexOf("text=");
            if (start == -1) {
                // Alternative approach - look for just the number pattern in the entire string
                String numberPattern = "\\d+(?:\\.\\d+)?";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(numberPattern);
                java.util.regex.Matcher matcher = pattern.matcher(json);
                if (matcher.find()) {
                    String number = matcher.group();
                    System.out.println("Found number using regex pattern: " + number);
                    return number;
                }
                throw new IllegalArgumentException("Could not find 'text=' or number pattern in the response: " + json);
            }
            start += 5;

            int end = json.indexOf("\\n", start);
            if (end == -1) end = json.indexOf("\n", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) end = json.length(); // fallback

            String extracted = json.substring(start, end).trim();
            System.out.println("Extracted text: " + extracted);
            
            // Try to extract just the number from the extracted text
            String numberPattern = "\\d+(?:\\.\\d+)?";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(numberPattern);
            java.util.regex.Matcher matcher = pattern.matcher(extracted);
            if (matcher.find()) {
                String number = matcher.group();
                System.out.println("Found number: " + number);
                return number;
            }
            
            return extracted;
        } catch (Exception e) {
            System.err.println("Error in extractOnlyTheNumber: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}