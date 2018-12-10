package com.sz.winter.basereptile.base;

import java.util.Date;

public class BaseModel {

    //记录主键
    private Long id;

    //记录创建时间
    private Date createDate;

    //记录最后更新时间
    private Date lastUpdate;

    //0:冻结  1:正常  2:删除
    private Integer status = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
