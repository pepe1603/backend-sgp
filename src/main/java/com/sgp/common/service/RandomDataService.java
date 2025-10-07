package com.sgp.common.service;


import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RandomDataService {

    private static final String[] RANDOM_ADDRESSES = {
            "123 Calle Ficticia, Ciudad Aleatoria, País Imaginario",
            "456 Av. Desconocida, Pueblo Misterioso, País Desconocido",
            "789 Plaza Central, Ciudad Inexistente, País Fantástico",
            "101 Avenida Siempre Viva, Springfield, USA"
    };

    private static final String[] RANDOM_PHONE_NUMBERS = {
            "+1234567890",
            "+0987654321",
            "+1122334455",
            "+5566778899"
    };

    private static final String[] RANDOM_RECOMMENDATIONS = {
            "Recomendado: Amante de la tecnología.",
            "Recomendado: Aficionado al cine y series.",
            "Recomendado: Apasionado por el deporte.",
            "Recomendado: Estudiante de idiomas y culturas."
    };

    private static final String[] RANDOM_OCCUPATIONS = {
            "Desarrollador de software",
            "Diseñador gráfico",
            "Marketing digital",
            "Analista de datos"
    };

    private static final String[] RANDOM_GENDERS = {
            "Masculino",
            "Femenino",
            "Otro"
    };

    private static final String[] RANDOM_COUNTRIES = {
            "Argentina",
            "Brasil",
            "México",
            "España",
            "Colombia"
    };

    private static final Random random = new Random();

    public String generateRandomAddress() {
        return RANDOM_ADDRESSES[random.nextInt(RANDOM_ADDRESSES.length)];
    }

    public String generateRandomPhoneNumber() {
        return RANDOM_PHONE_NUMBERS[random.nextInt(RANDOM_PHONE_NUMBERS.length)];
    }

    public String generateRandomRecommendation() {
        return RANDOM_RECOMMENDATIONS[random.nextInt(RANDOM_RECOMMENDATIONS.length)];
    }

    public String generateRandomOccupation() {
        return RANDOM_OCCUPATIONS[random.nextInt(RANDOM_OCCUPATIONS.length)];
    }

    public String generateRandomGender() {
        return RANDOM_GENDERS[random.nextInt(RANDOM_GENDERS.length)];
    }

    public String generateRandomCountry() {
        return RANDOM_COUNTRIES[random.nextInt(RANDOM_COUNTRIES.length)];
    }

    // Método para generar una fecha de nacimiento aleatoria
    public String generateRandomBirthdate() {
        int year = random.nextInt(30) + 1980;  // Entre 1980 y 2009
        int month = random.nextInt(12) + 1;  // Mes entre 1 y 12
        int day = random.nextInt(28) + 1;  // Día entre 1 y 28 para evitar meses con más de 31 días
        return year + "-" + month + "-" + day;
    }
}