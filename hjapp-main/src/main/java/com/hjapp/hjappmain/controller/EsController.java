package com.hjapp.hjappmain.controller;

import com.alibaba.fastjson.JSONObject;
import com.hjapp.elasticsearch.ElasticsearchUtil;
import io.netty.util.internal.StringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

/**
 * @Classname EsController
 * @Description TODO
 * @Date 2019/11/30 17:12
 * @Created by fanghoujun
 */
@RestController
@RequestMapping("/es")
public class EsController {

    private static final Logger logger = LoggerFactory.getLogger(EsController.class);

    /**
     * 测试索引
     */
    private String indexName="book";

    /**
     * 类型
     */
    private String esType="book";

    /**
     * 首页
     * @return
     */
    @RequestMapping("/index")
    public String index(){
        return "index";
    }


    /**
     *  http://localhost:8080/es/createIndex
     * 创建索引
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/createIndex")
    @ResponseBody
    public String createIndex(HttpServletRequest request, HttpServletResponse response) {
        if (!ElasticsearchUtil.checkIndexExist(indexName)) {
            if (ElasticsearchUtil.createIndex(indexName)) {
                return "索引创建成功";
            } else {
                return "索引已经失败";
            }
        } else {
            return "索引已经存在";
        }
    }

    /**
     * 插入记录
     * http://localhost:8080/es/addData
     * @return
     */
//    @RequestMapping("/addData")
//    @ResponseBody
//    public String addData() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("id", DateUtil.formatDate(new Date()));
//        jsonObject.put("age", 29);
//        jsonObject.put("name", "liming");
//        jsonObject.put("date", new Date());
//        String id=ElasticsearchUtil.addData(indexName, esType, jsonObject.getString("id"), jsonObject);
//        if(StringUtil.isNotBlank(id)){
//            return "插入成功";
//        }
//        else{
//            return "插入失败";
//        }
//    }

    /**
     * 查询所有
     * @return
     */
    @RequestMapping("/queryAll")
    @ResponseBody
    public String queryAll() {
        try {
            HttpEntity entity = new NStringEntity(
                    "{ \"query\": { \"match_all\": {}}}",
                    ContentType.APPLICATION_JSON);
            String endPoint = "/" + indexName + "/" + esType + "/_search";
            Response response = ElasticsearchUtil.getLowLevelClient().performRequest("POST", endPoint, Collections.<String, String>emptyMap(), entity);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "查询数据出错";
    }

    /**
     * 根据条件查询
     * @return
     */
    @RequestMapping("/queryByMatch")
    @ResponseBody
    public String queryByMatch(){
        try {
            String endPoint = "/" + indexName + "/" + esType + "/_search";

            IndexRequest indexRequest = new IndexRequest();
            XContentBuilder builder;
            try {
                builder = JsonXContent.contentBuilder()
                        .startObject()
                        .startObject("query")
                        .startObject("match")
                        .field("name.keyword", "zjj")
                        .endObject()
                        .endObject()
                        .endObject();
                indexRequest.source(builder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String source = indexRequest.source().utf8ToString();

            logger.info("source---->"+source);

            HttpEntity entity = new NStringEntity(source, ContentType.APPLICATION_JSON);

            Response response = ElasticsearchUtil.getLowLevelClient().performRequest("POST", endPoint, Collections.<String, String>emptyMap(), entity);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "查询数据出错";
    }

    /**
     * 复合查询
     * @return
     */
    @RequestMapping("/queryByCompound")
    @ResponseBody
    public String queryByCompound(){
        try {
            String endPoint = "/" + indexName + "/" + esType + "/_search";

            IndexRequest indexRequest = new IndexRequest();
            XContentBuilder builder;
            try {
                /**
                 * 查询名字等于 liming
                 * 并且年龄在30和35之间
                 */
                builder = JsonXContent.contentBuilder()
                        .startObject()
                        .startObject("query")
                        .startObject("bool")
                        .startObject("must")
                        .startObject("match")
                        .field("name.keyword", "liming")
                        .endObject()
                        .endObject()
                        .startObject("filter")
                        .startObject("range")
                        .startObject("age")
                        .field("gte", "30")
                        .field("lte", "35")
                        .endObject()
                        .endObject()
                        .endObject()
                        .endObject()
                        .endObject()
                        .endObject();
                indexRequest.source(builder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String source = indexRequest.source().utf8ToString();

            logger.info("source---->"+source);

            HttpEntity entity = new NStringEntity(source, ContentType.APPLICATION_JSON);

            Response response = ElasticsearchUtil.getLowLevelClient().performRequest("POST", endPoint, Collections.<String, String>emptyMap(), entity);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "查询数据出错";
    }

    /**
     * 删除查询的数据
     * @return
     */
    @RequestMapping("/delByQuery")
    @ResponseBody
    public String delByQuery() {

        String deleteText = "chy";

        String endPoint = "/" + indexName + "/" + esType + "/_delete_by_query";

        /**
         * 删除条件
         */
        IndexRequest indexRequest = new IndexRequest();
        XContentBuilder builder;
        try {
            builder = JsonXContent.contentBuilder()
                    .startObject()
                    .startObject("query")
                    .startObject("term")
                    //name中包含deleteText
                    .field("name.keyword", deleteText)
                    .endObject()
                    .endObject()
                    .endObject();
            indexRequest.source(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String source = indexRequest.source().utf8ToString();

        HttpEntity entity = new NStringEntity(source, ContentType.APPLICATION_JSON);
        try {
            Response response = ElasticsearchUtil.getLowLevelClient().performRequest("POST", endPoint, Collections.<String, String>emptyMap(), entity);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "删除错误";
    }

    /**
     * 演示聚合统计
     * @return
     */
    @RequestMapping("/aggregation")
    @ResponseBody
    public String aggregation(){
        try {
            String endPoint = "/" + indexName + "/" + esType + "/_search";

            IndexRequest indexRequest = new IndexRequest();
            XContentBuilder builder;
            try {
                builder = JsonXContent.contentBuilder()
                        .startObject()
                        .startObject("aggs")
                        .startObject("名称分组结果")
                        .startObject("terms")
                        .field("field", "name.keyword")
                        .startArray("order")
                        .startObject()
                        .field("_count", "asc")
                        .endObject()
                        .endArray()
                        .endObject()
                        .endObject()
                        .endObject()
                        .endObject();
                indexRequest.source(builder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String source = indexRequest.source().utf8ToString();

            logger.info("source---->"+source);

            HttpEntity entity = new NStringEntity(source, ContentType.APPLICATION_JSON);

            Response response = ElasticsearchUtil.getLowLevelClient().performRequest("POST", endPoint, Collections.<String, String>emptyMap(), entity);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "查询数据出错";
    }

}
