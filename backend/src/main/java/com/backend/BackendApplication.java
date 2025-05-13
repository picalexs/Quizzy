package com.backend;

import com.backend.utils.FlashcardImport;
import com.backend.utils.PDFtoText;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        //String courseName = "graph";
       // String courseNumber = "test";
        //String text = PDFtoText.generateTextFromPDF(courseName, courseNumber);
        //System.out.println("Extracted text: " + text);
        //PDFtoText.generateTextFromPDF();

        FlashcardImport flashcardImport = context.getBean(FlashcardImport.class);
        String folderPath = "backend/courses/graph";

        try {
            int importedCount = flashcardImport.importFlashcardsFromDirectory(folderPath, 1);
            System.out.println("Total flashcard-uri importate: " + importedCount);
        } catch (Exception e) {
            System.err.println("Eroare la importul flashcard-urilor: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
