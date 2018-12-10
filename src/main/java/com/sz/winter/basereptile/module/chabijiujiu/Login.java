package com.sz.winter.basereptile.module.chabijiujiu;

import com.sz.winter.basereptile.util.HttpUtil;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class Login {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String requestUrl = "http://23.225.123.161:1433/login";

    @Value("${base.proxy.host}")
    private String proxyHost;

    @Value("${base.proxy.port}")
    private Integer proxtPort;

    private String userName = "ly1836";

    private String password = "lyaa110.";


    public String getUserCookie() {
        String cookie = "";
        try {
            Map<String, String> formMap = new HashMap<>();
            formMap.put("username", userName);
            formMap.put("password", password);

            HttpResponse httpResponse = HttpUtil.httpDoPost(requestUrl, formMap, null, proxyHost, proxtPort);

            String result = httpResponse.getHeaders("Set-Cookie")[0].toString().replace("Set-Cookie:", "");

            System.out.println("===============cookie:" + result);
        } catch (Exception ex) {
            logger.error("获取用户Cookie异常:{}", ex);
        }

        return cookie;
    }
}
