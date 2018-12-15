package com.sz.winter.basereptile.service;

import com.sz.winter.basereptile.base.BaseDao;
import com.sz.winter.basereptile.model.Classify;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassifyService extends BaseDao {

    /**
     * <p>
     *     根据条件获取类别列表
     * </p>
     * @param classify 条件
     * @return List
     */
    public List<Classify> getClassifyByCondition(Classify classify){
        return getSession().selectList("getClassifyByCondition");
    }


}
