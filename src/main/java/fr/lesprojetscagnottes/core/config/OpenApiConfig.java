package fr.lesprojetscagnottes.core.config;

import fr.lesprojetscagnottes.core.LPCCoreApplication;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER).name("Authorization")))
                .addSecurityItem(
                        new SecurityRequirement().addList("bearer-jwt", Arrays.asList("read", "write")))
                .info(new Info()
                        .title("Les Projets Cagnottes - Core")
                        .description("This is the core service API documentation generated using OpenAPI.")
                        .version(LPCCoreApplication.class.getPackage().getImplementationVersion())
                        .license(new License().name("Open Software License (\"OSL\") v. 3.0").url("https://opensource.org/licenses/OSL-3.0"))
                );
    }

}
