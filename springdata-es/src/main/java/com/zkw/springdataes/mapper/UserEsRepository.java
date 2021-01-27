package com.zkw.springdataes.mapper;

import com.zkw.springdataes.pojo.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author zkw
 * @date 2020-12-25
 **/
public interface UserEsRepository extends ElasticsearchRepository<User,String> {

}
