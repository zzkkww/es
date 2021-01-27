package com.zkw.util;

import com.zkw.elasticsearch.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkw
 * @date 2020-12-17
 **/
@Component
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        //中文的话需要自己转义 Document parse = Jsoup.parse(new URL(url), 3000);在这里有重载的方法
        new HtmlParseUtil().parseJD("java").forEach(System.out::println);

    }

    public List<Content> parseJD(String keywords) throws IOException {
        //获取请求  https://search.jd.com/Search?keyword=java
        String url="https://search.jd.com/Search?keyword="+keywords;
        //解析网页 (Jsoup返回Document就是页面对象)
        Document parse = Jsoup.parse(new URL(url), 3000);
        //所有你在js中可以使用的方法 这里都能用
        Element element = parse.getElementById("J_goodsList");

        ArrayList<Content> goodList = new ArrayList<>();
//        System.out.println(element.html());
        //获取所有的li元素
        Elements lis = element.getElementsByTag("li");
        for (Element li : lis) {
            //图片特别多的网站 图片都是延迟加载的 所以这样子拿不到
//            String img = li.getElementsByTag("img").eq(0).attr("src");
            String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = li.getElementsByClass("p-price").eq(0).text();
            String title = li.getElementsByClass("p-name").eq(0).text();
//
//            System.out.println("=========================");
//            System.out.println(img);
//            System.out.println(price);
//            System.out.println(title);
            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            goodList.add(content);
        }
        return goodList;
    }
}
