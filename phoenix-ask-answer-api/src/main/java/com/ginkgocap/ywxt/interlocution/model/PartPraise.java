package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;

/**
 * Created by Wang fei on 2017/6/1.
 * 答案中 局部点赞过的人 简类 在问题详情中返回
 */
public class PartPraise implements Serializable{

    private static final long serialVersionUID = 8146712290836358494L;

    private long admirerId;

    private String admirerName;

    private String admirerPicPath;
    /**
     * 点赞者 类型 0：个人用户 1：组织用户
     */
    private short virtual;

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

    public short getVirtual() {
        return virtual;
    }

    public void setVirtual(short virtual) {
        this.virtual = virtual;
    }
}
