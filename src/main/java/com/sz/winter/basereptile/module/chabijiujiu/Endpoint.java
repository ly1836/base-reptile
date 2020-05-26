package com.sz.winter.basereptile.module.chabijiujiu;

import com.sz.winter.basereptile.service.ClassifyListService;
import com.sz.winter.basereptile.service.ClassifyService;
import com.sz.winter.basereptile.service.VideoFragmentationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;


public class Endpoint {

    @Autowired
    protected ClassifyService classifyService;

    @Autowired
    protected ClassifyListService classifyListService;

    @Autowired
    protected VideoFragmentationService videoFragmentationService;

    @Autowired
    protected TaskExecutor taskExecutor;

    //网址记录ID
    protected Long websitId = 1L;

    //网站别名
    protected String name ="chabijiujiu";

    //网站URl
    protected String host = "http://23.225.123.161:1433";

    //视频列表封面图硬盘存储目录
    protected String surfacePlotDir = "F:\\chabijiujiu\\image\\";

    protected String videoDir = "F:\\chabijiujiu\\video\\";

    //封面图后缀
    protected String surfacePlotsuffix = ".jpg";

    //视频下载域名
    protected String m3u8Url = "https://cdn.m3u8oxxb.pw/index.php/m3u8/";

    protected String m3u8FileSuffix = ".ts";

    public Long getWebsitId() {
        return websitId;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getSurfacePlotDir() {
        return surfacePlotDir;
    }

    public String getSurfacePlotsuffix() {
        return surfacePlotsuffix;
    }

    public String getM3u8Url() {
        return m3u8Url;
    }

    public String getVideoDir() {
        return videoDir;
    }

    public String getM3u8FileSuffix() {
        return m3u8FileSuffix;
    }
}
