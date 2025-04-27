package com.backend.controller;

import com.backend.model.AnswerFC;
import com.backend.service.AnswerFCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswerFCController {

    @Autowired
    private AnswerFCService answerFCService;

    @GetMapping
    public List<AnswerFC> getAllAnswers() {
        return answerFCService.getAllAnswers();
    }

    @GetMapping("/{id}")
    public AnswerFC getAnswerById(@PathVariable Long id) {
        return answerFCService.getAnswerById(id);
    }

    @PostMapping
    public AnswerFC createAnswer(@RequestBody AnswerFC answerFC) {
        return answerFCService.createAnswer(answerFC);
    }

    @PutMapping("/{id}")
    public AnswerFC updateAnswer(@PathVariable Long id, @RequestBody AnswerFC answerFC) {
        return answerFCService.updateAnswer(id, answerFC);
    }

    @DeleteMapping("/{id}")
    public void deleteAnswer(@PathVariable Long id) {
        answerFCService.deleteAnswer(id);
    }
}
