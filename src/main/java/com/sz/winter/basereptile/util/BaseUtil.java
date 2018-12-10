package com.sz.winter.basereptile.util;

import java.util.Random;

public class BaseUtil {

    /**
     * <p>
     *     字符数组转换为字符串
     * </p>
     * @param chars 字符数组
     * @return String
     */
    public static String conversionString(char[] chars){
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            //去除空格
            if(chars[i] == 32)
                continue;
            char c = (char) (chars[i] ^ 128);
            context.append(c);
        }

        return context.toString();
    }


    /**
     * <p>
     *     获取指定范围内的随机整数
     * </p>
     * @param min 最小数
     * @param max 最大数
     * @return int
     */
    public static int random(int min,int max){
        return new Random().nextInt(max)%(max-min+1) + min;
    }

}
