package com.sz.winter.basereptile;

import com.sz.winter.basereptile.util.BaseUtil;
import com.sz.winter.basereptile.util.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class BaseReptileApplicationTests {

    //@Test
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


    @Test
    public void compoundVideo(){
        try{
            //String fileName = "E:\\1.ts";
            String website = "https://cdn.m3u8oxxb.pw/index.php/m3u8/ts/4ad4QDH85Ldka3Z6_L4w04Ygiz_UT95bzl-FnFtK-gImvwoA91YXunQyQj56DAhXNtkWJkz5d1M.jpg";

            //System.out.println("Downloading File From: " + website);

            URL url = new URL(website);
            InputStream inputStream = url.openStream();
            //InputStream inputStream = new FileInputStream(new File("E:\\2.ts"));
            OutputStream outputStream = new FileOutputStream("F:\\chabijiujiu\\video\\1.ts",true);
            System.out.println(inputStream.available());

            byte[] buffer = new byte[2048];

            int length;

            int sum = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                System.out.println("Buffer Read of length: " + length);
                sum += length;
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            System.out.println("sum:" + sum);

        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }




}
