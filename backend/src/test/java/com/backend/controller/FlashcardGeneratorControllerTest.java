package com.backend.controller;

import com.backend.utils.FlashcardBatchGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardGeneratorControllerTest {

    @Mock
    private FlashcardBatchGenerator flashcardBatchGenerator;

    @InjectMocks
    private FlashcardGeneratorController flashcardGeneratorController;


    @Test
    void shouldReturnAlreadyRunningWhenGenerationInProgress() throws InterruptedException {
        // Given
        when(flashcardBatchGenerator.getTotalFiles()).thenReturn(10);
        when(flashcardBatchGenerator.getProcessedFiles()).thenReturn(0);

        doAnswer(invocation -> {
            // Simulate long running operation
            TimeUnit.MILLISECONDS.sleep(100);
            return null;
        }).when(flashcardBatchGenerator).generateAllFlashcards();

        // When - start first generation
        ResponseEntity<Map<String, Object>> firstResponse = flashcardGeneratorController.generateFlashcards();

        // Give some time for the async operation to start
        TimeUnit.MILLISECONDS.sleep(10);

        // Try to start second generation immediately
        ResponseEntity<Map<String, Object>> secondResponse = flashcardGeneratorController.generateFlashcards();

        // Then
        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals("started", firstResponse.getBody().get("status"));

        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertEquals("already_running", secondResponse.getBody().get("status"));
        assertTrue(secondResponse.getBody().get("message").toString().contains("deja în desfășurare"));
        assertNotNull(secondResponse.getBody().get("progress"));
    }

    @Test
    void shouldReturnGenerationStatusWithProgress() {
        // Given
        when(flashcardBatchGenerator.getTotalFiles()).thenReturn(100);
        when(flashcardBatchGenerator.getProcessedFiles()).thenReturn(50);

        // When
        ResponseEntity<Map<String, Object>> response = flashcardGeneratorController.getGenerationStatus();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals(false, responseBody.get("isGenerating"));
        assertEquals("idle", responseBody.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> progress = (Map<String, Object>) responseBody.get("progress");
        assertNotNull(progress);
        assertEquals(100, progress.get("totalFiles"));
        assertEquals(50, progress.get("processedFiles"));
        assertEquals(50.0, progress.get("percentage"));
    }

    @Test
    void shouldCalculateProgressPercentageCorrectly() {
        // Given
        when(flashcardBatchGenerator.getTotalFiles()).thenReturn(75);
        when(flashcardBatchGenerator.getProcessedFiles()).thenReturn(25);

        // When
        ResponseEntity<Map<String, Object>> response = flashcardGeneratorController.getGenerationStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> progress = (Map<String, Object>) response.getBody().get("progress");
        assertEquals(33.33, progress.get("percentage"));
    }

    @Test
    void shouldHandleZeroTotalFilesInProgress() {
        // Given
        when(flashcardBatchGenerator.getTotalFiles()).thenReturn(0);
        when(flashcardBatchGenerator.getProcessedFiles()).thenReturn(0);

        // When
        ResponseEntity<Map<String, Object>> response = flashcardGeneratorController.getGenerationStatus();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> progress = (Map<String, Object>) response.getBody().get("progress");
        assertEquals(0.0, progress.get("percentage"));
    }


    @Test
    void shouldReturnCorrectStatusCodeForGenerationStatus() {
        // Given
        when(flashcardBatchGenerator.getTotalFiles()).thenReturn(0);
        when(flashcardBatchGenerator.getProcessedFiles()).thenReturn(0);

        // When
        ResponseEntity<Map<String, Object>> response = flashcardGeneratorController.getGenerationStatus();

        // Then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldIncludeProgressInformationInAlreadyRunningResponse() throws InterruptedException {
        // Given
        when(flashcardBatchGenerator.getTotalFiles()).thenReturn(20);
        when(flashcardBatchGenerator.getProcessedFiles()).thenReturn(5);

        doAnswer(invocation -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return null;
        }).when(flashcardBatchGenerator).generateAllFlashcards();

        // When
        flashcardGeneratorController.generateFlashcards();
        TimeUnit.MILLISECONDS.sleep(10); // Let the first operation start

        ResponseEntity<Map<String, Object>> response = flashcardGeneratorController.generateFlashcards();

        // Then
        assertNotNull(response.getBody().get("progress"));
        @SuppressWarnings("unchecked")
        Map<String, Object> progress = (Map<String, Object>) response.getBody().get("progress");
        assertEquals(20, progress.get("totalFiles"));
        assertEquals(5, progress.get("processedFiles"));
        assertEquals(25.0, progress.get("percentage"));
    }

    @Test
    void shouldHaveCorrectEndpointMapping() {
        // This test ensures the controller has the expected endpoints
        // The actual annotations are tested through integration tests
        assertNotNull(flashcardGeneratorController);
        assertTrue(flashcardGeneratorController.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class));
    }
} 