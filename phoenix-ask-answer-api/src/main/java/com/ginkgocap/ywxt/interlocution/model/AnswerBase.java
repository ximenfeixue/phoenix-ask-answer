package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/26.
 *
 * 答案 详情 返回前端 model
 */
public class AnswerBase implements Serializable {

    private static final long serialVersionUID = 8746712290816358494L;

    private Answer answer;

    private List<Praise> praiseList;

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public List<Praise> getPraiseList() {
        return praiseList;
    }

    public void setPraiseList(List<Praise> praiseList) {
        this.praiseList = praiseList;
    }
}
