package com.ginkgocap.ywxt.interlocution.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Wang fei on 2017/6/5.
 */
public class ID<T> implements Serializable{

    private static final long serialVersionUID = 2246712290836358494L;

    private long id;

    private List<T> idList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<T> getIdList() {
        return idList;
    }

    public void setIdList(List<T> idList) {
        this.idList = idList;
    }
}
