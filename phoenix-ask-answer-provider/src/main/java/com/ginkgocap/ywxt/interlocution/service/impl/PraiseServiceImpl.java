package com.ginkgocap.ywxt.interlocution.service.impl;

import com.ginkgocap.ywxt.interlocution.dao.PraiseMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Praise;
import com.ginkgocap.ywxt.interlocution.service.PraiseService;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
@Service("praiseService")
public class PraiseServiceImpl implements PraiseService {

    private Logger logger = LoggerFactory.getLogger(PraiseServiceImpl.class);

    @Resource
    private PraiseMongoDao praiseMongoDao;

    public InterfaceResult create(Praise praise) {

        Praise savePraise = null;
        Long id = null;
        try {
            savePraise = praiseMongoDao.create(praise);
            id = savePraise.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(id);
    }

    public InterfaceResult delete(long answerId, long userId) {

        InterfaceResult result = null;
        try {
            boolean flag = praiseMongoDao.delete(answerId, userId);
            result = InterfaceResult.getSuccessInterfaceResultInstance(flag);
        } catch (Exception e) {
            logger.error("delete praise failed!");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }

    @Override
    public List<Praise> getPraiseUser(long answerId, int start, int size) throws Exception{

        return praiseMongoDao.getPraiseUser(answerId, start, size);
    }

    @Override
    public long countByAnswerId(long answerId) throws Exception {

        return praiseMongoDao.countByAnswerId(answerId);
    }

    @Override
    public Praise getPraiseByUIdAnswerId(long answerId, long userId) throws Exception {

        return praiseMongoDao.getPraiseByUIdAnswerId(answerId, userId);
    }

    @Override
    public List<Praise> getPartPraiseUser(long answerId, int start, int size) throws Exception {

        return praiseMongoDao.getPartPraiseUser(answerId, start, size);
    }
}
