package com.company.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public abstract class BaseCombatClass implements CombatClass {
    // INPUTS
    protected final Map<Integer, List<Integer>> armorClassToAccBonus = new LinkedHashMap<>();
    protected final int characterLevel;
    protected final int numberWeaponDamageDie; // e.g. xd6
    protected final int weaponDamageDie; // 1dx
    protected final int statBonus; // from STR/DEX
    protected final int critDie; // e.g., 19 for 19 or 20 crit
    protected final int proficiencyBonus;

    // Outputs
    protected final Map<Integer, Map<Integer, Integer>> singleLargestAttackDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalNumWeaponAttacksMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalNumWeaponAttacksHitMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalWeaponDamageMap = new LinkedHashMap<>();

    public BaseCombatClass(int characterLevel,
                           int numberWeaponDamageDie,
                           int weaponDamageDie,
                           int statBonus,
                           int critDie,
                           int proficiencyBonus) {
        this.characterLevel = characterLevel;
        this.numberWeaponDamageDie = numberWeaponDamageDie;
        this.weaponDamageDie = weaponDamageDie;
        this.statBonus = statBonus;
        this.critDie = critDie;
        this.proficiencyBonus = proficiencyBonus;

        // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            for (int accBonus = 5; accBonus <= 9; accBonus++) {
                armorClassToAccBonus.computeIfAbsent(armorClass, k -> new ArrayList<>()).add(accBonus);
                totalDamageMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                totalNumWeaponAttacksMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                totalNumWeaponAttacksHitMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                totalWeaponDamageMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                singleLargestAttackDamageMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
            }
        }
    }

    @Override
    public Map<String, Map<Integer, Map<Integer, ?>>> getStatistics(int numberOfTurns) {
        final Map<Integer, Map<Integer, Double>> averageTotalDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalDamageMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                averageTotalDamagePerTurnMap.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), innerEntry.getValue() / (double)numberOfTurns);
            }
        }

        final Map<Integer, Map<Integer, Double>> averageTotalWeaponDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalWeaponDamageMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                averageTotalWeaponDamagePerTurnMap.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), innerEntry.getValue() / (double)numberOfTurns);
            }
        }

        final Map<Integer, Map<Integer, Double>> attackAccuracyMap = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalNumWeaponAttacksMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                double averageAccuracy = (double)totalNumWeaponAttacksHitMap.get(entry.getKey()).get(innerEntry.getKey()) /
                        totalNumWeaponAttacksMap.get(entry.getKey()).get(innerEntry.getKey());
                attackAccuracyMap.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), averageAccuracy);
            }
        }


        Map stats = new LinkedHashMap();

        stats.put("singleLargestAttackDamageMap", singleLargestAttackDamageMap);
        stats.put("totalNumAttacksMap", totalNumWeaponAttacksMap);
        stats.put("totalDamageMap", totalDamageMap);
        stats.put("totalWeaponDamageMap", totalWeaponDamageMap);
        stats.put("attackAccuracyMap", attackAccuracyMap);
        stats.put("averageTotalDamagePerTurnMap", averageTotalDamagePerTurnMap);
        stats.put("averageTotalWeaponDamagePerTurnMap", averageTotalWeaponDamagePerTurnMap);

        return stats;
    }

    /**
     * @return 0 if weapon attack misses. 1 if weapon attack hits. 2 if weapon attack crits.
     */
    protected WeaponAttackResults doWeaponAttack(int accuracyBonus, int enemyArmorClass) {
        // Attack normally.
        int attackRoll = rollD20(false);
        boolean doesAttackHit = determineHit(attackRoll, accuracyBonus, enemyArmorClass);
        boolean isCriticalHit = false;
        int weaponAttackDamage = 0;
        if(doesAttackHit) {
            // Determine damage.
            isCriticalHit = isCriticalHit(attackRoll);
            weaponAttackDamage = calculateWeaponAttackDamage();
            if(isCriticalHit) {
                weaponAttackDamage += rollDie(weaponDamageDie, false);
            }

            // Accumulate total weapon damage.
            int totalWeaponDamage = totalWeaponDamageMap.get(enemyArmorClass).get(accuracyBonus);
            totalWeaponDamageMap.get(enemyArmorClass).put(accuracyBonus, totalWeaponDamage+weaponAttackDamage);
            // Accumulate total damage.
            int totalDamage = totalDamageMap.get(enemyArmorClass).get(accuracyBonus);
            totalDamageMap.get(enemyArmorClass).put(accuracyBonus, totalDamage+weaponAttackDamage);
            // Increment total number of attacks hit.
            int totalAttacksHit = totalNumWeaponAttacksHitMap.get(enemyArmorClass).get(accuracyBonus);
            totalNumWeaponAttacksHitMap.get(enemyArmorClass).put(accuracyBonus, totalAttacksHit+1);
        }
        // Increment total number of attacks.
        int totalAttacks = totalNumWeaponAttacksMap.get(enemyArmorClass).get(accuracyBonus);
        totalNumWeaponAttacksMap.get(enemyArmorClass).put(accuracyBonus, totalAttacks+1);

        return new WeaponAttackResults(weaponAttackDamage, isCriticalHit, doesAttackHit);
    }

    protected int calculateWeaponAttackDamage() {
        return rollDamage(numberWeaponDamageDie, weaponDamageDie, statBonus, 0);
    }

    protected boolean isCriticalHit(int roll) {
        return roll >= critDie;
    }

    protected static int rollDamage(int numberWeaponDamageDie, int damageDie, int statDamageBonus, int damageBonuses) {
        int damageRoll = 0;
        for(int i = 0; i < numberWeaponDamageDie; i++) {
            damageRoll += rollDie(damageDie, false);
        }
        return damageRoll + statDamageBonus + damageBonuses;
    }

    protected static int rollDie(int die, boolean advantage) {
        int first = ThreadLocalRandom.current().nextInt(1, die+1);
        if(!advantage) {
            return first;
        }
        int second = ThreadLocalRandom.current().nextInt(1, die+1);
        if(second > first) {
            return second;
        } else {
            return first;
        }
    }

    protected static int rollD20(boolean advantage) {
        return rollDie(20, advantage);
    }

    protected static boolean determineHit(int d20Roll, int accuracyBonus, int armorClass) {
        return (d20Roll + accuracyBonus) >= armorClass;
    }
}
