package com.zkw.springdataes.mapper;

import com.zkw.springdataes.pojo.Book;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author zkw
 * @date 2020-12-25
 **/
public interface IBookMapper extends ElasticsearchRepository<Book,String> {
}
