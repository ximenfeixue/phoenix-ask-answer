package com.ginkgocap.ywxt.interlocution.dao;

import com.ginkgocap.ywxt.interlocution.model.Praise;

/**
 * Created by Wang fei on 2017/5/31.
 */
public interface PraiseMongoDao {

    Praise create(Praise praise) throws Exception;

    boolean delete(long answerId) throws Exception;
}
