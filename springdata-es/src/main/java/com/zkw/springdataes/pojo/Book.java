package com.zkw.springdataes.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * @author zkw
 * @date 2020-12-25
 **/
public class Book implements Serializable {
    private static final long serialVersionUID = -6960385691211863901L;

    @Id     //主键
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String context;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String auto;
}
