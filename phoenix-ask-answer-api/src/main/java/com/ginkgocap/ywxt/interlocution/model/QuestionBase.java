package com.ginkgocap.ywxt.interlocution.model;

import com.ginkgocap.parasol.associate.model.Associate;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/26.
 *
 * 问题详情时 返回前端的 model
 */
public class QuestionBase implements Serializable{

    private static final long serialVersionUID = 8446712290836358494L;

    private Question question;

    private List<Associate> associateList;

    private List<Answer> answerList;

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList;
    }

    public List<Associate> getAssociateList() {
        return associateList;
    }

    public void setAssociateList(List<Associate> associateList) {
        this.associateList = associateList;
    }
}
