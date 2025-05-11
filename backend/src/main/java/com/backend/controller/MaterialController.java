package com.backend.controller;

import com.backend.model.Course;
import com.backend.model.Material;
import com.backend.service.CourseService;
import com.backend.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/Material")
public class MaterialController {

    private final CourseService courseService;
    private final MaterialService materialService;

    @Autowired
    public MaterialController(MaterialService materialService, CourseService courseService) {
        this.materialService = materialService;
        this.courseService = courseService;
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
    public ResponseEntity<Material> create(@RequestBody Material material) {
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

    @RequestMapping("/{courseName}/pdf/{index}")
    public ResponseEntity<Resource> getPDF(@PathVariable String courseName, @PathVariable Long index, @RequestParam(defaultValue = "1") int page) {
        try {
            Course course = courseService.findByName(courseName);

            if (course == null || course.getMaterials().size() <= index || index < 0) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Material> optionalMaterial = course.getMaterials().stream().filter(m -> Objects.equals(m.getId(), index)).findFirst();
            Material material = null;
            if (optionalMaterial.isPresent()) {
                material = optionalMaterial.get();
            } else {
                return ResponseEntity.notFound().build();
            }

            String s3Path = material.getPath();

            InputStream s3InputStream = null;
            if (s3InputStream == null) {
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(s3InputStream);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + material.getName() + "\"")
                    .header("page" + page)
                    .contentType(MediaType.APPLICATION_PDF)
                     .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

}
