package org.Kroj.Core.Tools.String;

public class SizeManager {
    private static final double log1024 = Math.log(1024);
    public static String formatSpeed(double bytes) {
        if (bytes < 1024) return String.format("%.2f B/s", bytes);
        int index = (int) (Math.log(bytes) / log1024);
        char unit = "KMGTPE".charAt(index - 1);
        return String.format("%.2f %cB/s", bytes / Math.pow(1024, index), unit);
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return String.format("%d B",bytes);
        int index = (int) (Math.log(bytes) / log1024);
        char unit = "KMGTPE".charAt(index - 1);
        return String.format("%.2f %cB", bytes / Math.pow(1024, index), unit);
    }
}
