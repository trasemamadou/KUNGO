package com.kungo.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI kungoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KUNGO API")
                        .description("Marketplace événementielle - API REST")
                        .version("v1")
                        .contact(new Contact().name("KUNGO").email("contact@kungo.app"))
                        .license(new License().name("Propriétaire")));
    }
}
