package com.sz.winter.basereptile.init;


import com.alibaba.fastjson.JSONObject;
import com.sz.winter.basereptile.config.Constant;
import com.sz.winter.basereptile.config.RedisService;
import com.sz.winter.basereptile.model.ClassifyList;
import com.sz.winter.basereptile.module.chabijiujiu.ClassifyListModule;
import com.sz.winter.basereptile.module.chabijiujiu.Login;
import com.sz.winter.basereptile.service.ClassifyListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class Init implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Login login;

    @Autowired
    private ClassifyListModule classifyListModule;

    @Autowired
    private ClassifyListService classifyListService;

    @Value("${base.runWebsite.ids}")
    private String runWebsiteIds;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            //login.getUserCookie();
            //new Thread(()->classifyListModule.getClassifyList()).start();

            /*String[] split = runWebsiteIds.split(",");
            for(String websiteId : split){

            }*/

            //加载列表信息到Redis中
            loadClassifyListInfoToRedis();


        }catch (Exception ex){
            logger.error("==============项目启动异常:{}============================",ex);
        }
    }


    /**
     * 加载列表信息到Redis中
     */
    private void loadClassifyListInfoToRedis(){
        try {
            List<ClassifyList> classifyLists = classifyListService.listCalssifyList();

            //key:网站ID   value:<key:类别ID>
            Map<Long, Map<Long, List<ClassifyList>>> collect =
                    classifyLists
                            .stream()
                            .collect(Collectors.groupingBy(ClassifyList::getWebsitId, Collectors.groupingBy(ClassifyList::getClassifyId)));

            Pipeline pipeline = RedisService.getPipeline();

            collect.forEach((key,value)-> value.forEach((k, v)->{
                String keys = Constant.CLASSIFY_LIST + key;

                pipeline.hset(keys,k.toString(), JSONObject.toJSONString(value));
            }));

            pipeline.sync();
            pipeline.close();

        }catch (Exception ex){
            logger.error("加载列表信息到Redis中异常:{}",ex);
        }
    }
}
