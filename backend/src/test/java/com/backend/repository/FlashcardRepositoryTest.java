package com.backend.repository;

import com.backend.model.AnswerFC;
import com.backend.model.Course;
import com.backend.model.Flashcard;
import com.backend.model.Material;
import com.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class FlashcardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FlashcardRepository flashcardRepository;

    private User testUser1;
    private User testUser2;
    private Course testCourse;
    private Material testMaterial;
    private Flashcard testFlashcard1;
    private Flashcard testFlashcard2;
    private Flashcard testFlashcard3;
    private Date currentDate;

    @BeforeEach
    void setUp() {
        currentDate = new Date();
        
        // Create test users
        testUser1 = new User();
        testUser1.setEmail("user1@test.com");
        testUser1.setFirstName("Alice");
        testUser1.setLastName("Student1");
        testUser1.setPassword("password");
        testUser1.setRole("STUDENT");
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = new User();
        testUser2.setEmail("user2@test.com");
        testUser2.setFirstName("Bob");
        testUser2.setLastName("Student2");
        testUser2.setPassword("password");
        testUser2.setRole("STUDENT");
        testUser2 = entityManager.persistAndFlush(testUser2);

        // Create test course
        testCourse = new Course();
        testCourse.setTitle("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setProfessor(testUser1);
        testCourse = entityManager.persistAndFlush(testCourse);

        // Create test material
        testMaterial = new Material();
        testMaterial.setName("Test Material");
        testMaterial.setPath("/test/path");
        testMaterial.setCourse(testCourse);
        testMaterial = entityManager.persistAndFlush(testMaterial);

        // Create test flashcards
        testFlashcard1 = createFlashcard("Question 1", 1, "MULTIPLE_CHOICE", 5, testUser1, testMaterial, 
            new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(1)));
        testFlashcard2 = createFlashcard("Question 2", 2, "TRUE_FALSE", 10, testUser1, testMaterial, null);
        testFlashcard3 = createFlashcard("Question 3", 1, "OPEN_ENDED", 15, testUser2, testMaterial, 
            new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(5)));

        entityManager.flush();
    }

    private Flashcard createFlashcard(String question, Integer level, String questionType, 
                                     Integer pageIndex, User user, Material material, Date lastStudied) {
        Flashcard flashcard = new Flashcard();
        flashcard.setQuestion(question);
        flashcard.setLevel(level);
        flashcard.setQuestionType(questionType);
        flashcard.setPageIndex(pageIndex);
        flashcard.setUser(user);
        flashcard.setMaterial(material);
        flashcard.setLastStudiedAt(lastStudied);
        
        // Create some answers
        Set<AnswerFC> answers = new HashSet<>();
        AnswerFC answer1 = new AnswerFC();
        answer1.setOptionText("Answer 1");
        answer1.setCorrect(true);
        answer1.setFlashcard(flashcard);
        answers.add(answer1);
        
        AnswerFC answer2 = new AnswerFC();
        answer2.setOptionText("Answer 2");
        answer2.setCorrect(false);
        answer2.setFlashcard(flashcard);
        answers.add(answer2);
        
        flashcard.setAnswers(answers);
        
        return entityManager.persistAndFlush(flashcard);
    }

    @Test
    void findByUserId_ShouldReturnFlashcardsForSpecificUser() {
        // When
        List<Flashcard> user1Flashcards = flashcardRepository.findByUserId(testUser1.getId());
        List<Flashcard> user2Flashcards = flashcardRepository.findByUserId(testUser2.getId());

        // Then
        assertEquals(2, user1Flashcards.size());
        assertEquals(1, user2Flashcards.size());
        
        assertTrue(user1Flashcards.stream().allMatch(f -> f.getUser().getId().equals(testUser1.getId())));
        assertTrue(user2Flashcards.stream().allMatch(f -> f.getUser().getId().equals(testUser2.getId())));
    }

    @Test
    void findByMaterialId_ShouldReturnFlashcardsForSpecificMaterial() {
        // When
        List<Flashcard> materialFlashcards = flashcardRepository.findByMaterialId(testMaterial.getId());

        // Then
        assertEquals(3, materialFlashcards.size());
        assertTrue(materialFlashcards.stream().allMatch(f -> f.getMaterial().getId().equals(testMaterial.getId())));
    }

    @Test
    void findDueFlashcards_ShouldReturnOverdueFlashcards() {
        // When
        List<Flashcard> dueFlashcards = flashcardRepository.findDueFlashcards(currentDate, testUser1.getId());

        // Then
        assertEquals(1, dueFlashcards.size());
        assertEquals("Question 1", dueFlashcards.get(0).getQuestion());
        assertTrue(dueFlashcards.get(0).getLastStudiedAt().before(currentDate));
    }

    @Test
    void findByLevelAndUserId_ShouldReturnFlashcardsWithSpecificLevel() {
        // When
        List<Flashcard> level1Flashcards = flashcardRepository.findByLevelAndUserId(1, testUser1.getId());
        List<Flashcard> level2Flashcards = flashcardRepository.findByLevelAndUserId(2, testUser1.getId());

        // Then
        assertEquals(1, level1Flashcards.size());
        assertEquals("Question 1", level1Flashcards.get(0).getQuestion());
        
        assertEquals(1, level2Flashcards.size());
        assertEquals("Question 2", level2Flashcards.get(0).getQuestion());
    }

    @Test
    void findByCourseIdAndUserId_ShouldReturnFlashcardsForCourseAndUser() {
        // When
        List<Flashcard> courseFlashcards = flashcardRepository.findByCourseIdAndUserId(testCourse.getId(), testUser1.getId());

        // Then
        assertEquals(2, courseFlashcards.size());
        assertTrue(courseFlashcards.stream().allMatch(f -> 
            f.getMaterial().getCourse().getId().equals(testCourse.getId()) && 
            f.getUser().getId().equals(testUser1.getId())));
    }

    @Test
    void findByQuestionTypeAndUserId_ShouldReturnFlashcardsWithSpecificType() {
        // When
        List<Flashcard> multipleChoiceFlashcards = flashcardRepository.findByQuestionTypeAndUserId("MULTIPLE_CHOICE", testUser1.getId());
        List<Flashcard> trueFalseFlashcards = flashcardRepository.findByQuestionTypeAndUserId("TRUE_FALSE", testUser1.getId());

        // Then
        assertEquals(1, multipleChoiceFlashcards.size());
        assertEquals("Question 1", multipleChoiceFlashcards.get(0).getQuestion());
        
        assertEquals(1, trueFalseFlashcards.size());
        assertEquals("Question 2", trueFalseFlashcards.get(0).getQuestion());
    }

    @Test
    void findByQuestion_ShouldReturnSpecificFlashcard() {
        // When
        Flashcard foundFlashcard = flashcardRepository.findByQuestion("Question 1");

        // Then
        assertNotNull(foundFlashcard);
        assertEquals("Question 1", foundFlashcard.getQuestion());
        assertEquals(testUser1.getId(), foundFlashcard.getUser().getId());
    }

    @Test
    void findByQuestion_WithNonExistentQuestion_ShouldReturnNull() {
        // When
        Flashcard foundFlashcard = flashcardRepository.findByQuestion("Non-existent Question");

        // Then
        assertNull(foundFlashcard);
    }

    @Test
    void findByPageIndex_ShouldReturnFlashcardsAtSpecificPage() {
        // When
        List<Flashcard> page5Flashcards = flashcardRepository.findByPageIndex(5);
        List<Flashcard> page10Flashcards = flashcardRepository.findByPageIndex(10);

        // Then
        assertEquals(1, page5Flashcards.size());
        assertEquals("Question 1", page5Flashcards.get(0).getQuestion());
        
        assertEquals(1, page10Flashcards.size());
        assertEquals("Question 2", page10Flashcards.get(0).getQuestion());
    }

    @Test
    void findByPageIndexAndUserId_ShouldReturnFlashcardsForPageAndUser() {
        // When
        List<Flashcard> user1Page5 = flashcardRepository.findByPageIndexAndUserId(5, testUser1.getId());
        List<Flashcard> user2Page15 = flashcardRepository.findByPageIndexAndUserId(15, testUser2.getId());

        // Then
        assertEquals(1, user1Page5.size());
        assertEquals("Question 1", user1Page5.get(0).getQuestion());
        
        assertEquals(1, user2Page15.size());
        assertEquals("Question 3", user2Page15.get(0).getQuestion());
    }

    @Test
    void findByPageIndexAndMaterialId_ShouldReturnFlashcardsForPageAndMaterial() {
        // When
        List<Flashcard> materialPage5 = flashcardRepository.findByPageIndexAndMaterialId(5, testMaterial.getId());

        // Then
        assertEquals(1, materialPage5.size());
        assertEquals("Question 1", materialPage5.get(0).getQuestion());
    }

    @Test
    void countByCourseId_ShouldReturnCorrectCount() {
        // When
        Long count = flashcardRepository.countByCourseId(testCourse.getId());

        // Then
        assertEquals(3L, count);
    }

    @Test
    void findAllByCourseId_ShouldReturnAllFlashcardsInCourse() {
        // When
        List<Flashcard> courseFlashcards = flashcardRepository.findAllByCourseId(testCourse.getId());

        // Then
        assertEquals(3, courseFlashcards.size());
        assertTrue(courseFlashcards.stream().allMatch(f -> 
            f.getMaterial().getCourse().getId().equals(testCourse.getId())));
    }

    @Test
    void deleteByMaterialId_ShouldRemoveAllFlashcardsFromMaterial() {
        // Given
        Long initialCount = flashcardRepository.count();
        assertEquals(3L, initialCount);

        // When
        flashcardRepository.deleteByMaterialId(testMaterial.getId());
        entityManager.flush();

        // Then
        Long finalCount = flashcardRepository.count();
        assertEquals(0L, finalCount);
        
        List<Flashcard> remainingFlashcards = flashcardRepository.findByMaterialId(testMaterial.getId());
        assertTrue(remainingFlashcards.isEmpty());
    }

    @Test
    void findFlashcardCountsByCourseIds_ShouldReturnCorrectCounts() {
        // Given
        Course course2 = new Course();
        course2.setTitle("Test Course 2");
        course2.setDescription("Test Description 2");
        course2.setProfessor(testUser1);
        course2 = entityManager.persistAndFlush(course2);

        Material material2 = new Material();
        material2.setName("Test Material 2");
        material2.setPath("/test/path2");
        material2.setCourse(course2);
        material2 = entityManager.persistAndFlush(material2);

        createFlashcard("Question 4", 1, "MULTIPLE_CHOICE", 1, testUser1, material2, null);
        createFlashcard("Question 5", 1, "MULTIPLE_CHOICE", 2, testUser1, material2, null);
        entityManager.flush();

        // When
        List<Object[]> counts = flashcardRepository.findFlashcardCountsByCourseIds(
            List.of(testCourse.getId(), course2.getId()));

        // Then
        assertEquals(2, counts.size());
        
        Map<Long, Long> countMap = counts.stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
            ));
        
        assertEquals(3L, countMap.get(testCourse.getId()));
        assertEquals(2L, countMap.get(course2.getId()));
    }

    @Test
    void getFlashcardCountsByCourseIds_DefaultMethod_ShouldReturnCorrectMap() {
        // Given
        Course course2 = new Course();
        course2.setTitle("Test Course 2");
        course2.setDescription("Test Description 2");
        course2.setProfessor(testUser1);
        course2 = entityManager.persistAndFlush(course2);

        Material material2 = new Material();
        material2.setName("Test Material 2");
        material2.setPath("/test/path2");
        material2.setCourse(course2);
        material2 = entityManager.persistAndFlush(material2);

        createFlashcard("Question 4", 1, "MULTIPLE_CHOICE", 1, testUser1, material2, null);
        entityManager.flush();

        // When
        Map<Long, Long> countMap = flashcardRepository.getFlashcardCountsByCourseIds(
            List.of(testCourse.getId(), course2.getId()));

        // Then
        assertEquals(2, countMap.size());
        assertEquals(3L, countMap.get(testCourse.getId()));
        assertEquals(1L, countMap.get(course2.getId()));
    }

    @Test
    void findAllFlashcards_ShouldReturnAllFlashcards() {
        // When
        List<Flashcard> allFlashcards = flashcardRepository.findAllFlashcards();

        // Then
        assertEquals(3, allFlashcards.size());
    }

    @Test
    void findAllFlashcardsByUserId_ShouldReturnUserFlashcards() {
        // When
        List<Flashcard> user1Flashcards = flashcardRepository.findAllFlashcardsByUserId(testUser1.getId());
        List<Flashcard> user2Flashcards = flashcardRepository.findAllFlashcardsByUserId(testUser2.getId());

        // Then
        assertEquals(2, user1Flashcards.size());
        assertEquals(1, user2Flashcards.size());
    }

    @Test
    void findAllFlashcardsByCourseId_ShouldReturnCourseFlashcards() {
        // When
        List<Flashcard> courseFlashcards = flashcardRepository.findAllFlashcardsByCourseId(testCourse.getId());

        // Then
        assertEquals(3, courseFlashcards.size());
    }

    @Test
    void findAllFlashcardsByMaterialId_ShouldReturnMaterialFlashcards() {
        // When
        List<Flashcard> materialFlashcards = flashcardRepository.findAllFlashcardsByMaterialId(testMaterial.getId());

        // Then
        assertEquals(3, materialFlashcards.size());
    }

    @Test
    void findAllFlashcardsByMaterialIdAndUserId_ShouldReturnFilteredFlashcards() {
        // When
        List<Flashcard> user1MaterialFlashcards = flashcardRepository.findAllFlashcardsByMaterialIdAndUserId(
            testMaterial.getId(), testUser1.getId());
        List<Flashcard> user2MaterialFlashcards = flashcardRepository.findAllFlashcardsByMaterialIdAndUserId(
            testMaterial.getId(), testUser2.getId());

        // Then
        assertEquals(2, user1MaterialFlashcards.size());
        assertEquals(1, user2MaterialFlashcards.size());
    }

    @Test
    void findByUserId_WithNonExistentUser_ShouldReturnEmptyList() {
        // When
        List<Flashcard> flashcards = flashcardRepository.findByUserId(999);

        // Then
        assertTrue(flashcards.isEmpty());
    }

    @Test
    void findByMaterialId_WithNonExistentMaterial_ShouldReturnEmptyList() {
        // When
        List<Flashcard> flashcards = flashcardRepository.findByMaterialId(999L);

        // Then
        assertTrue(flashcards.isEmpty());
    }

    @Test
    void findDueFlashcards_WithFutureDate_ShouldReturnEmptyList() {
        // Given
        Date futureDate = new Date(currentDate.getTime() - TimeUnit.DAYS.toMillis(10));

        // When
        List<Flashcard> dueFlashcards = flashcardRepository.findDueFlashcards(futureDate, testUser1.getId());

        // Then
        assertTrue(dueFlashcards.isEmpty());
    }

    @Test
    void repositoryOperations_ShouldMaintainDataIntegrity() {
        // Given
        Flashcard newFlashcard = new Flashcard();
        newFlashcard.setQuestion("New Question");
        newFlashcard.setLevel(3);
        newFlashcard.setQuestionType("ESSAY");
        newFlashcard.setPageIndex(20);
        newFlashcard.setUser(testUser1);
        newFlashcard.setMaterial(testMaterial);
        newFlashcard.setLastStudiedAt(currentDate);

        // When - Create
        Flashcard savedFlashcard = flashcardRepository.save(newFlashcard);

        // Then - Verify creation
        assertNotNull(savedFlashcard.getId());
        assertEquals("New Question", savedFlashcard.getQuestion());

        // When - Update
        savedFlashcard.setLevel(4);
        Flashcard updatedFlashcard = flashcardRepository.save(savedFlashcard);

        // Then - Verify update
        assertEquals(4, updatedFlashcard.getLevel());

        // When - Delete
        flashcardRepository.delete(savedFlashcard);
        boolean exists = flashcardRepository.existsById(savedFlashcard.getId());

        // Then - Verify deletion
        assertFalse(exists);
    }
} 