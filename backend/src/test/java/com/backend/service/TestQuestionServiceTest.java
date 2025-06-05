package com.backend.service;

import com.backend.dto.TestQuestionDTO;
import com.backend.mapper.TestQuestionMapper;
import com.backend.model.TestEntity;
import com.backend.model.TestQuestion;
import com.backend.repository.TestQuestionRepository;
import com.backend.repository.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestQuestionServiceTest {

    @Mock
    private TestQuestionRepository testQuestionRepository;

    @Mock
    private TestQuestionMapper testQuestionMapper;

    @Mock
    private TestRepository testRepository;

    @InjectMocks
    private TestQuestionService testQuestionService;

    private TestQuestion testQuestion;
    private TestQuestionDTO testQuestionDTO;
    private TestEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setTitle("Sample Test");

        testQuestion = new TestQuestion();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("What is 2+2?");
        testQuestion.setPointValue(5.0f);
        testQuestion.setTest(testEntity);

        testQuestionDTO = new TestQuestionDTO();
        testQuestionDTO.setId(1L);
        testQuestionDTO.setQuestionText("What is 2+2?");
        testQuestionDTO.setPointValue(5.0f);
        testQuestionDTO.setTestId(1L);
    }

    @Test
    void constructor_WithNullRepository_ShouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                new TestQuestionService(null, testQuestionMapper, testRepository));
    }

    @Test
    void getAllQuestions_ShouldReturnAllQuestions() {
        // Given
        List<TestQuestion> questions = Arrays.asList(testQuestion, testQuestion);
        when(testQuestionRepository.findAll()).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        Collection<TestQuestionDTO> result = testQuestionService.getAllQuestions();

        // Then
        assertEquals(2, result.size());
        verify(testQuestionRepository).findAll();
        verify(testQuestionMapper, times(2)).toDTO(testQuestion);
    }

    @Test
    void getQuestionById_WithValidId_ShouldReturnQuestion() {
        // Given
        Long questionId = 1L;
        when(testQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        TestQuestionDTO result = testQuestionService.getQuestionById(questionId);

        // Then
        assertEquals(testQuestionDTO, result);
        verify(testQuestionRepository).findById(questionId);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void getQuestionById_WithInvalidId_ShouldThrowException() {
        // Given
        Long questionId = 0L;

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testQuestionService.getQuestionById(questionId));
    }

    @Test
    void getQuestionById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long questionId = 999L;
        when(testQuestionRepository.findById(questionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testQuestionService.getQuestionById(questionId));
    }

    @Test
    void getQuestionsByTestId_WithValidId_ShouldReturnQuestions() {
        // Given
        Long testId = 1L;
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByTestId(testId)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        Collection<TestQuestionDTO> result = testQuestionService.getQuestionsByTestId(testId);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByTestId(testId);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void getQuestionsByTestId_WithInvalidId_ShouldThrowException() {
        // Given
        Long testId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.getQuestionsByTestId(testId));
    }

    @Test
    void countQuestionsByTest_WithValidId_ShouldReturnCount() {
        // Given
        Long testId = 1L;
        Long expectedCount = 5L;
        when(testQuestionRepository.countQuestionsByTestId(testId)).thenReturn(expectedCount);

        // When
        Long result = testQuestionService.countQuestionsByTest(testId);

        // Then
        assertEquals(expectedCount, result);
        verify(testQuestionRepository).countQuestionsByTestId(testId);
    }

    @Test
    void getTotalPointsByTest_WithValidId_ShouldReturnTotalPoints() {
        // Given
        Long testId = 1L;
        Float expectedTotal = 20.0f;
        when(testQuestionRepository.getTotalPointsByTestId(testId)).thenReturn(expectedTotal);

        // When
        Float result = testQuestionService.getTotalPointsByTest(testId);

        // Then
        assertEquals(expectedTotal, result);
        verify(testQuestionRepository).getTotalPointsByTestId(testId);
    }

    @Test
    void getQuestionsByCourse_WithValidId_ShouldReturnQuestions() {
        // Given
        Long courseId = 1L;
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByCourseId(courseId)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.getQuestionsByCourse(courseId);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByCourseId(courseId);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void getQuestionsByProfessor_WithValidId_ShouldReturnQuestions() {
        // Given
        Integer professorId = 1;
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByProfessorId(professorId)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.getQuestionsByProfessor(professorId);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByProfessorId(professorId);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void searchQuestionsByText_WithValidKeyword_ShouldReturnQuestions() {
        // Given
        String keyword = "What";
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByQuestionTextContaining(keyword)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.searchQuestionsByText(keyword);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByQuestionTextContaining(keyword);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void searchQuestionsByText_WithNullKeyword_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.searchQuestionsByText(null));
    }

    @Test
    void getQuestionsByPoints_WithValidPoints_ShouldReturnQuestions() {
        // Given
        Float points = 5.0f;
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByPointValue(points)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.getQuestionsByPoints(points);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByPointValue(points);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void getQuestionsByMinPoints_WithValidMinPoints_ShouldReturnQuestions() {
        // Given
        Float minPoints = 3.0f;
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByMinimumPoints(minPoints)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.getQuestionsByMinPoints(minPoints);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByMinimumPoints(minPoints);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void countQuestionsByCourse_WithValidId_ShouldReturnCount() {
        // Given
        Long courseId = 1L;
        Long expectedCount = 10L;
        when(testQuestionRepository.countQuestionsByCourseId(courseId)).thenReturn(expectedCount);

        // When
        Long result = testQuestionService.countQuestionsByCourse(courseId);

        // Then
        assertEquals(expectedCount, result);
        verify(testQuestionRepository).countQuestionsByCourseId(courseId);
    }

    @Test
    void countQuestionsByProfessor_WithValidId_ShouldReturnCount() {
        // Given
        Integer professorId = 1;
        Long expectedCount = 15L;
        when(testQuestionRepository.countQuestionsByProfessorId(professorId)).thenReturn(expectedCount);

        // When
        Long result = testQuestionService.countQuestionsByProfessor(professorId);

        // Then
        assertEquals(expectedCount, result);
        verify(testQuestionRepository).countQuestionsByProfessorId(professorId);
    }

    @Test
    void countQuestionsByPoints_WithValidPoints_ShouldReturnCount() {
        // Given
        Float points = 5.0f;
        Long expectedCount = 3L;
        when(testQuestionRepository.countQuestionsByPointValue(points)).thenReturn(expectedCount);

        // When
        Long result = testQuestionService.countQuestionsByPoints(points);

        // Then
        assertEquals(expectedCount, result);
        verify(testQuestionRepository).countQuestionsByPointValue(points);
    }

    @Test
    void countQuestionsByMinPoints_WithValidMinPoints_ShouldReturnCount() {
        // Given
        Float minPoints = 3.0f;
        Long expectedCount = 8L;
        when(testQuestionRepository.countQuestionsByMinimumPoints(minPoints)).thenReturn(expectedCount);

        // When
        Long result = testQuestionService.countQuestionsByMinPoints(minPoints);

        // Then
        assertEquals(expectedCount, result);
        verify(testQuestionRepository).countQuestionsByMinimumPoints(minPoints);
    }

    @Test
    void getAveragePointsPerQuestion_WithValidTestId_ShouldReturnAverage() {
        // Given
        Long testId = 1L;
        Float expectedAverage = 4.0f;
        when(testQuestionRepository.getAveragePointsPerQuestion(testId)).thenReturn(expectedAverage);

        // When
        Float result = testQuestionService.getAveragePointsPerQuestion(testId);

        // Then
        assertEquals(expectedAverage, result);
        verify(testQuestionRepository).getAveragePointsPerQuestion(testId);
    }

    @Test
    void getQuestionsByTestIdSortedByPoints_WithValidTestId_ShouldReturnSortedQuestions() {
        // Given
        Long testId = 1L;
        List<TestQuestion> sortedQuestions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByTestIdOrderByPointsDesc(testId)).thenReturn(sortedQuestions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.getQuestionsByTestIdSortedByPoints(testId);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByTestIdOrderByPointsDesc(testId);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void getQuestionsByTestAndMinPoints_WithValidParameters_ShouldReturnQuestions() {
        // Given
        Long testId = 1L;
        Float minPoints = 4.0f;
        List<TestQuestion> questions = Arrays.asList(testQuestion);
        when(testQuestionRepository.findByTestIdAndMinPoints(testId, minPoints)).thenReturn(questions);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.getQuestionsByTestAndMinPoints(testId, minPoints);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).findByTestIdAndMinPoints(testId, minPoints);
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void saveQuestion_WithValidDTO_ShouldReturnSavedDTO() {
        // Given
        when(testRepository.findById(testQuestionDTO.getTestId())).thenReturn(Optional.of(testEntity));
        when(testQuestionRepository.save(any(TestQuestion.class))).thenReturn(testQuestion);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        TestQuestionDTO result = testQuestionService.saveQuestion(testQuestionDTO);

        // Then
        assertEquals(testQuestionDTO, result);
        verify(testRepository).findById(testQuestionDTO.getTestId());
        verify(testQuestionRepository).save(any(TestQuestion.class));
        verify(testQuestionMapper).toDTO(testQuestion);
    }

    @Test
    void saveQuestion_WithNullDTO_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.saveQuestion(null));
    }

    @Test
    void saveQuestion_WithNullTestId_ShouldThrowException() {
        // Given
        testQuestionDTO.setTestId(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.saveQuestion(testQuestionDTO));
    }

    @Test
    void saveQuestion_WithNonExistentTestId_ShouldThrowException() {
        // Given
        when(testRepository.findById(testQuestionDTO.getTestId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testQuestionService.saveQuestion(testQuestionDTO));
    }

    @Test
    void createQuestion_WithValidDTO_ShouldReturnCreatedDTO() {
        // Given
        testQuestionDTO.setId(null); // New question should not have ID
        when(testRepository.findById(testQuestionDTO.getTestId())).thenReturn(Optional.of(testEntity));
        when(testQuestionRepository.save(any(TestQuestion.class))).thenReturn(testQuestion);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        TestQuestionDTO result = testQuestionService.createQuestion(testQuestionDTO);

        // Then
        assertEquals(testQuestionDTO, result);
        verify(testRepository).findById(testQuestionDTO.getTestId());
        verify(testQuestionRepository).save(any(TestQuestion.class));
    }

    @Test
    void createQuestion_WithExistingId_ShouldThrowException() {
        // Given - testQuestionDTO already has an ID

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.createQuestion(testQuestionDTO));
    }

    @Test
    void updateQuestion_WithValidData_ShouldReturnUpdatedDTO() {
        // Given
        Long questionId = 1L;
        when(testQuestionRepository.findById(questionId)).thenReturn(Optional.of(testQuestion));
        when(testQuestionRepository.save(testQuestion)).thenReturn(testQuestion);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        TestQuestionDTO result = testQuestionService.updateQuestion(questionId, testQuestionDTO);

        // Then
        assertEquals(testQuestionDTO, result);
        verify(testQuestionRepository).findById(questionId);
        verify(testQuestionRepository).save(testQuestion);
    }

    @Test
    void updateQuestion_WithNullDTO_ShouldThrowException() {
        // Given
        Long questionId = 1L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.updateQuestion(questionId, null));
    }

    @Test
    void updateQuestion_WithInvalidId_ShouldThrowException() {
        // Given
        Long questionId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.updateQuestion(questionId, testQuestionDTO));
    }

    @Test
    void updateQuestion_WithNonExistentId_ShouldThrowException() {
        // Given
        Long questionId = 999L;
        when(testQuestionRepository.findById(questionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testQuestionService.updateQuestion(questionId, testQuestionDTO));
    }

    @Test
    void deleteQuestionById_WithValidId_ShouldDeleteQuestion() {
        // Given
        Long questionId = 1L;
        when(testQuestionRepository.existsById(questionId)).thenReturn(true);
        doNothing().when(testQuestionRepository).deleteById(questionId);

        // When
        assertDoesNotThrow(() -> testQuestionService.deleteQuestionById(questionId));

        // Then
        verify(testQuestionRepository).existsById(questionId);
        verify(testQuestionRepository).deleteById(questionId);
    }

    @Test
    void deleteQuestionById_WithInvalidId_ShouldThrowException() {
        // Given
        Long questionId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testQuestionService.deleteQuestionById(questionId));
    }

    @Test
    void deleteQuestionById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long questionId = 999L;
        when(testQuestionRepository.existsById(questionId)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testQuestionService.deleteQuestionById(questionId));
    }

    @Test
    void createMultipleQuestions_WithValidList_ShouldReturnCreatedQuestions() {
        // Given
        TestQuestionDTO newQuestion = new TestQuestionDTO();
        newQuestion.setQuestionText("New Question");
        newQuestion.setTestId(1L);
        List<TestQuestionDTO> questions = Arrays.asList(newQuestion);
        
        when(testRepository.findById(anyLong())).thenReturn(Optional.of(testEntity));
        when(testQuestionRepository.save(any(TestQuestion.class))).thenReturn(testQuestion);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.createMultipleQuestions(questions);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).save(any(TestQuestion.class));
    }

    @Test
    void saveMultipleQuestions_WithValidList_ShouldReturnSavedQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO);
        when(testRepository.findById(anyLong())).thenReturn(Optional.of(testEntity));
        when(testQuestionRepository.save(any(TestQuestion.class))).thenReturn(testQuestion);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.saveMultipleQuestions(questions);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).save(any(TestQuestion.class));
    }

    @Test
    void updateMultipleQuestions_WithValidList_ShouldReturnUpdatedQuestions() {
        // Given
        List<TestQuestionDTO> questions = Arrays.asList(testQuestionDTO);
        when(testQuestionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(testQuestionRepository.save(any(TestQuestion.class))).thenReturn(testQuestion);
        when(testQuestionMapper.toDTO(testQuestion)).thenReturn(testQuestionDTO);

        // When
        List<TestQuestionDTO> result = testQuestionService.updateMultipleQuestions(questions);

        // Then
        assertEquals(1, result.size());
        verify(testQuestionRepository).save(any(TestQuestion.class));
    }
} 