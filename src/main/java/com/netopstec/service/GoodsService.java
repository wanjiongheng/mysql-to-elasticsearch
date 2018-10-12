package com.netopstec.service;

import com.netopstec.dao.GoodsMapper;
import com.netopstec.entity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    public List<Goods> findAll() {
        return goodsMapper.findAll();
    }
}
