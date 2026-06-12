package com.javaweb.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.config.DBConnection;
import com.javaweb.dto.QuestionDTO;
import com.javaweb.model.Question;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/quiz/start")
public class QuizStartServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false); // AuthFilter đã check nên chắc chắn không null

        try (Connection conn = DBConnection.getConnection()) {
            // Lấy 5 câu hỏi ngẫu nhiên từ MySQL
            String sql = "SELECT * FROM questions ORDER BY RAND() LIMIT 5";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<Question> quizQuestions = new ArrayList<>();
            while (rs.next()) {
                quizQuestions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_option")
                ));
            }

            if (quizQuestions.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"No questions available in database.\"}");
                return;
            }

            // Lưu trạng thái game vào Session của User này
            session.setAttribute("quiz_questions", quizQuestions);
            session.setAttribute("current_index", 0);
            session.setAttribute("score", 0);

            //  Trả về câu hỏi đầu tiên (đã bọc qua DTO để giấu đáp án đúng)
            Question firstQuestion = quizQuestions.get(0);
            QuestionDTO dto = new QuestionDTO(firstQuestion);

            response.getWriter().write(objectMapper.writeValueAsString(dto));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}