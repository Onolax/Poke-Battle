package com.pokemon.user.exception;

import java.util.List;

public class TeamValidationException extends RuntimeException {
    private final List<String> errors;

    public TeamValidationException(List<String> errors) {
        super("Team validation failed: " + errors);
        this.errors = errors;
    }

    public List<String> getErrors() { return errors; }
}
