package com.backend.repository;

import com.backend.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @Query("SELECT f FROM Flashcard f WHERE f.user.id = :userId")
    List<Flashcard> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.material.id = :materialId")
    List<Flashcard> findByMaterialId(@Param("materialId") Long materialId);

    @Query("SELECT f FROM Flashcard f WHERE f.lastStudiedAt <= :date AND f.user.id = :userId")
    List<Flashcard> findDueFlashcards(@Param("date") Date date, @Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.level = :level AND f.user.id = :userId")
    List<Flashcard> findByLevelAndUserId(@Param("level") Integer level, @Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.material.course.id = :courseId AND f.user.id = :userId")
    List<Flashcard> findByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.questionType = :type AND f.user.id = :userId")
    List<Flashcard> findByQuestionTypeAndUserId(@Param("type") String type, @Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.question = :questionText")
    Flashcard findByQuestion(@Param("questionText") String questionText);

    @Query("SELECT f FROM Flashcard f WHERE f.pageIndex = :pageIndex")
    List<Flashcard> findByPageIndex(@Param("pageIndex") Integer pageIndex);

    @Query("SELECT f FROM Flashcard f WHERE f.pageIndex = :pageIndex AND f.user.id = :userId")
    List<Flashcard> findByPageIndexAndUserId(
            @Param("pageIndex") Integer pageIndex,
            @Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.pageIndex = :pageIndex AND f.material.id = :materialId")
    List<Flashcard> findByPageIndexAndMaterialId(
            @Param("pageIndex") Integer pageIndex,
            @Param("materialId") Long materialId);

    @Query("SELECT COUNT(f) FROM Flashcard f WHERE f.material.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT f FROM Flashcard f WHERE f.material.course.id = :courseId")
    List<Flashcard> findAllByCourseId(@Param("courseId") Long courseId);
    /**
     * Get flashcard counts for multiple courses in batch to avoid N+1 query problem
     */
    @Query("SELECT f.material.course.id as courseId, COUNT(f) as count " +
            "FROM Flashcard f " +
            "WHERE f.material.course.id IN :courseIds " +
            "GROUP BY f.material.course.id")
    List<Object[]> findFlashcardCountsByCourseIds(@Param("courseIds") List<Long> courseIds);

    /**
     * Helper method to convert the query result to a Map
     */
    default Map<Long, Long> getFlashcardCountsByCourseIds(List<Long> courseIds) {
        return findFlashcardCountsByCourseIds(courseIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],    // courseId
                        row -> (Long) row[1]     // count
                ));
    }

    @Query("SELECT f FROM Flashcard f")
    List<Flashcard> findAllFlashcards();

    @Query("SELECT f FROM Flashcard f WHERE f.user.id = :userId")
    List<Flashcard> findAllFlashcardsByUserId(@Param("userId") Integer userId);

    @Query("SELECT f FROM Flashcard f WHERE f.material.course.id = :courseId")
    List<Flashcard> findAllFlashcardsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT f FROM Flashcard f WHERE f.material.id = :materialId")
    List<Flashcard> findAllFlashcardsByMaterialId(@Param("materialId") Long materialId);

    @Query("SELECT f FROM Flashcard f WHERE f.material.id = :materialId AND f.user.id = :userId")
    List<Flashcard> findAllFlashcardsByMaterialIdAndUserId(
            @Param("materialId") Long materialId,
            @Param("userId") Integer userId);
}