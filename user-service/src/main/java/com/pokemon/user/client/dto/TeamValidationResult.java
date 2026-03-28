package com.pokemon.user.client.dto;

import java.util.List;

public record TeamValidationResult(boolean valid, List<String> errors) {}
