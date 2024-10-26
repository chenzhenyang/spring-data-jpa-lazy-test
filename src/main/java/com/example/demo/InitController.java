package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.RepositoriesManager;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitController {

    @Autowired
    private ApplicationContext applicationContext;
    private @SuppressWarnings("null") @Autowired ResourceLoader resourceLoader;
    private @SuppressWarnings("null") @Autowired Environment environment;

    // 保证单例
    private RepositoriesManager repositoriesManager;

    @GetMapping("/init")
    public String initDataSource(Object object) {

        //convert object -> DataSourceProperties
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl("jdbc:postgresql://localhost:5434/postgres");
        dataSourceProperties.setDriverClassName("org.postgresql.Driver");
        dataSourceProperties.setUsername("postgres");
        dataSourceProperties.setPassword("postgres");

        // double check
        if(ObjectUtils.isEmpty(repositoriesManager)) {
            repositoriesManager = new RepositoriesManager(applicationContext,
                    resourceLoader,
                    environment,
                    dataSourceProperties);
            repositoriesManager.initDataSource();
        }
        return "Spring Data JPA initialized successfully.";
    }

}
