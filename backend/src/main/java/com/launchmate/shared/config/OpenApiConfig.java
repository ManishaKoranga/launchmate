package com.launchmate.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI launchMateOpenApi() {
        return new OpenAPI().info(new Info()
                .title("LaunchMate API")
                .version("v1")
                .description("Dashboard endpoints (/api/businesses/{businessId}/...) require a session cookie and "
                        + "the X-XSRF-TOKEN header for state-changing requests. /api/public/... endpoints serve "
                        + "published websites and accept customer inquiries."));
    }
}
