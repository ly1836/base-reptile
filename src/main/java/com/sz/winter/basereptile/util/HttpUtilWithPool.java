package com.sz.winter.basereptile.util;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * HttpClient工具类
 *
 * @return
 * @author SHANHY
 * @create 2015年12月18日
 */
public class HttpUtilWithPool {

    static final int timeOut = 10 * 1000;

    private static CloseableHttpClient httpClient = null;

    private final static Object syncLock = new Object();

    private final static Logger logger= LoggerFactory.getLogger(HttpUtilWithPool.class);

    private static void config(HttpRequestBase httpRequestBase,String host,Integer port) {
        // 设置Header等
        // httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
        // httpRequestBase
        // .setHeader("Accept",
        // "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // httpRequestBase.setHeader("Accept-Language",
        // "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
        // httpRequestBase.setHeader("Accept-Charset",
        // "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");
        //设置代理IP、端口、协议（请分别替换）
        HttpHost proxy =null;
        if (StringUtilExtend.isValidString(host) && port!=null){
           proxy= new HttpHost(host, port, "http");
        }

        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()/*.setProxy(proxy)*/
                .setConnectionRequestTimeout(timeOut)
                .setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();


        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 获取HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient getHttpClient(String url) throws Exception{
//        String hostname = url.split("/")[2];
//        int port = 80;
//        if (hostname.contains(":")) {
//            String[] arr = hostname.split(":");
//            hostname = arr[0];
//            port = Integer.parseInt(arr[1]);
//        }
        if (httpClient == null) {
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient(1000, 100, 150, null, 0);
                }
            }
        }

        logger.info("-------------------------httpClient|{}",httpClient);
        return httpClient;
    }

    /**
     * 创建HttpClient对象
     *
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient createHttpClient(int maxTotal,
                                                       int maxPerRoute, int maxRoute, String hostname, int port) throws Exception{
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                .getSocketFactory();
        //采用绕过验证的方式处理https请求
        SSLContext sslcontext = createIgnoreVerifySSL();
//        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
//                .getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory> create().register("http", plainsf)
                .register("https", new SSLConnectionSocketFactory(sslcontext)).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                registry);
        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);
//        HttpHost httpHost = new HttpHost(hostname, port);
//        // 将目标主机的最大连接数增加
//        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        /**
         * socket配置（默认配置 和 某个host的配置）
         */
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)     //是否立即发送数据，设置为true会关闭Socket缓冲，默认为false
                .setSoReuseAddress(true) //是否可以在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
                .setSoTimeout(5000)       //接收数据的等待超时时间，单位ms
                .setSoLinger(60)         //关闭Socket时，要么发送完所有数据，要么等待60s后，就关闭连接，此时socket.close()是阻塞的
                .setSoKeepAlive(true)    //开启监视TCP连接是否有效
                .build();
        cm.setDefaultSocketConfig(socketConfig);

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,
                                        int executionCount, HttpContext context) {
                if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// SSL握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext
                        .adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler).build();

        return httpClient;
    }

    /**
     * 绕过https验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        /**
         * JDK对SSL/TLS版本的支持
         JDK 8(March 2014 to present)：TLSv1.2 (default)，TLSv1.1，TLSv1，SSLv3
         JDK 7(July 2011 to present)：TLSv1.2，TLSv1.1，TLSv1 (default)，SSLv3
         JDK 6(2006 to end of public updates 2013)：TLS v1.1 (JDK 6 update 111 and above)，TLSv1 (default)，SSLv3
         */
        SSLContext sc = SSLContext.getInstance("TLSv1.2");

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

    private static void setPostParams(HttpPost httpost,
                                      Map<String, String> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * GET请求URL获取内容
     *
     * @param url
     * @return
     * @author SHANHY
     * @throws IOException
     * @create 2015年12月18日
     */
    public static String post(String url, Map<String, String> params,String proxyHost,Integer port){
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
        config(httppost,proxyHost,port);
        setPostParams(httppost, params);
        CloseableHttpResponse response = null;
        String result=null;
        try {
            response = getHttpClient(url).execute(httppost,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
           logger.error("http post error! url:"+url,e);
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * GET请求URL获取内容
     *
     * @param url
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static String get(String url,String proxyhost,Integer port,Map<String,Object> headerMap) throws Exception{
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");

        //设置http请求头
        if(headerMap != null && !headerMap.isEmpty()){
            for(Map.Entry<String,Object> entry : headerMap.entrySet()){
                httpget.setHeader(entry.getKey(),entry.getValue().toString());
            }
        }

        config(httpget,proxyhost,port);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url).execute(httpget,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);

            return result;
        } catch (IOException e) {
            logger.error("http get error! url:"+url,e);
            throw e;
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        //return null;
    }

    public static void main(String[] args) throws Exception{
//        logger.info(get("https://api.bithumb.com/public/transaction_history/BTC"));//
//        logger.info(get("https://api.coinbene.com/v1/market/orderbook?symbol=conieth&depth=50"));
//        logger.info(get("https://www.bitstamp.net/api/v2/ticker/eurusd","127.0.0.1",1080));
        HashMap<String, String> param = new HashMap<>();
        param.put("coin", "bch");
        param.put("part", "btc");
        logger.info(post("https://www.allcoin.ca/Api_Market/getCoinTrade",param,"127.0.0.1",1080));
//        // URL列表数组
//        String[] urisToGet = {
//                "https://www.zg.com/api/v1/tickers",
//                "https://api2.vvbtc.com/v1/common/getQuote?symbol=BTCUSDT",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//
//                "https://www.zg.com/api/v1/tickers",
//                "https://api2.vvbtc.com/v1/common/getQuote?symbol=BTCUSDT",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers",
//                "https://www.zg.com/api/v1/tickers" };
//
//        long start = System.currentTimeMillis();
//        try {
//            int pagecount = urisToGet.length;
//            ExecutorService executors = Executors.newFixedThreadPool(pagecount);
//            CountDownLatch countDownLatch = new CountDownLatch(pagecount);
//            for (int i = 0; i < pagecount; i++) {
//                HttpGet httpget = new HttpGet(urisToGet[i]);
//                config(httpget);
//                // 启动线程抓取
//                executors
//                        .execute(new GetRunnable(urisToGet[i], countDownLatch));
//            }
//            countDownLatch.await();
//            executors.shutdown();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("线程" + Thread.currentThread().getName() + ","
//                    + System.currentTimeMillis() + ", 所有线程已完成，开始进入下一步！");
//        }
//
//        long end = System.currentTimeMillis();
//        System.out.println("consume -> " + (end - start));

//        System.out.println(HttpUtilWithPool.get("https://api2.vvbtc.com/v1/common/getQuote?symbol=BTCUSDT"));
    }

    static class GetRunnable implements Runnable {
        private CountDownLatch countDownLatch;
        private String url;

        public GetRunnable(String url, CountDownLatch countDownLatch) {
            this.url = url;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
//                HttpUtilWithPool.get(url);
//                System.out.println(HttpUtilWithPool.get(url));
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                countDownLatch.countDown();
            }
        }
    }
}
//            Post使用方法
//
//// 其中 params 为 Map<String, Object> params
//        String ret = HttpClientUtil.post(url, params);
//        jsonRet = new JSONObject(ret);
//