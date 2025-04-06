package ru.store.springbooks.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "Book Application API",
                description = "API для приложения Book,"
                        + " позволяющего пользователям просматривать и бронировать книги.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Vadimka",
                        email = "koval.cap.stop.j@gmail.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        )
)
public class SwaggerConfig {
}