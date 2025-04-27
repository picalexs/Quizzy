package com.backend.controller;

import com.backend.model.Flashcard;
import com.backend.service.FlashcardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping
    public List<Flashcard> getAllFlashcards() {
        return flashcardService.getAllFlashcards();
    }

    @GetMapping("/{id}")
    public Optional<Flashcard> getFlashcardById(@PathVariable Long id) {
        return flashcardService.getFlashcardById(id);
    }

    @PostMapping
    public Flashcard createFlashcard(@RequestBody Flashcard flashcard) {
        return flashcardService.createFlashcard(flashcard);
    }

    @PutMapping("/{id}")
    public Flashcard updateFlashcard(@PathVariable Long id, @RequestBody Flashcard flashcard) {
        return flashcardService.updateFlashcard(id, flashcard);
    }

    @DeleteMapping("/{id}")
    public void deleteFlashcard(@PathVariable Long id) {
        flashcardService.deleteFlashcard(id);
    }

    // Extra queries

    @GetMapping("/user/{userId}")
    public List<Flashcard> getByUserId(@PathVariable Integer userId) {
        return flashcardService.getByUserId(userId);
    }

    @GetMapping("/material/{materialId}")
    public List<Flashcard> getByMaterialId(@PathVariable Long materialId) {
        return flashcardService.getByMaterialId(materialId);
    }

    @GetMapping("/due")
    public List<Flashcard> getDueFlashcards(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date,
            @RequestParam("userId") Integer userId
    ) {
        return flashcardService.getDueFlashcards(date, userId);
    }

    @GetMapping("/level")
    public List<Flashcard> getByLevelAndUserId(
            @RequestParam int level,
            @RequestParam Integer userId
    ) {
        return flashcardService.getByLevelAndUserId(level, userId);
    }

    @GetMapping("/course")
    public List<Flashcard> getByCourseAndUser(
            @RequestParam Long courseId,
            @RequestParam Integer userId
    ) {
        return flashcardService.getByCourseIdAndUserId(courseId, userId);
    }

    @GetMapping("/type")
    public List<Flashcard> getByQuestionTypeAndUserId(
            @RequestParam String type,
            @RequestParam Integer userId
    ) {
        return flashcardService.getByQuestionTypeAndUserId(type, userId);
    }
}
