package com.victorabreu.eventos.config;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class ApplicationConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("API de Eventos Industriais").version("1.0.0")
                .description("Eventos imutáveis de equipamentos. Datas de ocorrência seguem o relógio local da fábrica, sem fuso, conforme o contrato do teste. Auditoria em UTC."));
    }
    @Bean WebMvcConfigurer cors(@Value("${app.frontend-origin:http://localhost:5173}") String origin) {
        return new WebMvcConfigurer() {
            @Override public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins(origin).allowedMethods("GET", "POST").exposedHeaders("Location");
            }
        };
    }
}
