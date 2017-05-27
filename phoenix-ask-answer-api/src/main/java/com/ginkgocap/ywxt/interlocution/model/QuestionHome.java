package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;

/**
 * Created by Wang fei on 2017/5/26.
 *
 * 发现 问答 ：主页 返回前端 model
 */
public class QuestionHome implements Serializable{

    private static final long serialVersionUID = 8446712290836358494L;

    private Question question;

    private Answer answer;

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }
}
