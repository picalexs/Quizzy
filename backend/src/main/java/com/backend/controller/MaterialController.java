package com.backend.controller;

import com.backend.model.Course;
import com.backend.repository.FlashcardRepository;
import com.backend.utils.FlashcardBatchGenerator;
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
import org.springframework.transaction.annotation.Transactional;
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
    private FlashcardBatchGenerator flashcardBatchGenerator;

    @Autowired
    private FlashcardRepository flashcardRepository;

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

            // Step 1.1: Generate new course path
            String newFileName = newCourseFile.getOriginalFilename();
            String directoryPath = oldCoursePath.substring(0, oldCoursePath.lastIndexOf('/') + 1);
            String newCoursePath = directoryPath + newFileName;

            System.out.println("Step 1: Starting course replacement process");
            System.out.println("Old course path: " + oldCoursePath);
            System.out.println("New course path: " + newCoursePath);
            System.out.println("New file name: " + newFileName);

            // Step 1.5: Find and store old material info before operations
            System.out.println("🔍 DEBUG: Searching for material with exact path: '" + oldCoursePath + "'");

            Material oldMaterial = findMaterialByPathFlexible(oldCoursePath);
            if (oldMaterial == null) {
                System.out.println("⚠️  DEBUG: Exact path not found, trying partial match");
                List<Material> oldMaterials = materialService.findByPathContaining(oldCoursePath);
                System.out.println("🔍 DEBUG: Found " + oldMaterials.size() + " materials with partial path match");

                for (int i = 0; i < oldMaterials.size(); i++) {
                    Material mat = oldMaterials.get(i);
                    System.out.println("   Material " + (i+1) + ": ID=" + mat.getId() +
                            ", Name='" + mat.getName() + "', Path='" + mat.getPath() + "'");
                }

                if (!oldMaterials.isEmpty()) {
                    oldMaterial = oldMaterials.get(0);
                    System.out.println("✅ DEBUG: Using first matched material: " + oldMaterial.getPath());
                }
            } else {
                System.out.println("✅ DEBUG: Found exact material match: " + oldMaterial.getPath());
            }

            Long courseId = null;
            String oldMaterialName = null;
            if (oldMaterial != null) {
                courseId = oldMaterial.getCourse().getId();
                oldMaterialName = oldMaterial.getName();
                System.out.println("Step 1.5: Found old material - ID: " + oldMaterial.getId() +
                        ", Course ID: " + courseId + ", Name: " + oldMaterialName);
            } else {
                System.out.println("Step 1.5: WARNING - Old material not found in database for path: " + oldCoursePath);
            }

            // Step 2: Check if old course exists and delete it from S3
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

            // Step 4: Upload new PDF to S3 with the new path
            Resource pdfResource = new FileSystemResource(tempPdfFile.toFile());
            awsS3Service.uploadPdfToS3(bucketName, newCoursePath, pdfResource);
            System.out.println("Step 4: Successfully uploaded new course to S3");

            // **FIXED STEP 4.5: Update database IMMEDIATELY after S3 upload with separate transaction**
            Material newMaterial = updateMaterialInDatabase(oldMaterial, newCoursePath, newFileName, courseId);
            System.out.println("Step 4.5: Database updated successfully - Material ID: " +
                    (newMaterial != null ? newMaterial.getId() : "null"));

            // Initialize variables for flashcard generation results
            int importedFlashcards = 0;
            String flashcardFilePath = null;
            String flashcardStatus = "not_generated";
            String flashcardError = null;

            try {
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

                // Call PDF to text conversion
                ResponseEntity<String> conversionResult = pdfController.convertPdfToImage(newCoursePath, textOutputPath);

                if (conversionResult.getStatusCode() != HttpStatus.OK) {
                    throw new RuntimeException("PDF to text conversion failed: " + conversionResult.getBody());
                }
                System.out.println("Step 5: PDF successfully converted to text at: " + fullTextPath);

                // Step 6: Generate flashcards from the text file with retry logic
                String relativeTextPath = courseName + "/" + courseName + ".txt";
                flashcardFilePath = fullTextPath.replace(".txt", "_flashcards.txt");

                // Retry configuration
                int maxRetries = 3;
                int retryDelay = 2000; // 2 seconds between retries
                boolean flashcardSuccess = false;

                for (int attempt = 1; attempt <= maxRetries && !flashcardSuccess; attempt++) {
                    System.out.println("Step 6 - Attempt " + attempt + "/" + maxRetries + ": Starting flashcard generation process");

                    try {
                        // Step 6a: Generate the flashcards using FlashcardBatchGenerator
                        System.out.println("Step 6a (Attempt " + attempt + "): Generating flashcards using FlashcardBatchGenerator");
                        System.out.println("🔍 Sending path to FlashcardBatchGenerator: " + relativeTextPath);

                        flashcardBatchGenerator.processTextFile(relativeTextPath);
                        System.out.println("✅ Step 6a (Attempt " + attempt + "): Successfully generated flashcards file");

                        // Step 6b: Delete old flashcards before importing new ones
                        if (newMaterial != null && newMaterial.getId() != null) {
                            System.out.println("Step 6b (Attempt " + attempt + "): Deleting old flashcards for material ID: " + newMaterial.getId());
                            try {
                                deleteOldFlashcards(newMaterial.getId());
                                System.out.println("✅ Successfully deleted old flashcards for material ID: " + newMaterial.getId());
                            } catch (Exception e) {
                                System.err.println("⚠️ Warning: Failed to delete old flashcards: " + e.getMessage());
                                // Continue with import even if deletion fails
                            }
                        } else {
                            System.out.println("⚠️ Warning: No material ID available for flashcard deletion");
                        }

                        // Step 6c: Import the generated flashcards
                        System.out.println("Step 6c (Attempt " + attempt + "): Importing new flashcards from generated file");
                        ResponseEntity<Map<String, Object>> flashcardResult = flashcardController.generateFlashcardsFromFile(
                                flashcardFilePath, userId);

                        if (flashcardResult.getStatusCode() == HttpStatus.OK) {
                            Map<String, Object> flashcardResponse = flashcardResult.getBody();

                            // Check if the response indicates success and contains valid data
                            if (flashcardResponse != null &&
                                    flashcardResponse.get("importedCount") != null &&
                                    (Integer) flashcardResponse.get("importedCount") > 0) {

                                importedFlashcards = (Integer) flashcardResponse.get("importedCount");
                                flashcardStatus = "success";
                                flashcardSuccess = true;
                                System.out.println("✅ Step 6 (Attempt " + attempt + "): Successfully generated and imported " +
                                        importedFlashcards + " new flashcards");
                            } else {
                                throw new RuntimeException("Import returned OK but with invalid or empty data: " + flashcardResponse);
                            }
                        } else {
                            Map<String, Object> flashcardErrorResponse = flashcardResult.getBody();
                            String attemptError = (flashcardErrorResponse != null ?
                                    (String) flashcardErrorResponse.get("message") : "Unknown flashcard error");
                            throw new RuntimeException("Flashcard import failed with status " +
                                    flashcardResult.getStatusCode() + ": " + attemptError);
                        }

                    } catch (Exception e) {
                        System.err.println("❌ Step 6 (Attempt " + attempt + "/" + maxRetries + "): Flashcard generation failed: " + e.getMessage());

                        if (attempt == maxRetries) {
                            // Last attempt failed
                            flashcardError = "All " + maxRetries + " attempts failed. Last error: " + e.getMessage();
                            flashcardStatus = "failed";
                            System.err.println("💥 Step 6: All flashcard generation attempts exhausted. Final error: " + flashcardError);
                        } else {
                            // Wait before next attempt
                            System.out.println("⏳ Waiting " + (retryDelay/1000) + " seconds before retry...");
                            try {
                                Thread.sleep(retryDelay);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Retry interrupted", ie);
                            }
                        }
                    }
                }

                if (flashcardSuccess) {
                    System.out.println("🎉 Flashcard generation completed successfully after " +
                            (flashcardSuccess ? "attempt" : "attempts"));
                }

            } catch (Exception e) {
                flashcardError = e.getMessage();
                flashcardStatus = "failed";
                System.err.println("Step 5-6: Flashcard generation process failed: " + e.getMessage());
                e.printStackTrace();
                // Continue without throwing - database is already updated
            }

            // Step 7: Prepare response (always success since DB and S3 operations completed)
            response.put("status", "success");
            response.put("message", "Course replaced successfully in S3 and database");
            response.put("oldCoursePath", oldCoursePath);
            response.put("newCoursePath", newCoursePath);
            response.put("courseName", extractCourseNameFromPath(newCoursePath));
            response.put("oldMaterialId", oldMaterial != null ? oldMaterial.getId() : null);
            response.put("newMaterialId", newMaterial != null ? newMaterial.getId() : null);

            // Flashcard generation results (may be partial/failed)
            response.put("flashcardStatus", flashcardStatus);
            response.put("importedFlashcards", importedFlashcards);
            if (flashcardError != null) {
                response.put("flashcardError", flashcardError);
            }
            if (flashcardFilePath != null) {
                response.put("flashcardFilePath", flashcardFilePath);
            }
            if (textOutputPath != null) {
                response.put("textFilePath", Paths.get(System.getProperty("user.dir"), textOutputPath).toString());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Course replacement failed: " + e.getMessage());
            e.printStackTrace();

            response.put("status", "error");
            response.put("message", "Course replacement failed: " + e.getMessage());
            response.put("oldCoursePath", oldCoursePath);

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

    /**
     * Updates the material in database immediately after S3 operations
     * Uses separate transaction to ensure immediate commit
     */
    @Transactional
    protected Material updateMaterialInDatabase(Material oldMaterial, String newCoursePath,
                                                String newFileName, Long courseId) {
        try {
            if (oldMaterial != null) {
                // Update the existing material with new path and name
                oldMaterial.setPath(newCoursePath);
                oldMaterial.setName(newFileName);

                Material updatedMaterial = materialService.updateMaterial(oldMaterial.getId(), oldMaterial);
                System.out.println("✅ Successfully updated existing material - ID: " + updatedMaterial.getId());
                return updatedMaterial;

            } else if (courseId != null) {
                // Create new material if old one wasn't found
                Optional<Course> courseOpt = courseService.getCourseById(courseId);
                if (courseOpt.isPresent()) {
                    MaterialDTO newMaterialDTO = new MaterialDTO();
                    newMaterialDTO.setName(newFileName);
                    newMaterialDTO.setPath(newCoursePath);
                    newMaterialDTO.setCourseId(courseId);

                    Material newMaterial = materialService.createMaterial(newMaterialDTO);
                    System.out.println("✅ Successfully created new material - ID: " + newMaterial.getId());
                    return newMaterial;
                } else {
                    System.err.println("❌ Course not found with ID: " + courseId);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating material in database: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback of this specific transaction
        }

        return null;
    }

    /**
     * Deletes old flashcards for a specific material before importing new ones
     */
    @Transactional
    protected void deleteOldFlashcards(Long materialId) {
        try {
            // Assuming you have FlashcardRepository injected
            // You'll need to add this field at the top of your class:
            // @Autowired
            // private FlashcardRepository flashcardRepository;

            flashcardRepository.deleteByMaterialId(materialId);
            System.out.println("✅ Deleted old flashcards for material ID: " + materialId);
        } catch (Exception e) {
            System.err.println("❌ Error deleting old flashcards for material ID " + materialId + ": " + e.getMessage());
            throw e;
        }
    }

    private Material findMaterialByPathFlexible(String targetPath) {
        System.out.println("🔍 Flexible search for path: '" + targetPath + "'");

        // 1. Exact match
        Material exact = materialService.findByPath(targetPath);
        if (exact != null) {
            System.out.println("✅ Found exact match: " + exact.getPath());
            return exact;
        }

        // 2. Partial match
        List<Material> partialMatches = materialService.findByPathContaining(targetPath);
        if (!partialMatches.isEmpty()) {
            System.out.println("✅ Found " + partialMatches.size() + " partial matches");
            return partialMatches.get(0);
        }

        // 3. Try without "cursuri/" prefix if it exists
        if (targetPath.startsWith("cursuri/")) {
            String withoutPrefix = targetPath.substring(8); // Remove "cursuri/"
            List<Material> noPrefixMatches = materialService.findByPathContaining(withoutPrefix);
            if (!noPrefixMatches.isEmpty()) {
                System.out.println("✅ Found match without 'cursuri/' prefix: " + noPrefixMatches.get(0).getPath());
                return noPrefixMatches.get(0);
            }
        }

        // 4. Try similarity search (extract filename and search by it)
        String filename = targetPath.substring(targetPath.lastIndexOf('/') + 1);
        List<Material> filenameMatches = materialService.findByNameContaining(filename);
        if (!filenameMatches.isEmpty()) {
            System.out.println("✅ Found match by filename '" + filename + "': " + filenameMatches.get(0).getPath());
            return filenameMatches.get(0);
        }

        System.out.println("❌ No material found for path: " + targetPath);
        return null;
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