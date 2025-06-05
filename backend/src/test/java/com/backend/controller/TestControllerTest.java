package com.backend.controller;

import com.backend.dto.TestDTO;
import com.backend.model.TestEntity;
import com.backend.service.TestService;
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
class TestControllerTest {

    @Mock
    private TestService testService;

    @InjectMocks
    private TestController testController;

    private TestEntity testEntity;
    private TestDTO testDTO;
    private Date testDate;

    @BeforeEach
    void setUp() {
        testDate = new Date();
        
        testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setTitle("Sample Test");
        testEntity.setDescription("Test Description");
        testEntity.setDate(testDate);

        testDTO = new TestDTO();
        testDTO.setId(1L);
        testDTO.setTitle("Sample Test");
        testDTO.setDescription("Test Description");
        testDTO.setDate(testDate);
        testDTO.setProfessorId(1);
        testDTO.setCourseId(1L);
    }

    @Test
    void getAllTests_ShouldReturnAllTests() {
        // Given
        List<TestEntity> tests = Arrays.asList(testEntity, testEntity);
        when(testService.getAllTests()).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestEntity>> response = testController.getAllTests();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testService).getAllTests();
    }

    @Test
    void getTestById_WithValidId_ShouldReturnTest() {
        // Given
        Long testId = 1L;
        when(testService.getTestById(testId)).thenReturn(testEntity);

        // When
        ResponseEntity<TestEntity> response = testController.getTestById(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testEntity.getId(), response.getBody().getId());
        assertEquals(testEntity.getTitle(), response.getBody().getTitle());
        verify(testService).getTestById(testId);
    }

    @Test
    void createTest_WithValidTestDTO_ShouldCreateTest() {
        // Given
        when(testService.createTest(any(TestDTO.class))).thenReturn(testDTO);

        // When
        ResponseEntity<TestDTO> response = testController.createTest(testDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDTO.getId(), response.getBody().getId());
        assertEquals(testDTO.getTitle(), response.getBody().getTitle());
        verify(testService).createTest(testDTO);
    }

    @Test
    void saveTest_WithValidTestDTO_ShouldSaveTest() {
        // Given
        when(testService.saveTest(any(TestDTO.class))).thenReturn(testDTO);

        // When
        ResponseEntity<TestDTO> response = testController.saveTest(testDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDTO.getId(), response.getBody().getId());
        verify(testService).saveTest(testDTO);
    }

    @Test
    void updateTest_WithValidData_ShouldUpdateTest() {
        // Given
        Long testId = 1L;
        when(testService.updateTest(eq(testId), any(TestDTO.class))).thenReturn(testDTO);

        // When
        ResponseEntity<TestDTO> response = testController.updateTest(testId, testDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDTO.getId(), response.getBody().getId());
        verify(testService).updateTest(testId, testDTO);
    }

    @Test
    void deleteTestById_WithValidId_ShouldDeleteTest() {
        // Given
        Long testId = 1L;
        String deleteMessage = "Test deleted successfully";
        when(testService.deleteTestById(testId)).thenReturn(deleteMessage);

        // When
        ResponseEntity<String> response = testController.deleteTestById(testId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deleteMessage, response.getBody());
        verify(testService).deleteTestById(testId);
    }

    @Test
    void getTestsByProfessorId_WithValidProfessorId_ShouldReturnTests() {
        // Given
        Integer professorId = 1;
        List<TestDTO> tests = Arrays.asList(testDTO, testDTO);
        when(testService.findTestsByProfId(professorId)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByProfessorId(professorId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(testService).findTestsByProfId(professorId);
    }

    @Test
    void getTestsByCourseId_WithValidCourseId_ShouldReturnTests() {
        // Given
        Long courseId = 1L;
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findTestsByCourseId(courseId)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByCourseId(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findTestsByCourseId(courseId);
    }

    @Test
    void getTestsByStudentId_WithValidStudentId_ShouldReturnTests() {
        // Given
        Integer studentId = 1;
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findTestsForStudentEnrollments(studentId)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByStudentId(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findTestsForStudentEnrollments(studentId);
    }

    @Test
    void getUpcomingTests_ShouldReturnUpcomingTests() {
        // Given
        List<TestDTO> upcomingTests = Arrays.asList(testDTO);
        when(testService.findUpcomingTests()).thenReturn(upcomingTests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getUpcomingTests();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findUpcomingTests();
    }

    @Test
    void getTestsByDateRange_WithValidDateRange_ShouldReturnTests() {
        // Given
        Date startDate = new Date(System.currentTimeMillis() - 86400000); // 1 day ago
        Date endDate = new Date(System.currentTimeMillis() + 86400000);   // 1 day from now
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findByDateBetween(startDate, endDate)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByDateRange(startDate, endDate);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findByDateBetween(startDate, endDate);
    }

    @Test
    void getTestsByTitle_WithValidTitle_ShouldReturnTests() {
        // Given
        String title = "Sample";
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findByTitle(title)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByTitle(title);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findByTitle(title);
    }

    @Test
    void getTestsByDescription_WithValidDescription_ShouldReturnTests() {
        // Given
        String description = "Test Description";
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findByDescription(description)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByDescription(description);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findByDescription(description);
    }

    @Test
    void getTestsByMonth_WithValidMonth_ShouldReturnTests() {
        // Given
        Integer month = 12;
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findByMonth(month)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByMonth(month);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findByMonth(month);
    }

    @Test
    void getTestsByYear_WithValidYear_ShouldReturnTests() {
        // Given
        Integer year = 2024;
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findByYear(year)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByYear(year);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findByYear(year);
    }

    @Test
    void getTestsByExactDate_WithValidDate_ShouldReturnTests() {
        // Given
        Date exactDate = testDate;
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findTestsByExactDate(exactDate)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByExactDate(exactDate);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findTestsByExactDate(exactDate);
    }

    @Test
    void getTestsByMonthAndYear_WithValidMonthAndYear_ShouldReturnTests() {
        // Given
        Integer month = 12;
        Integer year = 2024;
        List<TestDTO> tests = Arrays.asList(testDTO);
        when(testService.findByMonthAndYear(month, year)).thenReturn(tests);

        // When
        ResponseEntity<Collection<TestDTO>> response = testController.getTestsByMonthAndYear(month, year);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(testService).findByMonthAndYear(month, year);
    }

    @Test
    void countTestsByCourse_WithValidCourseId_ShouldReturnCount() {
        // Given
        Integer courseId = 1;
        Long expectedCount = 5L;
        when(testService.countTestsByCourse(courseId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testController.countTestsByCourse(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testService).countTestsByCourse(courseId);
    }

    @Test
    void countTestsByProfessor_WithValidProfessorId_ShouldReturnCount() {
        // Given
        Integer professorId = 1;
        Long expectedCount = 3L;
        when(testService.countTestsByProfessor(professorId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testController.countTestsByProfessor(professorId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testService).countTestsByProfessor(professorId);
    }

    @Test
    void countUpcomingTests_ShouldReturnCount() {
        // Given
        Long expectedCount = 2L;
        when(testService.countUpcomingTests()).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testController.countUpcomingTests();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testService).countUpcomingTests();
    }

    @Test
    void countTestsByStudent_WithValidStudentId_ShouldReturnCount() {
        // Given
        Integer studentId = 1;
        Long expectedCount = 4L;
        when(testService.countTestsForStudent(studentId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testController.countTestsByStudent(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testService).countTestsForStudent(studentId);
    }

    @Test
    void countTestsByStudentEnrollments_WithValidStudentId_ShouldReturnCount() {
        // Given
        Integer studentId = 1;
        Long expectedCount = 6L;
        when(testService.countTestsForStudentEnrollments(studentId)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testController.countTestsByStudentEnrollments(studentId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testService).countTestsForStudentEnrollments(studentId);
    }

    @Test
    void countTestsByDateRange_WithValidDateRange_ShouldReturnCount() {
        // Given
        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);
        Long expectedCount = 3L;
        when(testService.countTestsByDateBetween(startDate, endDate)).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = testController.countTestsByDateRange(startDate, endDate);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCount, response.getBody());
        verify(testService).countTestsByDateBetween(startDate, endDate);
    }

    @Test
    void getAllTests_WhenNoTests_ShouldReturnEmptyCollection() {
        // Given
        when(testService.getAllTests()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Collection<TestEntity>> response = testController.getAllTests();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(testService).getAllTests();
    }
}