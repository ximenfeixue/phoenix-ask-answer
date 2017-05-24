package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;

/**
 * Created by wang fei on 2017/5/23.
 */
public class Praise implements Serializable{

    private static final long serialVersionUID = 8746712290836351492L;

    private long id;
    /** 回答者 id **/
    private long answerId;
    /** 赞赏者 id **/
    private long admirerId;
    /** 赞赏者 名字**/
    private String admirerName;
    /** 赞赏者 头像**/
    private String admirerPicPath;

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
}
