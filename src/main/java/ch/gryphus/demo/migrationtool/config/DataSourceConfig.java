package ch.gryphus.demo.migrationtool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@ConfigurationProperties
@EnableTransactionManagement
public class DataSourceConfig {

    @Autowired
    Environment environment;

    @Bean
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }

}
