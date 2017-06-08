package com.ginkgocap.ywxt.interlocution.dao;

import com.ginkgocap.ywxt.interlocution.model.Praise;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
public interface PraiseMongoDao {

    Praise create(Praise praise) throws Exception;

    boolean delete(long answerId, long userId) throws Exception;

    List<Praise> getPraiseUser(long answerId, int start, int size) throws Exception;

    long countByAnswerId(long answerId) throws Exception;
}
