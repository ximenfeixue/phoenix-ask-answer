package com.ginkgocap.ywxt.interlocution.dao;

import com.ginkgocap.ywxt.interlocution.model.DataSync;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
public interface DataSyncMongoDao {

    long saveDataSync(DataSync data);

    List<DataSync> batchSaveDataSync(List<DataSync> dataList);

    boolean deleteDataSync(final long id);

    List<DataSync> getDataSyncList();
}
