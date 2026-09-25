package com.bookshop.pos.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);


    private static final String PREFIX = "posdb-backup-";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

    private final JdbcTemplate jdbcTemplate;

    @Value("${pos.backup.dir:./backups}")
    private String backupDir;

    @Value("${pos.backup.keep:14}")
    private int keep;

    public BackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "${pos.backup.cron:0 0 21 * * *}")
    public void scheduledBackup() {
        try {
            backup();
        } catch (Exception e) {
            // A failed backup must never take the app down or interrupt a sale —
            // log it and move on, the next scheduled run (or a manual trigger) tries again.
            log.error("Scheduled database backup failed", e);
        }
    }

    public String backup() throws IOException {
        Path dir = Paths.get(backupDir);
        Files.createDirectories(dir);

        String filename = PREFIX + LocalDateTime.now().format(STAMP) + ".zip";
        Path target = dir.resolve(filename);

        jdbcTemplate.execute("BACKUP TO '" + target.toAbsolutePath() + "'");
        log.info("Database backup written to {}", target.toAbsolutePath());

        applyRetention(dir);
        return filename;
    }

    private void applyRetention(Path dir) throws IOException {
        // Filenames are zero-padded yyyy-MM-dd-HHmm, so lexicographic order is
        // chronological order — no filesystem-timestamp reliance needed.
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> backups = files
                    .filter(p -> p.getFileName().toString().startsWith(PREFIX))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .toList();

            for (Path old : backups.stream().skip(Math.max(keep, 0)).toList()) {
                try {
                    Files.delete(old);
                    log.info("Deleted old backup {}", old.getFileName());
                } catch (IOException e) {
                    log.warn("Could not delete old backup {}", old.getFileName(), e);
                }
            }
        }
    }
}
