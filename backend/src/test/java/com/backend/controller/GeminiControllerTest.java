package com.backend.controller;

import com.backend.model.Flashcard;
import com.backend.model.FlashcardProgress;
import com.backend.model.User;
import com.backend.service.FlashcardProgressService;
import com.backend.service.FlashcardService;
import com.backend.service.GeminiService;
import com.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiControllerTest {

    @Mock
    private GeminiService geminiService;
    
    @Mock
    private FlashcardProgressService flashcardProgressService;
    
    @Mock
    private FlashcardService flashcardService;
    
    @Mock
    private UserService userService;

    @InjectMocks
    private GeminiController geminiController;

    @Test
    void shouldReturnTestMessage() {
        // When
        String result = geminiController.testGemini();

        // Then
        assertEquals("Gemini controller is working!", result);
    }

    @Test
    void shouldGenerateResponseSuccessfully() throws IOException {
        // Given
        String inputFilePath = "test/path/file.txt";
        String expectedResponse = "Generated content";
        when(geminiService.processFile(inputFilePath)).thenReturn(expectedResponse);

        // When
        String result = geminiController.generateResponse(inputFilePath);

        // Then
        assertEquals(expectedResponse, result);
        verify(geminiService).processFile(inputFilePath);
    }

    @Test
    void shouldThrowIOExceptionWhenProcessFileFails() throws IOException {
        // Given
        String inputFilePath = "invalid/path/file.txt";
        when(geminiService.processFile(inputFilePath)).thenThrow(new IOException("File not found"));

        // When & Then
        assertThrows(IOException.class, () -> geminiController.generateResponse(inputFilePath));
        verify(geminiService).processFile(inputFilePath);
    }

    @Test
    void shouldGenerateResponseWithPromptSuccessfully() throws IOException {
        // Given
        String inputFilePath = "course/test.txt";
        String expectedContent = "Generated flashcards content";
        when(geminiService.processFileWithPrompt(eq(inputFilePath), anyString())).thenReturn(expectedContent);

        // When
        String result = geminiController.generateResponseWithPrompt(inputFilePath);

        // Then
        assertTrue(result.startsWith("ok - Saved to"));
        verify(geminiService).processFileWithPrompt(eq(inputFilePath), anyString());
    }

    @Test
    void shouldHandleErrorInGenerateWithPrompt() throws IOException {
        // Given
        String inputFilePath = "course/test.txt";
        when(geminiService.processFileWithPrompt(eq(inputFilePath), anyString()))
            .thenThrow(new IOException("Service error"));

        // When & Then
        assertThrows(IOException.class, () -> geminiController.generateResponseWithPrompt(inputFilePath));
        verify(geminiService).processFileWithPrompt(eq(inputFilePath), anyString());
    }

    @Test
    void shouldCompareUsersAnswerSuccessfullyForNewUser() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "4");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);
        Flashcard mockFlashcard = new Flashcard();
        mockFlashcard.setId(1L);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.of(mockFlashcard));
        when(flashcardProgressService.createFlashcardProgress(any(FlashcardProgress.class))).thenReturn(new FlashcardProgress());
        
        // Mock 5 Gemini responses for averaging
        when(geminiService.getGeminiResponse(anyString()))
            .thenReturn("text=90.0\\n")
            .thenReturn("text=88.0\\n")
            .thenReturn("text=92.0\\n")
            .thenReturn("text=89.0\\n")
            .thenReturn("text=91.0\\n");

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() >= 85 && response.getBody() <= 95); // Should be around 90
        verify(flashcardProgressService).createFlashcardProgress(any(FlashcardProgress.class));
        verify(geminiService, times(5)).getGeminiResponse(anyString());
    }

    @Test
    void shouldUpdateExistingFlashcardProgress() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "4");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        FlashcardProgress existingProgress = new FlashcardProgress();
        existingProgress.setId(1L);
        existingProgress.setRepetitions(3);
        existingProgress.setConsecutiveFailures(0);
        existingProgress.setTotalFailures(1);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(existingProgress);
        when(flashcardProgressService.updateFlashcardProgress(eq(1L), any(FlashcardProgress.class))).thenReturn(existingProgress);
        
        // Mock 5 good responses
        when(geminiService.getGeminiResponse(anyString()))
            .thenReturn("text=75.0\\n")
            .thenReturn("text=73.0\\n")
            .thenReturn("text=77.0\\n")
            .thenReturn("text=74.0\\n")
            .thenReturn("text=76.0\\n");

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() >= 70 && response.getBody() <= 80);
        verify(flashcardProgressService).updateFlashcardProgress(eq(1L), any(FlashcardProgress.class));
        verify(geminiService, times(5)).getGeminiResponse(anyString());
    }

    @Test
    void shouldHandleZeroScoreCorrectly() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "completely wrong");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);
        Flashcard mockFlashcard = new Flashcard();
        mockFlashcard.setId(1L);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.of(mockFlashcard));
        when(flashcardProgressService.createFlashcardProgress(any(FlashcardProgress.class))).thenReturn(new FlashcardProgress());
        
        // Mock responses with at least one zero
        when(geminiService.getGeminiResponse(anyString()))
            .thenReturn("text=0.0\\n")
            .thenReturn("text=5.0\\n")
            .thenReturn("text=2.0\\n")
            .thenReturn("text=1.0\\n")
            .thenReturn("text=3.0\\n");

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody()); // Should be 0.0 when any response is zero
    }

    @Test
    void shouldSetCorrectEaseFactorForBadAnswer() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "wrong");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);
        Flashcard mockFlashcard = new Flashcard();
        mockFlashcard.setId(1L);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.of(mockFlashcard));
        when(flashcardProgressService.createFlashcardProgress(any(FlashcardProgress.class))).thenReturn(new FlashcardProgress());
        
        // Mock responses for bad answer (≤33.33)
        when(geminiService.getGeminiResponse(anyString()))
            .thenReturn("text=30.0\\n")
            .thenReturn("text=25.0\\n")
            .thenReturn("text=33.0\\n")
            .thenReturn("text=28.0\\n")
            .thenReturn("text=31.0\\n");

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() <= 35); // Bad answer
        
        // Verify that createFlashcardProgress is called with correct ease factor
        verify(flashcardProgressService).createFlashcardProgress(argThat(fp -> 
            fp.getEaseFactor() == 2.0 && // Bad answer should set ease factor to 2.0
            fp.getConsecutiveFailures() == 1 &&
            fp.getTotalFailures() == 1
        ));
    }

    @Test
    void shouldSetCorrectEaseFactorForGoodAnswer() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "4");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);
        Flashcard mockFlashcard = new Flashcard();
        mockFlashcard.setId(1L);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.of(mockFlashcard));
        when(flashcardProgressService.createFlashcardProgress(any(FlashcardProgress.class))).thenReturn(new FlashcardProgress());
        
        // Mock responses for good answer (≥66.66)
        when(geminiService.getGeminiResponse(anyString()))
            .thenReturn("text=85.0\\n")
            .thenReturn("text=90.0\\n")
            .thenReturn("text=88.0\\n")
            .thenReturn("text=87.0\\n")
            .thenReturn("text=89.0\\n");

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() >= 85); // Good answer
        
        // Verify that createFlashcardProgress is called with correct ease factor
        verify(flashcardProgressService).createFlashcardProgress(argThat(fp -> 
            fp.getEaseFactor() == 1.3 && // Good answer should set ease factor to 1.3
            fp.getConsecutiveFailures() == 0 &&
            fp.getTotalFailures() == 0
        ));
    }

    @Test
    void shouldSetCorrectEaseFactorForMediumAnswer() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "approximately 4");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);
        Flashcard mockFlashcard = new Flashcard();
        mockFlashcard.setId(1L);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.of(mockFlashcard));
        when(flashcardProgressService.createFlashcardProgress(any(FlashcardProgress.class))).thenReturn(new FlashcardProgress());
        
        // Mock responses for medium answer (33.34 - 66.65)
        when(geminiService.getGeminiResponse(anyString()))
            .thenReturn("text=50.0\\n")
            .thenReturn("text=55.0\\n")
            .thenReturn("text=52.0\\n")
            .thenReturn("text=48.0\\n")
            .thenReturn("text=53.0\\n");

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() >= 45 && response.getBody() <= 60); // Medium answer
        
        // Verify that createFlashcardProgress is called with correct ease factor
        verify(flashcardProgressService).createFlashcardProgress(argThat(fp -> 
            fp.getEaseFactor() == 1.6 && // Medium answer should set ease factor to 1.6
            fp.getConsecutiveFailures() == 0 &&
            fp.getTotalFailures() == 0
        ));
    }

    @Test
    void shouldHandleGeminiServiceErrorGracefully() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "Test question");
        request.put("officialAnswer", "Test answer");
        request.put("usersAnswer", "Test user answer");
        request.put("flashcardId", "1");
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);
        Flashcard mockFlashcard = new Flashcard();
        mockFlashcard.setId(1L);

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(1L)).thenReturn(Optional.of(mockFlashcard));
        when(flashcardProgressService.createFlashcardProgress(any(FlashcardProgress.class))).thenReturn(new FlashcardProgress());

        when(geminiService.getGeminiResponse(anyString()))
            .thenThrow(new RuntimeException("Gemini API error"));

        // When
        ResponseEntity<Double> response = geminiController.compareUsersAnswerToOfficialAnswer(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody()); // Should return 0.0 when all calls fail
    }

    @Test
    void shouldHandleInvalidRequestParameters() {
        // Given
        Map<String, String> invalidRequest = new HashMap<>();
        invalidRequest.put("question", "Test question");
        // Missing other required fields

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            geminiController.compareUsersAnswerToOfficialAnswer(invalidRequest));
    }

    @Test
    void shouldHandleUserNotFound() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "4");
        request.put("flashcardId", "1");
        request.put("userId", "999"); // Non-existent user

        when(flashcardProgressService.getByFlashcardIdAndUserId(1L, 999)).thenReturn(null);
        when(userService.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            geminiController.compareUsersAnswerToOfficialAnswer(request));
    }

    @Test
    void shouldHandleFlashcardNotFound() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("question", "What is 2+2?");
        request.put("officialAnswer", "4");
        request.put("usersAnswer", "4");
        request.put("flashcardId", "999"); // Non-existent flashcard
        request.put("userId", "1");

        User mockUser = new User();
        mockUser.setId(1);

        when(flashcardProgressService.getByFlashcardIdAndUserId(999L, 1)).thenReturn(null);
        when(userService.findById(1)).thenReturn(Optional.of(mockUser));
        when(flashcardService.getFlashcardById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            geminiController.compareUsersAnswerToOfficialAnswer(request));
    }
} 