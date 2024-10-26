package org.springframework.data.jpa.repository.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class RepositoriesManager {

    private ApplicationContext applicationContext;
    private @SuppressWarnings("null") ResourceLoader resourceLoader;
    private @SuppressWarnings("null") Environment environment;
    private DataSourceProperties dataSourceProperties;
    private static final String packages = "com.example.demo";

    public RepositoriesManager(ApplicationContext applicationContext,
                               ResourceLoader resourceLoader,
                               Environment environment,
                               DataSourceProperties dataSourceProperties) {
        this.applicationContext = applicationContext;
        this.resourceLoader = resourceLoader;
        this.environment = environment;
        this.dataSourceProperties = dataSourceProperties;
    }

    public void initDataSource() {
        // 1. 动态创建数据源
        DataSource dataSource = createDataSource();

        // 2. 注册 DataSource Bean
        registerBean("dataSource", dataSource);

        // 3. 注册 EntityManagerFactory Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory = createEntityManagerFactory(dataSource);
        registerBean("entityManagerFactory", entityManagerFactory);

        // 4. 注册 TransactionManager Bean
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory((EntityManagerFactory) applicationContext.getBean("entityManagerFactory"));
        registerBean("transactionManager", transactionManager);

        // 5. 动态注册 JPA Repositories
        registerJpaRepositories();
    }

    private DataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        return dataSource;
    }

    private LocalContainerEntityManagerFactoryBean createEntityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan(packages);
        factoryBean.setPersistenceUnitName("default"); // 设置持久化单元名称
        // 指定 Hibernate 作为 JPA 提供者
        factoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        // 可选的 JPA 属性
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update"); // 自动更新数据库结构
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); // 根据数据库类型调整方言
        factoryBean.setJpaPropertyMap(jpaProperties);

        factoryBean.afterPropertiesSet();  // 手动初始化
        return factoryBean;
    }

    private void registerBean(String beanName, Object bean) {
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableContext.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition();
        beanDefinitionBuilder.getBeanDefinition().setBeanClass(bean.getClass());
        beanDefinitionBuilder.getBeanDefinition().setInstanceSupplier(()->bean);
        registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerJpaRepositories() {
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableContext.getBeanFactory();
        JpaRepositoriesRegistrar repositoryRegistrar = new JpaRepositoriesRegistrar();
        repositoryRegistrar.setEnvironment(environment);
        repositoryRegistrar.setResourceLoader(resourceLoader);
        AnnotationMetadata metadata = AnnotationMetadata.introspect(JpaRepositoriesConfig.class);
        repositoryRegistrar.registerBeanDefinitions(metadata, registry, ConfigurationClassPostProcessor.IMPORT_BEAN_NAME_GENERATOR);
    }

    @EnableJpaRepositories(
            basePackages = packages,
            entityManagerFactoryRef = "entityManagerFactory",
                transactionManagerRef = "transactionManager",
            bootstrapMode = BootstrapMode.LAZY
    )
    private static class JpaRepositoriesConfig{

    }

}
