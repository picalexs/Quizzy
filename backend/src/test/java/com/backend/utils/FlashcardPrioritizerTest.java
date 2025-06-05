package com.backend.utils;

import com.backend.model.AnswerFC;
import com.backend.model.Flashcard;
import com.backend.model.FlashcardProgress;
import com.backend.model.Material;
import com.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FlashcardPrioritizerTest {

    private List<Flashcard> flashcards;
    private List<FlashcardProgress> progressData;
    private Date currentDate;
    private User testUser;
    private Material testMaterial;

    @BeforeEach
    void setUp() {
        currentDate = new Date();
        
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");

        testMaterial = new Material();
        testMaterial.setId(1L);
        testMaterial.setName("Test Material");

        flashcards = new ArrayList<>();
        progressData = new ArrayList<>();
    }

    @Test
    void getPrioritizedFlashcards_EmptyList_ShouldReturnEmptyList() {
        // Given
        List<Flashcard> emptyList = new ArrayList<>();

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(emptyList, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPrioritizedFlashcards_WithoutProgressData_ShouldPrioritizeByFlashcardProperties() {
        // Given
        Flashcard highLevel = createFlashcard(1L, "High Level", 2, null, createAnswers(true));
        Flashcard lowLevel = createFlashcard(2L, "Low Level", 0, null, createAnswers(true));
        Flashcard mediumLevel = createFlashcard(3L, "Medium Level", 1, null, createAnswers(true));
        
        flashcards.addAll(Arrays.asList(highLevel, lowLevel, mediumLevel));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 3);

        // Then
        assertEquals(3, result.size());
        
        // Lower level should have higher priority (appear first)
        assertEquals("Low Level", result.get(0).getQuestion());
        assertEquals("Medium Level", result.get(1).getQuestion());
        assertEquals("High Level", result.get(2).getQuestion());
    }

    @Test
    void getPrioritizedFlashcards_WithProgressData_ShouldUseProgressForPrioritization() {
        // Given
        Flashcard flashcard1 = createFlashcard(1L, "Card 1", 1, null, createAnswers(true));
        Flashcard flashcard2 = createFlashcard(2L, "Card 2", 1, null, createAnswers(true));
        
        // Create progress with one card overdue
        Date overdue = new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(5));
        Date future = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(5));
        
        FlashcardProgress overdueProgress = createProgress(flashcard1, overdue, 2.5, 3, 7);
        FlashcardProgress futureProgress = createProgress(flashcard2, future, 2.5, 3, 7);
        
        flashcards.addAll(Arrays.asList(flashcard1, flashcard2));
        progressData.addAll(Arrays.asList(overdueProgress, futureProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 2);

        // Then
        assertEquals(2, result.size());
        // Overdue card should have higher priority
        assertEquals("Card 1", result.get(0).getQuestion());
        assertEquals("Card 2", result.get(1).getQuestion());
    }

    @Test
    void getPrioritizedFlashcards_LimitSmallerThanAvailable_ShouldReturnOnlyLimitNumber() {
        // Given
        for (int i = 1; i <= 10; i++) {
            flashcards.add(createFlashcard((long) i, "Card " + i, i % 3, null, createAnswers(true)));
        }

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 5);

        // Then
        assertEquals(5, result.size());
    }

    @Test
    void getPrioritizedFlashcards_LimitLargerThanAvailable_ShouldReturnAllAvailable() {
        // Given
        flashcards.add(createFlashcard(1L, "Card 1", 1, null, createAnswers(true)));
        flashcards.add(createFlashcard(2L, "Card 2", 1, null, createAnswers(true)));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 10);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getPrioritizedFlashcards_WithNullProgressData_ShouldHandleCorrectly() {
        // Given
        flashcards.add(createFlashcard(1L, "Card 1", 1, null, createAnswers(true)));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, null, 1, 1);

        // Then
        assertEquals(1, result.size());
        assertEquals("Card 1", result.get(0).getQuestion());
    }

    @Test
    void prioritization_OverdueCards_ShouldHaveHighestPriority() {
        // Given
        Flashcard overdueCard = createFlashcard(1L, "Overdue", 1, null, createAnswers(true));
        Flashcard normalCard = createFlashcard(2L, "Normal", 1, null, createAnswers(true));
        
        Date overdueDate = new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(10));
        Date normalDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(1));
        
        FlashcardProgress overdueProgress = createProgress(overdueCard, overdueDate, 2.5, 2, 3);
        FlashcardProgress normalProgress = createProgress(normalCard, normalDate, 2.5, 2, 3);
        
        flashcards.addAll(Arrays.asList(overdueCard, normalCard));
        progressData.addAll(Arrays.asList(overdueProgress, normalProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 2);

        // Then
        assertEquals("Overdue", result.get(0).getQuestion());
    }

    @Test
    void prioritization_LowEaseFactor_ShouldHaveHigherPriority() {
        // Given
        Flashcard difficultCard = createFlashcard(1L, "Difficult", 1, null, createAnswers(true));
        Flashcard easyCard = createFlashcard(2L, "Easy", 1, null, createAnswers(true));
        
        Date futureDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(5));
        
        FlashcardProgress difficultProgress = createProgress(difficultCard, futureDate, 1.3, 2, 3);
        FlashcardProgress easyProgress = createProgress(easyCard, futureDate, 2.8, 2, 3);
        
        flashcards.addAll(Arrays.asList(difficultCard, easyCard));
        progressData.addAll(Arrays.asList(difficultProgress, easyProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 2);

        // Then
        assertEquals("Difficult", result.get(0).getQuestion());
    }

    @Test
    void prioritization_LowRepetitions_ShouldHaveHigherPriority() {
        // Given
        Flashcard newCard = createFlashcard(1L, "New", 1, null, createAnswers(true));
        Flashcard practisedCard = createFlashcard(2L, "Practised", 1, null, createAnswers(true));
        
        Date futureDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(5));
        
        FlashcardProgress newProgress = createProgress(newCard, futureDate, 2.5, 1, 3);
        FlashcardProgress practisedProgress = createProgress(practisedCard, futureDate, 2.5, 5, 10);
        
        flashcards.addAll(Arrays.asList(newCard, practisedCard));
        progressData.addAll(Arrays.asList(newProgress, practisedProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 2);

        // Then
        assertEquals("New", result.get(0).getQuestion());
    }

    @Test
    void prioritization_ShortInterval_ShouldHaveHigherPriority() {
        // Given
        Flashcard shortIntervalCard = createFlashcard(1L, "Short", 1, null, createAnswers(true));
        Flashcard longIntervalCard = createFlashcard(2L, "Long", 1, null, createAnswers(true));
        
        Date futureDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(5));
        
        FlashcardProgress shortProgress = createProgress(shortIntervalCard, futureDate, 2.5, 2, 2);
        FlashcardProgress longProgress = createProgress(longIntervalCard, futureDate, 2.5, 2, 15);
        
        flashcards.addAll(Arrays.asList(shortIntervalCard, longIntervalCard));
        progressData.addAll(Arrays.asList(shortProgress, longProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 2);

        // Then
        assertEquals("Short", result.get(0).getQuestion());
    }

    @Test
    void prioritization_NeverStudied_ShouldHaveHighPriority() {
        // Given
        Flashcard neverStudied = createFlashcard(1L, "Never Studied", 1, null, createAnswers(true));
        Flashcard recentlyStudied = createFlashcard(2L, "Recently Studied", 1, 
            new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(1)), createAnswers(true));
        
        flashcards.addAll(Arrays.asList(neverStudied, recentlyStudied));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 2);

        // Then
        assertEquals("Never Studied", result.get(0).getQuestion());
    }

    @Test
    void prioritization_OldLastStudied_ShouldHaveHigherPriority() {
        // Given
        Flashcard oldStudied = createFlashcard(1L, "Old", 1, 
            new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(30)), createAnswers(true));
        Flashcard recentStudied = createFlashcard(2L, "Recent", 1, 
            new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(1)), createAnswers(true));
        
        flashcards.addAll(Arrays.asList(oldStudied, recentStudied));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 2);

        // Then
        assertEquals("Old", result.get(0).getQuestion());
    }

    @Test
    void prioritization_LowCorrectAnswerRatio_ShouldHaveHigherPriority() {
        // Given
        Set<AnswerFC> mixedAnswers = createMixedAnswers();
        Set<AnswerFC> correctAnswers = createAnswers(true);
        
        Flashcard difficultCard = createFlashcard(1L, "Difficult", 1, null, mixedAnswers);
        Flashcard easyCard = createFlashcard(2L, "Easy", 1, null, correctAnswers);
        
        flashcards.addAll(Arrays.asList(difficultCard, easyCard));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 2);

        // Then
        assertEquals("Difficult", result.get(0).getQuestion());
    }

    @Test
    void prioritization_NullAnswers_ShouldHandleCorrectly() {
        // Given
        Flashcard cardWithoutAnswers = createFlashcard(1L, "No Answers", 1, null, null);
        Flashcard cardWithAnswers = createFlashcard(2L, "With Answers", 1, null, createAnswers(true));
        
        flashcards.addAll(Arrays.asList(cardWithoutAnswers, cardWithAnswers));

        // When & Then - Should not throw exception
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 2);
        
        assertEquals(2, result.size());
    }

    @Test
    void prioritization_EmptyAnswers_ShouldHandleCorrectly() {
        // Given
        Flashcard cardWithEmptyAnswers = createFlashcard(1L, "Empty Answers", 1, null, new HashSet<>());
        Flashcard cardWithAnswers = createFlashcard(2L, "With Answers", 1, null, createAnswers(true));
        
        flashcards.addAll(Arrays.asList(cardWithEmptyAnswers, cardWithAnswers));

        // When & Then - Should not throw exception
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 2);
        
        assertEquals(2, result.size());
    }

    @Test
    void prioritization_SoonDueCards_ShouldHaveHigherPriority() {
        // Given
        Flashcard soonDue = createFlashcard(1L, "Soon Due", 1, null, createAnswers(true));
        Flashcard farDue = createFlashcard(2L, "Far Due", 1, null, createAnswers(true));
        
        Date soonDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(1));
        Date farDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(10));
        
        FlashcardProgress soonProgress = createProgress(soonDue, soonDate, 2.5, 2, 3);
        FlashcardProgress farProgress = createProgress(farDue, farDate, 2.5, 2, 3);
        
        flashcards.addAll(Arrays.asList(soonDue, farDue));
        progressData.addAll(Arrays.asList(soonProgress, farProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 2);

        // Then
        assertEquals("Soon Due", result.get(0).getQuestion());
    }

    @Test
    void prioritization_ComplexScenario_ShouldPrioritizeCorrectly() {
        // Given
        Flashcard overdue = createFlashcard(1L, "Overdue", 2, null, createAnswers(true));
        Flashcard soonDue = createFlashcard(2L, "Soon Due", 1, null, createAnswers(true));
        Flashcard neverStudied = createFlashcard(3L, "Never Studied", 0, null, createAnswers(false));
        Flashcard normal = createFlashcard(4L, "Normal", 1, null, createAnswers(true));
        
        Date overdueDate = new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(5));
        Date soonDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(2));
        Date normalDate = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(10));
        // For never studied card, set due date to current date to represent it needs attention
        Date neverStudiedDate = new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(1));
        
        FlashcardProgress overdueProgress = createProgress(overdue, overdueDate, 2.5, 2, 3);
        FlashcardProgress soonProgress = createProgress(soonDue, soonDate, 2.5, 2, 3);
        FlashcardProgress normalProgress = createProgress(normal, normalDate, 2.5, 5, 15);
        // Create progress for never studied card with new card characteristics
        FlashcardProgress neverStudiedProgress = createProgress(neverStudied, neverStudiedDate, 2.5, 0, 1);
        
        flashcards.addAll(Arrays.asList(overdue, soonDue, neverStudied, normal));
        progressData.addAll(Arrays.asList(overdueProgress, soonProgress, normalProgress, neverStudiedProgress));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(
            flashcards, progressData, 1, 4);

        // Then
        assertEquals(4, result.size());
        
        // Priority order should be: Overdue, Never Studied, Soon Due, Normal
        assertEquals("Overdue", result.get(0).getQuestion());
        assertEquals("Normal", result.get(3).getQuestion());
    }

    @Test
    void prioritization_ZeroLimit_ShouldReturnEmptyList() {
        // Given
        flashcards.add(createFlashcard(1L, "Card", 1, null, createAnswers(true)));

        // When
        List<Flashcard> result = FlashcardPrioritizer.getPrioritizedFlashcards(flashcards, 0);

        // Then
        assertTrue(result.isEmpty());
    }

    // Helper methods
    private Flashcard createFlashcard(Long id, String question, Integer level, Date lastStudied, Set<AnswerFC> answers) {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(id);
        flashcard.setQuestion(question);
        flashcard.setLevel(level);
        flashcard.setLastStudiedAt(lastStudied);
        flashcard.setUser(testUser);
        flashcard.setMaterial(testMaterial);
        flashcard.setAnswers(answers);
        return flashcard;
    }

    private FlashcardProgress createProgress(Flashcard flashcard, Date dueDate, Double easeFactor, 
                                           Integer repetitions, Integer interval) {
        FlashcardProgress progress = new FlashcardProgress();
        progress.setFlashcard(flashcard);
        progress.setUser(testUser);
        progress.setDueDate(dueDate);
        progress.setEaseFactor(easeFactor);
        progress.setRepetitions(repetitions);
        progress.setInterval(interval);
        progress.setLastReviewed(new Date());
        progress.setConfidenceLevel(3);
        progress.setConsecutiveFailures(0);
        progress.setLearningStage(1);
        progress.setRetentionScore(75.0);
        progress.setStudyTimeMs(30000L);
        progress.setTotalFailures(0);
        return progress;
    }

    private Set<AnswerFC> createAnswers(boolean allCorrect) {
        Set<AnswerFC> answers = new HashSet<>();
        
        AnswerFC answer1 = new AnswerFC();
        answer1.setOptionText("Answer 1");
        answer1.setCorrect(allCorrect);
        answers.add(answer1);
        
        AnswerFC answer2 = new AnswerFC();
        answer2.setOptionText("Answer 2");
        answer2.setCorrect(allCorrect);
        answers.add(answer2);
        
        return answers;
    }

    private Set<AnswerFC> createMixedAnswers() {
        Set<AnswerFC> answers = new HashSet<>();
        
        AnswerFC correctAnswer = new AnswerFC();
        correctAnswer.setOptionText("Correct Answer");
        correctAnswer.setCorrect(true);
        answers.add(correctAnswer);
        
        AnswerFC wrongAnswer1 = new AnswerFC();
        wrongAnswer1.setOptionText("Wrong Answer 1");
        wrongAnswer1.setCorrect(false);
        answers.add(wrongAnswer1);
        
        AnswerFC wrongAnswer2 = new AnswerFC();
        wrongAnswer2.setOptionText("Wrong Answer 2");
        wrongAnswer2.setCorrect(false);
        answers.add(wrongAnswer2);
        
        return answers;
    }
} 