package com.company.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaseCombatClass implements CombatClass {
    // INPUTS
    private  final Map<Integer, List<Integer>> ARMOR_CLASS_TO_ACC_BONUS = new LinkedHashMap<>();

    // Outputs
    protected final Map<Integer, Map<Integer, Double>> totalNumAttacks = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Double>> totalDamage = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Double>> attackAccuracy = new LinkedHashMap<>();



    private  final Map<Integer, Map<Integer, Double>> TOTAL_TECH_2HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Integer>> TURN_COUNT_TECH_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> AVG_TECH_2HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> TOTAL_TECH_3HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> AVG_TECH_3HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> TOTAL_EM_PER_TURN_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> AVG_EM_PER_TURN_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> TOTAL_TECH_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> AVG_TECH_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> TOTAL_IAI_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> AVG_IAI_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();

    private  final Map<Integer, Map<Integer, Double>> TOTAL_AVG_DAMAGE_MAP = new LinkedHashMap<>();
    private  final Map<Integer, Map<Integer, Double>> AVG_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();

    public BaseCombatClass() {
        // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            for (int accBonus = 5; accBonus <= 9; accBonus++) {
                ARMOR_CLASS_TO_ACC_BONUS.computeIfAbsent(armorClass, k -> new ArrayList<>()).add(accBonus);
                TOTAL_TECH_2HIT_ACCURACY_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                TOTAL_TECH_3HIT_ACCURACY_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                TURN_COUNT_TECH_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                TOTAL_EM_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                TOTAL_TECH_DAMAGE_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                TOTAL_IAI_DAMAGE_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                AVG_TECH_2HIT_ACCURACY_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                AVG_TECH_3HIT_ACCURACY_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                AVG_EM_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                AVG_TECH_DAMAGE_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                AVG_IAI_DAMAGE_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                TOTAL_AVG_DAMAGE_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
                AVG_DAMAGE_PER_TURN_MAP.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0.0);
            }
        }
    }

    @Override
    public int getCumulativeDamage() {
        return 0;
    }

    @Override
    public int getCumulativeNumberOfRoundsAttacking() {
        return 0;
    }

    @Override
    public int doCombatTurn() {
        return 0;
    }

    protected static boolean determineHit(int d20Roll, int accuracyBonus, int armorClass) {
        return (d20Roll + accuracyBonus) >= armorClass;
    }
}
