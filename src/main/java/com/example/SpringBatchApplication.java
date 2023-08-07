package com.example;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }

    @Bean
    public OpenAPI openAPIdefinition() {
        return new OpenAPI()
                .info(new Info()
                        .title("Taxonomic Job Management")
                        .version("demo")
                        .description("Spring Batch Demo project for Taxonomic Job Management"));
    }
}
