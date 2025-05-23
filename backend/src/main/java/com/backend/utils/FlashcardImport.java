package com.backend.utils;

import com.backend.dto.AnswerFCDTO;
import com.backend.dto.FlashcardDTO;
import com.backend.model.Flashcard;
import com.backend.model.Material;
import com.backend.model.User;
import com.backend.model.AnswerFC;
import com.backend.repository.AnswerFCRepository;
import com.backend.repository.FlashcardRepository;
import com.backend.repository.MaterialRepository;
import com.backend.repository.UserRepository;
import com.backend.service.MaterialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class FlashcardImport {

    private final FlashcardRepository flashcardRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final MaterialService materialService;
    private final AnswerFCRepository answerFCRepository;

    private static final Integer DEFAULT_USER_ID = 1;
    private static final int MAX_OPTION_TEXT_LENGTH = 255;

    @Autowired
    public FlashcardImport(
            FlashcardRepository flashcardRepository,
            MaterialRepository materialRepository,
            UserRepository userRepository,
            MaterialService materialService,
            AnswerFCRepository answerFCRepository) {
        this.flashcardRepository = flashcardRepository;
        this.materialRepository = materialRepository;
        this.userRepository = userRepository;
        this.materialService = materialService;
        this.answerFCRepository = answerFCRepository;
    }

    @Transactional
    public int importFlashcardsFromDirectory(String baseDir, Integer userId) throws IOException {
        Integer actualUserId = (userId != null) ? userId : DEFAULT_USER_ID;
        User user = userRepository.getReferenceById(actualUserId);

        int totalImported = 0;

        List<Path> flashcardFiles = findFlashcardFiles(Paths.get(baseDir));

        System.out.println("Found " + flashcardFiles.size() + " flashcard files to import");

        for (Path filePath : flashcardFiles) {
            try {
                String fileContent = Files.readString(filePath);

                // Extract material from file path
                Material material = findMaterialForFlashcardFile(filePath, baseDir);

                if (material == null) {
                    System.err.println("EROARE: Nu s-a gasit materialul pentru fisierul: " + filePath);
                    System.err.println("Fisierul va fi sarit.");
                    continue;
                }

                FlashCardParser parser = new FlashCardParser(fileContent);
                List<FlashcardDTO> flashcards = parser.getParsedText();

                int imported = importFlashcardsForMaterial(flashcards, material, user);
                totalImported += imported;

                System.out.println("Importate " + imported + " flashcard-uri din " + filePath +
                        " pentru materialul: " + material.getName() + " (ID: " + material.getId() + ")");

            } catch (Exception e) {
                System.err.println("EROARE la procesarea fisierului " + filePath + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return totalImported;
    }

    /**
     * Finds the corresponding Material for a flashcard file based on its path and filename.
     */
    private Material findMaterialForFlashcardFile(Path flashcardFilePath, String baseDir) {
        try {
            // Extract filename without extension and remove "_flashcards" suffix
            String fileName = flashcardFilePath.getFileName().toString();
            if (!fileName.endsWith("_flashcards.txt")) {
                System.err.println("EROARE: Numele fisierului nu respecta formatul asteptat: " + fileName);
                return null;
            }

            // Extract the base name (e.g., "agr1" from "agr1_flashcards.txt")
            String baseName = fileName.substring(0, fileName.length() - "_flashcards.txt".length());
            String expectedPdfName = baseName + ".pdf";

            System.out.println("Fisier flashcard: " + flashcardFilePath);
            System.out.println("BaseDir: " + baseDir);
            System.out.println("Nume fisier PDF asteptat: " + expectedPdfName);

            // Get the relative path from baseDir to the flashcard file
            Path baseDirectory = Paths.get(baseDir);
            Path relativePath = baseDirectory.relativize(flashcardFilePath);

            System.out.println("Path relativ: " + relativePath);

            String expectedMaterialPath;

            // Check if relativePath has a parent (i.e., it's in a subdirectory)
            if (relativePath.getParent() != null) {
                // Replace the flashcard filename with the expected PDF filename
                Path expectedPdfPath = relativePath.getParent().resolve(expectedPdfName);
                expectedMaterialPath = expectedPdfPath.toString().replace("\\", "/");
            } else {
                // File is directly in the base directory
                expectedMaterialPath = expectedPdfName;
            }

            System.out.println("Cautam materialul cu path-ul: " + expectedMaterialPath);

            // Search for material with this path - try multiple approaches
            Material material = findMaterialByPath(expectedMaterialPath, expectedPdfName, baseName);

            if (material != null) {
                System.out.println("Gasit material: " + material.getPath() + " (ID: " + material.getId() + ")");
            } else {
                System.err.println("Nu s-a gasit material pentru path-ul: " + expectedMaterialPath);
            }

            return material;

        } catch (Exception e) {
            System.err.println("EROARE la cautarea materialului pentru " + flashcardFilePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to find material using multiple search strategies
     */
    private Material findMaterialByPath(String expectedMaterialPath, String expectedPdfName, String baseName) {
        // Strategy 1: Exact path match
        Material material = materialService.findByPath(expectedMaterialPath);
        if (material != null) {
            System.out.println("Gasit material prin cautare exacta: " + material.getPath());
            return material;
        }

        // Strategy 2: Path containing the expected path
        List<Material> materialsContaining = materialService.findByPathContaining(expectedMaterialPath);
        if (!materialsContaining.isEmpty()) {
            material = materialsContaining.get(0);
            System.out.println("Gasit material prin cautare partiala (path complet): " + material.getPath());
            return material;
        }

        // Strategy 3: Search by PDF filename only
        List<Material> materialsByPdfName = materialService.findByPathContaining(expectedPdfName);
        if (!materialsByPdfName.isEmpty()) {
            material = materialsByPdfName.get(0);
            System.out.println("Gasit material prin numele fisierului PDF: " + material.getPath());
            return material;
        }

        // Strategy 4: Search by base name (without extension)
        List<Material> materialsByBaseName = materialService.findByPathContaining(baseName);
        if (!materialsByBaseName.isEmpty()) {
            material = materialsByBaseName.get(0);
            System.out.println("Gasit material prin numele de baza: " + material.getPath());
            return material;
        }

        // Strategy 5: Try to build alternative paths
        String[] alternativePaths = {
                "courses/" + expectedMaterialPath,
                "cursuri/" + expectedMaterialPath,
                expectedMaterialPath.replace("courses/", "cursuri/"),
                expectedMaterialPath.replace("cursuri/", "courses/")
        };

        for (String altPath : alternativePaths) {
            material = materialService.findByPath(altPath);
            if (material != null) {
                System.out.println("Gasit material prin path alternativ: " + material.getPath());
                return material;
            }

            List<Material> altMaterials = materialService.findByPathContaining(altPath);
            if (!altMaterials.isEmpty()) {
                material = altMaterials.get(0);
                System.out.println("Gasit material prin cautare partiala (path alternativ): " + material.getPath());
                return material;
            }
        }

        return null;
    }

    private List<Path> findFlashcardFiles(Path baseDir) throws IOException {
        List<Path> result = new ArrayList<>();

        if (!Files.exists(baseDir)) {
            throw new IOException("Directorul specificat nu exista: " + baseDir);
        }

        Files.walk(baseDir)
                .filter(path -> path.getFileName().toString().endsWith("_flashcards.txt"))
                .forEach(result::add);

        return result;
    }

    private int importFlashcardsForMaterial(List<FlashcardDTO> flashcards, Material material, User user) {
        int count = 0;

        for (FlashcardDTO fc : flashcards) {
            try {
                String questionText = extractQuestionText(fc.getQuestion());
                Optional<Flashcard> existingOpt = Optional.ofNullable(flashcardRepository.findByQuestion(questionText));
                Flashcard flashcard;

                if (existingOpt.isPresent()) {
                    flashcard = existingOpt.get();
                    // Update material if it's different
                    if (!flashcard.getMaterial().getId().equals(material.getId())) {
                        flashcard.setMaterial(material);
                        System.out.println("Actualizat materialul pentru flashcard existent: " + questionText);
                    }
                } else {
                    flashcard = new Flashcard();
                    flashcard.setQuestion(questionText);
                    flashcard.setLevel(fc.getLevel());
                    flashcard.setLastStudiedAt(null);
                    flashcard.setQuestionType(fc.getQuestionType());
                    flashcard.setUser(user);
                    flashcard.setMaterial(material);
                    flashcard.setPageIndex(fc.getPageIndex());

                    if (flashcard.getAnswers() == null) {
                        flashcard.setAnswers(new HashSet<>());
                    }

                    // Save flashcard first to generate ID
                    flashcard = flashcardRepository.save(flashcard);
                    count++;
                }

                // Update answers after flashcard is saved and has an ID
                updateAnswers(fc, flashcard);

                // Save flashcard again with updated answers
                flashcardRepository.save(flashcard);

            } catch (Exception e) {
                System.err.println("EROARE la procesarea flashcard-ului: " + fc.getQuestion());
                System.err.println("Eroare: " + e.getMessage());
                e.printStackTrace();
                // Continue with next flashcard instead of failing completely
            }
        }

        return count;
    }

    private void updateAnswers(FlashcardDTO fcDTO, Flashcard flashcard) {
        if (flashcard.getAnswers() == null) {
            flashcard.setAnswers(new HashSet<>());
        }

        if (fcDTO.getAnswers() == null) return;

        // Clear existing answers to avoid duplicates
        Set<AnswerFC> existingAnswers = new HashSet<>(flashcard.getAnswers());

        for (AnswerFCDTO answerDTO : fcDTO.getAnswers()) {
            String optionText = truncateOptionText(answerDTO.getOptionText());

            if (!answerExists(flashcard, optionText)) {
                try {
                    AnswerFC answer = new AnswerFC();
                    answer.setFlashcard(flashcard);
                    answer.setOptionText(optionText);
                    answer.setCorrect(answerDTO.isCorrect());

                    // Save the answer immediately to generate ID
                    answer = answerFCRepository.save(answer);
                    flashcard.getAnswers().add(answer);

                } catch (Exception e) {
                    System.err.println("EROARE la salvarea raspunsului: " + optionText);
                    System.err.println("Lungime text: " + (optionText != null ? optionText.length() : "null"));
                    System.err.println("Eroare: " + e.getMessage());
                    // Continue with next answer instead of failing completely
                }
            }
        }
    }

    /**
     * Truncate option text if it's too long for the database field
     */
    private String truncateOptionText(String optionText) {
        if (optionText == null) {
            return null;
        }

        if (optionText.length() <= MAX_OPTION_TEXT_LENGTH) {
            return optionText;
        }

        // Truncate and add ellipsis
        String truncated = optionText.substring(0, MAX_OPTION_TEXT_LENGTH - 3) + "...";
        System.out.println("AVERTISMENT: Text raspuns prea lung, s-a trunchiat la " + MAX_OPTION_TEXT_LENGTH + " caractere");
        System.out.println("Text original: " + optionText.substring(0, Math.min(100, optionText.length())) + "...");

        return truncated;
    }

    private String extractQuestionText(String rawQuestion) {
        if (rawQuestion.startsWith("[") && rawQuestion.endsWith("]")) {
            return rawQuestion.substring(1, rawQuestion.length() - 1);
        }
        return rawQuestion;
    }

    private Set<AnswerFC> createAnswers(FlashcardDTO fcDTO, Flashcard flashcard) {
        Set<AnswerFC> answers = new HashSet<>();

        if (fcDTO.getAnswers() == null) return answers;

        for (AnswerFCDTO answerDTO : fcDTO.getAnswers()) {
            String optionText = truncateOptionText(answerDTO.getOptionText());

            if (!answerExists(flashcard, optionText)) {
                try {
                    AnswerFC answer = new AnswerFC();
                    answer.setFlashcard(flashcard);
                    answer.setOptionText(optionText);
                    answer.setCorrect(answerDTO.isCorrect());

                    // Save the answer to generate ID
                    answer = answerFCRepository.save(answer);
                    answers.add(answer);

                } catch (Exception e) {
                    System.err.println("EROARE la crearea raspunsului: " + optionText);
                    System.err.println("Eroare: " + e.getMessage());
                    // Continue with next answer
                }
            }
        }

        return answers;
    }

    private boolean answerExists(Flashcard flashcard, String optionText) {
        if (flashcard.getAnswers() == null || flashcard.getAnswers().isEmpty()) {
            return false;
        }

        return flashcard.getAnswers().stream()
                .anyMatch(a -> a.getOptionText() != null && a.getOptionText().equals(optionText));
    }
}