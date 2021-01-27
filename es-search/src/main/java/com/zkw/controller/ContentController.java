package com.zkw.controller;

import com.zkw.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author zkw
 * @date 2020-12-17
 **/
@RestController
public class ContentController {
    @Autowired
    private ContentService  contentService;

    @GetMapping("/parse/{keyword}")
    Boolean parse(@PathVariable("keyword") String keyword) throws Exception {
        return contentService.parseContent(keyword);
    }

    @GetMapping("/search/{keyword}/{pageNum}/{pageSize}")
    List<Map<String,Object>> search(@PathVariable("keyword")String keyword,
                                    @PathVariable("pageNum")  int pageNo,
                                    @PathVariable("pageSize") int pageSize) throws IOException {
           return contentService.searchHighLightPage(keyword, pageNo, pageSize);
    }
}
