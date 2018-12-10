package com.sz.winter.basereptile.module.chabijiujiu;

import com.sz.winter.basereptile.service.ClassifyListService;
import com.sz.winter.basereptile.service.ClassifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public class Endpoint {

    @Autowired
    protected ClassifyService classifyService;

    @Autowired
    protected ClassifyListService classifyListService;

    //网址记录ID
    protected Long websitId = 1L;

    protected String name ="chabijiujiu";

    protected String host = "http://23.225.123.161:1433";

    public Long getWebsitId() {
        return websitId;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }
}
