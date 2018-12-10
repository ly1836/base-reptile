package com.sz.winter.basereptile.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * <p>
 *     项目基础配置
 * </p>
 */
@Configuration
public class BaseConfig {

    @Bean
    @ConfigurationProperties(prefix = "mybatis")
    public SqlSessionFactoryBean sqlSessionFactoryBean(@Autowired ProxyFactoryBean proxyFactoryBean) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        // 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource 作为数据源则不能实现切换
        sqlSessionFactoryBean.setDataSource((DataSource) proxyFactoryBean.getObject());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mapper/*.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sqlSessionFactoryBean;
    }


    @Bean("hotSwappableTargetSource")
    public HotSwappableTargetSource hotSwappableTargetSource(@Autowired DataSource dataSource){
        return new HotSwappableTargetSource(dataSource);
    }

    @Bean("proxyFactoryBean")
    public ProxyFactoryBean proxyFactoryBean(@Autowired HotSwappableTargetSource hotSwappableTargetSource){
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTargetSource(hotSwappableTargetSource);
        return proxyFactoryBean;
    }
}
