package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/namozbot";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1111";

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "chat_id VARCHAR(50) PRIMARY KEY, " +
                    "username VARCHAR(255), " +
                    "nickname VARCHAR(255), " +
                    "phone_number VARCHAR(50), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void saveUser(String chatId, String username, String nickname, String phoneNumber) {
        String sql = "INSERT INTO users (chat_id, username, nickname, phone_number) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (chat_id) DO UPDATE SET username = EXCLUDED.username, nickname = EXCLUDED.nickname, phone_number = EXCLUDED.phone_number";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chatId);
            pstmt.setString(2, username);
            pstmt.setString(3, nickname);
            pstmt.setString(4, phoneNumber);
            pstmt.executeUpdate();
            System.out.println("User saved: chatId=" + chatId + ", username=" + username + ", nickname=" + nickname + ", phone_number=" + phoneNumber);
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    public static void updateUserPhoneNumber(String chatId, String phoneNumber) {
        String sql = "UPDATE users SET phone_number = ? WHERE chat_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNumber);
            pstmt.setString(2, chatId);
            pstmt.executeUpdate();
            System.out.println("Phone number updated for chatId=" + chatId + ": " + phoneNumber);
        } catch (SQLException e) {
            System.err.println("Error updating phone number: " + e.getMessage());
        }
    }
}