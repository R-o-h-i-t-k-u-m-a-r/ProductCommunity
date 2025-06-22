package com.productcommunity.security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myCustomConfig(){
        return new OpenAPI()
                .info(
                        new Info().title("ShoppingCart APIs")
                                .description("By Rohit")
                )
                .servers(List.of(
                        new Server().url("http://localhost:9191").description("local")
                        ,new Server().url("http://localhost:9192").description("live")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                ))
                ;
    }
}

//swagger access link -> http://localhost:9191/swagger-ui/index.html
//if not accessible then
//run the following maven command
// mvn clean install
// mvn package
// mvn spring-boot:run
// then access using uri -> http://localhost:9191/swagger-ui/index.html

