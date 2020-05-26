package com.sz.winter.basereptile.init;


import com.alibaba.fastjson.JSONObject;
import com.sz.winter.basereptile.config.Constant;
import com.sz.winter.basereptile.config.RedisService;
import com.sz.winter.basereptile.model.ClassifyList;
import com.sz.winter.basereptile.model.VideoFragmentation;
import com.sz.winter.basereptile.model.resp.VideoFragmentationInfoResp;
import com.sz.winter.basereptile.module.chabijiujiu.ClassifyListModule;
import com.sz.winter.basereptile.module.chabijiujiu.Login;
import com.sz.winter.basereptile.service.ClassifyListService;
import com.sz.winter.basereptile.service.VideoFragmentationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;

import java.util.List;

@Service
public class Init implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Login login;

    @Autowired
    private ClassifyListModule classifyListModule;

    @Autowired
    private ClassifyListService classifyListService;

    @Autowired
    private VideoFragmentationService videoFragmentationService;

    @Autowired
    private TaskExecutor taskExecutor;

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
            //loadClassifyListInfoToRedis();
            //加载视频分片信息到Redis中
            loadvideoFragmentationToRedis();

            //获取视频分信息
            //new Thread(()->classifyListModule.getVideoInfo()).start();

            //下载视频封面图
            //new Thread(() -> classifyListModule.dowloadSurfacePlot()).start();

            //下载视频
            taskExecutor.execute(new Thread(() ->classifyListModule.dowloadVideo()));

        } catch (Exception ex) {
            logger.error("==============项目启动异常:{}============================", ex);
        }
    }


    /**
     * 加载列表信息到Redis中
     */
    private void loadClassifyListInfoToRedis() {
        try {
            List<ClassifyList> classifyLists = classifyListService.listCalssifyList();
            Pipeline pipeline = RedisService.getPipeline();

            /*//key:网站ID   value:<key:类别ID>
            Map<Long, Map<Long, List<ClassifyList>>> collect =
                    classifyLists
                            .stream()
                            .collect(Collectors.groupingBy(ClassifyList::getWebsitId, Collectors.groupingBy(ClassifyList::getClassifyId)));

            collect.forEach((key,value)-> value.forEach((k, v)->{
                String keys = Constant.CLASSIFY_LIST + key;

                pipeline.hset(keys,k.toString(), JSONObject.toJSONString(value));
            }));*/

            int size = classifyLists.size();
            for (int i = 0; i < size; i++) {
                ClassifyList classifyList = classifyLists.get(i);

                String keys = Constant.CLASSIFY_LIST + classifyList.getWebsitId() + ":" + classifyList.getClassifyId();
                pipeline.sadd(keys, JSONObject.toJSONString(classifyList));
            }

            pipeline.sync();
            pipeline.close();

            logger.info("================加载视频列表信息到Redis中完成!!!============================");
        } catch (Exception ex) {
            logger.error("加载列表信息到Redis中异常:{}", ex);
        }
    }


    /**
     * <p>
     *     加载视频分片信息到Redis中
     * </p>
     */
    public void loadvideoFragmentationToRedis() {
        try {
            //加载未下载的
            VideoFragmentation vf = new VideoFragmentation();
            vf.setDowloadType(0);
            List<VideoFragmentationInfoResp> videoFragmentationList = videoFragmentationService.getVideoFragmentationInfoRespv2(vf);

            Pipeline pipeline = RedisService.getPipeline();

            int size = videoFragmentationList.size();
            for (int i = 0; i < size; i++) {
                try {
                    VideoFragmentationInfoResp resp = videoFragmentationList.get(i);

                    //redis key
                    String key = Constant.VIDEO_FRAGMENTATION + resp.getWebsiteName() + ":" + resp.getClassifyId() + ":" + resp.getClassifyListId();

                    String[] palyloads = resp.getPalyload().trim().replaceAll("\n","").split(";");

                    Integer num = 0;
                    for(String palyload : palyloads){
                        try {
                            //ts/5771kFo2ZYxI-U53hudJlWdqquusuULQy5l1he27cqwwIvLpxYlutXhiU4wAoMvq4NE3tqlYuQU.jpg
                            String fg = palyload.split(",")[1];

                            pipeline.zadd(key,num.doubleValue(),fg);
                            num++;
                        }catch (Exception ex){
                            logger.error("{}",ex);
                        }
                    }

                    pipeline.sync();

                }catch (Exception ex){
                    logger.error("{}",ex);
                }
            }

            pipeline.close();

            logger.info("===============加载视频分片信息["+size+"]到Redis中成功!!=====================");

        } catch (Exception ex) {
            logger.error("加载视频分片信息到Redis中异常:{}", ex);
        }
    }
}
