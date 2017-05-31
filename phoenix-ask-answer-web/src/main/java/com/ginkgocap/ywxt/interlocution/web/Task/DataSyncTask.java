package com.ginkgocap.ywxt.interlocution.web.Task;

import com.ginkgocap.ywxt.interlocution.model.DataSync;
import com.ginkgocap.ywxt.interlocution.service.DataSyncService;
import com.gintong.ywxt.im.model.MessageNotify;
import com.gintong.ywxt.im.service.MessageNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by gintong on 2016/7/9.
 */
@Repository("dataSyncTask")
public class DataSyncTask implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DataSyncTask.class);
    private static final int MAX_QUEUE_NUM = 2000;

    @Autowired
    DataSyncService dataSyncService;

    @Autowired
    private MessageNotifyService messageNotifyService;

    private BlockingQueue<DataSync> dataSyncQueue = new ArrayBlockingQueue<DataSync>(MAX_QUEUE_NUM);

    public boolean saveDataNeedSync(DataSync data)
    {
        try {
            final long id = dataSyncService.saveDataSync(data);
            data.setId(id);
            addQueue(data);
        } catch (Exception ex) {
            logger.error("save sync data failed: dataSync: {}",data);
            return false;
        }
        return true;
    }

    public void run() {
        try {
            while(true) {
                DataSync dataSync = dataSyncQueue.take();
                if (dataSync != null) {
                    boolean result = false;
                    Object data = dataSync.getData();
                    if (data != null) {
                        if (data instanceof MessageNotify) {
                            result = sendMessageNotify((MessageNotify) data);
                        }
                        // 有时间 将 各种 count 也放到这里同步更新数据
                        /*else if(data instanceof Permission) {
                            final Permission perm = (Permission)data;
                            final short privated = DataCollect.privated(perm, false);
                            knowledgeOtherService.updateCollectedKnowledgePrivate(perm.getResId(), -1, privated);
                        }*/
                    }
                    if (result) {
                        dataSyncService.deleteDataSync(dataSync.getId());
                    }
                } else {
                    logger.info("data is null, so skip to send.");
                }
            }
        } catch (InterruptedException ex) {
            logger.error("queues thread interrupted. so exit this thread.");
        }
    }

    private boolean sendMessageNotify(MessageNotify message) {
        try {
            boolean result = messageNotifyService.sendMessageNotify(message);
            if (result) {
                logger.info("send response answer notify message success. fromId: " + message.getFromId() + " toId: " + message.getToId());
            }
            return result;
        } catch (Exception ex) {
            logger.error("send response answer notify message failed. error: " + ex.getMessage());
        }
        return false;
    }

    public void addQueue(DataSync data) {
        if (data != null) {
            try {
                dataSyncQueue.put(data);
            } catch (Exception ex) {
                logger.error("add sync data to queue failed.");
            }
        } else {
            logger.error("sync object is null, so skip it.");
        }
    }
}
