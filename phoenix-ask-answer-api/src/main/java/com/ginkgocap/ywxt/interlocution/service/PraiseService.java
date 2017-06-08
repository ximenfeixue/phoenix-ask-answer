package com.ginkgocap.ywxt.interlocution.service;

import com.ginkgocap.ywxt.interlocution.model.Praise;
import com.gintong.frame.util.dto.InterfaceResult;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
public interface PraiseService {

    InterfaceResult create(Praise praise);

    InterfaceResult delete(long answerId, long userId);

    List<Praise> getPraiseUser(long answerId, int start, int size) throws Exception;

    long countByAnswerId(long answerId) throws Exception;
}
