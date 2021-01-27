package com.zkw;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.AcknowledgedResponse;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.rankeval.*;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;

@SpringBootTest
class SpringdataEsTest2ApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {
        System.out.println("测试二");
    }

    @Test
    void testConnection() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("zzkkww_index");
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(response.toString());
    }

    @Test
    void testQuest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("zzkkww_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 查询所有数据
     * @throws IOException
     */
    @Test
    void testAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("test_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchRequest.source(searchSourceBuilder.query(QueryBuilders.matchAllQuery()));

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 根据条件查询   注意我这里使用了termQuery查询不出来数据  貌似是rest client客户端的问题
     * 如果要精确匹配直接使用matchPhraseQuery就可以了
     */
    @Test
    void testSimple() throws IOException {
        SearchRequest searchRequest = new SearchRequest().indices("test_index");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //自定义高亮标签
        //设置高亮
        String preTags = "<strong>";
        String postTags = "</strong>";
        //构造高亮查询器
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags(preTags).postTags(postTags);
        searchSourceBuilder
                .query(QueryBuilders.matchPhraseQuery("name","王五"))
                .from(0)
                .highlighter(highlightBuilder)
                .size(5)
                //进过上面的处理后再后置处理过滤条件
                .postFilter(QueryBuilders.rangeQuery("age").lt(30));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println("文本里面的消息时"+hit.getSourceAsMap().get("name"));
            System.out.println(hit.getSourceAsMap());
            //这里得到高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println(highlightFields.toString());
        }
    }


    /**
     * 测试布尔值 之后再过滤
     */
    @Test
    void testFilter() throws IOException {
        SearchRequest searchRequest = new SearchRequest("megacorp");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //last_name 精确匹配Simith
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("last_name", "Smith");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchPhraseQueryBuilder);


        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
            System.out.println(hit.getIndex());
            System.out.println(hit.getScore());
            System.out.println(hit.toString());;

        }
    }


    /**
     *
     * 查询结果的第一行会有:
     *
     * "_scroll_id": "DnF1ZXJ5VGhlbkZldGNoBAAAAAAABO-dFmRFSU9NM1VNU2JxNG9UUlNnSmpXMVEAAAAAAL7J_hYxT0dJOVJVMVFxU2I0N2xCR2IyVzJnAAAAAAC-　　　　SmQWWk1aN0sxUmRSQmFNS3EwVFh0R0luUQAAAAAAvkplFlpNWjdLMVJkUkJhTUtxMFRYdEdJblE=",
     *
     * 这个_scroll_id就相当于书签,之后的查询带着这个书签,就能根据size不断拿到之后的数据,但是前提是在过期时间之内
     *
     * 测试游标的滚动查询
     * 注意点：在默认情况下,ES查询每次返回的数量最多只有1W条,且只能是前1W条.
     * 这意味着,在不修改配置的情况下,想通过分页的方式(如下)拿到1W条之后的数据是做不到的
     * 所以使用游标的方式,相当于mysql中生成快照的方式,所以如果在游标查询期间有增删改操作,是获取不到最新的数据的.
     */
    @Test
    void testScroll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("test_index");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery())
                .size(1);

        String scrollId=null;
        searchRequest
                .source(searchSourceBuilder)
                .scroll(TimeValue.timeValueSeconds(1L));

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println("========================");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
        System.out.println("========================");

        //这一串是获取到的滚动ID  :DXF1ZXJ5QW5kRmV0Y2gBAAAAAAAFA3wWNW80N0tKQ1VTbVN0UzZqNkhTY0ZVQQ==
        scrollId = searchResponse.getScrollId();
        System.out.println(searchResponse.getScrollId());
        System.out.println(searchResponse.getHits());
    }

    /**
     * 经过测试 当开启游标查询的时候 会返回一个scrollId 每一次请求这个Id都会变化，每次查询的结果都会往下执行来获取数据，
     * 而这个每次获取数据的大小根据前面定义的size来。当达到最后的时候不会返回信息，记住开启了游标查询记得设计游标过期的时间
     * ，及时的释放ES的集群资源、或者自己手动的释放
     * @throws IOException
     */
    @Test
    void testScroll2() throws IOException {


            String scrollId="DXF1ZXJ5QW5kRmV0Y2gBAAAAAAAFVzoWNW80N0tKQ1VTbVN0UzZqNkhTY0ZVQQ==";

            //进行第二步的操作 现在才是根据滚动标识符来获取数据
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(TimeValue.timeValueSeconds(30));
            SearchResponse searchScrollResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchScrollResponse.getScrollId();
            for (SearchHit inhit : searchScrollResponse.getHits().getHits()) {
                System.out.println("输出这里的的点点滴滴"+inhit.getSourceAsMap());
                System.out.println("输出这里");
                System.out.println(scrollId);
            }
        }

    /**
     * 最后清除游标
      */
    @Test
    void clearScrollId() throws IOException {

        String scrollId="DXF1ZXJ5QW5kRmV0Y2gBAAAAAAAFV6kWNW80N0tKQ1VTbVN0UzZqNkhTY0ZVQQ==   ";
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);

        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        System.out.println(clearScrollResponse.isSucceeded());
        System.out.println("========"+scrollId);
    }

    /**
     * Multi-Search API  这次测试的是批量查询数据，通过2个请求 一起multi执行
     * 得到的数据如下
     */
    @Test
    void testMultiSearch() throws IOException {
        MultiSearchRequest request = new MultiSearchRequest();

        SearchRequest firstSearchRequest = new SearchRequest("test_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name","张三"));
        firstSearchRequest.source(searchSourceBuilder);
        request.add(firstSearchRequest);
        //上面是第一请求
        System.out.println("下面是第二个请求");
        SearchRequest secondSearchRequest = new SearchRequest("test_index");
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
        searchSourceBuilder1.query(QueryBuilders.matchQuery("name","王五"));
        secondSearchRequest.source(searchSourceBuilder1);
        request.add(secondSearchRequest);
        MultiSearchResponse msearch = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);
        System.out.println("查询时间"+msearch.getTook().toString());
        for (MultiSearchResponse.Item respons : msearch.getResponses()) {
            //失败的话会有信息出来
            System.out.println(respons.getFailureMessage());
            for (SearchHit hit : respons.getResponse().getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }


        System.out.println("查询多数据");
        SearchRequest searchRequest = new SearchRequest("test_index");
        SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
        searchSourceBuilder2.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder2);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
        System.out.println("全部查询出来的时间"+search.getTook().toString());

    }

    /**
     * 测试脚本来查询数据
     */
    @Test
    void testSearchTemplate() throws IOException {
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest("test_index"));
        request.setScriptType(ScriptType.INLINE);
        request.setScript(
                "{" +
                        "  \"query\": { \"match\" : { \"{{field}}\" : \"{{value}}\" } }," +
                        "  \"size\" : \"{{size}}\"" +
                        "}");
        HashMap<String, Object> scriptParams = new HashMap<String, Object>();
        scriptParams.put("field","name");
        scriptParams.put("value","张三");
        scriptParams.put("size",2);
        request.setScriptParams(scriptParams);
        SearchTemplateResponse searchTemplateResponse = restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT);
        for (SearchHit hit : searchTemplateResponse.getResponse().getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }

    /**
     * 这个api不是很熟
     * 在 build RankEvalRequest的时候，你首先需要创建一个评估规范（RankEvalSpec）
     * 测试Ranking Evaluation API ,,,,rating 1等级1
     */
    @Test
    void testRankingEvaluation() throws IOException {
        EvaluationMetric metric = new PrecisionAtK();
        ArrayList<RatedDocument> ratedDocs = new ArrayList<>();
        ratedDocs.add(new RatedDocument("test_index","1",1));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name","张三"));
        //下面开始创建ratedRequest
        RatedRequest ratedRequest = new RatedRequest("zkw_index", ratedDocs, searchSourceBuilder);
        List<RatedRequest> ratedRequests = Arrays.asList(ratedRequest);
        RankEvalSpec specification = new RankEvalSpec(ratedRequests, metric);
        RankEvalRequest request = new RankEvalRequest(specification, new String[]{"test_index"});
        RankEvalResponse response = restHighLevelClient.rankEval(request, RequestOptions.DEFAULT);
        response.getFailures().forEach((k,v)->{
            System.out.println(k);
            System.out.println(v);
        });
        double evaluationResult = response.getMetricScore();

    }

    /**
     * 测试统计结果有多少条
     */
    @Test
    void testCountApi() throws IOException {
        CountRequest countRequest = new CountRequest("test_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        countRequest.source(searchSourceBuilder);

        CountResponse count = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        long count1 = count.getCount();
        System.out.println(count1);
        System.out.println(count.getFailedShards());
        System.out.println(count.getShardStats());
    }

    /**
     * 测试获取集群节点信息的api
     */
    @Test
    void testInfoApi() throws IOException {
        MainResponse response = restHighLevelClient.info(RequestOptions.DEFAULT);
        String clusterName = response.getClusterName();
        System.out.println(clusterName);
        String clusterUuid = response.getClusterUuid();
        System.out.println(clusterUuid);
        String nodeName = response.getNodeName();
        System.out.println(nodeName);
        MainResponse.Version version = response.getVersion();
        String buildDate = version.getBuildDate();
        String buildFlavor = version.getBuildFlavor();
        String buildHash = version.getBuildHash();
        String buildType = version.getBuildType();
        String luceneVersion = version.getLuceneVersion();
        String minimumIndexCompatibilityVersion= version.getMinimumIndexCompatibilityVersion();
        String minimumWireCompatibilityVersion = version.getMinimumWireCompatibilityVersion();
        String number = version.getNumber();
    }

    /**
     * 测试分析词api
     */
    @Test
    void testAnalyze() throws IOException {

        AnalyzeRequest request = AnalyzeRequest.withGlobalAnalyzer("english",
                "Some text to analyze", "Some more text to analyze");

        AnalyzeResponse analyzeResponse = restHighLevelClient.indices().analyze(request, RequestOptions.DEFAULT);
        DetailAnalyzeResponse detail = analyzeResponse.detail();

        System.out.println(analyzeResponse.getTokens().toArray().length);

    }

    @Test
    void testCreateIndex(){
        CreateIndexRequest indexRequest = new CreateIndexRequest("ttest");
        ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                System.out.println("成功创建了");
            }

            @Override
            public void onFailure(Exception e) {

            }
        };

        restHighLevelClient.indices().createAsync(indexRequest,RequestOptions.DEFAULT,listener);
    }
}
