package com.backend.controller;

import com.backend.dto.TestAnswerDTO;
import com.backend.service.TestAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestAnswerControllerTest {

    @Mock
    private TestAnswerService testAnswerService;

    @InjectMocks
    private TestAnswerController testAnswerController;

    private TestAnswerDTO testAnswerDTO;
    private TestAnswerDTO secondAnswerDTO;

    @BeforeEach
    void setUp() {
        testAnswerDTO = new TestAnswerDTO();
        testAnswerDTO.setId(1L);
        testAnswerDTO.setOptionText("Paris");
        testAnswerDTO.setCorrect(true);
        testAnswerDTO.setQuestionId(1L);

        secondAnswerDTO = new TestAnswerDTO();
        secondAnswerDTO.setId(2L);
        secondAnswerDTO.setOptionText("London");
        secondAnswerDTO.setCorrect(false);
        secondAnswerDTO.setQuestionId(1L);
    }

    @Test
    void getAllAnswers_ShouldReturnAllAnswers() {
        // Given
        List<TestAnswerDTO> answers = Arrays.asList(testAnswerDTO, secondAnswerDTO);
        when(testAnswerService.getAllAnswers()).thenReturn(answers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getAllAnswers();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testAnswerService).getAllAnswers();
    }

    @Test
    void getAnswerById_WithValidId_ShouldReturnAnswer() {
        // Given
        Long answerId = 1L;
        when(testAnswerService.getAnswerById(answerId)).thenReturn(testAnswerDTO);

        // When
        ResponseEntity<TestAnswerDTO> response = testAnswerController.getAnswerById(answerId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testAnswerDTO.getId(), response.getBody().getId());
        assertEquals(testAnswerDTO.getOptionText(), response.getBody().getOptionText());
        assertEquals(testAnswerDTO.isCorrect(), response.getBody().isCorrect());
        verify(testAnswerService).getAnswerById(answerId);
    }

    @Test
    void createAnswer_WithValidAnswer_ShouldCreateAnswer() {
        // Given
        when(testAnswerService.createAnswer(any(TestAnswerDTO.class))).thenReturn(testAnswerDTO);

        // When
        ResponseEntity<TestAnswerDTO> response = testAnswerController.createAnswer(testAnswerDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testAnswerDTO.getId(), response.getBody().getId());
        assertEquals(testAnswerDTO.getOptionText(), response.getBody().getOptionText());
        verify(testAnswerService).createAnswer(testAnswerDTO);
    }

    @Test
    void saveAnswer_WithValidAnswer_ShouldSaveAnswer() {
        // Given
        when(testAnswerService.saveAnswer(any(TestAnswerDTO.class))).thenReturn(testAnswerDTO);

        // When
        ResponseEntity<TestAnswerDTO> response = testAnswerController.saveAnswer(testAnswerDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testAnswerDTO.getId(), response.getBody().getId());
        verify(testAnswerService).saveAnswer(testAnswerDTO);
    }

    @Test
    void updateAnswer_WithValidData_ShouldUpdateAnswer() {
        // Given
        Long answerId = 1L;
        when(testAnswerService.updateAnswer(eq(answerId), any(TestAnswerDTO.class))).thenReturn(testAnswerDTO);

        // When
        ResponseEntity<TestAnswerDTO> response = testAnswerController.updateAnswer(answerId, testAnswerDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testAnswerDTO.getId(), response.getBody().getId());
        verify(testAnswerService).updateAnswer(answerId, testAnswerDTO);
    }

    @Test
    void deleteAnswer_WithValidId_ShouldDeleteAnswer() {
        // Given
        Long answerId = 1L;
        doNothing().when(testAnswerService).deleteAnswerById(answerId);

        // When
        ResponseEntity<Void> response = testAnswerController.deleteAnswer(answerId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(testAnswerService).deleteAnswerById(answerId);
    }

    @Test
    void getAnswersByQuestionId_WithValidQuestionId_ShouldReturnAnswers() {
        // Given
        Long questionId = 1L;
        List<TestAnswerDTO> answers = Arrays.asList(testAnswerDTO, secondAnswerDTO);
        when(testAnswerService.getAnswersByQuestionId(questionId)).thenReturn(answers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getAnswersByQuestionId(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testAnswerService).getAnswersByQuestionId(questionId);
    }

    @Test
    void getAnswersByTestId_WithValidTestId_ShouldReturnAnswers() {
        // Given
        Long testId = 1L;
        List<TestAnswerDTO> answers = Arrays.asList(testAnswerDTO, secondAnswerDTO);
        when(testAnswerService.getAnswersByTestId(testId)).thenReturn(answers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getAnswersByTestId(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testAnswerService).getAnswersByTestId(testId);
    }

    @Test
    void getCorrectAnswersByQuestionId_WithValidQuestionId_ShouldReturnCorrectAnswers() {
        // Given
        Long questionId = 1L;
        List<TestAnswerDTO> correctAnswers = Arrays.asList(testAnswerDTO); // Only correct answer
        when(testAnswerService.getCorrectAnswersByQuestionId(questionId)).thenReturn(correctAnswers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getCorrectAnswersByQuestionId(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().iterator().next().isCorrect());
        verify(testAnswerService).getCorrectAnswersByQuestionId(questionId);
    }

    @Test
    void getIncorrectAnswersByQuestionId_WithValidQuestionId_ShouldReturnIncorrectAnswers() {
        // Given
        Long questionId = 1L;
        List<TestAnswerDTO> incorrectAnswers = Arrays.asList(secondAnswerDTO); // Only incorrect answer
        when(testAnswerService.getIncorrectAnswersByQuestionId(questionId)).thenReturn(incorrectAnswers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getIncorrectAnswersByQuestionId(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().iterator().next().isCorrect());
        verify(testAnswerService).getIncorrectAnswersByQuestionId(questionId);
    }

    @Test
    void getCorrectAnswersByTestId_WithValidTestId_ShouldReturnCorrectAnswers() {
        // Given
        Long testId = 1L;
        List<TestAnswerDTO> correctAnswers = Arrays.asList(testAnswerDTO);
        when(testAnswerService.getCorrectAnswersByTestId(testId)).thenReturn(correctAnswers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getCorrectAnswersByTestId(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().iterator().next().isCorrect());
        verify(testAnswerService).getCorrectAnswersByTestId(testId);
    }

    @Test
    void searchAnswersByOptionText_WithValidKeyword_ShouldReturnMatchingAnswers() {
        // Given
        String keyword = "Paris";
        List<TestAnswerDTO> matchingAnswers = Arrays.asList(testAnswerDTO);
        when(testAnswerService.findByOptionTextContaining(keyword)).thenReturn(matchingAnswers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.searchAnswersByOptionText(keyword);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().iterator().next().getOptionText().contains(keyword));
        verify(testAnswerService).findByOptionTextContaining(keyword);
    }

    @Test
    void countAnswersByQuestionId_WithValidQuestionId_ShouldReturnCount() {
        // Given
        Long questionId = 1L;
        Long expectedCount = 4L;
        when(testAnswerService.countAnswersByQuestionId(questionId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testAnswerController.countAnswersByQuestionId(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testAnswerService).countAnswersByQuestionId(questionId);
    }

    @Test
    void countCorrectAnswersByQuestionId_WithValidQuestionId_ShouldReturnCorrectCount() {
        // Given
        Long questionId = 1L;
        Long expectedCount = 1L;
        when(testAnswerService.countCorrectAnswersByQuestionId(questionId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testAnswerController.countCorrectAnswersByQuestionId(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testAnswerService).countCorrectAnswersByQuestionId(questionId);
    }

    @Test
    void countAnswersByTestId_WithValidTestId_ShouldReturnCount() {
        // Given
        Long testId = 1L;
        Long expectedCount = 20L;
        when(testAnswerService.countAnswersByTestId(testId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testAnswerController.countAnswersByTestId(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testAnswerService).countAnswersByTestId(testId);
    }

    @Test
    void countCorrectAnswersByTestId_WithValidTestId_ShouldReturnCorrectCount() {
        // Given
        Long testId = 1L;
        Long expectedCount = 5L;
        when(testAnswerService.countCorrectAnswersByTestId(testId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testAnswerController.countCorrectAnswersByTestId(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testAnswerService).countCorrectAnswersByTestId(testId);
    }

    @Test
    void createAnswers_WithValidAnswers_ShouldCreateMultipleAnswers() {
        // Given
        Collection<TestAnswerDTO> answers = Arrays.asList(testAnswerDTO, secondAnswerDTO);
        when(testAnswerService.createAnswers(anyCollection())).thenReturn(answers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.createAnswers(answers);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testAnswerService).createAnswers(answers);
    }

    @Test
    void saveAnswers_WithValidAnswers_ShouldSaveMultipleAnswers() {
        // Given
        Collection<TestAnswerDTO> answers = Arrays.asList(testAnswerDTO, secondAnswerDTO);
        when(testAnswerService.saveAnswers(anyCollection())).thenReturn(answers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.saveAnswers(answers);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testAnswerService).saveAnswers(answers);
    }

    @Test
    void getAllAnswers_WhenNoAnswers_ShouldReturnEmptyCollection() {
        // Given
        when(testAnswerService.getAllAnswers()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getAllAnswers();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testAnswerService).getAllAnswers();
    }

    @Test
    void getAnswersByQuestionId_WhenNoAnswers_ShouldReturnEmptyCollection() {
        // Given
        Long questionId = 1L;
        when(testAnswerService.getAnswersByQuestionId(questionId)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.getAnswersByQuestionId(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testAnswerService).getAnswersByQuestionId(questionId);
    }

    @Test
    void searchAnswersByOptionText_WhenNoMatches_ShouldReturnEmptyCollection() {
        // Given
        String keyword = "nonexistent";
        when(testAnswerService.findByOptionTextContaining(keyword)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.searchAnswersByOptionText(keyword);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testAnswerService).findByOptionTextContaining(keyword);
    }

    @Test
    void createAnswers_WithEmptyCollection_ShouldReturnEmptyCollection() {
        // Given
        Collection<TestAnswerDTO> emptyAnswers = Collections.emptyList();
        when(testAnswerService.createAnswers(emptyAnswers)).thenReturn(emptyAnswers);

        // When
        ResponseEntity<Collection<TestAnswerDTO>> response = testAnswerController.createAnswers(emptyAnswers);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testAnswerService).createAnswers(emptyAnswers);
    }
}
