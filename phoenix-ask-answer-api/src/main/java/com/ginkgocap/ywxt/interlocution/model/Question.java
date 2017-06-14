package com.ginkgocap.ywxt.interlocution.model;

import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wang fei on 2017/5/23.
 */
public class Question implements Serializable{

    private static final long serialVersionUID = 8746712290836351492L;

    private long id;
    /**
     * 问题标题
     */
    private String title;
    /**
     * 问题描述
     */
    private String describe;
    /**
     * 问题 类型
     * 1:金融 2：教育 3：医疗 4：人力资源 5：产品开发 6：法律 7：大数据 8：其他
     */
    private byte type;
    /**
     * 提问者 id
     */
    private long userId;
    /**
     * 提问者 名字
     */
    @Transient
    private String userName;
    /**
     * 提问者 头像
     */
    @Transient
    private String picPath;
    /**
     * 回答状态 0：未回答 1：已回答 -1: 全部
     */
    private byte status = 0;
    /**
     * 允许回答者 0：所有人均可回答 1：只允许组织用户回答
     */
    private byte answererType;
    /**
     * 问题 创建时间
     */
    private long createTime;
    /**
     * 问题 修改时间
     */
    private long updateTime;
    /**
     * 问题 置顶 0：非置顶 1：置顶
     */
    private byte top = 0;
    /**
     * 问题 回答个数
     */
    private int answerCount;
    /**
     * 问题 浏览数
     */
    private long readCount = 0;
    /**
     * 发现 首页中 与问题一起显示的 答案
     */
    private PartAnswer topAnswer;
    /**
     * 提问者 类型 0：个人用户 1：组织用户
     */
    private short virtual;
    /**
     * 问题 是否被收藏 0：未收藏 1：已收藏
     */
    @Transient
    private byte isCollect;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getAnswererType() {
        return answererType;
    }

    public void setAnswererType(byte answererType) {
        this.answererType = answererType;
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

    public byte getTop() {
        return top;
    }

    public void setTop(byte top) {
        this.top = top;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public long getReadCount() {
        return readCount;
    }

    public void setReadCount(long readCount) {
        this.readCount = readCount;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public short getVirtual() {
        return virtual;
    }

    public void setVirtual(short virtual) {
        this.virtual = virtual;
    }

    public PartAnswer getTopAnswer() {
        return topAnswer;
    }

    public void setTopAnswer(PartAnswer topAnswer) {
        this.topAnswer = topAnswer;
    }

    public byte getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(byte isCollect) {
        this.isCollect = isCollect;
    }

    public int addAnswerCount(int answerCount) {

        this.answerCount = answerCount++;
        return this.answerCount;
    }

    public int minusAnswerCount(int answerCount) {

        this.answerCount = answerCount--;
        return this.answerCount;
    }

    @Override
    public String toString() {
        return "Question{" +
                "answerCount=" + answerCount +
                ", readCount=" + readCount +
                '}';
    }
}
