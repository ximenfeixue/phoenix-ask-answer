package com.ginkgocap.ywxt.interlocution.id;

import java.io.Serializable;

/**
 * Created by gintong on 2016/7/29.
 */
public class CloudConfig implements Serializable {

	private static final long serialVersionUID = 4314936704325545146L;
	public CloudConfig(int id,String ip)
    {
        this.id = id;
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;
    private String ip;
}
