package com.vivek.clubRegistration.exception;

public class RegistrationException extends RuntimeException{

    private final String rule; // which rule was violated

    public RegistrationException(String message, String rule) {
        super(message);
        this.rule = rule;
    }

    public RegistrationException(String message) {
        super(message);
        this.rule = "GENERAL";
    }

    public String getRule() {
        return rule;
    }
}
