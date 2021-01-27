package com.zkw;

import com.alibaba.fastjson.JSON;
import com.zkw.elasticsearch.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.ExtendedStats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 测试索引的创建 Request   Request PUT
     */
    @Test
    public void testCreateIndex() throws IOException {

        //1、创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("zkw_index");
        //2、客户端执行请求  client.indices() 这里返回了一个IndicesClient  请求后获得相应
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    /**
     * 测试获取索引 ，判断其是否存在
     */
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 测试删除索引
     *
     * @throws IOException
     */
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("zkw_index");

        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);

        System.out.println(delete.isAcknowledged());
    }


    //测试添加文档
    @Test
    void testAddDocument() throws IOException {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //创建对象
        User user = new User("来啦老弟", 3);
        //创建请求
        IndexRequest request = new IndexRequest("zkw_index");
        //规则 put/zkw_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");

        //将我们的数据放入请求 json
        request.source(JSON.toJSONString(user), XContentType.JSON);

        //客户端发送请求 获取响应的结果
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    /**
     * 获取文档，判断是否存在  get/index/doc/1
     */
    @Test
    void testIsExists() throws IOException {
        GetRequest getRequest = new GetRequest("zkw_index", "1");
        //不获取返回的_source的上下文
        getRequest.fetchSourceContext(new FetchSourceContext(false));

        getRequest.storedFields("_none_");

        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 获得文档的信息
     */
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest("zkw_index", "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        //打印文档的内容
        System.out.println(getResponse.getSourceAsString());
    }

    /**
     * 更新文档的信息
     */
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("zkw_index", "1");
        updateRequest.timeout("1s");
        updateRequest.timeout(TimeValue.MINUS_ONE);

        User user = new User("啦啦啦啦", 18);
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);

        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update.status());
        System.out.println(update.toString());
    }

    /**
     * 删除文档的信息
     */
    @Test
    void testDeleteDocument() throws IOException{
        DeleteRequest deleteRequest = new DeleteRequest("zkw_index");
        deleteRequest.id("1");

        DeleteResponse delete = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(delete.toString());
        System.out.println(delete.status());
    }

    //特殊的，真的项目一般都会批量插入数据
    @Test
    void testBankRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("hhh",7));
        userList.add(new User("hhh2",9));
        userList.add(new User("hhh5",10));
        userList.add(new User("hhh6",20));
        userList.add(new User("hhh8",10));
        userList.add(new User("hhh88",88));
        userList.add(new User("hhh99",11));

        //批处理请求
        for (int i = 0; i < userList.size(); i++) {
            //批量更新和批量删除，就在这里修改对应的请求就可以了
            bulkRequest.add(new IndexRequest("zkw_index")
                    .id(""+(i+1))
                    .source(JSON.toJSONString(userList.get(i)),XContentType.JSON)
            );
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否失败， false 代表成功
        System.out.println(bulkResponse.hasFailures());
    }

    /**
     * 查询
     */
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("zkw_index");
        //可以设置路由
//        searchRequest.routing("routing");
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //QueryBuilders.termQuery 精确匹配
        //QueryBuilders.matchAllQuery() 匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "hhh2");
        sourceBuilder.query(termQueryBuilder);
        //可以设置分页    不设置默认就是0 10
        sourceBuilder.from(0);
        sourceBuilder.size(6);
        //设置超时时间
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //最后放入到source中
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("==========================");
        for (SearchHit hit : searchResponse.getHits()) {
            System.out.println("hit===="+hit);
            System.out.println(".............");
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 聚合查询方式 去重统计
     */
    @Test
    void testCardinality() throws IOException {
        //先创建一个
        SearchRequest request = new SearchRequest("zkw_index");

        //指定使用的聚合查询方式
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //
        builder.aggregation(AggregationBuilders.cardinality("统计结果").field("age"));

        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //获取返回结果
        Cardinality agg = response.getAggregations().get("统计结果");
        System.out.println(agg.getValue());
    }

    /**
     * 聚合查询 范围查询
     */
    @Test
    void testBbAA() throws IOException {
        //先创建一个
        SearchRequest request = new SearchRequest("zkw_index");

        //指定使用的聚合查询方式
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //
        builder.aggregation(AggregationBuilders.range("统计结果").field("age")
                                                .addUnboundedTo(5)
                                                .addRange(5,10)
                                                .addUnboundedFrom(10)
        );

        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //获取返回结果
        Range agg = response.getAggregations().get("统计结果");
        for (Range.Bucket bucket : agg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            Object from = bucket.getFrom();
            Object to = bucket.getTo();
            long docCount = bucket.getDocCount();

        }
    }

    /**
     * 聚合查询方式  统计聚合查询某一个字段
     */
    @Test
    void testExtendedStats() throws IOException {
        //先创建一个
        SearchRequest request = new SearchRequest("zkw_index");

        //指定使用的聚合查询方式
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //
        builder.aggregation(AggregationBuilders.extendedStats("统计结果").field("age"));

        request.source(builder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //获取返回结果
        ExtendedStats agg = response.getAggregations().get("统计结果");

        System.out.println(agg.getMax());

    }

    @Test
    void testQuest(){

    }
}
