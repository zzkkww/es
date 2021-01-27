package com.zkw.elasticsearch.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * @author zkw
 * @date 2021-01-07
 *
 **/
@Data
///@Document() 用在类上 作用：将Emp的对象映射成Es中一条json格式文档
///indexName = "ems" 用来指定这个对象的转为json文档存入那个索引中 要求 ES服务器中之前不能存在次索引名字
@Document(indexName = "ems")
public class Emp {

    /**
     * 用来将对象中ID属性与文档中id --对应
     */
    @Id
    private String id;

    /**
     * 用在属性上 代表mapping中一个属性 一个字段 type:属性 用来指定字段类型
     * @Field(type = FieldType.Text)
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String name;

    @Field(type = FieldType.Integer)
    private  Integer age;

    @Field(type = FieldType.Date)
    private Date bir;

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String content;

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String address;
}
