package com.backend.controller;


import com.backend.model.Course;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.backend.dto.MaterialDTO;
import org.springframework.beans.factory.annotation.Value;
import com.backend.model.Material;
import com.backend.service.AWSS3Service;
import com.backend.service.CourseService;
import com.backend.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/Material")
public class MaterialController {


    private static final Logger logger = LoggerFactory.getLogger(MaterialController.class);
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    private final CourseService courseService;
    private final MaterialService materialService;

    @Autowired
    private AWSS3Service awsS3Service;

    @Autowired
    private PDFController pdfController;

    @Autowired
    private FlashcardController flashcardController;

    @Autowired
    public MaterialController(MaterialService materialService, CourseService courseService, AWSS3Service awsS3Service) {
        this.materialService = materialService;
        this.courseService = courseService;
        this.awsS3Service = awsS3Service;
    }

    @GetMapping
    public ResponseEntity<List<Material>> getAll() {
        List<Material> materials = materialService.getAllMaterials();
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> getById(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        return ResponseEntity.ok(material);
    }

    @PostMapping
    public ResponseEntity<Material> create(@RequestBody MaterialDTO material) {
        Material created = materialService.createMaterial(material);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> update(@PathVariable Long id, @RequestBody Material material) {
        Material updated = materialService.updateMaterial(id, material);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Material>> getByCourse(@PathVariable Long courseId) {
        List<Material> materials = materialService.findByCourseId(courseId);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Material>> searchByName(@RequestParam String name) {
        List<Material> materials = materialService.findByNameContaining(name);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/search/path")
    public ResponseEntity<List<Material>> searchByPath(@RequestParam String path) {
        List<Material> materials = materialService.findByPathContaining(path);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<Material>> getByProfessor(@PathVariable Integer professorId) {
        List<Material> materials = materialService.findByProfessorId(professorId);
        return ResponseEntity.ok(materials);
    }

    @GetMapping("/path/**")
    public ResponseEntity<Resource> getPDF(HttpServletRequest request) {
        try {

            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String remainingPath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, fullPath);

            List<String> path = Arrays.stream(remainingPath.split("/")).toList();

            if (path.get(0) == null || !path.get(0).equals("cursuri")) {
                logger.error("Invalid path. First word should be cursuri");
                return ResponseEntity.badRequest().body(null);
            }

            String courseTitle = path.get(1);
            if (courseTitle == null) {
                logger.error("Invalid path. Second word should be a course title!");
                return ResponseEntity.badRequest().body(null);
            }

            String courseName = path.get(path.size() - 1);
            if (courseName == null || !courseName.endsWith(".pdf")) {
                logger.error("Invalid path. Last word should be a course name with \".pdf\" at the end!");
                return ResponseEntity.badRequest().body(null);
            }

            logger.info("---Successful---{} and {}",courseName,courseTitle);

            Course course = courseService.findByTitle(courseTitle);
            if(course == null) {
                logger.error("Course '{}' not found", courseTitle);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Optional<Material> materialOptional = course.getMaterials().stream().filter( m  ->m.getName().equals(courseName)).findFirst();
            Material material = null;
            if(materialOptional.isPresent()) {
                material = materialOptional.get();
            } else{
                logger.error("Material couldn't be found by name {}, for {}!",courseName,courseTitle);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            String s3Path = material.getPath();
            if (s3Path == null || s3Path.isEmpty()) {
                logger.error("Material with name '{}' for course '{}' has an invalid S3 path", courseName, courseTitle);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            logger.debug("Fetching PDF from S3 bucket '{}' with path '{}'", bucketName, s3Path);
            Resource pdfResource = awsS3Service.getPdfResourceFromS3(bucketName, s3Path);
            if (pdfResource == null) {
                logger.warn("PDF file not found in S3 for path '{}'", s3Path);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            logger.info("Successfully retrieved PDF for course '{}' with name '{}'", courseTitle, courseName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfResource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/replace-course")
    public ResponseEntity<Map<String, Object>> replaceCourseInS3(
            @RequestParam String oldCoursePath,
            @RequestParam MultipartFile newCourseFile,
            @RequestParam String newCoursePath,
            @RequestParam(required = false, defaultValue = "1") Integer userId) {

        Map<String, Object> response = new HashMap<>();
        String bucketName = "quizzy-s3-bucket";
        String tempPdfPath = null;
        String textOutputPath = null;

        try {
            // Step 1: Validate input parameters
            if (oldCoursePath == null || oldCoursePath.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Old course path cannot be null or empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (newCoursePath == null || newCoursePath.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "New course path cannot be null or empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (newCourseFile == null || newCourseFile.isEmpty()) {
                response.put("status", "error");
                response.put("message", "New course file cannot be null or empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newCourseFile.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                response.put("status", "error");
                response.put("message", "New course file must be a PDF");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("Step 1: Starting course replacement process");
            System.out.println("Old course path: " + oldCoursePath);
            System.out.println("New course path: " + newCoursePath);

            // Step 2: Check if old course exists and delete it
            if (!awsS3Service.doesObjectExist(bucketName, oldCoursePath)) {
                response.put("status", "error");
                response.put("message", "Old course not found in S3 at path: " + oldCoursePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            awsS3Service.deletePdfFromS3(bucketName, oldCoursePath);
            System.out.println("Step 2: Successfully deleted old course from S3");

            // Step 3: Save new PDF to temporary location
            tempPdfPath = "/tmp/new_course_" + System.currentTimeMillis() + ".pdf";
            Path tempPdfFile = Paths.get(tempPdfPath);

            try {
                Files.copy(newCourseFile.getInputStream(), tempPdfFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Step 3: New PDF saved to temporary file: " + tempPdfPath);
            } catch (IOException e) {
                System.err.println("Step 3 FAILED: Could not save PDF to temp file - " + e.getMessage());
                throw e;
            }

            // Step 4: Upload new PDF to S3
            Resource pdfResource = new InputStreamResource(Files.newInputStream(tempPdfFile));
            awsS3Service.uploadPdfToS3(bucketName, newCoursePath, pdfResource);
            System.out.println("Step 4: Successfully uploaded new course to S3");

            // Step 5: Convert PDF to text
            String projectDir = System.getProperty("user.dir");
            String courseName = extractCourseNameFromPath(newCoursePath);
            textOutputPath = "courses/" + courseName + "/" + courseName + ".txt";
            String fullTextPath = Paths.get(projectDir, textOutputPath).toString();

            // Create output directory if it doesn't exist
            Path outputPath = Paths.get(fullTextPath);
            Path parentDir = outputPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
                System.out.println("Step 5a: Created directories: " + parentDir.toString());
            }

            // Call PDF to text conversion using RestTemplate or direct method call
            ResponseEntity<String> conversionResult = pdfController.convertPdfToImage(newCoursePath, textOutputPath);

            if (conversionResult.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("PDF to text conversion failed: " + conversionResult.getBody());
            }
            System.out.println("Step 5: PDF successfully converted to text at: " + fullTextPath);

            // Step 6: Generate flashcards from the text file
            String flashcardFilePath = fullTextPath.replace(".txt", "_flashcards.txt");

            ResponseEntity<Map<String, Object>> flashcardResult = flashcardController.generateFlashcardsFromFile(
                    flashcardFilePath, userId);

            if (flashcardResult.getStatusCode() != HttpStatus.OK) {
                Map<String, Object> flashcardError = flashcardResult.getBody();
                throw new RuntimeException("Flashcard generation failed: " +
                        (flashcardError != null ? flashcardError.get("message") : "Unknown error"));
            }

            Map<String, Object> flashcardResponse = flashcardResult.getBody();
            int importedFlashcards = (Integer) flashcardResponse.get("importedCount");
            System.out.println("Step 6: Successfully generated " + importedFlashcards + " flashcards");

            // Step 7: Prepare success response
            response.put("status", "success");
            response.put("message", "Course replaced successfully");
            response.put("oldCoursePath", oldCoursePath);
            response.put("newCoursePath", newCoursePath);
            response.put("textFilePath", fullTextPath);
            response.put("flashcardFilePath", flashcardFilePath);
            response.put("importedFlashcards", importedFlashcards);
            response.put("courseName", courseName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Course replacement failed: " + e.getMessage());
            e.printStackTrace();

            response.put("status", "error");
            response.put("message", "Course replacement failed: " + e.getMessage());
            response.put("oldCoursePath", oldCoursePath);
            response.put("newCoursePath", newCoursePath);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } finally {
            // Cleanup temporary files
            if (tempPdfPath != null) {
                try {
                    Path tempFile = Paths.get(tempPdfPath);
                    if (Files.exists(tempFile)) {
                        Files.delete(tempFile);
                        System.out.println("Cleanup: Deleted temporary PDF file");
                    }
                } catch (IOException e) {
                    System.err.println("Failed to delete temporary PDF file: " + tempPdfPath + " - " + e.getMessage());
                }
            }
        }
    }

    // Helper method to extract course name from S3 path
    private String extractCourseNameFromPath(String s3Path) {
        String fileName = s3Path.substring(s3Path.lastIndexOf('/') + 1);
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }
}