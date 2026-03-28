package com.pokemon.gamedata.service;

import java.util.List;

public record ValidationResult(boolean valid, List<String> errors) {
}
