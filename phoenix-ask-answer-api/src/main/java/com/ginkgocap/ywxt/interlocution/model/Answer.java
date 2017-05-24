package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;

/**
 * Created by wang fei on 2017/5/23.
 */
public class Answer implements Serializable{

    private static final long serialVersionUID = 8746712290836358494L;

    private long id;
    /** 问题 id **/
    private long questionId;
    /** 答案 内容 **/
    private String content;
    /** 回答者 id **/
    private long answererId;
    /** 回答者 名字 **/
    private String answererName;
    /** 回答者 头像 **/
    private String answererPicPath;
    /** 答案 创建时间 **/
    private long createTime;
    /** 答案 修改时间 **/
    private long updateTime;
    /** 点赞数 **/
    private int praiseCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getAnswererId() {
        return answererId;
    }

    public void setAnswererId(long answererId) {
        this.answererId = answererId;
    }

    public String getAnswererName() {
        return answererName;
    }

    public void setAnswererName(String answererName) {
        this.answererName = answererName;
    }

    public String getAnswererPicPath() {
        return answererPicPath;
    }

    public void setAnswererPicPath(String answererPicPath) {
        this.answererPicPath = answererPicPath;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.praiseCount = praiseCount;
    }
}
