package com.backend.repository;

import com.backend.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT m FROM Material m WHERE m.course.id = :courseId")
    List<Material> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT m FROM Material m WHERE m.name LIKE %:name%")
    List<Material> findByNameContaining(@Param("name") String name);

    @Query("SELECT m FROM Material m WHERE m.path LIKE %:path%")
    List<Material> findByPathContaining(@Param("path") String path);

    @Query("SELECT m FROM Material m WHERE m.course.professor.id = :professorId")
    List<Material> findByProfessorId(@Param("professorId") Integer professorId);

    @Query("SELECT m FROM Material m WHERE m.path = :path")
    Material findByPath(@Param("path") String path);

    /**
     * Get material counts for multiple courses in batch to avoid N+1 query problem
     */
    @Query("SELECT m.course.id as courseId, COUNT(m) as count " +
            "FROM Material m " +
            "WHERE m.course.id IN :courseIds " +
            "GROUP BY m.course.id")
    List<Object[]> findMaterialCountsByCourseIds(@Param("courseIds") List<Long> courseIds);

    /**
     * Helper method to convert the query result to a Map
     */
    default Map<Long, Long> getMaterialCountsByCourseIds(List<Long> courseIds) {
        return findMaterialCountsByCourseIds(courseIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],    // courseId
                        row -> (Long) row[1]     // count
                ));
    }

}
