package com.sz.winter.basereptile.model.resp;

import com.sz.winter.basereptile.model.VideoFragmentation;

public class VideoFragmentationInfoResp extends VideoFragmentation {

    //网站名称
    public String websiteName;

    //类别ID
    public Long classifyId;

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }
}
