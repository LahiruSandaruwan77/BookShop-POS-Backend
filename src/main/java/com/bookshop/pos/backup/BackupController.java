package com.bookshop.pos.backup;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    // Lets an admin force a backup on demand (e.g. before closing up) in
    // addition to the daily schedule.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> trigger() {
        try {
            return Map.of("filename", backupService.backup());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Backup failed: " + e.getMessage());
        }
    }
}
