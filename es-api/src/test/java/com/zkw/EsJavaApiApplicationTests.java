package com.zkw;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;

@SpringBootTest
class EsJavaApiApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void testIndexRequest(){
        IndexRequest request = new IndexRequest("posts");
        request.id("1");
        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);
    }

    /**
     * 测试同步方法
     */
    @Test
    void testTong() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("user", "kimchy");
            builder.timeField("postDate", new Date());
            builder.field("message", "trying out Elasticsearch");
        }
        builder.endObject();
        IndexRequest indexRequest = new IndexRequest("zkw_index");
        indexRequest.id("1").source(RequestOptions.DEFAULT);
        indexRequest.timeout(TimeValue.timeValueSeconds(5));
        indexRequest.opType(DocWriteRequest.OpType.INDEX);


        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            String index = indexResponse.getIndex();
            String type = indexResponse.getType();
            String id = indexResponse.getId();
            long version = indexResponse.getVersion();
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("添加成功");
                System.out.println("type:" + type);
                System.out.println("id:" + id);
                System.out.println("version:" + version);
                System.out.println("index:" + index);
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("更新成功");
                System.out.println("index:" + index);
                System.out.println("type:" + type);
                System.out.println("id:" + id);
                System.out.println("version:" + version);
            }
        }catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                System.out.println("创建的文档与已存在的发生冲突");
            }
        }

        client.close();

    }

    @Test
    void testAsy(){

    }

}
