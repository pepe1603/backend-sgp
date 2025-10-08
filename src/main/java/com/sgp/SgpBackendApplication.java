package com.sgp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.sgp")  // Asegúrate de que todo el paquete com.sgp esté siendo escaneado
@EnableScheduling
@EnableJpaAuditing // ⬅️ Habilita la auditoría de JPA para las entidades
@EnableAsync // ⬅️ Habilita la ejecución asíncrona (@Async)
@EnableRetry // ⬅️ Habilita el reintento

public class SgpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgpBackendApplication.class, args);
	}

}
