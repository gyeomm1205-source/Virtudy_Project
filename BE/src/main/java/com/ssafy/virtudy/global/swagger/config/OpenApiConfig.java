package com.ssafy.virtudy.global.swagger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Virtudy API")
<<<<<<< HEAD
                .version("v1.1.0")
=======
                .version("v1.2.0")
>>>>>>> 317f96e202cdb0fc59fa575fb5cd7806f9f6905d
                .description("Virtudy 프로젝트의 API 명세서입니다.");

        String jwtSchemeName = "AccessToken";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}
