package com.backend.controller;

import com.backend.model.Test;
import com.backend.service.TestService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Date;

@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public ResponseEntity<Collection<Test>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }

    @PostMapping
    public ResponseEntity<Test> createTest(@RequestBody Test test) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.createTest(test));
    }

    @PostMapping("/save")
    public ResponseEntity<Test> saveTest(@RequestBody Test test) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.saveTest(test));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Test> updateTest(@PathVariable Long id, @RequestBody Test test) {
        return ResponseEntity.ok(testService.updateTest(id, test));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestById(@PathVariable Long id) {
        testService.deleteTestById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<Collection<Test>> getTestsByProfessorId(@PathVariable Integer professorId) {
        return ResponseEntity.ok(testService.findTestsByProfId(professorId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Collection<Test>> getTestsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(testService.findTestsByCourseId(courseId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Collection<Test>> getTestsByStudentId(@PathVariable Integer studentId) {
        return ResponseEntity.ok(testService.findTestsForStudentEnrollments(studentId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Collection<Test>> getUpcomingTests() {
        return ResponseEntity.ok(testService.findUpcomingTests());
    }

    @GetMapping("/byDate")
    public ResponseEntity<Collection<Test>> getTestsByDateRange(@RequestParam Date start, @RequestParam Date end) {
        return ResponseEntity.ok(testService.findByDateBetween(start, end));
    }

    @GetMapping("/byTitle")
    public ResponseEntity<Collection<Test>> getTestsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(testService.findByTitle(title));
    }

    @GetMapping("/byDescription")
    public ResponseEntity<Collection<Test>> getTestsByDescription(@RequestParam String description) {
        return ResponseEntity.ok(testService.findByDescription(description));
    }

    @GetMapping("/byMonth")
    public ResponseEntity<Collection<Test>> getTestsByMonth(@RequestParam Integer month) {
        return ResponseEntity.ok(testService.findByMonth(month));
    }

    @GetMapping("/byYear")
    public ResponseEntity<Collection<Test>> getTestsByYear(@RequestParam Integer year) {
        return ResponseEntity.ok(testService.findByYear(year));
    }

    @GetMapping("/byExactDate")
    public ResponseEntity<Collection<Test>> getTestsByExactDate(@RequestParam Date date) {
        return ResponseEntity.ok(testService.findTestsByExactDate(date));
    }

    @GetMapping("/byMonthYear")
    public ResponseEntity<Collection<Test>> getTestsByMonthAndYear(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(testService.findByMonthAndYear(month, year));
    }

    @GetMapping("/countByCourse/{courseId}")
    public ResponseEntity<Long> countTestsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(testService.countTestsByCourse(courseId));
    }

    @GetMapping("/countByProfessor/{professorId}")
    public ResponseEntity<Long> countTestsByProfessor(@PathVariable Integer professorId) {
        return ResponseEntity.ok(testService.countTestsByProfessor(professorId));
    }

    @GetMapping("/countUpcoming")
    public ResponseEntity<Long> countUpcomingTests() {
        return ResponseEntity.ok(testService.countUpcomingTests());
    }

    @GetMapping("/countByStudent/{studentId}")
    public ResponseEntity<Long> countTestsByStudent(@PathVariable Integer studentId) {
        return ResponseEntity.ok(testService.countTestsForStudent(studentId));
    }

    @GetMapping("/countByStudentEnrollments/{studentId}")
    public ResponseEntity<Long> countTestsByStudentEnrollments(@PathVariable Integer studentId) {
        return ResponseEntity.ok(testService.countTestsForStudentEnrollments(studentId));
    }

    @GetMapping("/countByDateRange")
    public ResponseEntity<Long> countTestsByDateRange(@RequestParam Date start, @RequestParam Date end) {
        return ResponseEntity.ok(testService.countTestsByDateBetween(start, end));
    }
}