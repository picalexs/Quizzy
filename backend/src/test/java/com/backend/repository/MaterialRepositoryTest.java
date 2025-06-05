package com.backend.repository;

import com.backend.model.Course;
import com.backend.model.Material;
import com.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MaterialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MaterialRepository materialRepository;

    private User testProfessor1;
    private User testProfessor2;
    private Course testCourse1;
    private Course testCourse2;
    private Material testMaterial1;
    private Material testMaterial2;
    private Material testMaterial3;

    @BeforeEach
    void setUp() {
        // Create test professors
        testProfessor1 = new User();
        testProfessor1.setEmail("prof1@test.com");
        testProfessor1.setFirstName("John");
        testProfessor1.setLastName("Professor1");
        testProfessor1.setPassword("password");
        testProfessor1.setRole("PROFESSOR");
        testProfessor1 = entityManager.persistAndFlush(testProfessor1);

        testProfessor2 = new User();
        testProfessor2.setEmail("prof2@test.com");
        testProfessor2.setFirstName("Jane");
        testProfessor2.setLastName("Professor2");
        testProfessor2.setPassword("password");
        testProfessor2.setRole("PROFESSOR");
        testProfessor2 = entityManager.persistAndFlush(testProfessor2);

        // Create test courses
        testCourse1 = new Course();
        testCourse1.setTitle("Computer Science 101");
        testCourse1.setDescription("Introduction to Computer Science");
        testCourse1.setProfessor(testProfessor1);
        testCourse1 = entityManager.persistAndFlush(testCourse1);

        testCourse2 = new Course();
        testCourse2.setTitle("Mathematics 201");
        testCourse2.setDescription("Advanced Mathematics");
        testCourse2.setProfessor(testProfessor2);
        testCourse2 = entityManager.persistAndFlush(testCourse2);

        // Create test materials
        testMaterial1 = createMaterial("Java Programming Guide", "/courses/cs101/java-guide.pdf", testCourse1);
        testMaterial2 = createMaterial("Data Structures Tutorial", "/courses/cs101/data-structures.pdf", testCourse1);
        testMaterial3 = createMaterial("Calculus Textbook", "/courses/math201/calculus.pdf", testCourse2);

        entityManager.flush();
    }

    private Material createMaterial(String name, String path, Course course) {
        Material material = new Material();
        material.setName(name);
        material.setPath(path);
        material.setCourse(course);
        return entityManager.persistAndFlush(material);
    }

    @Test
    void findByCourseId_ShouldReturnMaterialsForSpecificCourse() {
        // When
        List<Material> course1Materials = materialRepository.findByCourseId(testCourse1.getId());
        List<Material> course2Materials = materialRepository.findByCourseId(testCourse2.getId());

        // Then
        assertEquals(2, course1Materials.size());
        assertEquals(1, course2Materials.size());

        assertTrue(course1Materials.stream().allMatch(m -> m.getCourse().getId().equals(testCourse1.getId())));
        assertTrue(course2Materials.stream().allMatch(m -> m.getCourse().getId().equals(testCourse2.getId())));

        // Verify specific materials
        assertTrue(course1Materials.stream().anyMatch(m -> m.getName().equals("Java Programming Guide")));
        assertTrue(course1Materials.stream().anyMatch(m -> m.getName().equals("Data Structures Tutorial")));
        assertTrue(course2Materials.stream().anyMatch(m -> m.getName().equals("Calculus Textbook")));
    }

    @Test
    void findByNameContaining_ShouldReturnMaterialsWithMatchingNames() {
        // When
        List<Material> javaResults = materialRepository.findByNameContaining("Java");
        List<Material> tutorialResults = materialRepository.findByNameContaining("Tutorial");
        List<Material> programmingResults = materialRepository.findByNameContaining("Programming");

        // Then
        assertEquals(1, javaResults.size());
        assertEquals("Java Programming Guide", javaResults.get(0).getName());

        assertEquals(1, tutorialResults.size());
        assertEquals("Data Structures Tutorial", tutorialResults.get(0).getName());

        assertEquals(1, programmingResults.size());
        assertEquals("Java Programming Guide", programmingResults.get(0).getName());
    }

    @Test
    void findByNameContaining_WithPartialMatch_ShouldReturnCorrectResults() {
        // When
        List<Material> dataResults = materialRepository.findByNameContaining("data");
        List<Material> textbookResults = materialRepository.findByNameContaining("Textbook");

        // Then
        assertEquals(1, dataResults.size());
        assertEquals("Data Structures Tutorial", dataResults.get(0).getName());

        assertEquals(1, textbookResults.size());
        assertEquals("Calculus Textbook", textbookResults.get(0).getName());
    }

    @Test
    void findByNameContaining_WithNoMatches_ShouldReturnEmptyList() {
        // When
        List<Material> results = materialRepository.findByNameContaining("NonExistent");

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void findByPathContaining_ShouldReturnMaterialsWithMatchingPaths() {
        // When
        List<Material> cs101Results = materialRepository.findByPathContaining("cs101");
        List<Material> math201Results = materialRepository.findByPathContaining("math201");
        List<Material> pdfResults = materialRepository.findByPathContaining(".pdf");

        // Then
        assertEquals(2, cs101Results.size());
        assertTrue(cs101Results.stream().allMatch(m -> m.getPath().contains("cs101")));

        assertEquals(1, math201Results.size());
        assertTrue(math201Results.stream().allMatch(m -> m.getPath().contains("math201")));

        assertEquals(3, pdfResults.size());
        assertTrue(pdfResults.stream().allMatch(m -> m.getPath().contains(".pdf")));
    }

    @Test
    void findByPathContaining_WithNoMatches_ShouldReturnEmptyList() {
        // When
        List<Material> results = materialRepository.findByPathContaining("nonexistent");

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void findByProfessorId_ShouldReturnMaterialsForSpecificProfessor() {
        // When
        List<Material> prof1Materials = materialRepository.findByProfessorId(testProfessor1.getId());
        List<Material> prof2Materials = materialRepository.findByProfessorId(testProfessor2.getId());

        // Then
        assertEquals(2, prof1Materials.size());
        assertEquals(1, prof2Materials.size());

        assertTrue(prof1Materials.stream().allMatch(m -> 
            m.getCourse().getProfessor().getId().equals(testProfessor1.getId())));
        assertTrue(prof2Materials.stream().allMatch(m -> 
            m.getCourse().getProfessor().getId().equals(testProfessor2.getId())));
    }

    @Test
    void findByPath_ShouldReturnSpecificMaterial() {
        // When
        Material foundMaterial = materialRepository.findByPath("/courses/cs101/java-guide.pdf");

        // Then
        assertNotNull(foundMaterial);
        assertEquals("Java Programming Guide", foundMaterial.getName());
        assertEquals("/courses/cs101/java-guide.pdf", foundMaterial.getPath());
        assertEquals(testCourse1.getId(), foundMaterial.getCourse().getId());
    }

    @Test
    void findByPath_WithNonExistentPath_ShouldReturnNull() {
        // When
        Material foundMaterial = materialRepository.findByPath("/nonexistent/path.pdf");

        // Then
        assertNull(foundMaterial);
    }

    @Test
    void findMaterialCountsByCourseIds_ShouldReturnCorrectCounts() {
        // Given
        Course course3 = new Course();
        course3.setTitle("Physics 301");
        course3.setDescription("Advanced Physics");
        course3.setProfessor(testProfessor1);
        course3 = entityManager.persistAndFlush(course3);

        createMaterial("Physics Lab Manual", "/courses/physics301/lab-manual.pdf", course3);
        createMaterial("Physics Theory", "/courses/physics301/theory.pdf", course3);
        createMaterial("Physics Problems", "/courses/physics301/problems.pdf", course3);
        entityManager.flush();

        // When
        List<Object[]> counts = materialRepository.findMaterialCountsByCourseIds(
            List.of(testCourse1.getId(), testCourse2.getId(), course3.getId()));

        // Then
        assertEquals(3, counts.size());
        
        Map<Long, Long> countMap = counts.stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
            ));
        
        assertEquals(2L, countMap.get(testCourse1.getId()));
        assertEquals(1L, countMap.get(testCourse2.getId()));
        assertEquals(3L, countMap.get(course3.getId()));
    }

    @Test
    void getMaterialCountsByCourseIds_DefaultMethod_ShouldReturnCorrectMap() {
        // Given
        Course course3 = new Course();
        course3.setTitle("Biology 401");
        course3.setDescription("Advanced Biology");
        course3.setProfessor(testProfessor2);
        course3 = entityManager.persistAndFlush(course3);

        createMaterial("Biology Textbook", "/courses/bio401/textbook.pdf", course3);
        createMaterial("Biology Lab Guide", "/courses/bio401/lab-guide.pdf", course3);
        entityManager.flush();

        // When
        Map<Long, Long> countMap = materialRepository.getMaterialCountsByCourseIds(
            List.of(testCourse1.getId(), testCourse2.getId(), course3.getId()));

        // Then
        assertEquals(3, countMap.size());
        assertEquals(2L, countMap.get(testCourse1.getId()));
        assertEquals(1L, countMap.get(testCourse2.getId()));
        assertEquals(2L, countMap.get(course3.getId()));
    }

    @Test
    void getMaterialCountsByCourseIds_WithEmptyList_ShouldReturnEmptyMap() {
        // When
        Map<Long, Long> countMap = materialRepository.getMaterialCountsByCourseIds(List.of());

        // Then
        assertTrue(countMap.isEmpty());
    }

    @Test
    void getMaterialCountsByCourseIds_WithNonExistentCourseIds_ShouldReturnEmptyMap() {
        // When
        Map<Long, Long> countMap = materialRepository.getMaterialCountsByCourseIds(List.of(999L, 998L));

        // Then
        assertTrue(countMap.isEmpty());
    }

    @Test
    void findByCourseId_WithNonExistentCourse_ShouldReturnEmptyList() {
        // When
        List<Material> materials = materialRepository.findByCourseId(999L);

        // Then
        assertTrue(materials.isEmpty());
    }

    @Test
    void findByProfessorId_WithNonExistentProfessor_ShouldReturnEmptyList() {
        // When
        List<Material> materials = materialRepository.findByProfessorId(999);

        // Then
        assertTrue(materials.isEmpty());
    }

    @Test
    void repositoryOperations_ShouldMaintainDataIntegrity() {
        // Given
        Material newMaterial = new Material();
        newMaterial.setName("New Material");
        newMaterial.setPath("/test/new-material.pdf");
        newMaterial.setCourse(testCourse1);

        // When - Create
        Material savedMaterial = materialRepository.save(newMaterial);

        // Then - Verify creation
        assertNotNull(savedMaterial.getId());
        assertEquals("New Material", savedMaterial.getName());

        // When - Update
        savedMaterial.setName("Updated Material");
        Material updatedMaterial = materialRepository.save(savedMaterial);

        // Then - Verify update
        assertEquals("Updated Material", updatedMaterial.getName());

        // When - Find by new name
        List<Material> foundMaterials = materialRepository.findByNameContaining("Updated");

        // Then - Verify search works with updated data
        assertEquals(1, foundMaterials.size());
        assertEquals("Updated Material", foundMaterials.get(0).getName());

        // When - Delete
        materialRepository.delete(savedMaterial);
        boolean exists = materialRepository.existsById(savedMaterial.getId());

        // Then - Verify deletion
        assertFalse(exists);
    }

    @Test
    void caseInsensitiveSearch_ShouldWork() {
        // When
        List<Material> upperCaseResults = materialRepository.findByNameContaining("JAVA");
        List<Material> lowerCaseResults = materialRepository.findByNameContaining("java");
        List<Material> mixedCaseResults = materialRepository.findByNameContaining("JaVa");

        // Then
        assertEquals(1, upperCaseResults.size());
        assertEquals(1, lowerCaseResults.size());
        assertEquals(1, mixedCaseResults.size());

        assertEquals("Java Programming Guide", upperCaseResults.get(0).getName());
        assertEquals("Java Programming Guide", lowerCaseResults.get(0).getName());
        assertEquals("Java Programming Guide", mixedCaseResults.get(0).getName());
    }

    @Test
    void findAll_ShouldReturnAllMaterials() {
        // When
        List<Material> allMaterials = materialRepository.findAll();

        // Then
        assertEquals(3, allMaterials.size());

        List<String> materialNames = allMaterials.stream()
            .map(Material::getName)
            .sorted()
            .toList();

        assertEquals(List.of("Calculus Textbook", "Data Structures Tutorial", "Java Programming Guide"), 
                    materialNames);
    }

    @Test
    void complexQueries_ShouldWorkCorrectly() {
        // Given - Add more materials for complex testing
        Course course3 = new Course();
        course3.setTitle("Advanced Java Programming");
        course3.setDescription("Advanced Programming Course");
        course3.setProfessor(testProfessor1);
        course3 = entityManager.persistAndFlush(course3);

        createMaterial("Advanced Java Concepts", "/courses/java-advanced/concepts.pdf", course3);
        createMaterial("Java Design Patterns", "/courses/java-advanced/patterns.pdf", course3);
        entityManager.flush();

        // When - Test complex queries
        List<Material> javaResults = materialRepository.findByNameContaining("Java");
        List<Material> prof1Results = materialRepository.findByProfessorId(testProfessor1.getId());
        List<Material> courseResults = materialRepository.findByPathContaining("java");

        // Then
        assertEquals(3, javaResults.size()); // All Java-related materials
        assertEquals(4, prof1Results.size()); // All materials from professor 1
        assertEquals(2, courseResults.size()); // Materials in java-related paths
    }

    @Test
    void testMaterialEntityRelationships() {
        // When
        Material material = materialRepository.findByPath("/courses/cs101/java-guide.pdf");

        // Then - Verify relationships are properly loaded
        assertNotNull(material);
        assertNotNull(material.getCourse());
        assertNotNull(material.getCourse().getProfessor());

        assertEquals("Computer Science 101", material.getCourse().getTitle());
        assertEquals("prof1@test.com", material.getCourse().getProfessor().getEmail());
    }
} 