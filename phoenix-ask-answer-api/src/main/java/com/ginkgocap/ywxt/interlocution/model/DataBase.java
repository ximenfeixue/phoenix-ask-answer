package com.ginkgocap.ywxt.interlocution.model;

import com.ginkgocap.parasol.associate.model.Associate;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wang fei on 2017/5/23.
 */
public class DataBase implements Serializable {

    private static final long serialVersionUID = 8746712290836351492L;

    private Question question;

    private List<Associate> asso;

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Associate> getAsso() {
        return asso;
    }

    public void setAsso(List<Associate> asso) {
        this.asso = asso;
    }
}
