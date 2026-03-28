package com.pokemon.gamedata.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseStats {
    private int hp;
    private int atk;
    private int def;
    private int spa;
    private int spd;
    private int spe;
}
