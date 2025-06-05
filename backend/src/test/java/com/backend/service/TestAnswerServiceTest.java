package com.backend.service;

import com.backend.dto.TestAnswerDTO;
import com.backend.mapper.TestAnswerMapper;
import com.backend.model.TestAnswer;
import com.backend.model.TestQuestion;
import com.backend.repository.TestAnswerRepository;
import com.backend.repository.TestQuestionRepository;
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
class TestAnswerServiceTest {

    @Mock
    private TestAnswerRepository testAnswerRepository;

    @Mock
    private TestAnswerMapper testAnswerMapper;

    @Mock
    private TestQuestionRepository testQuestionRepository;

    @InjectMocks
    private TestAnswerService testAnswerService;

    private TestAnswer testAnswer;
    private TestAnswerDTO testAnswerDTO;
    private TestQuestion testQuestion;

    @BeforeEach
    void setUp() {
        testQuestion = new TestQuestion();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("What is the capital of France?");

        testAnswer = new TestAnswer();
        testAnswer.setId(1L);
        testAnswer.setOptionText("Paris");
        testAnswer.setCorrect(true);
        testAnswer.setTestQuestion(testQuestion);

        testAnswerDTO = new TestAnswerDTO();
        testAnswerDTO.setId(1L);
        testAnswerDTO.setOptionText("Paris");
        testAnswerDTO.setCorrect(true);
        testAnswerDTO.setQuestionId(1L);
    }

