package com.backend.service;

import com.backend.dto.GradeDTO;
import com.backend.model.Grade;
import com.backend.model.GradeId;
import com.backend.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public Grade addGrade(Grade grade) {
        return gradeRepository.save(grade);
    }

    public Optional<Grade> getGrade(Long testId, Integer userId) {
        return gradeRepository.findById(new GradeId(testId, userId));
    }

    public Optional<Grade> updateGrade(Long testId, Integer userId, Grade gradeDetails) {
        GradeId gradeId = new GradeId(testId, userId);

        Optional<Grade> grade = gradeRepository.findById(gradeId);
        Grade existingGrade;
        if (grade.isPresent()) {
            existingGrade = grade.get();
        } else {
            return Optional.empty();
        }
        if (gradeDetails.getGrade() != 0) {
            existingGrade.setGrade(gradeDetails.getGrade());
        }
        if (gradeDetails.getSubmissionDate() != null) {
            existingGrade.setSubmissionDate(gradeDetails.getSubmissionDate());
        }

        return Optional.of(gradeRepository.save(existingGrade));
    }

    public void deleteGrade(Long testId, Integer userId) {
        if (gradeRepository.existsById(new GradeId(testId, userId))) {
            gradeRepository.deleteById(new GradeId(testId, userId));
        }
    }

    public List<Grade> getGradesByUserId(Integer userId) {
        return gradeRepository.findAllByUserId(userId);
    }

    public double calculateAverageForTest(Long testId) {
        return gradeRepository.calculateAverageGrade(testId).orElse(0.0);
    }

    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }
}
