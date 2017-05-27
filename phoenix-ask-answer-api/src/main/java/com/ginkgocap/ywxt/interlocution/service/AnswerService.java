package com.ginkgocap.ywxt.interlocution.service;

import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.user.model.User;
import com.gintong.frame.util.dto.InterfaceResult;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/27.
 */
public interface AnswerService {

    InterfaceResult insert(Answer answer);

    List<Answer> getAnswerListByQuestionId(long questionId, int start, int size) throws Exception;
}
