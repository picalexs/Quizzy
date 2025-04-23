package com.backend.repository;

import com.backend.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {

    @Query("SELECT t FROM Test t WHERE t.professor.id = :professorId")
    List<Test> findByProfessorId(@Param("professorId") Integer professorId);

    @Query("SELECT t FROM Test t WHERE t.course.id = :courseId")
    List<Test> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT t FROM Test t WHERE t.date >= :startDate AND t.date <= :endDate")
    List<Test> findByDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT t FROM Test t WHERE t.date >= CURRENT_DATE ORDER BY t.date ASC")
    List<Test> findUpcomingTests();

    @Query("SELECT t FROM Test t WHERE t.course.id IN (SELECT e.course.id FROM Enrollment e WHERE e.user.id = :studentId)")
    List<Test> findTestsForStudentEnrollments(@Param("studentId") Integer studentId);

    @Query("SELECT t FROM Test t WHERE t.date = :date")
    List<Test> findTestsByExactDate(@Param("date") Date date);

    @Query("SELECT COUNT(t) FROM Test t WHERE t.professor.id = :professorId")
    Long countTestsByProfessor(@Param("professorId") int professorId);

    @Query("SELECT COUNT(t) FROM Test t WHERE t.course.id = :courseId")
    Long countTestsByCourse(@Param("courseId") int courseId);

    @Query("SELECT COUNT(t) FROM Test t WHERE t.date >= :startDate AND t.date <= :endDate")
    Long countTestsByDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COUNT(t) FROM Test t WHERE t.date >= CURRENT_DATE ORDER BY t.date ASC")
    Long countUpcomingTests();

    @Query("SELECT COUNT(t) FROM Test t WHERE t.course.id IN (SELECT e.course.id FROM Enrollment e WHERE e.user.id = :studentId)")
    Long countTestsForStudentEnrollments(@Param("studentId") Integer studentId);

    @Query("SELECT COUNT(t) FROM Test t WHERE t.course.id IN " +
            "(SELECT e.course.id FROM Enrollment e WHERE e.user.id = :studentId)")
    Long countTestsForStudent(@Param("studentId") Integer studentId);

    @Query("SELECT t FROM Test t WHERE FUNCTION('MONTH', t.date) = :month AND FUNCTION('YEAR', t.date) = :year")
    List<Test> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT t FROM Test t WHERE FUNCTION('MONTH', t.date) = :month")
    List<Test> findByMonth(@Param("month") int month);

    @Query("SELECT t FROM Test t WHERE FUNCTION('YEAR', t.date) = :year")
    List<Test> findByYear(@Param("year") int year);

    @Query("SELECT t FROM Test t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Test> findByTitle(@Param("keyword") String keyword);

    @Query("SELECT t FROM Test t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Test> findByDescription(@Param("keyword") String keyword);
}