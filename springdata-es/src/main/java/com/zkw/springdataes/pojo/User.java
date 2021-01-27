package com.zkw.springdataes.pojo;

import lombok.Data;
import lombok.ToString;
import org.elasticsearch.common.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * @author zkw
 * @date 2020-12-25
 **/
@Data
@ToString
@Document(indexName = "my_user")
public class User implements Persistable<String> {
    @Id
    @Nullable
    private String id;

    @Field(value = "last-name", type = FieldType.Keyword)
    private String lastName;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Integer)
    private Integer age;

    @Nullable @Field(name = "birth-date", type = FieldType.Date, format = DateFormat.basic_date)
    private Date birthDate;

    @Field(type = FieldType.Boolean)
    private Boolean isDeleted;
    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private Date createTime;
    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private Date updateTime;

    @Override
    public boolean isNew() {
        return id == null || (createTime == null);
    }
}

