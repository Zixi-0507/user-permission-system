package com.tonpower.userservice.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis Plus 配置
 *
 * @author https://github.com/Zixi-0507
 */
@Configuration
//@MapperScan(basePackages = "com.tonpower.userservice.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
public class MyBatisPlusConfig {

    /**
     * 拦截器配置
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 配置SqlSessionFactory
     * 使用@Primary确保ShardingSphere的数据源被优先使用
     * 使用MybatisSqlSessionFactoryBean替代SqlSessionFactoryBean
     */
//    @Bean
//    @Primary
//    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
//        // 确保使用ShardingSphere数据源
//        sqlSessionFactoryBean.setDataSource(dataSource);
//
//        // 设置Mapper XML文件位置
//        sqlSessionFactoryBean
//                .setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
//
//        // 设置实体类包路径
//        sqlSessionFactoryBean.setTypeAliasesPackage("com.tonpower.userservice.model.entity");
//
//        // 配置MyBatis-Plus
//        MybatisConfiguration configuration = new MybatisConfiguration();
//        configuration.setMapUnderscoreToCamelCase(true);
//        configuration.setCacheEnabled(true);
//        sqlSessionFactoryBean.setConfiguration(configuration);
//
//        return sqlSessionFactoryBean.getObject();
//    }
//
//    /**
//     * 配置SqlSessionTemplate
//     */
//    @Bean
//    @Primary
//    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
}