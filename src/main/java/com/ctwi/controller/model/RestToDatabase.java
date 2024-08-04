package com.ctwi.controller.model;

import com.ctwi.service.Auth;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RestToDatabase {

    private static final String DB_URL = "jdbc:mysql://localhost:13306/db";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";
    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH = 32;

    //ユーザーをデータベースに挿入するメソッド
    public static void insertUser(String username, String email, String hashedPassword, String saltBase64) throws SQLException {
        String sql = """
                INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)
                """;
        try (Connection con = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2,email);
            ps.setString(3, hashedPassword);
            ps.setString(4, saltBase64);
            ps.executeUpdate();
        } catch (SQLException e) {
            //error文
            System.out.println("SQL Error: " + e.getMessage());
            throw e;
        }
    }


    public static User fetchUserByEmail(String email) throws SQLException {
        String sql = """
                SELECT * FROM users WHERE email = ?
                """;
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.email = rs.getString("email");
                user.hashedPassword = rs.getString("password_hash");
                user.salt = rs.getString("salt");
                return user;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            throw e;
        }
    }

    public static boolean authenticateUser(String email, String password) throws SQLException {
        User user = fetchUserByEmail(email);
        if(user == null) {
            return false;
        }

        //passwordを比較
        return Auth.verifyPassword(password, user.hashedPassword, user.salt, ITERATION_COUNT, KEY_LENGTH);
    }

    public static class User {
        public String email;
        public String hashedPassword;
        public String salt;
    }
}