package com.netopstec.util;

import com.netopstec.annotation.es.ESEntity;
import com.netopstec.annotation.es.ESField;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ElasticsearchClient {


    public static final Logger LOGGER = Logger.getLogger(ElasticsearchClient.class);

    /**
     * 单例的客户端
     */
    private enum Client {
        INSTANCE;
        private RestHighLevelClient client;

        Client() {
            try {
                client = new RestHighLevelClient(
                        RestClient.builder(
                                new HttpHost("111.231.104.73", 9201, "http"),
                                new HttpHost("111.231.104.73", 9202, "http"),
                                new HttpHost("111.231.104.73", 9203, "http")

                        ));
            } catch (Exception e) {
                LOGGER.error("elasticsearch 集群连接失败");
                e.printStackTrace();
            }
            LOGGER.error("elasticsearch 集群连接成功");
        }

        public RestHighLevelClient getClient() {
            return client;
        }
    }


    /**
     * 获取客户端
     */
    public static RestHighLevelClient getClient() {
        return Client.INSTANCE.getClient();
    }

    /**
     * 添加es数据
     */
    public static boolean insert(String index, String type, String id, String jsonContent) {
        IndexResponse indexResponse = null;
        try{
            // 文档中有多种方式可以组织请求体
            XContentParser xContentParser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY,jsonContent);
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            indexRequest.index(index);
            indexRequest.type(type);
            indexRequest.source(xContentParser.map());
            indexRequest.create(true);
            indexResponse = getClient().index(indexRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if(shardInfo.getFailed() > 0) {
            for(ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()){
                String reason = failure.reason();
                LOGGER.error(reason);
            }
            return false;
        }
        return true;
    }


    /**
     * 批量添加es 数据
     */
    public static boolean insertBatch(String index, String type, Map<String, String> idContentJsonMap) {
        BulkResponse bulkResponse = null;
        try {
            BulkRequest bulkRequest = new BulkRequest();
            Set<String> ids = idContentJsonMap.keySet();
            for (String id : ids) {
                String jsonContent = idContentJsonMap.get(id);
                IndexRequest indexRequest = new IndexRequest(index, type, id);
                XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, jsonContent);
                indexRequest.source(parser.map());
                bulkRequest.add(indexRequest);
            }
            // es 的添加策略。需要改成立即添加
            bulkRequest.timeout(TimeValue.timeValueMinutes(2));
            bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            bulkResponse = getClient().bulk(bulkRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        boolean hasFailures = bulkResponse.hasFailures();
        if (hasFailures) {
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                String failureMessage = bulkItemResponse.getFailureMessage();
                LOGGER.error("failureMessage=" + failureMessage);
            }
            return false;
        }
        return true;
    }





    /**
     * 添加索引
     */
    public static void main(String[] args) throws ClassNotFoundException {
        String index = "test_goods";
        Class clazz = Class.forName("com.netopstec.entity.Goods");
        boolean index1 = createIndex(index, null, clazz);
        System.out.println("执行结果：" + index1);
    }

    /**
     * 创建索引公共方法
     */
    public static boolean createIndex(String index, String type, Class clazz) {

        /**
         * Map <String，Object> jsonMap = new HashMap <>（）;
         * Map <String，Object> message = new HashMap <>（）;
         * message.put（“type”，“text”）;
         * Map <String，Object> properties = new HashMap <>（）;
         * properties.put（“message”，message）;
         * Map <String，Object> tweet = new HashMap <>（）;
         * tweet.put（“properties”，properties）;
         * jsonMap.put（“tweet”，tweet）;
         * request.mapping（“tweet”，jsonMap）;
         */
        // 先获取type，type为NULL，clazz 获取类名首字母变小写
        if (type == null || "".equals(type)) {
            Annotation annotation = clazz.getAnnotation(ESEntity.class);
            if (annotation == null) {
                return false;
            }
            String simpleName = clazz.getSimpleName().toLowerCase();
            type = simpleName;
        }


        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(index);

        // 设置索引的分片和副本
        request.settings(Settings.builder()
                .put("index.number_of_shards",3)
                .put("index.number_of_replicas",2)
        );


        // 创建索引字段映射
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        Map<String, Object> typeMap = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        // 反射获取每个属性
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return false;
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            ESField annotation = field.getAnnotation(ESField.class);
            if (annotation == null) continue;
            String fieldType = annotation.value();
            if ("".equals(fieldType)) {
                fieldType = annotation.type();
            }
            Map<String, Object> property = new HashMap<>();
            property.put("type", fieldType);

            String fieldName = field.getName();
            properties.put(fieldName, property);
        }
        typeMap.put("properties", properties);
        jsonMap.put(type, typeMap);
        request.mapping(type, jsonMap);

        //设置别名
        request.alias(
                new Alias(type + "_alias")
        );

        ////超时等待所有节点确认索引创建为 TimeValue
        request.timeout(TimeValue.timeValueMinutes(2));

        //作为超时连接到主节点 TimeValue
        request.masterNodeTimeout(TimeValue.timeValueMinutes(1));

        CreateIndexResponse createIndexResponse = null;
        //同步执行
        try {
            RestHighLevelClient client = getClient();
            createIndexResponse = client.indices().create(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //异步执行
//		ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
//		    @Override
//		    public void onResponse(CreateIndexResponse createIndexResponse) {
////		        执行成功完成时调用。答复是作为参数提供的
//		    }
//			@Override
//			public void onFailure(Throwable e) {
//				// TODO Auto-generated method stub
////				在失败的情况下调用。引发的异常是作为参数提供的
//			}
//		};
//		client.indices().createAsync(request, listener);

        //判断是否执行成功
        return createIndexResponse.isAcknowledged();
    }



}
