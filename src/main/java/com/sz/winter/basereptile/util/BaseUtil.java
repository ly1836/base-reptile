package com.sz.winter.basereptile.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Random;

public class BaseUtil {
    static Logger logger = LoggerFactory.getLogger(BaseUtil.class);

    /**
     * <p>
     * 字符数组转换为字符串
     * </p>
     *
     * @param chars 字符数组
     * @return String
     */
    public static String conversionString(char[] chars) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            //去除空格
            if (chars[i] == 32)
                continue;
            char c = (char) (chars[i] ^ 128);
            context.append(c);
        }

        return context.toString();
    }


    /**
     * <p>
     * 获取指定范围内的随机整数
     * </p>
     *
     * @param min 最小数
     * @param max 最大数
     * @return int
     */
    public static int random(int min, int max) {
        return new Random().nextInt(max) % (max - min + 1) + min;
    }


    /**
     * <p>
     * 下载网络文件,指定存储文件名(可选)
     * </p>
     *
     * @param sourceUrl 网络路径
     * @param fileName  文件名
     * @param append 是否追加文件
     * @return boolean true:成功  false:失败
     */
    public static boolean dowloadNerworkFile(String sourceUrl, String fileName,boolean append) {
        try {
            logger.info("Downloading File From: " + sourceUrl);

            URL url = new URL(sourceUrl);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            InputStream inputStream = url.openStream();

            OutputStream outputStream;
            //判断是否追加写入文件
            if(append){
                outputStream = new FileOutputStream(fileName,true);
            }else {
                outputStream = new FileOutputStream(fileName);
            }

            byte[] buffer = new byte[2048];

            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();

        } catch (Exception ex) {
            logger.error("下载{}失败:{}", sourceUrl, ex);
            return false;
        }

        return true;
    }

}
