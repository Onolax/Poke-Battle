package com.pokemon.gamedata.seed;

import java.util.List;
import java.util.Map;

public class SeedData {

    public static final List<String> POKEMON_SLUGS = List.of(
            "garchomp", "great-tusk", "iron-hands", "gholdengo", "kingambit",
            "dragapult", "roaring-moon", "iron-valiant", "volcarona", "garganacl",
            "ting-lu", "clodsire", "skeledirge", "meowscarada", "iron-moth",
            "glimmora", "corviknight", "hatterene", "rillaboom", "toxapex"
    );

    public static final List<String> MOVE_SLUGS = List.of(
            "earthquake", "thunderbolt", "flamethrower", "ice-beam", "surf",
            "shadow-ball", "close-combat", "knock-off", "u-turn", "protect",
            "stealth-rock", "recover", "will-o-wisp", "toxic", "swords-dance",
            "nasty-plot", "calm-mind", "body-press", "draco-meteor", "spore"
    );

    public static final List<String> ITEM_SLUGS = List.of(
            "leftovers", "choice-scarf", "choice-band", "choice-specs", "life-orb",
            "rocky-helmet", "eviolite", "assault-vest", "heavy-duty-boots", "booster-energy"
    );

    // tier[pokemonSlug] = tier string in gen9ou format
    public static final Map<String, String> GEN9OU_TIERS = Map.ofEntries(
            Map.entry("garchomp", "OU"),
            Map.entry("great-tusk", "OU"),
            Map.entry("iron-hands", "OU"),
            Map.entry("gholdengo", "OU"),
            Map.entry("kingambit", "OU"),
            Map.entry("dragapult", "OU"),
            Map.entry("roaring-moon", "OU"),
            Map.entry("iron-valiant", "OU"),
            Map.entry("volcarona", "OU"),
            Map.entry("garganacl", "OU"),
            Map.entry("ting-lu", "OU"),
            Map.entry("clodsire", "OU"),
            Map.entry("skeledirge", "OU"),
            Map.entry("meowscarada", "OU"),
            Map.entry("iron-moth", "OU"),
            Map.entry("glimmora", "OU"),
            Map.entry("corviknight", "OU"),
            Map.entry("hatterene", "OU"),
            Map.entry("rillaboom", "OU"),
            Map.entry("toxapex", "OU")
    );

    // learnset[pokemonSlug] = list of move slugs
    public static final Map<String, List<String>> LEARNSETS = Map.ofEntries(
            Map.entry("garchomp", List.of("earthquake", "draco-meteor", "swords-dance", "u-turn", "stealth-rock")),
            Map.entry("great-tusk", List.of("earthquake", "close-combat", "stealth-rock", "u-turn", "body-press")),
            Map.entry("iron-hands", List.of("close-combat", "thunderbolt", "swords-dance", "u-turn", "protect")),
            Map.entry("gholdengo", List.of("shadow-ball", "nasty-plot", "recover", "thunderbolt", "flamethrower")),
            Map.entry("kingambit", List.of("knock-off", "swords-dance", "close-combat", "protect", "u-turn")),
            Map.entry("dragapult", List.of("draco-meteor", "shadow-ball", "thunderbolt", "flamethrower", "u-turn")),
            Map.entry("roaring-moon", List.of("knock-off", "u-turn", "draco-meteor", "earthquake", "swords-dance")),
            Map.entry("iron-valiant", List.of("close-combat", "shadow-ball", "thunderbolt", "calm-mind", "nasty-plot")),
            Map.entry("volcarona", List.of("flamethrower", "nasty-plot", "calm-mind", "recover", "protect")),
            Map.entry("garganacl", List.of("body-press", "recover", "stealth-rock", "toxic", "protect")),
            Map.entry("ting-lu", List.of("earthquake", "knock-off", "stealth-rock", "toxic", "protect")),
            Map.entry("clodsire", List.of("earthquake", "toxic", "recover", "stealth-rock", "protect")),
            Map.entry("skeledirge", List.of("flamethrower", "shadow-ball", "recover", "will-o-wisp", "protect")),
            Map.entry("meowscarada", List.of("knock-off", "u-turn", "spore", "swords-dance", "protect")),
            Map.entry("iron-moth", List.of("flamethrower", "nasty-plot", "calm-mind", "thunderbolt", "recover")),
            Map.entry("glimmora", List.of("stealth-rock", "toxic", "flamethrower", "protect", "recover")),
            Map.entry("corviknight", List.of("body-press", "u-turn", "stealth-rock", "recover", "protect")),
            Map.entry("hatterene", List.of("calm-mind", "nasty-plot", "recover", "shadow-ball", "flamethrower")),
            Map.entry("rillaboom", List.of("knock-off", "u-turn", "swords-dance", "protect", "earthquake")),
            Map.entry("toxapex", List.of("toxic", "recover", "knock-off", "will-o-wisp", "protect"))
    );

