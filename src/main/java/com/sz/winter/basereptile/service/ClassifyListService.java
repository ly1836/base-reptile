package com.sz.winter.basereptile.service;

import com.sz.winter.basereptile.base.BaseDao;
import com.sz.winter.basereptile.model.Classify;
import com.sz.winter.basereptile.model.ClassifyList;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassifyListService extends BaseDao {

    /**
     * <p>
     *     批量插入列表信息
     * </p>
     * @param classifyLists 列表详情
     */
    public void insertBatchClassifys(List<ClassifyList> classifyLists){
        getSession().insert("insertBatchClassifyList",classifyLists);
    }


    /**
     * <p>
     *     获取全部列表
     * </p>
     * @return List
     */
    public List<ClassifyList> listCalssifyList(){
        return getSession().selectList("listCalssifyList");
    }
}
