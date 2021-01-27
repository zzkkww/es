package com.zkw.service;

import com.alibaba.fastjson.JSON;
import com.zkw.elasticsearch.pojo.Content;
import com.zkw.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zkw
 * @date 2020-12-17
 **/
@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private HtmlParseUtil htmlParseUtil;

    /**
     * 解析数据放入es索引中
     */
    public Boolean parseContent(String keywords) throws Exception {
        List<Content> contents = new HtmlParseUtil().parseJD(keywords);
        //把查询到的数据放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2ms");

        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_index")
            .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        //返回成功
        return !bulk.hasFailures();
    }


    /**
     * 获取这些数据实现搜索功能
     */
    public List<Map<String,Object>> searchPage(String keyword,int pageNum,int pageSize) throws IOException {
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //精确匹配  条件构造器
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);

        //进行分页
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);

        //最后丢进去
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();

        for (SearchHit hit : searchResponse.getHits()) {
            list.add(hit.getSourceAsMap());
        }
        return list;
    }


    /**
     * 获取这些数据实现搜索功能   实现高亮
     */
    public List<Map<String,Object>> searchHighLightPage(String keyword,int pageNum,int pageSize) throws IOException {
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //精确匹配  条件构造器
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        //这样写是为了如果 在一个结果有多个相同的字段 则只高亮一个字段 不写的话 就全部高亮
        highlightBuilder.requireFieldMatch(false);
        //前缀的标签   相当于html中的<>  </>
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        sourceBuilder.highlighter(highlightBuilder);

        //进行分页
        sourceBuilder.from(pageNum);
        sourceBuilder.size(pageSize);

        //最后丢进去
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();

        for (SearchHit hit : searchResponse.getHits()) {
            //原来的结果
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            //解析高亮的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            if (title!=null){
                //就是把原来的结果中没有高亮的字段替换成现在有的高亮字段
                Text[] fragments = title.fragments();
                String newTitle="";
                for (Text text : fragments) {
                    newTitle +=text;
                }
                //把原来的字段用高亮字段替换掉
                sourceAsMap.put("title",newTitle);
            }

            list.add(sourceAsMap);
        }
        return list;
    }

}
