package com.itdev.enums;

public enum SubjectDomain {
    MEDICINE_AND_BIOLOGY("medicine and biology"),
    PSYCHOLOGY_AND_SOCIAL_SCIENCES("psychology and social sciences"),
    ECONOMICS_AND_FINANCE("economics and finance");

    private final String name;

    SubjectDomain(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
