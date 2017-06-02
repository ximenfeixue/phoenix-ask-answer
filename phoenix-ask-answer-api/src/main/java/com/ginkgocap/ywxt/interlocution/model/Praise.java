package com.ginkgocap.ywxt.interlocution.model;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * Created by wang fei on 2017/5/23.
 */
public class Praise implements Serializable{

    private static final long serialVersionUID = 8746712290836351492L;

    private long id;
    /**
     * 答案 id
     */
    private long answerId;
    /**
     * 回答者 id
     */
    private long answererId;
    /**
     * 点赞者 id
     */
    private long admirerId;
    /**
     * 点赞者 名字
     */
    @Transient
    private String admirerName;
    /**
     * 点赞者 头像
     */
    @Transient
    private String admirerPicPath;
    /**
     * 点赞时间
     */
    private long admireTime;
    /**
     * 点赞者 类型 0：个人用户 1：组织用户
     */
    private short virtual;

    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    public long getAdmirerId() {
        return admirerId;
    }

    public void setAdmirerId(long admirerId) {
        this.admirerId = admirerId;
    }

    public String getAdmirerName() {
        return admirerName;
    }

    public void setAdmirerName(String admirerName) {
        this.admirerName = admirerName;
    }

    public String getAdmirerPicPath() {
        return admirerPicPath;
    }

    public void setAdmirerPicPath(String admirerPicPath) {
        this.admirerPicPath = admirerPicPath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAdmireTime() {
        return admireTime;
    }

    public void setAdmireTime(long admireTime) {
        this.admireTime = admireTime;
    }

    public long getAnswererId() {
        return answererId;
    }

    public void setAnswererId(long answererId) {
        this.answererId = answererId;
    }

    public short getVirtual() {
        return virtual;
    }

    public void setVirtual(short virtual) {
        this.virtual = virtual;
    }
}
