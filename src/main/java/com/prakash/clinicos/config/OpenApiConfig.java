package com.prakash.clinicos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clinicosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClinicOS API")
                        .version("1.0.0")
                        .description("""
                                **ClinicOS** — Multi-tenant clinic management platform.

                                Covers: Authentication · Clinics · Doctors · Patients · \
                                Appointments · Queue · Billing · Prescriptions · Vitals · \
                                Clinical Notes · Notifications · Audit Logs · \
                                Patient Portal · Subscriptions · Platform Admin · Reports

                                **Auth:** All write endpoints require `Authorization: Bearer <token>`. \
                                Use `POST /api/v1/auth/login` to get your token, then click \
                                **Authorize** above and paste it in.
                                """)
                        .contact(new Contact()
                                .name("Prakash Jha")
                                .email("prakash@clinicos.in"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT access token here (without the 'Bearer ' prefix)")));
    }
}
