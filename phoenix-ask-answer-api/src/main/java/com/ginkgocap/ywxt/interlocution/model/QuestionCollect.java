package com.ginkgocap.ywxt.interlocution.model;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * Created by Wang fei on 2017/5/25.
 */
public class QuestionCollect implements Serializable{

    private static final long serialVersionUID = 8746712290836351412L;

    private long id;
    /**
     * 问题 id
     **/
    private long questionId;
    /**
     * 问题标题
     */
    private String questionTitle;
    /**
     * 提出问题者 id
     */
    private long ownerId;
    /**
     * 提出问题者 name
     */
    @Transient
    private String ownerName;
    /**
     * 提出问题者 头像
     */
    @Transient
    private String ownerPicPath;
    /**
     * 当前用户 id
     */
    private long userId;
    /**
     * 收藏 创建时间
     */
    private long createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPicPath() {
        return ownerPicPath;
    }

    public void setOwnerPicPath(String ownerPicPath) {
        this.ownerPicPath = ownerPicPath;
    }
}
