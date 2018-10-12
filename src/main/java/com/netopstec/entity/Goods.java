package com.netopstec.entity;


import com.netopstec.annotation.es.ESEntity;
import com.netopstec.annotation.es.ESField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@ESEntity
public class Goods {

    @ESField("long")
    private Long id;
    @ESField(type = "text")
    private String name;
    @ESField(type = "text")
    private String brand;
    @ESField(type = "text")
    private String price;
    @ESField(type = "text")
    private String note;
    @ESField("long")
    private Long createTime;
    @ESField("long")
    private Long modifyTime;
    @ESField(type = "integer")
    private Integer useState;
    @ESField(type = "integer")
    private Integer flag;
}
