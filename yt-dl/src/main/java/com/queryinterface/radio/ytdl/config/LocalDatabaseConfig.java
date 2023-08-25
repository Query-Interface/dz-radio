package com.queryinterface.radio.ytdl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalDatabaseConfig {
    /*
    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(getSystemProperty("DB_URI", "jdbc:postgresql://localhost:5432/radio"));
        dataSourceBuilder.username(getSystemProperty("DB_USER", "admin"));
        dataSourceBuilder.password(getSystemProperty("DB_PASSWORD","Password1"));
        DataSource dataSource = dataSourceBuilder.build();
        return dataSource;
    }

    private String getSystemProperty(final String propertyName, final String defaultValue) {
        final String value = System.getenv(propertyName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        adapter.setDatabase(Database.POSTGRESQL);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(adapter);
        factory.setPackagesToScan("com.queryinterface.radio.ytdl");
        factory.setDataSource(getDataSource());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }*/
}
