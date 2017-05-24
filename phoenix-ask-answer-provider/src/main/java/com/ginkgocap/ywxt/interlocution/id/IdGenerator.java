package com.ginkgocap.ywxt.interlocution.id;

public interface IdGenerator {
	/**
	   * 生成下一个不重复的流水号
	   * @return
	   */
	  String next();
}
