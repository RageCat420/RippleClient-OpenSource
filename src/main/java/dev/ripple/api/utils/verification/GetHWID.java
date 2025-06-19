package dev.ripple.api.utils.verification;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GetHWID {
    public static String generateHWID() {
        String HWID = "Unknown HWID";
        try {
            String raw = System.getProperty("os.name") +
                    System.getProperty("user.name") +
                    System.getenv("COMPUTERNAME") +
                    System.getenv("PROCESSOR_IDENTIFIER") +
                    System.getProperty("os.arch") +
                    System.getProperty("os.version") +
                    System.getProperty("user.language") +
                    System.getenv("PROCESSOR_LEVEL") +
                    System.getenv("PROCESSOR_REVISION") +
                    System.getenv("PROCESSOR_ARCHITECTURE") +
                    System.getenv("NUMBER_OF_PROCESSORS") +
                    System.getenv("PROCESSOR_ARCHITEW6432") +
                    System.getenv("HOSTNAME") +
                    System.getenv("PROCESSOR");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            HWID = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HWID;
    }
}