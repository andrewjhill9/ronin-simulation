package com.company;

public class Stats {
    public static final double MIN_WPN_DMG = 1; // 1d8
    public static final double MAX_WPN_DMG = 8; // 1d8
    public static final double AVG_WPN_DMG = 4.5; // 1d8
    public static final double STAT_DMG_BONUS = 4; // DEX 18
    public static final double STANCE_DMG_BONUS = 3; // Fire lvl 2
    public static final double IAI_EM_COST = 4;
    public static final double AVG_HINGAN_DMG_PER_CHARGE = 5.5; // 1d10
    public static final int CRIT_DIE = 19; // 19 or 20
    public static final double MAX_ENERGY_CHARGES = 5;
    public static final double EM_CHARGES_PER_LIMIT_BREAK = 5;
    public static final double COMBATS_PER_REST = 2;
    public static final double ROUNDS_PER_COMBAT = 6;
    public static final double TOTAL_COMBAT_ROUNDS = COMBATS_PER_REST * ROUNDS_PER_COMBAT;
    public static final int MEDITATION_CHARGES = 3;
    public static final double MEDITATION_PROC_RATE = MEDITATION_CHARGES / TOTAL_COMBAT_ROUNDS;
    public static final double TECH_DMG_MOD = 3.0/3.0;
}
