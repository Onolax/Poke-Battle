package com.pokemon.gamedata.seed;

import com.pokemon.gamedata.client.PokeApiClient;
import com.pokemon.gamedata.client.dto.PokeApiMove;
import com.pokemon.gamedata.client.dto.PokeApiPokemon;
import com.pokemon.gamedata.domain.*;
import com.pokemon.gamedata.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedRunner implements ApplicationRunner {

    private final PokeApiClient pokeApiClient;
    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final ItemRepository itemRepository;
    private final FormatRepository formatRepository;
    private final TypeChartRepository typeChartRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (pokemonRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding game data...");
        seedPokemon();
        seedMoves();
        seedItems();
        seedFormats();
        seedTypeChart();
        log.info("Seeding complete.");
    }

    private void seedPokemon() {
        List<Pokemon> pokemon = SeedData.POKEMON_SLUGS.stream().map(slug -> {
            PokeApiPokemon dto = pokeApiClient.fetchPokemon(slug);

            Pokemon p = new Pokemon();
            p.setId(slug);
            p.setDexNumber(dto.id());
            p.setName(dto.name());
            p.setTypes(dto.types().stream()
                    .map(t -> capitalize(t.type().name()))
                    .toList());
            p.setBaseStats(mapStats(dto.stats()));
            p.setAbilities(dto.abilities().stream()
                    .filter(a -> !a.isHidden())
                    .map(a -> a.ability().name())
                    .toList());
            p.setLearnset(SeedData.LEARNSETS.getOrDefault(slug, List.of()));
            p.setTier(Map.of("gen9ou", SeedData.GEN9OU_TIERS.getOrDefault(slug, "Untiered")));
            p.setWeightKg(dto.weight() / 10.0);
            return p;
        }).toList();

        pokemonRepository.saveAll(pokemon);
        log.info("Seeded {} pokemon", pokemon.size());
    }

    private void seedMoves() {
        List<Move> moves = SeedData.MOVE_SLUGS.stream().map(slug -> {
            PokeApiMove dto = pokeApiClient.fetchMove(slug);

            Move m = new Move();
            m.setId(slug);
            m.setName(dto.name());
            m.setType(capitalize(dto.type().name()));
            m.setCategory(capitalize(dto.damageClass().name()));
            m.setBasePower(dto.power());
            m.setAccuracy(dto.accuracy());
            m.setPp(dto.pp());
            m.setPriority(dto.priority());
            return m;
        }).toList();

        moveRepository.saveAll(moves);
        log.info("Seeded {} moves", moves.size());
    }

    private void seedItems() {
        List<Item> items = SeedData.ITEM_SLUGS.stream().map(slug -> {
            Item item = new Item();
            item.setId(slug);
            item.setName(slug);
            item.setEffect("UNKNOWN");
            item.setParam(0.0);
            return item;
        }).toList();

        itemRepository.saveAll(items);
        log.info("Seeded {} items", items.size());
    }

    private void seedFormats() {
        Format gen9ou = new Format();
        gen9ou.setId("gen9ou");
        gen9ou.setName("Gen 9 OU");
        gen9ou.setGeneration(9);
        gen9ou.setBannedPokemon(List.of());
        gen9ou.setBannedMoves(List.of("spore"));
        gen9ou.setTeamSize(6);
        gen9ou.setActiveSize(1);

        formatRepository.save(gen9ou);
        log.info("Seeded formats");
    }

    private void seedTypeChart() {
        TypeChart chart = new TypeChart();
        chart.setId("type_chart_gen9");
        chart.setChart(SeedData.TYPE_CHART);

        typeChartRepository.save(chart);
        log.info("Seeded type chart");
    }

    private BaseStats mapStats(List<PokeApiPokemon.StatEntry> stats) {
        BaseStats bs = new BaseStats();
        for (PokeApiPokemon.StatEntry s : stats) {
            switch (s.stat().name()) {
                case "hp" -> bs.setHp(s.baseStat());
                case "attack" -> bs.setAtk(s.baseStat());
                case "defense" -> bs.setDef(s.baseStat());
                case "special-attack" -> bs.setSpa(s.baseStat());
                case "special-defense" -> bs.setSpd(s.baseStat());
                case "speed" -> bs.setSpe(s.baseStat());
            }
        }
        return bs;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
