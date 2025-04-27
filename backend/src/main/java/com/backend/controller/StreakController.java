package com.backend.controller;

import com.backend.model.Streak;
import com.backend.service.StreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/streaks")
public class StreakController {

    @Autowired
    private StreakService streakService;

    @GetMapping
    public List<Streak> getAllStreaks() {
        return streakService.getAllStreaks();
    }

    @GetMapping("/{id}")
    public Optional<Streak> getStreakById(@PathVariable Long id) {
        return streakService.getStreakById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Streak> getStreaksByUserId(@PathVariable Integer userId) {
        return streakService.getStreaksByUserId(userId);
    }

    @GetMapping("/user/{userId}/date/{date}")
    public Optional<Streak> getStreakByUserIdAndDate(@PathVariable Integer userId, @PathVariable Date date) {
        return streakService.getStreakByUserIdAndDate(userId, date);
    }

    @GetMapping("/min/{minStreak}")
    public List<Streak> getStreaksByMinimumStreak(@PathVariable int minStreak) {
        return streakService.getStreaksByMinimumStreak(minStreak);
    }

    @GetMapping("/top")
    public List<Streak> getTopStreaks() {
        return streakService.getTopStreaks();
    }

    @GetMapping("/average")
    public Float getAverageStreak() {
        return streakService.getAverageStreak();
    }

    @PostMapping
    public Streak createStreak(@RequestBody Streak streak) {
        return streakService.createStreak(streak);
    }

    @DeleteMapping("/{id}")
    public void deleteStreak(@PathVariable Long id) {
        streakService.deleteStreak(id);
    }
}
