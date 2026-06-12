package com.javaweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.config.DBConnection;

//import com.javaweb.config;.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
             // Đọc body JSON thủ công
            Map<String, String> credentials = objectMapper.readValue(request.getReader(), Map.class);
            String user = credentials.get("username");
            String pass = credentials.get("password");

            //  Kiểm tra DB bằng PreparedStatement (Tránh SQL Injection - Tư duy competitive/security)
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // Đăng nhập đúng -> Tạo HttpSession
                    HttpSession session = request.getSession(true);
                    session.setAttribute("username", user);

                    response.getWriter().write("{\"message\": \"Login successful!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Invalid username or password.\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}