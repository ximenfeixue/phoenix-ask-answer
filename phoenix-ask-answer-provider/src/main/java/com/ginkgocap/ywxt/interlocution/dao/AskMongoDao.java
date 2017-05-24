package com.ginkgocap.ywxt.interlocution.dao;


import com.ginkgocap.ywxt.interlocution.model.Question;

/**
 * Created by Wang fei on 2017/5/24.
 */
public interface AskMongoDao {

    Question insert(Question question) throws Exception;

}
