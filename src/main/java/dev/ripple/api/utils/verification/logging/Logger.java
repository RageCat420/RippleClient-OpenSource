package dev.ripple.api.utils.verification.logging;

public class Logger {

    private Logger() {
        // Prevent instantiation
    }

    public static void debug(String message) {
        System.out.println("[Starlight Verification] DEBUG: " + message);
    }

    public static void info(String message) {
        System.out.println("[Starlight Verification] INFO: " + message);
    }

    public static void warn(String message) {
        System.out.println("[Starlight Verification] WARN: " + message);
    }

    public static void error(String message) {
        System.out.println("[Starlight Verification] ERROR: " + message);
    }
}