    // TYPE_CHART: attackType -> defenseType -> multiplier
    public static final Map<String, Map<String, Double>> TYPE_CHART = Map.ofEntries(
            Map.entry("normal",   Map.of("rock", 0.5, "ghost", 0.0, "steel", 0.5)),
            Map.entry("fire",     Map.of("fire", 0.5, "water", 0.5, "grass", 2.0, "ice", 2.0, "bug", 2.0, "rock", 0.5, "dragon", 0.5, "steel", 2.0)),
            Map.entry("water",    Map.of("fire", 2.0, "water", 0.5, "grass", 0.5, "ground", 2.0, "rock", 2.0, "dragon", 0.5)),
            Map.entry("grass",    Map.of("fire", 0.5, "water", 2.0, "grass", 0.5, "poison", 0.5, "ground", 2.0, "flying", 0.5, "bug", 0.5, "rock", 2.0, "dragon", 0.5, "steel", 0.5)),
            Map.entry("electric", Map.of("water", 2.0, "electric", 0.5, "grass", 0.5, "ground", 0.0, "flying", 2.0, "dragon", 0.5)),
            Map.entry("ice",      Map.of("fire", 0.5, "water", 0.5, "grass", 2.0, "ice", 0.5, "ground", 2.0, "flying", 2.0, "dragon", 2.0, "steel", 0.5)),
            Map.entry("fighting", Map.ofEntries(Map.entry("normal", 2.0), Map.entry("ice", 2.0), Map.entry("poison", 0.5), Map.entry("flying", 0.5), Map.entry("psychic", 0.5), Map.entry("bug", 0.5), Map.entry("rock", 2.0), Map.entry("ghost", 0.0), Map.entry("dark", 2.0), Map.entry("steel", 2.0), Map.entry("fairy", 0.5))),
            Map.entry("poison",   Map.of("grass", 2.0, "poison", 0.5, "ground", 0.5, "rock", 0.5, "ghost", 0.5, "steel", 0.0, "fairy", 2.0)),
            Map.entry("ground",   Map.of("fire", 2.0, "electric", 2.0, "grass", 0.5, "poison", 2.0, "flying", 0.0, "bug", 0.5, "rock", 2.0, "steel", 2.0)),
            Map.entry("flying",   Map.of("electric", 0.5, "grass", 2.0, "fighting", 2.0, "bug", 2.0, "rock", 0.5, "steel", 0.5)),
            Map.entry("psychic",  Map.of("fighting", 2.0, "poison", 2.0, "psychic", 0.5, "dark", 0.0, "steel", 0.5)),
            Map.entry("bug",      Map.ofEntries(Map.entry("fire", 0.5), Map.entry("grass", 2.0), Map.entry("fighting", 0.5), Map.entry("poison", 0.5), Map.entry("flying", 0.5), Map.entry("psychic", 2.0), Map.entry("ghost", 0.5), Map.entry("dark", 2.0), Map.entry("steel", 0.5), Map.entry("fairy", 0.5))),
            Map.entry("rock",     Map.of("fire", 2.0, "ice", 2.0, "fighting", 0.5, "ground", 0.5, "flying", 2.0, "bug", 2.0, "steel", 0.5)),
            Map.entry("ghost",    Map.of("normal", 0.0, "psychic", 2.0, "ghost", 2.0, "dark", 0.5)),
            Map.entry("dragon",   Map.of("dragon", 2.0, "steel", 0.5, "fairy", 0.0)),
            Map.entry("dark",     Map.of("fighting", 0.5, "psychic", 2.0, "ghost", 2.0, "dark", 0.5, "fairy", 0.5)),
            Map.entry("steel",    Map.of("fire", 0.5, "water", 0.5, "electric", 0.5, "ice", 2.0, "rock", 2.0, "steel", 0.5, "fairy", 2.0)),
            Map.entry("fairy",    Map.of("fire", 0.5, "fighting", 2.0, "poison", 0.5, "dragon", 2.0, "dark", 2.0, "steel", 0.5))
    );

    private SeedData() {}
}
