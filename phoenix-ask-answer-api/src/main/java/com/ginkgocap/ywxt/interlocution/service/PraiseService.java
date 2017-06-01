package com.ginkgocap.ywxt.interlocution.service;

import com.ginkgocap.ywxt.interlocution.model.Praise;
import com.gintong.frame.util.dto.InterfaceResult;

/**
 * Created by Wang fei on 2017/5/31.
 */
public interface PraiseService {

    InterfaceResult create(Praise praise);

    InterfaceResult delete(long answerId, long userId);
}
