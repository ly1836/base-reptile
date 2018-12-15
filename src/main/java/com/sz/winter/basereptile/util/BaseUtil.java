package com.sz.winter.basereptile.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * 下载网络图片,指定文件名(可选)
     * </p>
     *
     * @param sourceUrl 网络路径
     * @param fileName  文件名
     * @return boolean true:成功  false:失败
     */
    public static boolean dowloadImage(String sourceUrl, String fileName) {
        try {
            logger.info("Downloading File From: " + sourceUrl);

            URL url = new URL(sourceUrl);
            InputStream inputStream = url.openStream();
            OutputStream outputStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[2048];

            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

        } catch (Exception ex) {
            logger.error("下载{}失败:{}", sourceUrl, ex);
            return false;
        }

        return true;
    }

}
