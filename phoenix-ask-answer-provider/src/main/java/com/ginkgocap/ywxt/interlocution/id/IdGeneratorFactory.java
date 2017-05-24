package com.ginkgocap.ywxt.interlocution.id;

import com.ginkgocap.ywxt.interlocution.utils.NetworkUtil;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class IdGeneratorFactory {

	private final static Logger logger = LoggerFactory.getLogger(IdGeneratorFactory.class);
	 
    public static DefaultIdGenerator idGenerator(MongoTemplate mongoTemplate)
    {
    	DefaultIdGenerator defaultIdGenerator = null;
        String ipAddress = NetworkUtil.getLocalIp();
        if (ipAddress == null) {
            logger.error("Can't get host Ip address, please check the host configure..");
            //Dummy a address
            ipAddress = "127.0.0.101";
        }
        int sequence = 1;
        String collectionName = CloudConfig.class.getSimpleName();
        logger.info("ipAddress: {} collectionName: {}",ipAddress, collectionName);
        Query query = ipQuery(ipAddress);
        CloudConfig cloud = mongoTemplate.findOne(query, CloudConfig.class, collectionName);
        if (cloud != null) {
            logger.info("find ip: {} in collectionName id: {}",ipAddress, cloud.getId());
            defaultIdGenerator = new DefaultIdGenerator(String.valueOf(cloud.getId()));
        }
        else {
            query = idQuery();
            cloud = mongoTemplate.findOne(query, CloudConfig.class, collectionName);
            if (cloud != null) {
                logger.info("find the max ip: {} in collectionName id: {}",ipAddress, cloud.getId());
                cloud = saveCloudConfig(mongoTemplate, cloud.getId()+1, ipAddress, collectionName);
                defaultIdGenerator = new DefaultIdGenerator(String.valueOf(cloud.getId()));
            } else {
                logger.error("Can't get exist cloud configure, please check..");
                cloud = saveCloudConfig(mongoTemplate, sequence, ipAddress, collectionName);
                defaultIdGenerator = new DefaultIdGenerator(String.valueOf(cloud.getId()));
            }
        }
        
        return defaultIdGenerator;
    }

    public static DefaultIdGenerator idGenerator(final String prefix)
    {
        return new DefaultIdGenerator(prefix);
    }

    private static Query ipQuery(String ipAddress){
        Query query = new Query();
        query.addCriteria(Criteria.where("ip").is(ipAddress));
        return query;
    }

    private static Query idQuery()
    {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, Constant._ID));
        query.limit(1);
        return query;
    }

    private static CloudConfig saveCloudConfig(MongoTemplate mongoTemplate,int id,String ipAddress,String collectionName)
    {
        CloudConfig cloud = new CloudConfig(id, ipAddress);
        mongoTemplate.save(cloud, collectionName);
        return cloud;
    }
    
}

