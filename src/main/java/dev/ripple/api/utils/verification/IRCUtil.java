package dev.ripple.api.utils.verification;


import dev.ripple.core.impl.CommandManager;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.ripple.api.utils.Wrapper.mc;

public class IRCUtil {
    private static final String DEFAULT_SERVER = "hbsx.zyeidc.cn";
    private static final int DEFAULT_PORT = 50014;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    public static String nickname;
    private static boolean connected;
    private static final java.util.Map<String, String> groupColors = new java.util.HashMap<>();
    private static int onlinePlayers = 0;

    public static void init() {
        nickname = VerificationManager.username;
        connect();
    }

    private static void connect() {
        executor.submit(() -> {
            try {
                socket = new Socket(DEFAULT_SERVER, DEFAULT_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
                out.println("CONNECTED " + nickname);
                connected = true;
                CommandManager.sendChatMessage("§a已连接到IRC服务器");
                out.println("REQUEST_COLORS");
                out.println("REQUEST_ONLINE_USERS");

                String line;
                while ((line = in.readLine()) != null) {
                    handleServerMessage(line);
                }
            } catch (IOException e) {
                CommandManager.sendChatMessage("§c连接IRC服务器时出错: " + e.getMessage());
            } finally {
                disconnect();
            }
        });
    }

    private static void handleColorInfo(String message) {
        if (message.startsWith("COLORS ")) {
            String[] colorParts = message.substring(7).split(" ");
            for (String colorPart : colorParts) {
                String[] groupColor = colorPart.split(":");
                if (groupColor.length == 2) {
                    groupColors.put(groupColor[0], groupColor[1]);
                }
            }
        }
    }

    public static boolean sendMessage(String message) {
        if (connected) {
            nickname = VerificationManager.username;
            out.println("CHAT " + nickname + ": " + message);
            out.flush();
            return true;
        }
        return false;
    }

    public static void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            CommandManager.sendChatMessage("§c断开IRC连接时出错: " + e.getMessage());
        } finally {
            connected = false;
            socket = null;
            in = null;
            out = null;
            groupColors.clear();
        }
    }

    private static void handleServerMessage(String message) {
        if (message.startsWith("CHAT ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length == 3) {
                String groupInfo = parts[1];
                String chatContent = parts[2];
                String[] messageParts = chatContent.split(": ", 2);

                if (messageParts.length == 2) {
                    String sender = messageParts[0];
                    String content = messageParts[1];
                    String colorCode = groupColors.getOrDefault(groupInfo, "§7");
                    String formattedMessage = "§b[IRC]" + colorCode + "[" + groupInfo + "]§a[" + sender + "] §f" + content;
                    CommandManager.sendChatMessage(formattedMessage);
                }
            }
        } else if (message.startsWith("COLORS ")) {
            handleColorInfo(message);
        } else if (message.startsWith("ONLINE_USERS ")) {
            onlinePlayers = Integer.parseInt(message.substring(13));
        } else if (message.startsWith("USER_CONNECTED ")) {
            String username = message.substring(15);
            CommandManager.sendChatMessage("§b[IRC] §a" + username + " §a加入了聊天");
        }
    }

    public static int getOnlinePlayers() {
        return onlinePlayers;
    }
}