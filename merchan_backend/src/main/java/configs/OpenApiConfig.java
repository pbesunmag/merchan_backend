package configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI docConfig(){
        return new OpenAPI().info(new Info()
                .title("Documentación API REST Biblioteca")
                .version("0.0.1")
                .description("Alquiler de libros")
                .contact(new Contact()
                        .name("Pol, Linneth y Lucia")
                        .email("soporte@hackaboss.com")));
    }
}
