package com.backend.service;

import com.backend.dto.TestDTO;
import com.backend.mapper.TestMapper;
import com.backend.model.Course;
import com.backend.model.TestEntity;
import com.backend.model.User;
import com.backend.repository.CourseRepository;
import com.backend.repository.TestRepository;
import com.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestMapper testMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private TestService testService;

    private TestEntity testEntity;
    private TestDTO testDTO;
    private User professor;
    private Course course;
    private Date testDate;

    @BeforeEach
    void setUp() {
        testDate = new Date();

        professor = new User();
        professor.setId(1);
        professor.setRole("PROFESOR");

        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");

        testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setTitle("Sample Test");
        testEntity.setDescription("Test Description");
        testEntity.setDate(testDate);
        testEntity.setProfessor(professor);
        testEntity.setCourse(course);

        testDTO = new TestDTO();
        testDTO.setId(1L);
        testDTO.setTitle("Sample Test");
        testDTO.setDescription("Test Description");
        testDTO.setDate(testDate);
        testDTO.setProfessorId(1);
        testDTO.setCourseId(1L);
    }

    @Test
    void constructor_WithNullRepository_ShouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                new TestService(null, testMapper, userRepository, courseRepository));
    }

    @Test
    void saveTest_WithValidDTO_ShouldReturnSavedDTO() {
        // Given
        when(testMapper.toEntity(testDTO)).thenReturn(testEntity);
        when(testRepository.save(testEntity)).thenReturn(testEntity);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        TestDTO result = testService.saveTest(testDTO);

        // Then
        assertEquals(testDTO, result);
        verify(testMapper).toEntity(testDTO);
        verify(testRepository).save(testEntity);
        verify(testMapper).toDTO(testEntity);
    }

    @Test
    void saveTest_WithNullDTO_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.saveTest(null));
    }

    @Test
    void createTest_WithValidDTO_ShouldReturnCreatedDTO() {
        // Given
        TestDTO newTestDTO = new TestDTO();
        newTestDTO.setTitle("New Test");
        newTestDTO.setDescription("New Test Description");
        newTestDTO.setDate(testDate);
        newTestDTO.setProfessorId(1);
        newTestDTO.setCourseId(1L);
        // Note: No ID set for new test

        TestEntity newEntity = new TestEntity();
        newEntity.setTitle("New Test");
        newEntity.setDate(testDate);

        when(testRepository.save(any(TestEntity.class))).thenReturn(testEntity);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        TestDTO result = testService.createTest(newTestDTO);

        // Then
        assertEquals(testDTO, result);
        verify(testRepository).save(any(TestEntity.class));
        verify(testMapper).toDTO(testEntity);
    }

    @Test
    void createTest_WithExistingId_ShouldThrowException() {
        // Given - testDTO already has an ID (set in setUp method)
        // The createTest method creates its own mapper internally
        // and should throw an exception if the resulting entity has an ID

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.createTest(testDTO));
    }

    @Test
    void getAllTests_ShouldReturnAllTests() {
        // Given
        List<TestEntity> testEntities = Arrays.asList(testEntity, testEntity);
        when(testRepository.findAll()).thenReturn(testEntities);

        // When
        Collection<TestEntity> result = testService.getAllTests();

        // Then
        assertEquals(2, result.size());
        verify(testRepository).findAll();
    }

    @Test
    void getTestById_WithValidId_ShouldReturnTest() {
        // Given
        Long testId = 1L;
        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        // When
        TestEntity result = testService.getTestById(testId);

        // Then
        assertEquals(testEntity, result);
        verify(testRepository).findById(testId);
    }

    @Test
    void getTestById_WithInvalidId_ShouldThrowException() {
        // Given
        Long testId = 0L;

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testService.getTestById(testId));
    }

    @Test
    void getTestById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long testId = 999L;
        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testService.getTestById(testId));
    }

    @Test
    void findTestsByProfId_WithValidId_ShouldReturnTests() {
        // Given
        Integer profId = 1;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByProfessorId(profId)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findTestsByProfId(profId);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByProfessorId(profId);
        verify(testMapper).toDTO(testEntity);
    }

    @Test
    void findTestsByProfId_WithInvalidId_ShouldThrowException() {
        // Given
        Integer profId = -1;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.findTestsByProfId(profId));
    }

    @Test
    void findTestsByCourseId_WithValidId_ShouldReturnTests() {
        // Given
        Long courseId = 1L;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByCourseId(courseId)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findTestsByCourseId(courseId);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByCourseId(courseId);
    }

    @Test
    void findUpcomingTests_ShouldReturnUpcomingTests() {
        // Given
        List<TestEntity> upcomingTests = Arrays.asList(testEntity);
        when(testRepository.findUpcomingTests()).thenReturn(upcomingTests);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findUpcomingTests();

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findUpcomingTests();
    }

    @Test
    void findTestsForStudentEnrollments_WithValidId_ShouldReturnTests() {
        // Given
        Integer studentId = 1;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findTestsForStudentEnrollments(studentId)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findTestsForStudentEnrollments(studentId);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findTestsForStudentEnrollments(studentId);
    }

    @Test
    void findByDateBetween_WithValidDates_ShouldReturnTests() {
        // Given
        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByDateBetween(startDate, endDate)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findByDateBetween(startDate, endDate);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByDateBetween(startDate, endDate);
    }

    @Test
    void findByDateBetween_WithEndDateBeforeStart_ShouldThrowException() {
        // Given
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() - 86400000);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                testService.findByDateBetween(startDate, endDate));
    }

    @Test
    void findByTitle_WithValidTitle_ShouldReturnTests() {
        // Given
        String title = "Sample Test";
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByTitle(title)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findByTitle(title);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByTitle(title);
    }

    @Test
    void findByTitle_WithNullTitle_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.findByTitle(null));
    }

    @Test
    void findByDescription_WithValidDescription_ShouldReturnTests() {
        // Given
        String description = "Test Description";
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByDescription(description)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findByDescription(description);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByDescription(description);
    }

    @Test
    void findByMonth_WithValidMonth_ShouldReturnTests() {
        // Given
        Integer month = 12;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByMonth(month)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findByMonth(month);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByMonth(month);
    }

    @Test
    void findByYear_WithValidYear_ShouldReturnTests() {
        // Given
        Integer year = 2024;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByYear(year)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findByYear(year);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByYear(year);
    }

    @Test
    void findTestsByExactDate_WithValidDate_ShouldReturnTests() {
        // Given
        Date exactDate = testDate;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findTestsByExactDate(exactDate)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findTestsByExactDate(exactDate);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findTestsByExactDate(exactDate);
    }

    @Test
    void findByMonthAndYear_WithValidParameters_ShouldReturnTests() {
        // Given
        Integer month = 12;
        Integer year = 2024;
        List<TestEntity> testEntities = Arrays.asList(testEntity);
        when(testRepository.findByMonthAndYear(month, year)).thenReturn(testEntities);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        Collection<TestDTO> result = testService.findByMonthAndYear(month, year);

        // Then
        assertEquals(1, result.size());
        verify(testRepository).findByMonthAndYear(month, year);
    }

    @Test
    void countTestsByCourse_WithValidId_ShouldReturnCount() {
        // Given
        Integer courseId = 1;
        Long expectedCount = 5L;
        when(testRepository.countTestsByCourse(courseId)).thenReturn(expectedCount);

        // When
        Long result = testService.countTestsByCourse(courseId);

        // Then
        assertEquals(expectedCount, result);
        verify(testRepository).countTestsByCourse(courseId);
    }

    @Test
    void countTestsByProfessor_WithValidId_ShouldReturnCount() {
        // Given
        Integer professorId = 1;
        Long expectedCount = 3L;
        when(testRepository.countTestsByProfessor(professorId)).thenReturn(expectedCount);

        // When
        Long result = testService.countTestsByProfessor(professorId);

        // Then
        assertEquals(expectedCount, result);
        verify(testRepository).countTestsByProfessor(professorId);
    }

    @Test
    void countUpcomingTests_ShouldReturnCount() {
        // Given
        Long expectedCount = 2L;
        when(testRepository.countUpcomingTests()).thenReturn(expectedCount);

        // When
        Long result = testService.countUpcomingTests();

        // Then
        assertEquals(expectedCount, result);
        verify(testRepository).countUpcomingTests();
    }

    @Test
    void countTestsForStudentEnrollments_WithValidId_ShouldReturnCount() {
        // Given
        Integer studentId = 1;
        Long expectedCount = 4L;
        when(testRepository.countTestsForStudentEnrollments(studentId)).thenReturn(expectedCount);

        // When
        Long result = testService.countTestsForStudentEnrollments(studentId);

        // Then
        assertEquals(expectedCount, result);
        verify(testRepository).countTestsForStudentEnrollments(studentId);
    }

    @Test
    void countTestsForStudent_WithValidId_ShouldReturnCount() {
        // Given
        Integer studentId = 1;
        Long expectedCount = 6L;
        when(testRepository.countTestsForStudent(studentId)).thenReturn(expectedCount);

        // When
        Long result = testService.countTestsForStudent(studentId);

        // Then
        assertEquals(expectedCount, result);
        verify(testRepository).countTestsForStudent(studentId);
    }

    @Test
    void countTestsByDateBetween_WithValidDates_ShouldReturnCount() {
        // Given
        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);
        Long expectedCount = 3L;
        when(testRepository.countTestsByDateBetween(startDate, endDate)).thenReturn(expectedCount);

        // When
        Long result = testService.countTestsByDateBetween(startDate, endDate);

        // Then
        assertEquals(expectedCount, result);
        verify(testRepository).countTestsByDateBetween(startDate, endDate);
    }

    @Test
    void deleteTestById_WithValidId_ShouldDeleteTest() {
        // Given
        Long testId = 1L;
        when(testRepository.existsById(testId)).thenReturn(true);
        doNothing().when(testRepository).deleteById(testId);

        // When
        String result = testService.deleteTestById(testId);

        // Then
        assertEquals("Test deleted successfully", result);
        verify(testRepository).existsById(testId);
        verify(testRepository).deleteById(testId);
    }

    @Test
    void deleteTestById_WithNonExistentId_ShouldThrowException() {
        // Given
        Long testId = 999L;
        when(testRepository.existsById(testId)).thenReturn(false);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testService.deleteTestById(testId));
    }

    @Test
    void updateTest_WithValidData_ShouldUpdateTest() {
        // Given
        Long testId = 1L;
        TestEntity entityWithProfessor = new TestEntity();
        entityWithProfessor.setId(testId);
        entityWithProfessor.setProfessor(professor);
        
        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(testMapper.toEntity(testDTO)).thenReturn(entityWithProfessor);
        when(testRepository.save(testEntity)).thenReturn(testEntity);
        when(testMapper.toDTO(testEntity)).thenReturn(testDTO);

        // When
        TestDTO result = testService.updateTest(testId, testDTO);

        // Then
        assertEquals(testDTO, result);
        verify(testRepository).findById(testId);
        verify(testRepository).save(testEntity);
    }

    @Test
    void updateTest_WithNullDTO_ShouldThrowException() {
        // Given
        Long testId = 1L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.updateTest(testId, null));
    }

    @Test
    void updateTest_WithInvalidId_ShouldThrowException() {
        // Given
        Long testId = 0L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.updateTest(testId, testDTO));
    }

    @Test
    void updateTest_WithNonExistentId_ShouldThrowException() {
        // Given
        Long testId = 999L;
        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> testService.updateTest(testId, testDTO));
    }

    @Test
    void updateTest_WithIncompleteDTOFields_ShouldThrowException() {
        // Given
        Long testId = 1L;
        TestDTO incompleteDTO = new TestDTO();
        incompleteDTO.setTitle(null);

        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> testService.updateTest(testId, incompleteDTO));
    }

    @Test
    void updateTest_WithNonProfessorRole_ShouldThrowException() {
        // Given
        Long testId = 1L;
        User student = new User();
        student.setRole("STUDENT");
        
        TestEntity entityWithStudent = new TestEntity();
        entityWithStudent.setProfessor(student);

        when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(testMapper.toEntity(testDTO)).thenReturn(entityWithStudent);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> testService.updateTest(testId, testDTO));
    }
} 