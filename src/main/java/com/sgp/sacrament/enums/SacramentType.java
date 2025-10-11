package com.sgp.sacrament.enums;

import lombok.Getter;

@Getter
public enum SacramentType {
    BAPTISM("Bautismo", 1),
    CONFIRMATION("Confirmación", 2),
    COMMUNION("Primera Comunión", 3),
    MATRIMONY("Matrimonio", 4),
    HOLY_ORDERS("Orden Sacerdotal", 5),
    ANOINTING_OF_THE_SICK("Unción de los Enfermos", 6);

    private final String displayName;
    private final int level; // Opcional: define el orden canónico o importancia

    SacramentType(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
}