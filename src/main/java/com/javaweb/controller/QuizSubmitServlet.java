package com.javaweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.dto.QuestionDTO;
import com.javaweb.model.Question;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/quiz/submit")
public class QuizSubmitServlet extends HttpServlet {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        List<Question> questions = (List<Question>) session.getAttribute("quiz_questions");
        Integer currentIndex = (Integer) session.getAttribute("current_index");
        Integer score = (Integer) session.getAttribute("score");

        // Kiểm tra xem user đã thực sự bấm start game chưa
        if (questions == null || currentIndex == null || score == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"No active quiz game found. Please start a new game.\"}");
            return;
        }

        try {
            //  Đọc đáp án Client gửi lên (Ví dụ JSON: {"choice": "A"})
            Map<String, String> body = objectMapper.readValue(request.getReader(), Map.class);
            String userChoice = body.get("choice");

            if (userChoice == null || userChoice.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing choice attribute.\"}");
                return;
            }

            //  Chấm điểm câu hiện tại
            Question currentQuestion = questions.get(currentIndex);
            boolean isCorrect = currentQuestion.getCorrectOption().equalsIgnoreCase(userChoice.trim());
            
            if (isCorrect) {
                score += 1; // Đúng thì cộng 1 điểm
                session.setAttribute("score", score);
            }

            // Chuyển sang câu tiếp theo
            currentIndex++;
            session.setAttribute("current_index", currentIndex);

            // Kiểm tra xem đã hết bộ câu hỏi chưa
            if (currentIndex < questions.size()) {
                // Còn câu hỏi -> Trả về câu tiếp theo
                Question nextQuestion = questions.get(currentIndex);
                QuestionDTO dto = new QuestionDTO(nextQuestion);
                
                // Trả về kèm trạng thái của câu vừa làm để client hiển thị đúng/sai
                String jsonResult = String.format(
                    "{\"correct\": %b, \"next_question\": %s}", 
                    isCorrect, 
                    objectMapper.writeValueAsString(dto)
                );
                response.getWriter().write(jsonResult);
            } else {
                // Hết câu hỏi -> Kết thúc game, xóa dữ liệu game cũ trong Session
                session.removeAttribute("quiz_questions");
                session.removeAttribute("current_index");
                session.removeAttribute("score");

                String jsonFinalResult = String.format(
                    "{\"message\": \"Quiz completed!\", \"correct\": %b, \"final_score\": %d, \"total_questions\": %d}",
                    isCorrect, score, questions.size()
                );
                response.getWriter().write(jsonFinalResult);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Processing error: " + e.getMessage() + "\"}");
        }
    }
}