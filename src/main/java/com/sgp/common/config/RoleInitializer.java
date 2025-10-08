package com.sgp.common.config;

import com.sgp.common.enums.RoleName;
import com.sgp.user.model.Role;
import com.sgp.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

// Usamos @Configuration para que Spring lo detecte como un Bean
@Configuration
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        // 1. Verificar y Crear el Rol USER
        if (roleRepository.findByName(RoleName.USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(RoleName.USER);
            roleRepository.save(userRole);
            System.out.println("Rol 'USER' creado exitosamente.");
        }

        // 2. Verificar y Crear el Rol COORDINATOR
        if (roleRepository.findByName(RoleName.COORDINATOR).isEmpty()) {
            Role coordinatorRole = new Role();
            coordinatorRole.setName(RoleName.COORDINATOR);
            roleRepository.save(coordinatorRole);
            System.out.println("Rol 'COORDINATOR' creado exitosamente.");
        }

        // 3. Verificar y Crear el Rol GESTOR
        if (roleRepository.findByName(RoleName.GESTOR).isEmpty()) {
            Role gestorRole = new Role();
            gestorRole.setName(RoleName.GESTOR);
            roleRepository.save(gestorRole);
            System.out.println("Rol 'GESTOR' creado exitosamente.");
        }

        // 3. Verificar y Crear el Rol ADMIN
        if (roleRepository.findByName(RoleName.ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(RoleName.ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Rol 'ADMIN' creado exitosamente.");
        }
    }
}