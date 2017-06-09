package com.ginkgocap.ywxt.interlocution.web.service;


import com.ginkgocap.parasol.associate.exception.AssociateServiceException;
import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.parasol.associate.model.AssociateType;
import com.ginkgocap.parasol.associate.service.AssociateService;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.DataBase;
import com.ginkgocap.ywxt.user.model.User;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public InterfaceResult updateAssociate(long questionId, User user, DataBase base) {

        Map<AssociateType, List<Associate>> assoMap = null;
        try {
            assoMap = associateService.getAssociatesBy(Constant.APP_ID, Constant.TYPE_ID, questionId);
            if (assoMap == null) {
                logger.error("asso item null or converted failed...");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } catch (Exception e) {
            logger.error("Asso update failed！reason：" + e.getMessage());
        }
        //TODO: If this step failed, how to do ?
        if (MapUtils.isNotEmpty(assoMap)) {
            for (Iterator i = assoMap.values().iterator(); i.hasNext(); ) {
                List<Associate> associateList = (List) i.next();
                for (int j = 0; j < associateList.size(); j++) {
                    Associate associate = associateList.get(j);
                    if (associate == null) {
                        logger.error("Associate object is null, index: " + j);
                        continue;
                    }
                    try {
                        associateService.removeAssociate(Constant.APP_ID, user.getId(), associate.getId());
                    } catch (Exception e) {
                        logger.error("Asso update failed！reason：" + e.getMessage());
                    }
                }
            }
        }
        List<Associate> as = base.getAssociateList();
        createAssociate(as, questionId, user);

        return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SUCCESS);
    }
}
