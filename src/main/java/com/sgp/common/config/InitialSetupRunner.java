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
import com.sgp.sacrament.model.SacramentDetail;
import com.sgp.sacrament.repository.SacramentDetailRepository;
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
            // ⭐ Repositorio de Detalles de Sacramento ⭐
            SacramentDetailRepository sacramentDetailRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. Inicializar Roles
            initializeRoles(roleRepository);

            // 2. Crear Parroquia de prueba
            Parish demoParish = createDemoParish(parishRepository);

            // 3. Crear el Usuario ADMIN y su Persona
            // ⭐ CAPTURAR DIRECTAMENTE LA PERSONA DEL ADMIN ⭐
            Person adminPerson = createAdminUser(userRepository, roleRepository, personRepository, passwordEncoder, demoParish);

            // 4. Crear el resto de datos de prueba
            // ⭐ Pasamos adminPerson para que pueda ser el Ministro Oficiante ⭐
            createDemoUsersAndData(userRepository, roleRepository, personRepository, appointmentRepository, sacramentRepository, sacramentDetailRepository, passwordEncoder, demoParish, adminPerson);
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
    // LÓGICA DE PARROQUIA (Mantenida)
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
    // LÓGICA DE USUARIO ADMIN (CORREGIDA: Ahora retorna Person)
    // ----------------------------------------------------------------------------------
    private Person createAdminUser( // ⭐ CAMBIAR EL TIPO DE RETORNO A Person ⭐
                                    UserRepository userRepository,
                                    RoleRepository roleRepository,
                                    PersonRepository personRepository,
                                    PasswordEncoder passwordEncoder,
                                    Parish parish) {

        return personRepository.findByIdentificationTypeAndIdentificationNumber("CC", "000316") // Buscamos por ID de Persona
                .orElseGet(() -> {
                    Role adminRole = roleRepository.findByName(RoleName.ADMIN).get();

                    User adminUser = User.builder()
                            .email(ADMIN_EMAIL)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .isEnabled(true)
                            .forcePasswordChange(false)
                            .roles(Set.of(adminRole))
                            .build();

                    User savedUser = userRepository.save(adminUser);

                    Person adminPerson = Person.builder()
                            .firstName("Jose Colombio")
                            .lastName("Gonzalez Perez")
                            .identificationType("CC")
                            .identificationNumber("000316")
                            .gender(Gender.MALE)
                            .birthDate(LocalDate.of(1990, 1, 1))
                            .user(savedUser) // ⭐ CRUCIAL: Asociar el User al objeto Person ⭐
                            .parish(parish)
                            .build();

                    Person savedPerson = personRepository.save(adminPerson);
                    System.out.println("✅ Usuario Super ADMIN y Persona creados.");
                    return savedPerson; // ⭐ RETORNAR LA PERSONA GUARDADA ⭐
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
            SacramentDetailRepository sacramentDetailRepository, // ⭐ Repositorio de Detalles
            PasswordEncoder passwordEncoder,
            Parish demoParish,
            Person adminPerson) { // ⭐ Persona del Admin

        // Si ya hay más de 3 personas (Admin + Coordinator + 1 Feligrés base), omitimos la creación.
        if (personRepository.count() > 3) {
            System.out.println("ℹ️ Ya existen datos de prueba suficientes. Omitiendo creación de DEMO.");
            return;
        }

        Role userRole = roleRepository.findByName(RoleName.USER).get();
        Role coordinatorRole = roleRepository.findByName(RoleName.COORDINATOR).get();

        // --- 1. PERSONAS DE ROL CANÓNICO Y ADMINISTRATIVO ---

        // Usuario COORDINATOR/GESTOR
        User coordinatorUser = User.builder()
                .email("coordinador@demo.com")
                .password(passwordEncoder.encode("Coord123"))
                .isEnabled(true)
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

        // Ministro Oficiante (Pbro. - Sin User de aplicación)
        Person minister = Person.builder()
                .firstName("Padre Juan Pablo")
                .lastName("Vergara Silva")
                .identificationType("CC")
                .identificationNumber("11223344")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1975, 7, 25))
                .parish(demoParish)
                .build();
        minister = personRepository.save(minister);
        System.out.println("✅ Persona Ministro (Pbro.) creada.");

        // Padrino 1 / Testigo 1 (Sin User de aplicación)
        Person godfatherWitness1 = Person.builder()
                .firstName("Sofía")
                .lastName("Ramos Castro")
                .identificationType("CC")
                .identificationNumber("66778899")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1995, 3, 10))
                .parish(demoParish)
                .build();
        godfatherWitness1 = personRepository.save(godfatherWitness1);
        System.out.println("✅ Persona Padrino/Testigo 1 (Sofía) creada.");

        // Padrino 2 / Testigo 2 (Sin User de aplicación)
        Person godfatherWitness2 = Person.builder()
                .firstName("Roberto")
                .lastName("Molina Flores")
                .identificationType("CC")
                .identificationNumber("55443322")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1992, 9, 18))
                .parish(demoParish)
                .build();
        godfatherWitness2 = personRepository.save(godfatherWitness2);
        System.out.println("✅ Persona Padrino/Testigo 2 (Roberto) creada.");

        // --- 2. FELIGRESES (SACRAMENTANDOS) ---

        // Feligrés 1 (Carlos López - Contrayente 1, Bautizado, Confirmado)
        User regularUser = User.builder()
                .email("feligres@demo.com")
                .password(passwordEncoder.encode("Feligres123"))
                .isEnabled(true)
                .forcePasswordChange(false)
                .roles(Set.of(userRole))
                .build();
        regularUser = userRepository.save(regularUser);

        Person feligres1 = Person.builder()
                .firstName("Carlos Andrés")
                .lastName("López Díaz")
                .identificationType("CC")
                .identificationNumber("98765432")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 10, 20))
                .parish(demoParish)
                .user(regularUser)
                .build();
        feligres1 = personRepository.save(feligres1);
        System.out.println("✅ Feligrés 1 (Carlos López) creado.");

        // Feligrés 2 / Cónyuge (Luisa Vargas - Contrayente 2)
        Person feligres2Spouse = Person.builder()
                .firstName("Luisa Fernanda")
                .lastName("Vargas Pérez")
                .identificationType("CC")
                .identificationNumber("22446688")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2002, 5, 12))
                .parish(demoParish)
                .build();
        feligres2Spouse = personRepository.save(feligres2Spouse);
        System.out.println("✅ Feligrés 2 (Luisa Vargas) creado.");


        // --- 3. CREACIÓN DE REGISTROS SACRAMENTALES (con Detalles) ---

        // BAUTISMO
        Sacrament baptism = Sacrament.builder()
                .type(SacramentType.BAPTISM)
                .person(feligres1)
                .parish(demoParish)
                .celebrationDate(LocalDate.of(2003, 5, 10))
                .bookNumber("B-101")
                .pageNumber("30")
                .entryNumber("1234")
                .notes("Registrado en la Parroquia de San Judas Tadeo.")
                .build();
        baptism = sacramentRepository.save(baptism);

        SacramentDetail baptismDetail = SacramentDetail.builder()
                .sacrament(baptism)
                .officiantMinister(minister)
                .godfather1(godfatherWitness1)
                .godfather2(godfatherWitness2)
                .fatherNameText("Pedro López González")
                .motherNameText("Marta Díaz Ríos")
                .build();
        sacramentDetailRepository.save(baptismDetail);
        baptism.setSacramentDetail(baptismDetail);
        sacramentRepository.save(baptism);
        System.out.println("✅ Sacramento de Bautismo (ID: " + baptism.getId() + ") completo.");

        // CONFIRMACIÓN
        Sacrament confirmation = Sacrament.builder()
                .type(SacramentType.CONFIRMATION)
                .person(feligres1)
                .parish(demoParish)
                .celebrationDate(LocalDate.of(2015, 6, 20))
                .bookNumber("C-050")
                .pageNumber("15")
                .entryNumber("5678")
                .build();
        confirmation = sacramentRepository.save(confirmation);

        SacramentDetail confirmationDetail = SacramentDetail.builder()
                .sacrament(confirmation)
                .officiantMinister(adminPerson) // Usamos la Persona del Admin como Obispo
                .godfather1(godfatherWitness1)
                .fatherNameText("Pedro López González")
                .motherNameText("Marta Díaz Ríos")
                .build();
        sacramentDetailRepository.save(confirmationDetail);
        confirmation.setSacramentDetail(confirmationDetail);
        sacramentRepository.save(confirmation);
        System.out.println("✅ Sacramento de Confirmación (ID: " + confirmation.getId() + ") completo.");

        // MATRIMONIO
        Sacrament matrimony = Sacrament.builder()
                .type(SacramentType.MATRIMONY)
                .person(feligres1) // Contrayente 1 (Carlos)
                .parish(demoParish)
                .celebrationDate(LocalDate.of(2025, 1, 15))
                .bookNumber("M-022")
                .pageNumber("07")
                .entryNumber("9012")
                .build();
        matrimony = sacramentRepository.save(matrimony);

        SacramentDetail matrimonyDetail = SacramentDetail.builder()
                .sacrament(matrimony)
                .officiantMinister(minister)
                .spouse(feligres2Spouse) // Contrayente 2 (Luisa)
                .witness1(godfatherWitness1)
                .witness2(godfatherWitness2)
                .fatherNameText("Pedro López González")
                .motherNameText("Marta Díaz Ríos")
                .spouseMotherNameText("Luisa Perez Mateo")
                .spouseFatherNameText("Felipe Loarca Leones")
                .build();
        sacramentDetailRepository.save(matrimonyDetail);
        matrimony.setSacramentDetail(matrimonyDetail);
        sacramentRepository.save(matrimony);
        System.out.println("✅ Sacramento de Matrimonio (ID: " + matrimony.getId() + ") completo.");


        // --- 4. CITA DE PRUEBA ---
        Appointment upcomingAppointment = Appointment.builder()
                .person(feligres1)
                .parish(demoParish)
                .appointmentDateTime(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0))
                .status(AppointmentStatus.PENDING)
                .subject("Solicitud de Matrimonio")
                .notes("Revisar expedientes de Bautismo y Confirmación.")
                .sacrament(matrimony)
                .build();
        appointmentRepository.save(upcomingAppointment);
        System.out.println("✅ Cita de demostración creada.");

        System.out.println("\n=======================================================");
        System.out.println("         🎉 DATOS DE DEMO CARGADOS EXITOSAMENTE 🎉      ");
        System.out.println("=======================================================");
        System.out.println("  👤 ADMIN: " + ADMIN_EMAIL + " | Pass: " + ADMIN_PASSWORD);
        System.out.println("  👷 COORDINATOR: coordinador@demo.com | Pass: Coord123");
        System.out.println("  👨 FELIGRÉS: feligres@demo.com | Pass: Feligres123");
        System.out.println("  💡 IDs de prueba para certificados (GET /api/v1/certificates/{id}/pdf):");
        System.out.println("     - Bautismo (ID): " + baptism.getId());
        System.out.println("     - Confirmación (ID): " + confirmation.getId());
        System.out.println("     - Matrimonio (ID): " + matrimony.getId());
        System.out.println("=======================================================");
    }
}