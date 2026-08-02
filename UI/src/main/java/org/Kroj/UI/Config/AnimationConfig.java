package org.Kroj.UI.Config;

public class AnimationConfig {
    private static double speedMultiplier = 1.0; // 0.5 (fast) to 2.0 (slow)

    public static double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public static void setSpeedMultiplier(double multiplier) {
        speedMultiplier = multiplier;
    }
}
