package com.sz.winter.basereptile.module.chabijiujiu;

import com.alibaba.fastjson.JSONObject;
import com.sz.winter.basereptile.config.Constant;
import com.sz.winter.basereptile.config.RedisService;
import com.sz.winter.basereptile.model.Classify;
import com.sz.winter.basereptile.model.ClassifyList;
import com.sz.winter.basereptile.model.VideoFragmentation;
import com.sz.winter.basereptile.model.resp.VideoFragmentationInfoResp;
import com.sz.winter.basereptile.task.chabijiujiu.DowloadVideoTask;
import com.sz.winter.basereptile.util.BaseUtil;
import com.sz.winter.basereptile.util.HttpUtil;
import com.sz.winter.basereptile.util.HttpUtilWithPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ClassifyListModule extends Endpoint {
    //获取嵌入页面地址API
    private static String apiurl = "http://23.225.123.161:1433/api.php";

    Logger logger = LoggerFactory.getLogger(getClass());

    String cookie = "member=YWwSVlPhERgOxyXZivvSXKQe5F7QO82smQIO6AEj2cGj4jHubrSbD%2B68evP%2BAZ6Bbtv1ag9blw7VAR6fq9f2X18E8lBMJ3i9rvhHWAlTb075J4on5ifuKQOTeXdiHh%2BN4mds1B0bQb%2FViLAOpnhn%2F26XB%2F%2FjB1ZHVK6zkafTbn3TAxFtkgCbYZ51W2hkF4PwSPfwuhL7fsZ5oonK06iK4FqSZpw%2FjqZmeTRQmVkYyIw%3D; path=/";

    /**
     * <p>
     * 获取类别列表封面信息
     * </p>
     */
    public void getClassifyList() {
        try {
            //查询当前网站分类列表新消息
            Classify classify = new Classify();
            classify.setWebsitId(getWebsitId());
            classify.setStatus(1);
            List<Classify> classifyByCondition = classifyService.getClassifyByCondition(classify);

            int size = 0;//classifyByCondition.size();
            for (int i = 0; i < size; i++) {
                Classify cif = classifyByCondition.get(i);

                //页码
                int pageNo = 0;

                while (true) {
                    try {
                        Map<String, Object> cookieMap = new HashMap<>();
                        //cookieMap.put("Cookie",cookie);

                        String result = HttpUtil.doGet(cif.getStartListUrl().replaceAll("<code>", String.valueOf(++pageNo)), cookieMap, null, null);

                        Document parse = Jsoup.parse(result);

                        Elements elementsByClass = parse.getElementsByClass("thumb-wrap");

                        if (elementsByClass == null || elementsByClass.size() == 0)
                            break;

                        List<ClassifyList> classifyLists = new ArrayList<>();
                        for (Element element : elementsByClass) {
                            try {
                                //详情页url
                                String contextUrl = getHost() + element.getElementsByTag("a").attr("href");
                                //封面图url
                                String surfacePlotUrl = element.getElementsByTag("img").attr("src");

                                char[] contextResult = element.getElementsByClass("title").html().split("\"")[1].toCharArray();
                                //视频内容描述
                                String describe = BaseUtil.conversionString(contextResult);

                                char[] durationResult = element.getElementsByClass("time").html().split("\"")[1].toCharArray();
                                //视频时长
                                String duration = BaseUtil.conversionString(durationResult);

                                //相对热度
                                char[] heatResult = element.getElementsByClass("views").first().html().split("\"")[1].toCharArray();
                                String heat = BaseUtil.conversionString(heatResult);

                                ClassifyList classifyList = new ClassifyList(cif.getWebsitId(), cif.getId(), surfacePlotUrl, describe, duration, heat, contextUrl);
                                classifyList.setCreateDate(new Date());
                                classifyList.setLastUpdate(new Date());
                                classifyLists.add(classifyList);
                            } catch (Exception ex) {
                                logger.error("{}", ex);
                            }
                        }

                        //批量插入分类列表信息
                        classifyListService.insertBatchClassifys(classifyLists);
                        logger.info("================批量插入分类信息成功:类别:[" + cif.getClassify() + "],页码:[" + pageNo + "],条数:[" + classifyLists.size() + "]=========================");

                        Thread.sleep(BaseUtil.random(1500, 2800));
                    } catch (Exception ex) {
                        logger.error("{}", ex);
                        --pageNo;
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("==============获取类别列表封面信息异常:{}============================", ex);
        }
    }


    /**
     * <p>
     * 获取视频分片信息
     * </p>
     */
    public void getVideoInfo() {
        try {
            Pipeline pipeline = RedisService.getPipeline();

            //获取当前网站所有类别
            Classify classify = new Classify();
            classify.setWebsitId(getWebsitId());
            classify.setStatus(1);
            List<Classify> classifies = classifyService.getClassifyByCondition(classify);

            for (Classify cfi : classifies) {
                String keys = Constant.CLASSIFY_LIST + cfi.getWebsitId() + ":" + cfi.getId();
                pipeline.smembers(keys);
            }

            //管道操作获取所有视频列表信息
            List<Object> objectList = pipeline.syncAndReturnAll();

            for (Object object : objectList) {

                //List<ClassifyList> cfl = JSONObject.parseArray(object.toString(), ClassifyList.class);
                //获取未拉去的视频信息列表
                List<ClassifyList> cfl = classifyListService.getNotDowloadVideo();

                int size = cfl.size();
                for (int i = 0; i < size; i++) {
                    Map<String, Object> cookieMap = new HashMap<>();
                    cookieMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
                    cookieMap.put("Cache-Control", "no-cache");
                    cookieMap.put("Cookie", cookie);

                    try {
                        ClassifyList classifyList = cfl.get(i);

                        //视屏播放页URL
                        String contextUrl = classifyList.getContextUrl();

                        //cookieMap.remove("Referer");
                        String result = HttpUtil.doGet(contextUrl, cookieMap, null, null);//192.168.1.188
                        if (isContinue(result)) continue;

                        Document parse = Jsoup.parse(result);

                        String palyload = parse.getElementsByTag("script").get(26).html();
                        //获取palylaod开始下标
                        int start = palyload.indexOf("{");
                        int end = palyload.indexOf("}");

                        palyload = palyload.substring(start, end + 1).replaceAll("\'", "\"");

                        JSONObject jsonObject = JSONObject.parseObject(palyload);
                        Map<String, String> formMap = new HashMap<>();
                        formMap.put("url", jsonObject.getString("url"));

                        cookieMap.put("Referer", contextUrl);
                        //播放页面真实URL
                        String reallyUrl = HttpUtil.getContext(HttpUtil.httpDoPost(apiurl, formMap, cookieMap, null, null));
                        if (isContinue(reallyUrl)) continue;

                        //播放器页面
                        String reallyContext = HttpUtil.doGet(reallyUrl, cookieMap, null, null);
                        if (isContinue(reallyContext)) continue;

                        parse = Jsoup.parse(reallyContext);

                        //视频分片地址
                        String m3u8Url = parse.getElementsByTag("script").get(2).html().toString().split("\'")[5];

                        //视频分片信息
                        String videoFragmentationInfo = HttpUtil.doGet(m3u8Url, cookieMap, null, null);
                        if (isContinue(videoFragmentationInfo)) continue;

                        start = videoFragmentationInfo.indexOf("#EXTINF");
                        end = videoFragmentationInfo.indexOf("#EXT-X-ENDLIST");
                        videoFragmentationInfo = videoFragmentationInfo.substring(start, end);

                        StringBuilder sb = new StringBuilder();
                        for (String str : videoFragmentationInfo.split("\n")) {
                            sb.append(str);
                            if (str.contains(".jpg"))
                                sb.append(";");
                        }

                        //构建待入库的视频分片信息
                        VideoFragmentation vf = new VideoFragmentation(classifyList.getId(), sb.toString(), 0);
                        vf.setCreateDate(new Date());
                        vf.setLastUpdate(new Date());
                        vf.setStatus(1);
                        videoFragmentationService.insertVideoFragmentations(vf);

                        logger.info("[视频信息入库]:==>" + JSONObject.toJSONString(vf));

                        Thread.sleep(100);
                    } catch (Exception ex) {
                        logger.error("{}", ex);
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("获取[" + getName() + "]视频信息异常:{}", ex);
        }
    }


    /**
     * <p>
     * 判断当前页面字符串中是否包含404,500字样
     * </p>
     *
     * @param str 待匹配字符
     * @return boolean
     */
    private boolean isContinue(String str) {
        return str.contains("<title>404") || str.contains("<title>500");

    }


    /**
     * <p>
     * 下载视频封面图
     * </p>
     */
    public void dowloadSurfacePlot() {
        try {
            Pipeline pipeline = RedisService.getPipeline();

            //获取当前网站所有类别
            Classify classify = new Classify();
            classify.setWebsitId(getWebsitId());
            classify.setStatus(1);
            List<Classify> classifies = classifyService.getClassifyByCondition(classify);

            for (Classify cfi : classifies) {
                String keys = Constant.CLASSIFY_LIST + cfi.getWebsitId() + ":" + cfi.getId();
                pipeline.smembers(keys);
            }

            //管道操作获取所有视频列表信息
            List<Object> objectList = pipeline.syncAndReturnAll();

            for (Object object : objectList) {
                //获取未拉去的视频信息列表
                //List<ClassifyList> cfl = classifyListService.getNotDowloadImage();
                List<ClassifyList> cfl = JSONObject.parseArray(object.toString(), ClassifyList.class);

                int size = cfl.size();
                for (int i = 0; i < size; i++) {
                    ClassifyList classifyList = cfl.get(i);

                    try {
                        //封面图url
                        String surfacePlotUrl = classifyList.getSurfacePlotUrl();

                        //拼接完整磁盘路径文件名
                        String fileName = getSurfacePlotDir() + System.currentTimeMillis() + BaseUtil.random(1, 999) + getSurfacePlotsuffix();

                        boolean flag = BaseUtil.dowloadNerworkFile(surfacePlotUrl, fileName, false);

                        if (flag) {
                            ClassifyList cl = new ClassifyList();
                            cl.setId(classifyList.getId());
                            cl.setSurfacePlotDir(fileName);
                            classifyListService.updateClassifys(cl);
                        }

                        Thread.sleep(100);
                    } catch (Exception ex) {
                        logger.error("{}", ex);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("下载视频封面图异常:{}", ex);
        }
    }

    /**
     * <p>
     * 下载未下载的视频
     * </p>
     */
    public void dowloadVideo() {
        try {
            //加载未下载的及下载中的
            VideoFragmentation vft = new VideoFragmentation();
            vft.setDowloadType(1);
            List<VideoFragmentationInfoResp> videoFragmentationList = videoFragmentationService.getVideoFragmentationInfoRespv1(vft);

            int size = videoFragmentationList.size();
            for (int i = 0; i < size; i++) {
                VideoFragmentationInfoResp resp = videoFragmentationList.get(i);
                DowloadVideoTask dowloadVideoTask = new DowloadVideoTask(resp,classifyListService,videoFragmentationService,this);
                //暂时放弃多线程策略
                //taskExecutor.execute(dowloadVideoTask);

                dowloadVideoTask.run();

                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            logger.error("下载未下载的视频异常:{}", ex);
        }
    }
}
