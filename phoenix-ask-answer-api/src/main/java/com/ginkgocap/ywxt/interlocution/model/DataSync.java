package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;

/**
 * Created by Wang fei on 2017/5/31.
 */
public class DataSync<T> implements Serializable {

    private static final long serialVersionUID = 8746712290836358492L;

    /**
     * 唯一 主键
     */
    private long id;
    /**
     * 同步数据 对象
     */
    private T data;

    public DataSync(long id, T data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
