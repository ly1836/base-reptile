package com.sz.winter.basereptile.model;


import com.sz.winter.basereptile.base.BaseModel;

/**
 * <p>
 *     分类列表信息实体
 * </p>
 */
public class ClassifyList extends BaseModel {
    //网址记录id
    private Long websitId;

    //分类id
    private Long classifyId;

    //封面图url
    private String surfacePlotUrl;

    //视频内容描述
    private String describes;

    //视频时长
    private String duration;

    //相对热度
    private String heat;

    //详情页url
    private String contextUrl;

    //是否下载 0:未下载  1:已下载  2:下载中
    private Integer isDownload;

    //封面图磁盘路径
    private String surfacePlotDir;

    public Long getWebsitId() {
        return websitId;
    }

    public void setWebsitId(Long websitId) {
        this.websitId = websitId;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public String getSurfacePlotUrl() {
        return surfacePlotUrl;
    }

    public void setSurfacePlotUrl(String surfacePlotUrl) {
        this.surfacePlotUrl = surfacePlotUrl;
    }

    public String getDescribes() {
        return describes;
    }

    public void setDescribes(String describes) {
        this.describes = describes;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getHeat() {
        return heat;
    }

    public void setHeat(String heat) {
        this.heat = heat;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(String contextUrl) {
        this.contextUrl = contextUrl;
    }


    public Integer getIsDownload() {
        return isDownload;
    }

    public void setIsDownload(Integer isDownload) {
        this.isDownload = isDownload;
    }

    public String getSurfacePlotDir() {
        return surfacePlotDir;
    }

    public void setSurfacePlotDir(String surfacePlotDir) {
        this.surfacePlotDir = surfacePlotDir;
    }

    public ClassifyList() {
    }

    public ClassifyList(Long websitId, Long classifyId, String surfacePlotUrl, String describes,
                        String duration, String heat, String contextUrl) {
        this.websitId = websitId;
        this.classifyId = classifyId;
        this.surfacePlotUrl = surfacePlotUrl;
        this.describes = describes;
        this.duration = duration;
        this.heat = heat;
        this.contextUrl = contextUrl;
    }
}
