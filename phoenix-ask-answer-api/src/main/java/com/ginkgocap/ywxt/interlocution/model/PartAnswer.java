package com.ginkgocap.ywxt.interlocution.model;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * Created by Wang fei on 2017/6/1.
 *
 * 发现 问答 页面 答案简表
 */
public class PartAnswer implements Serializable{

    private static final long serialVersionUID = 8746712290816358494L;
    /**
     * 答案 id
     */
    private long answerId;
    /**
     * 答案内容
     */
    private String content;
    /**
     * 内容类型 0：文字 1：语音
     */
    private byte type;
    /**
     * 点赞 数
     */
    private int praiseCount;
    /**
     * 回答者 id
     */
    private long answererId;
    /**
     * 回答者 名字
     */
    @Transient
    private String answererName;
    /**
     * 回答者 头像
     */
    @Transient
    private String answererPicPath;
    /**
     * 回答者 类型 0：个人用户 1：组织用户
     */
    private short virtual;
    /**
     * 答案 是否置顶 0：非置顶 1：置顶
     */
    private byte top;
    /**
     * 是否 点赞 0：非点赞 1：已点赞
     */
    private byte isPraise;

    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.praiseCount = praiseCount;
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

    public short getVirtual() {
        return virtual;
    }

    public void setVirtual(short virtual) {
        this.virtual = virtual;
    }

    public byte getTop() {
        return top;
    }

    public void setTop(byte top) {
        this.top = top;
    }

    public byte getIsPraise() {
        return isPraise;
    }

    public void setIsPraise(byte isPraise) {
        this.isPraise = isPraise;
    }
}
