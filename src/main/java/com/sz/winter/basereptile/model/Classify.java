package com.sz.winter.basereptile.model;


import com.sz.winter.basereptile.base.BaseModel;

/**
 * <p>
 *     分类列表信息实体
 * </p>
 */
public class Classify extends BaseModel {
    //网址记录id
    private Long websitId;

    //分类名称
    private String classify;

    //分类起始页url
    private String startListUrl;

    public Long getWebsitId() {
        return websitId;
    }

    public void setWebsitId(Long websitId) {
        this.websitId = websitId;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getStartListUrl() {
        return startListUrl;
    }

    public void setStartListUrl(String startListUrl) {
        this.startListUrl = startListUrl;
    }
}
