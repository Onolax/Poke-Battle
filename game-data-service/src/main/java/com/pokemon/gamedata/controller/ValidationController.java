package com.pokemon.gamedata.controller;

import com.pokemon.gamedata.service.ValidationResult;
import com.pokemon.gamedata.service.ValidationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/validate")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/team")
    public ValidationResult validateTeam(@RequestBody TeamRequest request) {
        return validationService.validateTeam(request.pokemonSlugs(), request.formatId());
    }

    public record TeamRequest(List<String> pokemonSlugs, String formatId) {
    }
}
