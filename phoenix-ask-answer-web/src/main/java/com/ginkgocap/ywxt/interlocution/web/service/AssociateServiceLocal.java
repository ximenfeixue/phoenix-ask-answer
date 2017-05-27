package com.ginkgocap.ywxt.interlocution.web.service;


import com.ginkgocap.parasol.associate.exception.AssociateServiceException;
import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.parasol.associate.service.AssociateService;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.user.model.User;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/24.
 */
@Service("associateServiceLocal")
public class AssociateServiceLocal {

    private final Logger logger = LoggerFactory.getLogger(AssociateServiceLocal.class);

    private static final long defaultId = 0l;

    @Resource
    private AssociateService associateService;

    public List<Associate> createAssociate(List<Associate> associateList, Long questionId, User user) {

        for (Associate associate : associateList) {
            if (associate == null)
                continue;
            Long assoId = 0l;
            associate.setUserId(user.getId());
            associate.setSourceId(questionId);
            associate.setUserName(user.getName());
            associate.setSourceTypeId(Constant.TYPE_ID);
            try {
                assoId = associateService.createAssociate(Constant.APP_ID, user.getId(), associate);
                logger.info("save asso success : id = " + assoId);
            } catch (AssociateServiceException e) {
                e.printStackTrace();
                return null;
            }
        }
        return associateList;
    }

    public List<Associate> getAssoByQuestionId(long questionId) throws Exception{

        List<Associate> associateList = null;
        try {
            associateList = associateService.getAssociatesBySourceId(Constant.APP_ID, defaultId, questionId, Constant.TYPE_ID);

        } catch (Exception e) {
            logger.error("invoke associateService failed ! please check service " + "sourceId:" + questionId);
            e.printStackTrace();
            throw new Exception(e);
        }
        return associateList;
    }
}
