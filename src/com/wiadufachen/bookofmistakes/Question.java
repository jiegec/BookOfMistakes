package com.wiadufachen.bookofmistakes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by win7 on 14-3-15.
 */
public class Question {
    private Integer id;
    private String title;
    private String answer;
    private String solution;
    private Integer category;
    public Question(Integer id,String title, String answer, String solution, Integer category) {
        this.id = id;
        this.title = title;
        this.answer = answer;
        this.solution = solution;
        this.category = category;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
