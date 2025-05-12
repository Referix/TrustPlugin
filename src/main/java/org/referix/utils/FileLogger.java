package org.referix.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileLogger {

    private final JavaPlugin plugin;
    private final File logFile;

    public FileLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.logFile = new File(dataFolder, "log.yml");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Не удалось создать log.yml: " + e.getMessage());
            }
        }
    }

    public void logReputationChange(
            String actorName,
            Double actorTrust,
            String targetName,
            Double targetTrust,
            Double delta,
            String reason,
            boolean accepted,
            boolean denied
    ) {
        String actorNameStr = actorName != null ? actorName : "N/A";
        String actorTrustStr = (actorTrust != null && actorTrust != 0) ? String.format("%.2f", actorTrust) : "N/A";
        String targetNameStr = targetName != null ? targetName : "N/A";
        String targetTrustStr = (targetTrust != null && targetTrust != 0) ? String.format("%.2f", targetTrust) : "N/A";
        String deltaStr = (delta != null && delta != 0) ? String.format("%+,.2f", delta) : "N/A";
        String reasonStr = (reason != null && !reason.isEmpty()) ? reason : "N/A";

        String logEntry = String.format(
                "%s:(%s) изменил репутацию игрока %s:(%s) на %s по причине %s%s%s",
                actorNameStr,
                actorTrustStr,
                targetNameStr,
                targetTrustStr,
                deltaStr,
                reasonStr,
                accepted ? " [accept]" : "",
                denied ? " [deny]" : ""
        );

        try {
            List<String> lines = Files.readAllLines(logFile.toPath());
            lines.add(logEntry);
            Files.write(logFile.toPath(), lines);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Ошибка при записи в log.yml: " + e.getMessage());
        }
    }

}
