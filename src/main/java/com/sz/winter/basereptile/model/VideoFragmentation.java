package com.sz.winter.basereptile.model;

import com.sz.winter.basereptile.base.BaseModel;

/**
 * <p>
 *     视频分片信息
 * </p>
 */
public class VideoFragmentation extends BaseModel {

    //列表ID
    private Long classifyListId;

    //视频分片信息,分号分隔
    private String palyload;

    //0:待下载  1:已下载 2:下载中
    private Integer dowloadType;

    public Long getClassifyListId() {
        return classifyListId;
    }

    public void setClassifyListId(Long classifyListId) {
        this.classifyListId = classifyListId;
    }

    public String getPalyload() {
        return palyload;
    }

    public void setPalyload(String palyload) {
        this.palyload = palyload;
    }

    public Integer getDowloadType() {
        return dowloadType;
    }

    public void setDowloadType(Integer dowloadType) {
        this.dowloadType = dowloadType;
    }

    public VideoFragmentation() {
    }

    public VideoFragmentation(Long classifyListId, String palyload, Integer dowloadType) {
        this.classifyListId = classifyListId;
        this.palyload = palyload;
        this.dowloadType = dowloadType;
    }
}
