package com.ginkgocap.ywxt.interlocution.service;

import com.ginkgocap.ywxt.interlocution.model.Question;
import com.gintong.frame.util.dto.InterfaceResult;

/**
 * Created by Wang fei on 2017/5/23.
 */
public interface AskService {

    /**
     * 插入数据
     * @param question
     * @return
     */
    InterfaceResult insert(Question question);
}
