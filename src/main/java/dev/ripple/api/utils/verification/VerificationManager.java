package dev.ripple.api.utils.verification;

import dev.ripple.api.utils.verification.logging.Logger;
import sun.misc.Unsafe;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class VerificationManager {
    private static final String KEY_SERVER_HOST = "hbsx.zyeidc.cn";
    private static final int KEY_SERVER_PORT = 50033;
    private static final String VERIFY_SERVER_HOST = "hbsx.zyeidc.cn";
    private static final int VERIFY_SERVER_PORT = 50071;
    private static final Unsafe unsafe;
    public static String username;
    static String hwid = GetHWID.generateHWID();
    private static final String filePath = System.getProperty("user.home") + "\\Starlight\\User.txt";
    public static final long EXPIRATION_DAYS = 14;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Unsafe", e);
        }
    }

    public static void crashJVM() {
        unsafe.putAddress(0L, 0L);
    }

    public static void client(String projectName) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Logger.error("No saved login information found.");
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean foundValidUser = false;
            while ((line = reader.readLine()) != null) {
                String decodedData = new String(Base64.getDecoder().decode(line));
                String[] parts = decodedData.split(":");
                if (parts.length != 3) {
                    continue;
                }
                String savedUsername = parts[0];
                String savedPassword = parts[1];
                username = savedUsername;
                long savedTime = Long.parseLong(parts[2]);
                long currentTime = System.currentTimeMillis();
                long timeDiff = (currentTime - savedTime) / (1000 * 60 * 60 * 24);
                if (timeDiff > EXPIRATION_DAYS) {
                    deleteExpiredUser(line);
                    continue;
                }
                foundValidUser = true;
                Key aesKey = getAESKeyFromServer();
                if (aesKey == null) {
                    Logger.error("Failed to retrieve encryption key from server.");
                    return;
                }
                if (!TimeChecker.checkTime()) {
                    Logger.error("The time verification failed, please try to update the system time.");
                    return;
                }
                String dataToSend = "CLIENT:" + savedUsername + ":" + savedPassword + ":" + hwid + ":" + projectName;
                byte[] encryptedData = encryptData(dataToSend.getBytes(), aesKey);
                if (encryptedData == null) {
                    Logger.error("Data encryption failed.");
                    return;
                }
                String response = sendEncryptedDataToServer(encryptedData);
                handleClientResponse(response);
            }
            reader.close();
            if (!foundValidUser) {
                Logger.error("No valid saved login information found.");
            }
        } catch (Exception e) {
            Logger.error("Client verification error: " + e.getMessage());
        }
    }

    private static void deleteExpiredUser(String encodedData) {
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(encodedData)) {
                    content.append(line).append(System.lineSeparator());
                }
            }
            reader.close();
            FileWriter writer = new FileWriter(file);
            writer.write(content.toString());
            writer.close();
        } catch (IOException e) {
            Logger.error("Failed to delete expired user: " + e.getMessage());
        }
    }

    private static void handleClientResponse(String response) {
        switch (response) {
            case "SUCCESS":
                Logger.info( "Login successful!");
                break;
            case "PASSWORD_ERROR":
                Logger.error("Login failed: Password error.");
                crashJVM();
                break;
            case "HWID_MISMATCH":
                Logger.error("Login failed: HWID mismatch.");
                crashJVM();
                break;
            case "USER_NOT_FOUND":
                Logger.error("Login failed: User not found.");
                crashJVM();
                break;
            default:
                Logger.error("Login failed: " + response);
                crashJVM();
        }
    }


    private static Key getAESKeyFromServer() {
        try (Socket keySocket = new Socket(KEY_SERVER_HOST, KEY_SERVER_PORT);
             InputStream keyInputStream = keySocket.getInputStream()) {
            byte[] keyBytes = new byte[16];
            int bytesRead = keyInputStream.read(keyBytes);
            if (bytesRead == 16) {
                return new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            } else {
                Logger.error("Invalid key length received from server: " + bytesRead + " bytes");
                return null;
            }
        } catch (IOException e) {
            Logger.error("Failed to retrieve AES key from server: " + e.getMessage());
            return null;
        }
    }

    private static byte[] encryptData(byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            Logger.error("Data encryption error: " + e.getMessage());
            return null;
        }
    }

    private static String sendEncryptedDataToServer(byte[] encryptedData) {
        try (Socket verifySocket = new Socket(VERIFY_SERVER_HOST, VERIFY_SERVER_PORT);
             OutputStream outputStream = verifySocket.getOutputStream();
             InputStream inputStream = verifySocket.getInputStream()) {
            outputStream.write(encryptedData);
            outputStream.flush();
            byte[] responseBytes = new byte[1024];
            int bytesReceived = inputStream.read(responseBytes);
            if (bytesReceived > 0) {
                return new String(responseBytes, 0, bytesReceived).trim();
            }
            return "Invalid response";
        } catch (UnknownHostException e) {
            Logger.error("Server not found: " + e.getMessage());
        } catch (IOException e) {
            Logger.error("Failed to send data to server: " + e.getMessage());
        }
        return "Failed to connect to server";
    }
}