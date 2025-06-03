package com.backend.repository;

import com.backend.model.AnswerFC;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerFCRepository extends JpaRepository<AnswerFC, Long> {

    @Query("SELECT a FROM AnswerFC a WHERE a.flashcard.id = :flashcardId")
    List<AnswerFC> findByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Query("SELECT a FROM AnswerFC a WHERE a.isCorrect = true AND a.flashcard.id = :flashcardId")
    List<AnswerFC> findCorrectAnswersByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AnswerFC a WHERE a.flashcard.id = :flashcardId")
    void deleteByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AnswerFC a WHERE a.flashcard.id IN :flashcardIds")
    void deleteByFlashcardIds(@Param("flashcardIds") List<Long> flashcardIds);
}