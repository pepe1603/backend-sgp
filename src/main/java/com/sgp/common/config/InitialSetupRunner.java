package com.sgp.common.config;

import com.sgp.appointment.model.Appointment;
import com.sgp.appointment.repository.AppointmentRepository;
import com.sgp.common.enums.AppointmentStatus;
import com.sgp.common.enums.Gender;
import com.sgp.common.enums.RoleName;
import com.sgp.parish.model.Parish;
import com.sgp.parish.repository.ParishRepository;
import com.sgp.person.model.Person;
import com.sgp.person.repository.PersonRepository;
import com.sgp.sacrament.enums.SacramentType;
import com.sgp.sacrament.model.Sacrament;
import com.sgp.sacrament.repository.SacramentRepository;
import com.sgp.user.model.Role;
import com.sgp.user.model.User;
import com.sgp.user.repository.RoleRepository;
import com.sgp.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clase para inicializar datos esenciales (Roles) y de prueba
 * (Usuario Admin, Parroquia, Personas, Citas, Sacramentos).
 */
@Configuration
public class InitialSetupRunner {

    private static final String ADMIN_EMAIL = "000316jose@gmail.com";
    private static final String ADMIN_PASSWORD = "Pa55vv0Rd";

    @Bean
    @Transactional
    public CommandLineRunner initDemoData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PersonRepository personRepository,
            ParishRepository parishRepository,
            AppointmentRepository appointmentRepository,
            SacramentRepository sacramentRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. Inicializar Roles
            initializeRoles(roleRepository);

            // 2. Crear Parroquia de prueba (sin Diócesis por ahora)
            Parish demoParish = createDemoParish(parishRepository);

            // 3. Crear el Usuario ADMIN y su Persona
            createAdminUser(userRepository, roleRepository, personRepository, passwordEncoder, demoParish);

