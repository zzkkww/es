package com.zkw;

import com.zkw.elasticsearch.mapper.EmpRespository;
import com.zkw.elasticsearch.pojo.Emp;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.UUID;

/**
 * @author zkw
 * @date 2021-01-07
 **/

@SpringBootTest
public class TestEmpRespository {
    @Autowired
    private EmpRespository empRespository;

    /**
     * 保存一条对象
     */
    @Test
    void testSave(){
        Emp s = new Emp();
        s.setId(UUID.randomUUID().toString());
        s.setName("张三丰");
        s.setBir(new Date());
        s.setAge(23);
        s.setAddress("武当山学院");
        s.setContent("武侠大师，一生创建多种武功，如太极，武当剑法");
        empRespository.save(s);
    }

    @Test
    void testDelete(){
        empRespository.deleteById("xxx");
    }

    @Test
    void testDeleteAll(){
        empRespository.deleteAll();
    }

    @Test
    void testFind(){
        Iterable<Emp> all = empRespository.findAll(Sort.by(Sort.Order.asc("age")));
        all.forEach(emp -> {
            System.out.println(emp);
        });
    }

    @Test
    void testFindPage(){
        Page<Emp> search = empRespository.search(QueryBuilders.matchAllQuery(), PageRequest.of(0, 20));
        search.forEach(emp -> System.out.println(emp));


    }
}
