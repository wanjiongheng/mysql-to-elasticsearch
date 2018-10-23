package com.netopstec.entity.esentity;

import com.netopstec.annotation.es.ESEntity;
import com.netopstec.annotation.es.ESField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@ESEntity
public class ESGoods {


    private Long id;
    private String name;
    private String brand;
    private String price;
    private String note;
    private Long createTime;
    private Long modifyTime;
    private Integer useState;
    private Integer flag;

    private String name_pinyin;
    private String brand_pinyin;

}
