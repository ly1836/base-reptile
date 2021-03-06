package com.sz.winter.basereptile.base;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by leiyang on 2018/12/9
 */
@Repository
public class BaseDao {

    @Autowired
    private SqlSessionTemplate sessionTemplate;

    public SqlSession getSession() {
        return sessionTemplate;
    }
}
