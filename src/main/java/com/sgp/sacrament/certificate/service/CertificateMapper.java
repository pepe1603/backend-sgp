package com.sgp.sacrament.certificate.service;

import com.sgp.person.model.Person;
import com.sgp.sacrament.certificate.dto.SacramentCertificateDTO;
import com.sgp.sacrament.enums.SacramentType;
import com.sgp.sacrament.model.Sacrament;
import com.sgp.sacrament.model.SacramentDetail;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Mapper para convertir las entidades Sacrament y relacionadas en el DTO de Certificado.
 * Incluye lógica de manejo de relaciones Lazy/nulas y campos específicos de Matrimonio.
 */
@Mapper(componentModel = "spring")
public interface CertificateMapper {

    CertificateMapper INSTANCE = Mappers.getMapper(CertificateMapper.class);

    // ⭐ FUNCIONES AUXILIARES PARA MANEJAR NULOS ⭐

    /** Retorna el nombre completo de una persona o "-" si no está registrado. */
    default String getFullNameOrNotRegistered(Person person) {
        return Optional.ofNullable(person)
                .map(Person::getFullName)
                .orElse("-");
    }

    /** Retorna el texto del campo si existe, o un guion. */
    default String getTextOrHyphen(String text) {
        return Optional.ofNullable(text)
                .orElse("-");
    }

    // ⭐ MÉTODO PRINCIPAL DE MAPEO ⭐
    default SacramentCertificateDTO toCertificateDTO(Sacrament sacrament) {
        if (sacrament == null) {
            return null;
        }

        SacramentDetail detail = Optional.ofNullable(sacrament.getSacramentDetail()).orElse(new SacramentDetail());
        Person feligres = sacrament.getPerson();

        // Roles comunes
        String ministro = getFullNameOrNotRegistered(detail.getOfficiantMinister());
        String padrino1 = getFullNameOrNotRegistered(detail.getGodfather1());
        String padrino2 = getFullNameOrNotRegistered(detail.getGodfather2());

        // Roles de matrimonio/testigos
        String contrayente2 = getFullNameOrNotRegistered(detail.getSpouse());
        String testigo1 = getFullNameOrNotRegistered(detail.getWitness1());
        String testigo2 = getFullNameOrNotRegistered(detail.getWitness2());

        // Ajuste para bautismo o confirmación
        if (sacrament.getType() != SacramentType.MATRIMONY) {
            testigo1 = padrino1;
            testigo2 = padrino2;
        }

        return SacramentCertificateDTO.builder()
                .sacramentId(sacrament.getId())
                .tipoSacramento(sacrament.getType())
                .fechaCelebracion(sacrament.getCelebrationDate())
                .libro(getTextOrHyphen(sacrament.getBookNumber()))
                .folio(getTextOrHyphen(sacrament.getPageNumber()))
                .asiento(getTextOrHyphen(sacrament.getEntryNumber()))
                .notas(getTextOrHyphen(sacrament.getNotes()))

                // Datos del feligrés
                .nombreCompletoFeligres(feligres.getFullName())
                .fechaNacimientoFeligres(feligres.getBirthDate())
                .identificacionFeligres(feligres.getIdentificationType() + " " + feligres.getIdentificationNumber())

                // Datos de la parroquia
                .nombreParroquiaCelebracion(sacrament.getParish().getName())
                .direccionParroquiaCelebracion(sacrament.getParish().getAddress())
                .ciudadParroquiaCelebracion(sacrament.getParish().getCity())

                // Datos adicionales (roles comunes)
                .ministroOficianteNombre(ministro)
                .padreNombreText(getTextOrHyphen(detail.getFatherNameText()))
                .madreNombreText(getTextOrHyphen(detail.getMotherNameText()))

                // Padrinos (usados para bautismo/confirmación)
                .padrino1Nombre(padrino1)
                .padrino2Nombre(padrino2)

                // Datos exclusivos matrimonio/testigos
                .contrayente2NombreCompleto(contrayente2)

                .testigo1Nombre(testigo1)
                .testigo2Nombre(testigo2)
                // ⭐ Mapear los campos del cónyuge 2 que ya existen en la Entidad ⭐
                .contrayente2PadreNombreText(getTextOrHyphen(detail.getSpouseFatherNameText()))
                .contrayente2MadreNombreText(getTextOrHyphen(detail.getSpouseMotherNameText()))
                .fechaEmisionCertificado(LocalDate.now())
                .build();
    }
}