            // 4. Crear el resto de datos de prueba
            createDemoUsersAndData(userRepository, roleRepository, personRepository, appointmentRepository, sacramentRepository, passwordEncoder, demoParish);
        };
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DE ROLES (Mantenida)
    // ----------------------------------------------------------------------------------
    private void initializeRoles(RoleRepository roleRepository) {
        Set<RoleName> allRoleNames = Arrays.stream(RoleName.values()).collect(Collectors.toSet());
        for (RoleName roleName : allRoleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
        System.out.println("✅ Roles inicializados.");
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DE PARROQUIA (AJUSTADO: Sin Diócesis)
    // ----------------------------------------------------------------------------------
    private Parish createDemoParish(ParishRepository parishRepository) {
        return parishRepository.findByName("Parroquia de San Miguel Arcángel")
                .orElseGet(() -> {
                    Parish parish = Parish.builder()
                            .name("Parroquia de San Miguel Arcángel")
                            .address("Calle Falsa 123, Demo City")
                            .phone("555-1234")
                            .email("parroquia.demo@example.com")
                            .city("Demo City")
                            .build();
                    System.out.println("✅ Parroquia de demostración creada.");
                    return parishRepository.save(parish);
                });
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DE USUARIO ADMIN (AJUSTADO: Sin isActive en builder)
    // ----------------------------------------------------------------------------------
    private User createAdminUser(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PersonRepository personRepository,
            PasswordEncoder passwordEncoder,
            Parish parish) {

        return userRepository.findByEmail(ADMIN_EMAIL)
                .orElseGet(() -> {
                    Role adminRole = roleRepository.findByName(RoleName.ADMIN).get();

                    User adminUser = User.builder()
                            .email(ADMIN_EMAIL)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .isEnabled(true)
                            // ⭐ IMPORTANTE: isActive SE OMITE, USA EL DEFAULT=TRUE DE Auditable ⭐
                            .forcePasswordChange(false)
                            .roles(Set.of(adminRole))
                            .build();

                    // Si quisiéramos asegurarnos, podríamos usar el setter, pero el default ya es true:
                    // adminUser.setActive(true);

                    User savedUser = userRepository.save(adminUser);

                    Person adminPerson = Person.builder()
                            .firstName("Jose COlombio")
                            .lastName("Gonzalez Perez")
                            .identificationType("CC")
                            .identificationNumber("000316")
                            .gender(Gender.MALE)
                            .birthDate(LocalDate.of(1990, 1, 1))
                            .user(savedUser)
                            .parish(parish)
                            .build();

                    personRepository.save(adminPerson);
                    System.out.println("✅ Usuario Super ADMIN creado.");
                    return savedUser;
                });
    }

    // ----------------------------------------------------------------------------------
    // LÓGICA DE DATOS DE DEMOSTRACIÓN (Usuarios COORDINATOR y USER)
    // ----------------------------------------------------------------------------------

    private void createDemoUsersAndData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PersonRepository personRepository,
            AppointmentRepository appointmentRepository,
            SacramentRepository sacramentRepository,
            PasswordEncoder passwordEncoder,
            Parish demoParish) {

        if (userRepository.count() > 1) {
            System.out.println("ℹ️ Ya existen usuarios de prueba. Omitiendo creación de DEMO.");
            return;
        }

        Role userRole = roleRepository.findByName(RoleName.USER).get();
        Role coordinatorRole = roleRepository.findByName(RoleName.COORDINATOR).get();

        // 1. Usuario COORDINATOR/GESTOR
        User coordinatorUser = User.builder()
                .email("coordinador@demo.com")
                .password(passwordEncoder.encode("Coord123"))
                .isEnabled(true)
                // isActive se omite (usa default=true)
                .forcePasswordChange(true)
                .roles(Set.of(coordinatorRole, userRole))
                .build();
        coordinatorUser = userRepository.save(coordinatorUser);

        Person coordinatorPerson = Person.builder()
                .firstName("Ana María")
                .lastName("García Solís")
                .identificationType("CC")
                .identificationNumber("12345678")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1985, 5, 15))
                .parish(demoParish)
                .user(coordinatorUser)
                .build();
        personRepository.save(coordinatorPerson);
        System.out.println("✅ Usuario COORDINATOR creado.");


        // 2. Usuario USER/FELIGRÉS (El que recibe el sacramento)
        User regularUser = User.builder()
                .email("feligres@demo.com")
                .password(passwordEncoder.encode("Feligres123"))
                .isEnabled(true)
                // isActive se omite (usa default=true)
                .forcePasswordChange(false)
                .roles(Set.of(userRole))
                .build();
        regularUser = userRepository.save(regularUser);

        Person regularPerson = Person.builder()
                .firstName("Carlos Andrés")
                .lastName("López Díaz")
                .identificationType("CC")
                .identificationNumber("98765432")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 10, 20))
                .parish(demoParish)
                .user(regularUser)
                .build();
        personRepository.save(regularPerson);
        System.out.println("✅ Usuario USER/FELIGRÉS creado.");


        // ... (Creación de Sacramentos y Citas de Prueba) ...
        Sacrament baptism = Sacrament.builder()
                .type(SacramentType.BAPTISM)
                .person(regularPerson)
                .parish(demoParish)
                .celebrationDate(LocalDate.of(2023, 11, 5))
                .bookNumber("B-101")
                .pageNumber("30")
                .entryNumber("1234")
                .build();
        sacramentRepository.save(baptism);
        System.out.println("✅ Bautismo de Feligrés registrado.");

        Appointment upcomingAppointment = Appointment.builder()
                .person(regularPerson)
                .parish(demoParish)
                .appointmentDateTime(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0))
                .status(AppointmentStatus.PENDING)
                .subject("Confirmación Solicitada")
                .notes("Esto es un campo NOTA")
                .sacrament(baptism)
                .build();
        appointmentRepository.save(upcomingAppointment);
        System.out.println("✅ Cita de demostración creada.");

        System.out.println("\n=======================================================");
        System.out.println("         🎉 DATOS DE DEMO CARGADOS EXITOSAMENTE 🎉      ");
        System.out.println("=======================================================");
        System.out.println("  👤 ADMIN: " + ADMIN_EMAIL + " | Pass: " + ADMIN_PASSWORD);
        System.out.println("  👷 COORDINATOR: coordinador@demo.com | Pass: Coord123");
        System.out.println("  👨 USER: feligres@demo.com | Pass: Feligres123");
        System.out.println("=======================================================");
    }
}