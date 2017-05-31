package com.ginkgocap.ywxt.interlocution.service.impl;

import com.ginkgocap.ywxt.interlocution.dao.DataSyncMongoDao;
import com.ginkgocap.ywxt.interlocution.model.DataSync;
import com.ginkgocap.ywxt.interlocution.service.DataSyncService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
@Service("dataSyncService")
public class DataSyncServiceImpl implements DataSyncService{

    @Resource
    private DataSyncMongoDao dataSyncMongoDao;

    @Service
    public long saveDataSync(DataSync data) {

        return dataSyncMongoDao.saveDataSync(data);
    }

    @Service
    public boolean deleteDataSync(long id) {

        return dataSyncMongoDao.deleteDataSync(id);
    }

    @Service
    public List<DataSync> getDataSyncList() {

        return dataSyncMongoDao.getDataSyncList();
    }
}
