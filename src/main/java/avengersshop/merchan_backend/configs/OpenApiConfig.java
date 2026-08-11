package avengersshop.merchan_backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

// Clase de configuración personalizada para OpenAPI / Swagger UI
@Configuration
public class OpenApiConfig {
    // Registra un Bean en el contenedor de Spring IoC para personalizar la cabecera e información general de Swagger UI.
    @Bean
    public OpenAPI docConfig(){
        return new OpenAPI().info(new Info()
                .title("Documentación API REST Merchandising Avengers Shop")
                .version("4.1.0")
                .description("Personalización de productos con tus personajes favoritos de Avengers")
                .contact(new Contact()
                        .name("Pol y Linneth")
                        .email("soporte@avengersshop.com")));
    }
}