    @Test
    void constructor_WithNullRepository_ShouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                new TestAnswerService(null, testAnswerMapper, testQuestionRepository));
    }

    @Test
    void getAllAnswers_ShouldReturnAllAnswers() {
        // Given
        List<TestAnswer> answers = Arrays.asList(testAnswer, testAnswer);
        when(testAnswerRepository.findAll()).thenReturn(answers);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getAllAnswers();

        // Then
        assertEquals(2, result.size());
        verify(testAnswerRepository).findAll();
        verify(testAnswerMapper, times(2)).toDTO(testAnswer);
    }

    @Test
    void getAnswerById_WithValidId_ShouldReturnAnswer() {
        // Given
        Long answerId = 1L;
        when(testAnswerRepository.findById(answerId)).thenReturn(Optional.of(testAnswer));
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        TestAnswerDTO result = testAnswerService.getAnswerById(answerId);

        // Then
        assertEquals(testAnswerDTO, result);
        verify(testAnswerRepository).findById(answerId);
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void getAnswerById_WithInvalidId_ShouldThrowException() {
        // Given
        Long answerId = 0L;

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testAnswerService.getAnswerById(answerId));
    }

    @Test
    void getAnswerById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long answerId = 999L;
        when(testAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testAnswerService.getAnswerById(answerId));
    }

    @Test
    void getAnswersByQuestionId_WithValidId_ShouldReturnAnswers() {
        // Given
        Long questionId = 1L;
        List<TestAnswer> answers = Arrays.asList(testAnswer);
        when(testAnswerRepository.findByQuestionId(questionId)).thenReturn(answers);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getAnswersByQuestionId(questionId);

        // Then
        assertEquals(1, result.size());
        verify(testAnswerRepository).findByQuestionId(questionId);
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void getAnswersByQuestionId_WithInvalidId_ShouldThrowException() {
        // Given
        Long questionId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.getAnswersByQuestionId(questionId));
    }

    @Test
    void getCorrectAnswersByQuestionId_WithValidId_ShouldReturnCorrectAnswers() {
        // Given
        Long questionId = 1L;
        List<TestAnswer> correctAnswers = Arrays.asList(testAnswer);
        when(testAnswerRepository.findCorrectAnswersByQuestionId(questionId)).thenReturn(correctAnswers);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getCorrectAnswersByQuestionId(questionId);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.iterator().next().isCorrect());
        verify(testAnswerRepository).findCorrectAnswersByQuestionId(questionId);
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void getAnswersByTestId_WithValidId_ShouldReturnAnswers() {
        // Given
        Long testId = 1L;
        List<TestAnswer> answers = Arrays.asList(testAnswer);
        when(testAnswerRepository.findByTestId(testId)).thenReturn(answers);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getAnswersByTestId(testId);

        // Then
        assertEquals(1, result.size());
        verify(testAnswerRepository).findByTestId(testId);
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void getAnswersByTestId_WithInvalidId_ShouldThrowException() {
        // Given
        Long testId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.getAnswersByTestId(testId));
    }

    @Test
    void countCorrectAnswersByQuestionId_WithValidId_ShouldReturnCount() {
        // Given
        Long questionId = 1L;
        Long expectedCount = 1L;
        when(testAnswerRepository.countCorrectAnswersByQuestionId(questionId)).thenReturn(expectedCount);

        // When
        Long result = testAnswerService.countCorrectAnswersByQuestionId(questionId);

        // Then
        assertEquals(expectedCount, result);
        verify(testAnswerRepository).countCorrectAnswersByQuestionId(questionId);
    }

    @Test
    void countAnswersByTestId_WithValidId_ShouldReturnCount() {
        // Given
        Long testId = 1L;
        Long expectedCount = 20L;
        when(testAnswerRepository.countAnswersByTestId(testId)).thenReturn(expectedCount);

        // When
        Long result = testAnswerService.countAnswersByTestId(testId);

        // Then
        assertEquals(expectedCount, result);
        verify(testAnswerRepository).countAnswersByTestId(testId);
    }

    @Test
    void countCorrectAnswersByTestId_WithValidId_ShouldReturnCount() {
        // Given
        Long testId = 1L;
        Long expectedCount = 5L;
        when(testAnswerRepository.countCorrectAnswersByTestId(testId)).thenReturn(expectedCount);

        // When
        Long result = testAnswerService.countCorrectAnswersByTestId(testId);

        // Then
        assertEquals(expectedCount, result);
        verify(testAnswerRepository).countCorrectAnswersByTestId(testId);
    }

    @Test
    void getIncorrectAnswersByQuestionId_WithValidId_ShouldReturnIncorrectAnswers() {
        // Given
        Long questionId = 1L;
        TestAnswer incorrectAnswer = new TestAnswer();
        incorrectAnswer.setCorrect(false);
        incorrectAnswer.setOptionText("London");

        TestAnswerDTO incorrectAnswerDTO = new TestAnswerDTO();
        incorrectAnswerDTO.setCorrect(false);
        incorrectAnswerDTO.setOptionText("London");

        List<TestAnswer> incorrectAnswers = Arrays.asList(incorrectAnswer);
        when(testAnswerRepository.findIncorrectAnswersByQuestionId(questionId)).thenReturn(incorrectAnswers);
        when(testAnswerMapper.toDTO(incorrectAnswer)).thenReturn(incorrectAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getIncorrectAnswersByQuestionId(questionId);

        // Then
        assertEquals(1, result.size());
        assertFalse(result.iterator().next().isCorrect());
        verify(testAnswerRepository).findIncorrectAnswersByQuestionId(questionId);
    }

    @Test
    void findByOptionTextContaining_WithValidKeyword_ShouldReturnMatchingAnswers() {
        // Given
        String keyword = "Paris";
        List<TestAnswer> matchingAnswers = Arrays.asList(testAnswer);
        when(testAnswerRepository.findByOptionTextContaining(keyword)).thenReturn(matchingAnswers);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.findByOptionTextContaining(keyword);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.iterator().next().getOptionText().contains(keyword));
        verify(testAnswerRepository).findByOptionTextContaining(keyword);
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void findByOptionTextContaining_WithNullKeyword_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.findByOptionTextContaining(null));
    }

    @Test
    void getCorrectAnswersByTestId_WithValidId_ShouldReturnCorrectAnswers() {
        // Given
        Long testId = 1L;
        List<TestAnswer> correctAnswers = Arrays.asList(testAnswer);
        when(testAnswerRepository.findCorrectAnswersByTestId(testId)).thenReturn(correctAnswers);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getCorrectAnswersByTestId(testId);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.iterator().next().isCorrect());
        verify(testAnswerRepository).findCorrectAnswersByTestId(testId);
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void countAnswersByQuestionId_WithValidId_ShouldReturnCount() {
        // Given
        Long questionId = 1L;
        Long expectedCount = 4L;
        when(testAnswerRepository.countAnswersByQuestionId(questionId)).thenReturn(expectedCount);

        // When
        Long result = testAnswerService.countAnswersByQuestionId(questionId);

        // Then
        assertEquals(expectedCount, result);
        verify(testAnswerRepository).countAnswersByQuestionId(questionId);
    }

    @Test
    void saveAnswer_WithValidDTO_ShouldReturnSavedDTO() {
        // Given
        when(testQuestionRepository.findById(testAnswerDTO.getQuestionId())).thenReturn(Optional.of(testQuestion));
        when(testAnswerRepository.save(any(TestAnswer.class))).thenReturn(testAnswer);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        TestAnswerDTO result = testAnswerService.saveAnswer(testAnswerDTO);

        // Then
        assertEquals(testAnswerDTO, result);
        verify(testQuestionRepository).findById(testAnswerDTO.getQuestionId());
        verify(testAnswerRepository).save(any(TestAnswer.class));
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void saveAnswer_WithNullDTO_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.saveAnswer(null));
    }

    @Test
    void saveAnswer_WithNullQuestionId_ShouldThrowException() {
        // Given
        testAnswerDTO.setQuestionId(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.saveAnswer(testAnswerDTO));
    }

    @Test
    void saveAnswer_WithNonExistentQuestionId_ShouldThrowException() {
        // Given
        when(testQuestionRepository.findById(testAnswerDTO.getQuestionId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testAnswerService.saveAnswer(testAnswerDTO));
    }

    @Test
    void createAnswer_WithValidDTO_ShouldReturnCreatedDTO() {
        // Given
        testAnswerDTO.setId(null); // New answer should not have ID
        when(testQuestionRepository.findById(testAnswerDTO.getQuestionId())).thenReturn(Optional.of(testQuestion));
        when(testAnswerRepository.save(any(TestAnswer.class))).thenReturn(testAnswer);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        TestAnswerDTO result = testAnswerService.createAnswer(testAnswerDTO);

        // Then
        assertEquals(testAnswerDTO, result);
        verify(testQuestionRepository).findById(testAnswerDTO.getQuestionId());
        verify(testAnswerRepository).save(any(TestAnswer.class));
    }

    @Test
    void createAnswer_WithExistingId_ShouldThrowException() {
        // Given - testAnswerDTO already has an ID

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.createAnswer(testAnswerDTO));
    }

    @Test
    void updateAnswer_WithValidData_ShouldReturnUpdatedDTO() {
        // Given
        Long answerId = 1L;
        when(testAnswerRepository.findById(answerId)).thenReturn(Optional.of(testAnswer));
        when(testAnswerRepository.save(testAnswer)).thenReturn(testAnswer);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        TestAnswerDTO result = testAnswerService.updateAnswer(answerId, testAnswerDTO);

        // Then
        assertEquals(testAnswerDTO, result);
        verify(testAnswerRepository).findById(answerId);
        verify(testAnswerRepository).save(testAnswer);
    }

    @Test
    void updateAnswer_WithNullDTO_ShouldThrowException() {
        // Given
        Long answerId = 1L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.updateAnswer(answerId, null));
    }

    @Test
    void updateAnswer_WithInvalidId_ShouldThrowException() {
        // Given
        Long answerId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.updateAnswer(answerId, testAnswerDTO));
    }

    @Test
    void updateAnswer_WithNonExistentId_ShouldThrowException() {
        // Given
        Long answerId = 999L;
        when(testAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testAnswerService.updateAnswer(answerId, testAnswerDTO));
    }

    @Test
    void deleteAnswerById_WithValidId_ShouldDeleteAnswer() {
        // Given
        Long answerId = 1L;
        when(testAnswerRepository.existsById(answerId)).thenReturn(true);
        doNothing().when(testAnswerRepository).deleteById(answerId);

        // When
        assertDoesNotThrow(() -> testAnswerService.deleteAnswerById(answerId));

        // Then
        verify(testAnswerRepository).existsById(answerId);
        verify(testAnswerRepository).deleteById(answerId);
    }

    @Test
    void deleteAnswerById_WithInvalidId_ShouldThrowException() {
        // Given
        Long answerId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.deleteAnswerById(answerId));
    }

    @Test
    void deleteAnswerById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long answerId = 999L;
        when(testAnswerRepository.existsById(answerId)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testAnswerService.deleteAnswerById(answerId));
    }

    @Test
    void saveAnswers_WithValidCollection_ShouldReturnSavedAnswers() {
        // Given
        Collection<TestAnswerDTO> answerDTOs = Arrays.asList(testAnswerDTO);
        when(testQuestionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(testAnswerRepository.save(any(TestAnswer.class))).thenReturn(testAnswer);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.saveAnswers(answerDTOs);

        // Then
        assertEquals(1, result.size());
        verify(testAnswerRepository).save(any(TestAnswer.class));
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void createAnswers_WithValidCollection_ShouldReturnCreatedAnswers() {
        // Given
        TestAnswerDTO newAnswerDTO = new TestAnswerDTO();
        newAnswerDTO.setOptionText("New Answer");
        newAnswerDTO.setQuestionId(1L);
        Collection<TestAnswerDTO> answerDTOs = Arrays.asList(newAnswerDTO);

        when(testQuestionRepository.findById(anyLong())).thenReturn(Optional.of(testQuestion));
        when(testAnswerRepository.save(any(TestAnswer.class))).thenReturn(testAnswer);
        when(testAnswerMapper.toDTO(testAnswer)).thenReturn(testAnswerDTO);

        // When
        Collection<TestAnswerDTO> result = testAnswerService.createAnswers(answerDTOs);

        // Then
        assertEquals(1, result.size());
        verify(testAnswerRepository).save(any(TestAnswer.class));
        verify(testAnswerMapper).toDTO(testAnswer);
    }

    @Test
    void getAllAnswers_WhenNoAnswers_ShouldReturnEmptyCollection() {
        // Given
        when(testAnswerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getAllAnswers();

        // Then
        assertTrue(result.isEmpty());
        verify(testAnswerRepository).findAll();
    }

    @Test
    void getAnswersByQuestionId_WhenNoAnswers_ShouldReturnEmptyCollection() {
        // Given
        Long questionId = 1L;
        when(testAnswerRepository.findByQuestionId(questionId)).thenReturn(Collections.emptyList());

        // When
        Collection<TestAnswerDTO> result = testAnswerService.getAnswersByQuestionId(questionId);

        // Then
        assertTrue(result.isEmpty());
        verify(testAnswerRepository).findByQuestionId(questionId);
    }

    @Test
    void findByOptionTextContaining_WhenNoMatches_ShouldReturnEmptyCollection() {
        // Given
        String keyword = "nonexistent";
        when(testAnswerRepository.findByOptionTextContaining(keyword)).thenReturn(Collections.emptyList());

        // When
        Collection<TestAnswerDTO> result = testAnswerService.findByOptionTextContaining(keyword);

        // Then
        assertTrue(result.isEmpty());
        verify(testAnswerRepository).findByOptionTextContaining(keyword);
    }

    @Test
    void saveAnswers_WithEmptyCollection_ShouldThrowException() {
        // Given
        Collection<TestAnswerDTO> emptyAnswers = Collections.emptyList();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.saveAnswers(emptyAnswers));
        verify(testAnswerRepository, never()).save(any(TestAnswer.class));
    }

    @Test
    void createAnswers_WithEmptyCollection_ShouldThrowException() {
        // Given
        Collection<TestAnswerDTO> emptyAnswers = Collections.emptyList();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testAnswerService.createAnswers(emptyAnswers));
        verify(testAnswerRepository, never()).save(any(TestAnswer.class));
    }
} 