package com.sz.winter.basereptile.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);


    public static String loadJson(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 代理 post 访问 http 接口
     * @param url
     * @param params
     * @param proxyIP
     * @param port
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, String> params,String proxyIP,Integer port){
        return HttpUtilWithPool.post(url,params,null,null);
    }

    /**
     * 通过代理访问 HTTP/HTTPS 接口
     *
     * @param httpUrl
     * @param proxyIp
     * @param proxyPort
     * @return
     */
    public static String doGet(String httpUrl,Map<String,Object> headerMap, String proxyIp, Integer proxyPort) {
        String response = null;
        try {
//            // 創建httpGet實例
//            HttpGet httpGet = new HttpGet(httpUrl);
//            if (StringUtilExtend.isValidString(proxyIp) && proxyPort != null) {
//                // 設置代理IP，設置連接超時時間 、 設置 請求讀取數據的超時時間 、 設置從connect Manager獲取Connection超時時間、
//                HttpHost proxy = new HttpHost(proxyIp, proxyPort);
//                RequestConfig requestConfig = RequestConfig.custom()
//                        .setProxy(proxy)
//                        .setConnectTimeout(10000)
//                        .setSocketTimeout(10000)
//                        .setConnectionRequestTimeout(3000)
//                        .build();
//                httpGet.setConfig(requestConfig);
//            }
//            // 設置請求頭消息
//            httpGet.setHeader("Content-type", "application/x-www-form-urlencoded");
//            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
//            HttpClient httpClient = HttpClients.createDefault();
//            //SSL设置
//            if (httpUrl.contains("https")) {
//                //采用绕过验证的方式处理https请求
//                SSLContext sslcontext = createIgnoreVerifySSL();
//
//                // 设置协议http和https对应的处理socket链接工厂的对象
//                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
//                        .register("http", PlainConnectionSocketFactory.INSTANCE)
//                        .register("https", new SSLConnectionSocketFactory(sslcontext))
//                        .build();
//                PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//                httpClient = HttpClients.custom().setConnectionManager(connManager).build();
//                response = httpRequest(httpClient, httpGet);
//            }
            response=HttpUtilWithPool.get(httpUrl,proxyIp,proxyPort,headerMap);
        } catch (Exception e) {
            log.error("doGet ERROR", e);
        }

        return response;
    }


    /**
     * 通过代理访问 HTTP/HTTPS 接口
     *
     * @param httpUrl
     * @param proxyIp
     * @param proxyPort
     * @return
     */
    public static String doGetWithAcceptLanguage(String httpUrl, String proxyIp, Integer proxyPort) {
        String response = null;
        try {
            // 創建httpGet實例
            HttpGet httpGet = new HttpGet(httpUrl);
            if (StringUtilExtend.isValidString(proxyIp) && proxyPort != null) {
                // 設置代理IP，設置連接超時時間 、 設置 請求讀取數據的超時時間 、 設置從connect Manager獲取Connection超時時間、
                HttpHost proxy = new HttpHost(proxyIp, proxyPort);
                RequestConfig requestConfig = RequestConfig.custom()
                        .setProxy(proxy)
                        .setConnectTimeout(10000)
                        .setSocketTimeout(10000)
                        .setConnectionRequestTimeout(3000)
                        .build();
                httpGet.setConfig(requestConfig);
            }
            // 設置請求頭消息
            httpGet.setHeader("accept-language", "zh-CN,zh;q=0.8");
            httpGet.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
            HttpClient httpClient = HttpClients.createDefault();
            //SSL设置
            if (httpUrl.contains("https")) {
                //采用绕过验证的方式处理https请求
                SSLContext sslcontext = createIgnoreVerifySSL();

                // 设置协议http和https对应的处理socket链接工厂的对象
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslcontext))
                        .build();
                PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                httpClient = HttpClients.custom().setConnectionManager(connManager).build();
                response = httpRequestWithCharset(httpClient, httpGet);
            }
        } catch (Exception e) {
            log.error("doGet ERROR", e);
        }

        return response;
    }


    /**
     * 通过代理访问 HTTP 接口
     *
     * @param httpUrl   http url
     * @param proxyIp   http ip
     * @param proxyPort 代理端口
     * @return String
     */
    public static String httpDoGet(String httpUrl,Map<String,Object> headerMap, String proxyIp, Integer proxyPort) throws Exception {
        String response = null;
        try {
            // 創建httpGet實例
            HttpGet httpGet = new HttpGet(httpUrl);
           /* if (StringUtilExtend.isValidString(proxyIp) && proxyPort != null) {
                // 設置代理IP，設置連接超時時間 、 設置 請求讀取數據的超時時間 、 設置從connect Manager獲取Connection超時時間、
                HttpHost proxy = new HttpHost(proxyIp, proxyPort);
                RequestConfig requestConfig = RequestConfig.custom()
                        .setProxy(proxy)
                        .setConnectTimeout(3000)
                        .setSocketTimeout(3000)
                        .setConnectionRequestTimeout(3000)
                        .build();
                httpGet.setConfig(requestConfig);
            }*/
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(3000)
                    .setSocketTimeout(3000)
                    .setConnectionRequestTimeout(3000)
                    .build();
            httpGet.setConfig(requestConfig);
            // 設置請求頭消息
            httpGet.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
            HttpClient httpClient = HttpClients.createDefault();

            response = httpRequest(httpClient, httpGet);
            response=HttpUtilWithPool.get(httpUrl,proxyIp,proxyPort,headerMap);
        } catch (Exception e) {
            log.error("httpDoGet ERROR",e);
            throw e;
        }

        return response;
    }

    private static String httpRequestWithCharset(HttpClient httpClient, HttpUriRequest httpUriRequest) {
        String responseContent = null;
        try {

            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            responseContent = bufferedReader.readLine();
            while (responseContent != null) {
                stringBuffer.append(responseContent);
                responseContent = bufferedReader.readLine();
            }
            responseContent = stringBuffer.toString();
        } catch (Exception e) {
            log.error("httpRequest ERROR", e);
        }
        return responseContent;
    }

    private static String httpRequest(HttpClient httpClient, HttpUriRequest httpUriRequest) throws IOException {
        String responseContent = null;
        try {

            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            responseContent = bufferedReader.readLine();
            while (responseContent != null) {
                stringBuffer.append(responseContent);
                responseContent = bufferedReader.readLine();
            }
            responseContent = stringBuffer.toString();
        } catch (Exception e) {
            log.error("httpRequest ERROR", e);
            throw e;
        }
        return responseContent;
    }

    /**
     * 绕过https验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    public static HttpResponse httpDoPost(String url,Map<String,String> formParameters,Map<String,Object> headerMap,String proxyHost,Integer proxyPort){
        HttpResponse httpResponse = null;
        try {
            HttpHost proxy=null;
            if (StringUtilExtend.isValidString(proxyHost) && proxyPort!=null){
                proxy=new HttpHost(proxyHost,proxyPort,"https");
            }
            RequestConfig requestConfig=RequestConfig.custom().setConnectTimeout(1000*20).setSocketTimeout(1000*20).setProxy(proxy).build();
            HttpClient httpClient =  HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
            HttpPost httpPost=new HttpPost(url);
            if (formParameters!=null && !formParameters.isEmpty()){
                Set<String> keys=formParameters.keySet();
                List<NameValuePair> params = new ArrayList<>();
                for (String key:keys){
                    params.add(new BasicNameValuePair(key, formParameters.get(key)));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }

            //设置HTTP默认头
            httpPost.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
            httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");


            //设置http请求头
            if(headerMap != null && !headerMap.isEmpty()){
                for(Map.Entry<String,Object> entry : headerMap.entrySet()){
                    httpPost.setHeader(entry.getKey(),entry.getValue().toString());
                }
            }

            httpResponse = httpClient.execute(httpPost);

        } catch (Exception e) {
            log.error("httpRequest ERROR", e);
        }

        return httpResponse;
    }


    public String getContext(HttpResponse httpResponse){
        String responseContent = null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            responseContent = bufferedReader.readLine();
            while (responseContent != null) {
                stringBuffer.append(responseContent);
                responseContent = bufferedReader.readLine();
            }
            responseContent = stringBuffer.toString();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return responseContent;
    }


}
