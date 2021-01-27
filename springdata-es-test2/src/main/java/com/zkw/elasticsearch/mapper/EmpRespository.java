package com.zkw.elasticsearch.mapper;

import com.zkw.elasticsearch.pojo.Emp;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author zkw
 * @date 2021-01-07
 **/
public interface EmpRespository extends ElasticsearchRepository<Emp,String> {
}
