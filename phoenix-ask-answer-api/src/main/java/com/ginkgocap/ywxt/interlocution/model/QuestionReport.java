package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;

/**
 * Created by Wang fei on 2017/5/25.
 */
public class QuestionReport implements Serializable{

    private static final long serialVersionUID = 8746712190836351412L;

    private long id;
    /**
     * 问题 id
     */
    private long questionId;
    /**
     * 问题 标题
     */
    private String questionTitle;
    /**
     * 举报 时间
     */
    private long createTime;

    /**
     * 问题提出者 id
     */
    private long ownerId;
    /**
     * 举报 描述
     */
    private String content;
    /**
     * 举报 原因
     */
    private String reason;
    /**
     * 举报 联系方式
     */
    private long contact;
    /**
     * 当前用户 id
     */
    private long userId;

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

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getContact() {
        return contact;
    }

    public void setContact(long contact) {
        this.contact = contact;
    }
}
