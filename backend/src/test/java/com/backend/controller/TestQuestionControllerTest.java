package com.backend.controller;

import com.backend.dto.TestQuestionDTO;
import com.backend.service.TestQuestionService;
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
class TestQuestionControllerTest {

    @Mock
    private TestQuestionService testQuestionService;

    @InjectMocks
    private TestQuestionController testQuestionController;

    private TestQuestionDTO testQuestionDTO;
    private TestQuestionDTO secondQuestionDTO;

    @BeforeEach
    void setUp() {
        testQuestionDTO = new TestQuestionDTO();
        testQuestionDTO.setId(1L);
        testQuestionDTO.setQuestionText("What is 2+2?");
        testQuestionDTO.setPointValue(5.0f);
        testQuestionDTO.setTestId(1L);

        secondQuestionDTO = new TestQuestionDTO();
        secondQuestionDTO.setId(2L);
        secondQuestionDTO.setQuestionText("What is the capital of France?");
        secondQuestionDTO.setPointValue(3.0f);
        secondQuestionDTO.setTestId(1L);
    }

    @Test
    void getAllTestQuestions_ShouldReturnAllQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.getAllQuestions()).thenReturn(questions);

        // When
        ResponseEntity<Collection<TestQuestionDTO>> response = testQuestionController.getAllTestQuestions();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).getAllQuestions();
    }

    @Test
    void getTestQuestionById_WithValidId_ShouldReturnQuestion() {
        // Given
        Long questionId = 1L;
        when(testQuestionService.getQuestionById(questionId)).thenReturn(testQuestionDTO);

        // When
        ResponseEntity<TestQuestionDTO> response = testQuestionController.getTestQuestionById(questionId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testQuestionDTO.getId(), response.getBody().getId());
        assertEquals(testQuestionDTO.getQuestionText(), response.getBody().getQuestionText());
        verify(testQuestionService).getQuestionById(questionId);
    }

    @Test
    void createTestQuestion_WithValidQuestion_ShouldCreateQuestion() {
        // Given
        when(testQuestionService.createQuestion(any(TestQuestionDTO.class))).thenReturn(testQuestionDTO);

        // When
        ResponseEntity<TestQuestionDTO> response = testQuestionController.createTestQuestion(testQuestionDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testQuestionDTO.getId(), response.getBody().getId());
        assertEquals(testQuestionDTO.getQuestionText(), response.getBody().getQuestionText());
        verify(testQuestionService).createQuestion(testQuestionDTO);
    }

    @Test
    void createMultipleTestQuestions_WithValidQuestions_ShouldCreateQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.createMultipleQuestions(anyList())).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.createMultipleTestQuestions(questions);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).createMultipleQuestions(questions);
    }

    @Test
    void saveQuestion_WithValidQuestion_ShouldSaveQuestion() {
        // Given
        when(testQuestionService.saveQuestion(any(TestQuestionDTO.class))).thenReturn(testQuestionDTO);

        // When
        ResponseEntity<TestQuestionDTO> response = testQuestionController.saveQuestion(testQuestionDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testQuestionDTO.getId(), response.getBody().getId());
        verify(testQuestionService).saveQuestion(testQuestionDTO);
    }

    @Test
    void createMultipleQuestions_WithValidQuestions_ShouldCreateQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.createMultipleQuestions(anyList())).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.createMultipleQuestions(questions);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).createMultipleQuestions(questions);
    }

    @Test
    void saveMultipleQuestions_WithValidQuestions_ShouldSaveQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.saveMultipleQuestions(anyList())).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.saveMultipleQuestions(questions);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).saveMultipleQuestions(questions);
    }

    @Test
    void updateMultipleQuestions_WithValidQuestions_ShouldUpdateQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.updateMultipleQuestions(anyList())).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.updateMultipleQuestions(questions);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).updateMultipleQuestions(questions);
    }

    @Test
    void updateTestQuestion_WithValidData_ShouldUpdateQuestion() {
        // Given
        Long questionId = 1L;
        when(testQuestionService.updateQuestion(eq(questionId), any(TestQuestionDTO.class))).thenReturn(testQuestionDTO);

        // When
        ResponseEntity<TestQuestionDTO> response = testQuestionController.updateTestQuestion(questionId, testQuestionDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testQuestionDTO.getId(), response.getBody().getId());
        verify(testQuestionService).updateQuestion(questionId, testQuestionDTO);
    }

    @Test
    void deleteTestQuestion_WithValidId_ShouldDeleteQuestion() {
        // Given
        Long questionId = 1L;
        doNothing().when(testQuestionService).deleteQuestionById(questionId);

        // When
        ResponseEntity<Void> response = testQuestionController.deleteTestQuestion(questionId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(testQuestionService).deleteQuestionById(questionId);
    }

    @Test
    void getTestQuestionsByTestId_WithValidTestId_ShouldReturnQuestions() {
        // Given
        Long testId = 1L;
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.getQuestionsByTestId(testId)).thenReturn(questions);

        // When
        ResponseEntity<Collection<TestQuestionDTO>> response = testQuestionController.getTestQuestionsByTestId(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).getQuestionsByTestId(testId);
    }

    @Test
    void countQuestionsByTestId_WithValidTestId_ShouldReturnCount() {
        // Given
        Long testId = 1L;
        Long expectedCount = 5L;
        when(testQuestionService.countQuestionsByTest(testId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testQuestionController.countQuestionsByTestId(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testQuestionService).countQuestionsByTest(testId);
    }

    @Test
    void getTotalPointsByTest_WithValidTestId_ShouldReturnTotalPoints() {
        // Given
        Long testId = 1L;
        Float expectedTotal = 20.0f;
        when(testQuestionService.getTotalPointsByTest(testId)).thenReturn(expectedTotal);

        // When
        ResponseEntity<Float> response = testQuestionController.getTotalPointsByTest(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTotal, response.getBody());
        verify(testQuestionService).getTotalPointsByTest(testId);
    }

    @Test
    void getQuestionsByCourse_WithValidCourseId_ShouldReturnQuestions() {
        // Given
        Long courseId = 1L;
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO);
        when(testQuestionService.getQuestionsByCourse(courseId)).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.getQuestionsByCourse(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testQuestionService).getQuestionsByCourse(courseId);
    }

    @Test
    void getQuestionsByProfessor_WithValidProfessorId_ShouldReturnQuestions() {
        // Given
        Integer professorId = 1;
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.getQuestionsByProfessor(professorId)).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.getQuestionsByProfessor(professorId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).getQuestionsByProfessor(professorId);
    }

    @Test
    void searchQuestionsByText_WithValidKeyword_ShouldReturnQuestions() {
        // Given
        String keyword = "capital";
        List<TestQuestionDTO> questions = Arrays.asList(secondQuestionDTO);
        when(testQuestionService.searchQuestionsByText(keyword)).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.searchQuestionsByText(keyword);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testQuestionService).searchQuestionsByText(keyword);
    }

    @Test
    void getQuestionsByPoints_WithValidPoints_ShouldReturnQuestions() {
        // Given
        Float points = 5.0f;
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO);
        when(testQuestionService.getQuestionsByPoints(points)).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.getQuestionsByPoints(points);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testQuestionService).getQuestionsByPoints(points);
    }

    @Test
    void getQuestionsByMinPoints_WithValidMinPoints_ShouldReturnQuestions() {
        // Given
        Float minPoints = 3.0f;
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO, secondQuestionDTO);
        when(testQuestionService.getQuestionsByMinPoints(minPoints)).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.getQuestionsByMinPoints(minPoints);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).getQuestionsByMinPoints(minPoints);
    }

    @Test
    void countQuestionsByCourse_WithValidCourseId_ShouldReturnCount() {
        // Given
        Long courseId = 1L;
        Long expectedCount = 10L;
        when(testQuestionService.countQuestionsByCourse(courseId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testQuestionController.countQuestionsByCourse(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testQuestionService).countQuestionsByCourse(courseId);
    }

    @Test
    void countQuestionsByProfessor_WithValidProfessorId_ShouldReturnCount() {
        // Given
        Integer professorId = 1;
        Long expectedCount = 15L;
        when(testQuestionService.countQuestionsByProfessor(professorId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testQuestionController.countQuestionsByProfessor(professorId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testQuestionService).countQuestionsByProfessor(professorId);
    }

    @Test
    void countQuestionsByPoints_WithValidPoints_ShouldReturnCount() {
        // Given
        Float points = 5.0f;
        Long expectedCount = 3L;
        when(testQuestionService.countQuestionsByPoints(points)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testQuestionController.countQuestionsByPoints(points);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testQuestionService).countQuestionsByPoints(points);
    }

    @Test
    void countQuestionsByMinPoints_WithValidMinPoints_ShouldReturnCount() {
        // Given
        Float minPoints = 3.0f;
        Long expectedCount = 8L;
        when(testQuestionService.countQuestionsByMinPoints(minPoints)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testQuestionController.countQuestionsByMinPoints(minPoints);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testQuestionService).countQuestionsByMinPoints(minPoints);
    }

    @Test
    void getAveragePointsPerQuestion_WithValidTestId_ShouldReturnAverage() {
        // Given
        Long testId = 1L;
        Float expectedAverage = 4.0f;
        when(testQuestionService.getAveragePointsPerQuestion(testId)).thenReturn(expectedAverage);

        // When
        ResponseEntity<Float> response = testQuestionController.getAveragePointsPerQuestion(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAverage, response.getBody());
        verify(testQuestionService).getAveragePointsPerQuestion(testId);
    }

    @Test
    void getQuestionsByTestIdSortedByPoints_WithValidTestId_ShouldReturnSortedQuestions() {
        // Given
        Long testId = 1L;
        List<TestQuestionDTO> sortedQuestions = Arrays.asList(secondQuestionDTO, testQuestionDTO); // sorted by points ascending
        when(testQuestionService.getQuestionsByTestIdSortedByPoints(testId)).thenReturn(sortedQuestions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.getQuestionsByTestIdSortedByPoints(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testQuestionService).getQuestionsByTestIdSortedByPoints(testId);
    }

    @Test
    void getQuestionsByTestAndMinPoints_WithValidParameters_ShouldReturnQuestions() {
        // Given
        Long testId = 1L;
        Float minPoints = 4.0f;
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO);
        when(testQuestionService.getQuestionsByTestAndMinPoints(testId, minPoints)).thenReturn(questions);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.getQuestionsByTestAndMinPoints(testId, minPoints);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testQuestionService).getQuestionsByTestAndMinPoints(testId, minPoints);
    }

    @Test
    void getAllTestQuestions_WhenNoQuestions_ShouldReturnEmptyCollection() {
        // Given
        when(testQuestionService.getAllQuestions()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Collection<TestQuestionDTO>> response = testQuestionController.getAllTestQuestions();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testQuestionService).getAllQuestions();
    }

    @Test
    void createMultipleQuestions_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<TestQuestionDTO> emptyList = Collections.emptyList();
        when(testQuestionService.createMultipleQuestions(emptyList)).thenReturn(emptyList);

        // When
        ResponseEntity<List<TestQuestionDTO>> response = testQuestionController.createMultipleQuestions(emptyList);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testQuestionService).createMultipleQuestions(emptyList);
    }
}
