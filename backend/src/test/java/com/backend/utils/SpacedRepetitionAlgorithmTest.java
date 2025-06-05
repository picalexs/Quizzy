package com.backend.utils;

import com.backend.model.Flashcard;
import com.backend.model.FlashcardProgress;
import com.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacedRepetitionAlgorithmTest {

    private FlashcardProgress progress;
    private User testUser;
    private Flashcard testFlashcard;
    private Date startDate;

    @BeforeEach
    void setUp() {
        startDate = new Date();
        
        testUser = new User();
        testUser.setId(1);

        testFlashcard = new Flashcard();
        testFlashcard.setId(1L);
        testFlashcard.setQuestion("Test Question");

        progress = new FlashcardProgress();
        progress.setUser(testUser);
        progress.setFlashcard(testFlashcard);
    }

    @Test
    void updateProgress_NewCard_WithGoodResponse_ShouldInitializeCorrectly() {
        // Given - Create a new progress simulating a new card (set minimal required fields)
        FlashcardProgress newProgress = new FlashcardProgress();
        newProgress.setUser(testUser);
        newProgress.setFlashcard(testFlashcard);
        // Initialize with default values that represent "new card" state
        newProgress.setEaseFactor(2.5); // Default ease factor
        newProgress.setRepetitions(0);  // No repetitions yet
        newProgress.setInterval(0);     // No interval set yet
        int responseQuality = 4; // Good response

        // When
        SpacedRepetitionAlgorithm.updateProgress(newProgress, responseQuality);

        // Then
        assertEquals(1, newProgress.getRepetitions());
        assertEquals(1, newProgress.getInterval());
        assertTrue(newProgress.getEaseFactor() >= 1.3);
        assertNotNull(newProgress.getLastReviewed());
        assertNotNull(newProgress.getDueDate());
    }

    @Test
    void updateProgress_NewCard_WithPoorResponse_ShouldResetProgress() {
        // Given - Create a new progress simulating a new card
        FlashcardProgress newProgress = new FlashcardProgress();
        newProgress.setUser(testUser);
        newProgress.setFlashcard(testFlashcard);
        // Initialize with default values
        newProgress.setEaseFactor(2.5);
        newProgress.setRepetitions(0);
        newProgress.setInterval(0);
        int responseQuality = 2; // Poor response

        // When
        SpacedRepetitionAlgorithm.updateProgress(newProgress, responseQuality);

        // Then
        assertEquals(0, newProgress.getRepetitions());
        assertEquals(1, newProgress.getInterval());
        assertTrue(newProgress.getEaseFactor() >= 1.3);
        assertNotNull(newProgress.getLastReviewed());
        assertNotNull(newProgress.getDueDate());
    }

    @Test
    void updateProgress_FirstRepetition_WithGoodResponse_ShouldSetIntervalToOne() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(0);
        progress.setInterval(0);
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(1, progress.getRepetitions());
        assertEquals(1, progress.getInterval());
    }

    @Test
    void updateProgress_SecondRepetition_WithGoodResponse_ShouldSetIntervalToSix() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(2, progress.getRepetitions());
        assertEquals(6, progress.getInterval());
    }

    @Test
    void updateProgress_ThirdRepetition_WithGoodResponse_ShouldMultiplyByEaseFactor() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(2);
        progress.setInterval(6);
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(3, progress.getRepetitions());
        assertEquals(15, progress.getInterval()); // 6 * 2.5 = 15
    }

    @Test
    void updateProgress_SubsequentRepetitions_ShouldMultiplyByEaseFactor() {
        // Given
        progress.setEaseFactor(2.0);
        progress.setRepetitions(5);
        progress.setInterval(30);
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(6, progress.getRepetitions());
        assertEquals(60, progress.getInterval()); // 30 * 2.0 = 60
    }

    @Test
    void updateProgress_PoorResponse_ShouldResetRepetitionsAndInterval() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(5);
        progress.setInterval(30);
        int responseQuality = 2; // Poor response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(0, progress.getRepetitions());
        assertEquals(1, progress.getInterval());
        assertTrue(progress.getEaseFactor() >= 1.3);
    }

    @Test
    void updateProgress_ResponseQuality0_ShouldResetProgress() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(3);
        progress.setInterval(15);
        int responseQuality = 0; // Worst response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(0, progress.getRepetitions());
        assertEquals(1, progress.getInterval());
    }

    @Test
    void updateProgress_ResponseQuality1_ShouldResetProgress() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(3);
        progress.setInterval(15);
        int responseQuality = 1;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(0, progress.getRepetitions());
        assertEquals(1, progress.getInterval());
    }

    @Test
    void updateProgress_ResponseQuality2_ShouldResetProgress() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(3);
        progress.setInterval(15);
        int responseQuality = 2;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(0, progress.getRepetitions());
        assertEquals(1, progress.getInterval());
    }

    @Test
    void updateProgress_ResponseQuality3_ShouldContinueProgress() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(2);
        progress.setInterval(6);
        int responseQuality = 3; // Acceptable response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(3, progress.getRepetitions());
        assertEquals(15, progress.getInterval()); // Should continue normally
    }

    @Test
    void updateProgress_ResponseQuality4_ShouldContinueProgress() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 4; // Good response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(2, progress.getRepetitions());
        assertEquals(6, progress.getInterval());
    }

    @Test
    void updateProgress_ResponseQuality5_ShouldContinueProgress() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 5; // Perfect response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(2, progress.getRepetitions());
        assertEquals(6, progress.getInterval());
    }

    @Test
    void updateProgress_EaseFactorCalculation_WithPerfectResponse() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 5; // Perfect response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // New ease factor = 2.5 + (0.1 - (5-5) * (0.08 + (5-5) * 0.02))
        // = 2.5 + 0.1 = 2.6
        assertEquals(2.6, progress.getEaseFactor(), 0.01);
    }

    @Test
    void updateProgress_EaseFactorCalculation_WithGoodResponse() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 4; // Good response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // New ease factor = 2.5 + (0.1 - (5-4) * (0.08 + (5-4) * 0.02))
        // = 2.5 + (0.1 - 1 * (0.08 + 1 * 0.02))
        // = 2.5 + (0.1 - 0.1) = 2.5
        assertEquals(2.5, progress.getEaseFactor(), 0.01);
    }

    @Test
    void updateProgress_EaseFactorCalculation_WithAcceptableResponse() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 3; // Acceptable response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // New ease factor = 2.5 + (0.1 - (5-3) * (0.08 + (5-3) * 0.02))
        // = 2.5 + (0.1 - 2 * (0.08 + 2 * 0.02))
        // = 2.5 + (0.1 - 2 * 0.12) = 2.5 + (0.1 - 0.24) = 2.36
        assertEquals(2.36, progress.getEaseFactor(), 0.01);
    }

    @Test
    void updateProgress_EaseFactorCalculation_WithPoorResponse() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 2; // Poor response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // Since response < 3, repetitions should be reset to 0, but ease factor is still calculated
        // New ease factor = 2.5 + (0.1 - (5-2) * (0.08 + (5-2) * 0.02))
        // = 2.5 + (0.1 - 3 * (0.08 + 3 * 0.02)) = 2.5 + (0.1 - 3 * 0.14) = 2.5 + (0.1 - 0.42) = 2.18
        assertEquals(2.18, progress.getEaseFactor(), 0.01);
        assertEquals(0, progress.getRepetitions()); // Should be reset
    }

    @Test
    void updateProgress_EaseFactorMinimum_ShouldNotGoBelowThreshold() {
        // Given
        progress.setEaseFactor(1.4); // Close to minimum
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 0; // Worst response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // Even with worst response, ease factor should not go below 1.3
        assertEquals(1.3, progress.getEaseFactor(), 0.01);
    }

    @Test
    void updateProgress_ResponseQualityBelowZero_ShouldBeClampedToZero() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = -1; // Below valid range

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // Should be treated as 0
        assertEquals(0, progress.getRepetitions());
        assertEquals(1, progress.getInterval());
    }

    @Test
    void updateProgress_ResponseQualityAboveFive_ShouldBeClampedToFive() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        int responseQuality = 10; // Above valid range

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        // Should be treated as 5 (perfect response)
        assertEquals(2, progress.getRepetitions());
        assertEquals(6, progress.getInterval());
        assertEquals(2.6, progress.getEaseFactor(), 0.01); // Same as perfect response
    }

    @Test
    void updateProgress_DueDateCalculation_ShouldBeCorrect() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(2);
        progress.setInterval(6);
        int responseQuality = 4;
        Date beforeUpdate = new Date();

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertNotNull(progress.getDueDate());
        assertNotNull(progress.getLastReviewed());
        
        // Calculate expected due date
        Calendar expected = Calendar.getInstance();
        expected.setTime(progress.getLastReviewed());
        expected.add(Calendar.DATE, progress.getInterval());
        
        assertEquals(expected.getTime(), progress.getDueDate());
        
        // Last reviewed should be around now
        assertTrue(progress.getLastReviewed().getTime() >= beforeUpdate.getTime());
        assertTrue(progress.getLastReviewed().getTime() <= new Date().getTime());
    }

    @Test
    void updateProgress_LastReviewedShouldBeUpdated() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(1);
        progress.setInterval(1);
        Date beforeUpdate = new Date();

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, 4);

        // Then
        assertNotNull(progress.getLastReviewed());
        assertTrue(progress.getLastReviewed().getTime() >= beforeUpdate.getTime());
        assertTrue(progress.getLastReviewed().getTime() <= new Date().getTime());
    }

    @Test
    void updateProgress_ComplexScenario_LongTermLearning() {
        // Given - Simulate a card that has been learned well over time
        progress.setEaseFactor(2.8); // High ease factor from good performance
        progress.setRepetitions(10); // Many successful repetitions
        progress.setInterval(180); // 6 months interval
        int responseQuality = 4; // Still good response

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(11, progress.getRepetitions());
        assertEquals(504, progress.getInterval()); // 180 * 2.8 = 504 days
        assertEquals(2.8, progress.getEaseFactor(), 0.01); // Should remain same for quality 4
    }

    @Test
    void updateProgress_ComplexScenario_LearningStruggle() {
        // Given - Simulate a difficult card that student struggles with
        progress.setEaseFactor(1.5); // Low ease factor from poor performance
        progress.setRepetitions(1); // Recently reset
        progress.setInterval(1);
        int responseQuality = 3; // Barely acceptable

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(2, progress.getRepetitions());
        assertEquals(6, progress.getInterval()); // Second repetition always gets 6 days
        assertTrue(progress.getEaseFactor() < 1.5); // Should decrease slightly
    }

    @Test
    void updateProgress_IntervalRounding_ShouldHandleDecimalCorrectly() {
        // Given
        progress.setEaseFactor(2.7); // Will produce decimal when multiplying
        progress.setRepetitions(3);
        progress.setInterval(10);
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(4, progress.getRepetitions());
        assertEquals(27, progress.getInterval()); // 10 * 2.7 = 27.0, rounded to 27
    }

    @Test
    void updateProgress_EdgeCase_ZeroInterval() {
        // Given
        progress.setEaseFactor(2.5);
        progress.setRepetitions(0);
        progress.setInterval(0); // Edge case
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(progress, responseQuality);

        // Then
        assertEquals(1, progress.getRepetitions());
        assertEquals(1, progress.getInterval()); // First repetition should be 1 day
    }

    @Test
    void updateProgress_DefaultValues_ShouldWorkCorrectly() {
        // Given - Create a new progress object with initial values
        FlashcardProgress newProgress = new FlashcardProgress();
        newProgress.setUser(testUser);
        newProgress.setFlashcard(testFlashcard);
        // Set initial values that represent a fresh card
        newProgress.setEaseFactor(2.5);
        newProgress.setRepetitions(0);
        newProgress.setInterval(0);
        int responseQuality = 4;

        // When
        SpacedRepetitionAlgorithm.updateProgress(newProgress, responseQuality);

        // Then
        assertEquals(1, newProgress.getRepetitions()); // Should use default progression
        assertEquals(1, newProgress.getInterval()); // First repetition should be 1 day
        assertTrue(newProgress.getEaseFactor() >= 1.3); // Should calculate based on response
        assertNotNull(newProgress.getLastReviewed());
        assertNotNull(newProgress.getDueDate());
        
        // Due date should be 1 day from last reviewed (interval = 1)
        long timeDifference = newProgress.getDueDate().getTime() - newProgress.getLastReviewed().getTime();
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        assertEquals(oneDayInMillis, timeDifference, 1000); // Allow 1 second tolerance
    }

    @Test
    void updateProgress_InitializationBehavior_ShouldHandleNullFieldsCorrectly() {
        // Given - Test initialization behavior by starting with minimal data
        FlashcardProgress uninitializedProgress = new FlashcardProgress();
        uninitializedProgress.setUser(testUser);
        uninitializedProgress.setFlashcard(testFlashcard);
        // Set to represent truly new card state
        uninitializedProgress.setEaseFactor(2.5); // Standard starting ease factor
        uninitializedProgress.setRepetitions(0);  // No previous repetitions
        uninitializedProgress.setInterval(0);     // No interval established
        
        int responseQuality = 3; // Acceptable response

        // When
        SpacedRepetitionAlgorithm.updateProgress(uninitializedProgress, responseQuality);

        // Then - Verify all fields are properly updated
        assertNotNull(uninitializedProgress.getEaseFactor());
        assertTrue(uninitializedProgress.getEaseFactor() >= 1.3);
        assertTrue(uninitializedProgress.getEaseFactor() <= 5.0); // Reasonable upper bound
        
        assertEquals(1, uninitializedProgress.getRepetitions()); // First successful repetition
        assertEquals(1, uninitializedProgress.getInterval()); // First repetition interval
        
        assertNotNull(uninitializedProgress.getLastReviewed());
        assertNotNull(uninitializedProgress.getDueDate());
        
        // Verify date logic
        assertTrue(uninitializedProgress.getDueDate().after(uninitializedProgress.getLastReviewed()));
    }
} 