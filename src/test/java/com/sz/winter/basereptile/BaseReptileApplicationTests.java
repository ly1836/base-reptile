package com.sz.winter.basereptile;

import com.sz.winter.basereptile.util.BaseUtil;
import com.sz.winter.basereptile.util.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class BaseReptileApplicationTests {

    @Test
    public void contextLoads() {
        Map<String,Object> cookieMap = new HashMap<>();
        //cookieMap.put("Cookie",cookie);
        String s = HttpUtil.doGet("http://23.225.123.161:1433/utl/2/page/1",cookieMap, null, null);

        Document parse = Jsoup.parse(s);

        Elements elementsByClass = parse.getElementsByClass("thumb-wrap");
        for (Element element : elementsByClass) {

            //详情页url
            String contextUrl = "http://23.225.123.161:1433" + element.getElementsByTag("a").attr("href");
            //封面图url
            String surfacePlotUrl = element.getElementsByTag("img").attr("src");

            char[] contextResult = element.getElementsByClass("title").html().split("\"")[1].toCharArray();
            //视频内容描述
            String context = BaseUtil.conversionString(contextResult);

            char[] durationResult = element.getElementsByClass("time").html().split("\"")[1].toCharArray();
            //视频时长
            String duration = BaseUtil.conversionString(durationResult);

            //相对热度
            char[] heatResult = element.getElementsByClass("views").first().html().split("\"")[1].toCharArray();
            String heat = BaseUtil.conversionString(heatResult);


        }


    }

}
