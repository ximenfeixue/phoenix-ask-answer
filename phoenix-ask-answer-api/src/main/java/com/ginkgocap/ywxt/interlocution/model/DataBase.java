package com.ginkgocap.ywxt.interlocution.model;

import com.ginkgocap.parasol.associate.model.Associate;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wang fei on 2017/5/23.
 *
 * 创建（提出）问题 时 所用的 model
 */
public class DataBase implements Serializable {

    private static final long serialVersionUID = 8746712290836351492L;

    private Question question;

    private List<Associate> associateList;

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Associate> getAssociateList() {
        return associateList;
    }

    public void setAssociateList(List<Associate> associateList) {
        this.associateList = associateList;
    }
}
