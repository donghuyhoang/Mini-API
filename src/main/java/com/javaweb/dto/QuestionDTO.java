package com.javaweb.dto;

import com.javaweb.model.Question;

//import com.quiz.model.Question;

public class QuestionDTO {
    private int id;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    // Constructor convert nhanh từ Question gốc sang DTO
    public QuestionDTO(Question q) {
        this.id = q.getId();
        this.content = q.getContent();
        this.optionA = q.getOptionA();
        this.optionB = q.getOptionB();
        this.optionC = q.getOptionC();
        this.optionD = q.getOptionD();
    }

    // Getters
    public int getId() { return id; }
    public String getContent() { return content; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
}