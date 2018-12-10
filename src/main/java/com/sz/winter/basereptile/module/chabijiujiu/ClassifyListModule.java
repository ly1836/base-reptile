package com.sz.winter.basereptile.module.chabijiujiu;

import com.sz.winter.basereptile.model.Classify;
import com.sz.winter.basereptile.model.ClassifyList;
import com.sz.winter.basereptile.util.BaseUtil;
import com.sz.winter.basereptile.util.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassifyListModule extends Endpoint {
    Logger logger = LoggerFactory.getLogger(getClass());

    String cookie = "member=YWwSVlPhERgOxyXZivvSXKQe5F7QO82smQIO6AEj2cGj4jHubrSbD%2B68evP%2BAZ6Bbtv1ag9blw7VAR6fq9f2X18E8lBMJ3i9rvhHWAlTb075J4on5ifuKQOTeXdiHh%2BN4mds1B0bQb%2FViLAOpnhn%2F26XB%2F%2FjB1ZHVK6zkafTbn3TAxFtkgCbYZ51W2hkF4PwSPfwuhL7fsZ5oonK06iK4FqSZpw%2FjqZmeTRQmVkYyIw%3D; path=/";

    /**
     * <p>
     *     获取类别列表封面信息
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

                while (true){
                    try {
                        Map<String,Object> cookieMap = new HashMap<>();
                        //cookieMap.put("Cookie",cookie);

                        String result = HttpUtil.doGet(cif.getStartListUrl().replaceAll("<code>", String.valueOf(++pageNo)),cookieMap, null, null);

                        Document parse = Jsoup.parse(result);

                        Elements elementsByClass = parse.getElementsByClass("thumb-wrap");

                        if(elementsByClass == null || elementsByClass.size() == 0)
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

                                ClassifyList classifyList = new ClassifyList(cif.getWebsitId(),cif.getId(),surfacePlotUrl,describe,duration,heat,contextUrl);
                                classifyList.setCreateDate(new Date());
                                classifyList.setLastUpdate(new Date());
                                classifyLists.add(classifyList);
                            }catch (Exception ex){
                                logger.error("{}",ex);
                            }
                        }

                        //批量插入分类列表信息
                        classifyListService.insertBatchClassifys(classifyLists);
                        logger.info("================批量插入分类信息成功:类别:["+cif.getClassify()+"],页码:["+pageNo+"],条数:["+classifyLists.size()+"]=========================");

                        Thread.sleep(BaseUtil.random(1500,2800));
                    }catch (Exception ex){
                        logger.error("{}",ex);
                        --pageNo;
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("==============获取类别列表封面信息异常:{}============================", ex);
        }
    }



}
