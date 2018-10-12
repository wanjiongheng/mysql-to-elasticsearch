import com.alibaba.fastjson.JSONObject;
import com.netopstec.entity.Goods;
import com.netopstec.service.GoodsService;
import com.netopstec.util.ElasticsearchClient;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.fetch.subphase.highlight.UnifiedHighlighter;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:spring/spring-total.xml")
public class Test {


    public static final Logger LOGGER = Logger.getLogger(Test.class);

    @Autowired
    private GoodsService goodsService;


    @org.junit.Test
    public void getAll() {
        List<Goods> all = goodsService.findAll();
        LOGGER.error(all.size());
        System.out.println(all);
    }


    @org.junit.Test
    public void dataMysql2Elasticsearch() {
        List<Goods> all = goodsService.findAll();
        Map<String, String> idJsonContentMap = new HashMap<>();
        all.forEach(goods -> {
            Long id = goods.getId();
            String s = JSONObject.toJSONString(goods);
            idJsonContentMap.put(id.toString(),s);
        });
        boolean b = ElasticsearchClient.insertBatch("test_goods", "goods", idJsonContentMap);
        System.out.println("执行结果：" + b);
    }



    @org.junit.Test
    public void search() throws IOException {
        // 创建searchRequest
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("test_goods");
        searchRequest.types("goods");

        // 创建searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // searchSourceBuilder.query(QueryBuilders.termQuery("brand", "森马"));
        // 设置索引开始，显示多少条
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        // 设置搜索10秒
        searchSourceBuilder.timeout(TimeValue.timeValueSeconds(10));

        // searchSourceBuilder.query(QueryBuilders.matchQuery("brand","森马").fuzziness(Fuzziness.AUTO).prefixLength(3).maxExpansions(10));


        // 排序
        // 倒序排列
        // searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.ASC));

        // 只返回部分的字段,需要干逼
        // searchSourceBuilder.fetchSource(new String[]{}, new String[]{});

        // 因为下面接受了高亮显示，创建高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        field.highlighterType("unified");
        highlightBuilder.field(field);
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);


        // 同步执行
        SearchResponse searchResponse = ElasticsearchClient.getClient().search(searchRequest);
        RestStatus status = searchResponse.status();
        System.out.println("返回的状态码：" + status);
        // 请求时间
        TimeValue take = searchResponse.getTook();
        System.out.println("请求时间：" + take.toString());
        // 是否是提前中止
        Boolean terminatedEarly = searchResponse.isTerminatedEarly();
        System.out.println("是否提前中止：" + terminatedEarly);
        boolean timedOut = searchResponse.isTimedOut();
        System.out.println("是否超时：" + timedOut);

        // 处理文档
        SearchHits hits = searchResponse.getHits();
        // 总数
        long totalHits = hits.getTotalHits();
        // 最大分数
        float maxScore = hits.getMaxScore();
        
        // 遍历每个搜索结果
        SearchHit[] hitArray = hits.getHits();

        List<Goods> goodsList = new ArrayList<>();
        for (SearchHit hit : hitArray) {
            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();

            String sourceAsString = hit.getSourceAsString();
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            goodsList.add(goods);

            // 检索出来突出显示
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get("name");
            if (highlight == null) continue;
            Text[] fragments = highlight.getFragments();
            System.out.println("高亮显示的是：" + fragments);
        }
        System.out.println(goodsList);
    }
}
