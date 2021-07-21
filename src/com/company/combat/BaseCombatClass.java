package com.company.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public abstract class BaseCombatClass implements CombatClass {
    // INPUTS
    protected static final List<Integer> ARMOR_CLASSES = new ArrayList<>();
    protected final String characterName;
    protected final int characterLevel;
    protected final int numberWeaponDamageDie; // e.g. xd6
    protected final int weaponDamageDie; // 1dx
    protected final int statBonus; // from STR/DEX
    protected final int critDie; // e.g., 19 for 19 or 20 crit
    protected final int proficiencyBonus;

    // Outputs - Enemy Armor Class to Some result.
    protected final Map<Integer, Integer> singleLargestAttackDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalNumWeaponAttacksMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalNumWeaponAttacksHitMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalWeaponDamageMap = new LinkedHashMap<>();

    static {
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            ARMOR_CLASSES.add(armorClass);
        }
    }

    public BaseCombatClass(String characterName,
                           int characterLevel,
                           int numberWeaponDamageDie,
                           int weaponDamageDie,
                           int statBonus,
                           int critDie,
                           int proficiencyBonus) {
        this.characterName = characterName;
        this.characterLevel = characterLevel;
        this.numberWeaponDamageDie = numberWeaponDamageDie;
        this.weaponDamageDie = weaponDamageDie;
        this.statBonus = statBonus;
        this.critDie = critDie;
        this.proficiencyBonus = proficiencyBonus;

        for (int armorClass: ARMOR_CLASSES) {
            totalDamageMap.computeIfAbsent(armorClass, k -> 0);
            totalNumWeaponAttacksMap.computeIfAbsent(armorClass, k -> 0);
            totalNumWeaponAttacksHitMap.computeIfAbsent(armorClass, k -> 0);
            totalWeaponDamageMap.computeIfAbsent(armorClass, k -> 0);
            singleLargestAttackDamageMap.computeIfAbsent(armorClass, k -> 0);
        }
    }

    @Override
    public String getName() {
        return characterName;
    }

    @Override
    public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
        final Map<Integer, Double> averageTotalDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalDamageMap.entrySet()) {
            averageTotalDamagePerTurnMap.put(entry.getKey(), entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> averageTotalWeaponDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalWeaponDamageMap.entrySet()) {
            averageTotalWeaponDamagePerTurnMap.put(entry.getKey(), entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> attackAccuracyMap = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalNumWeaponAttacksMap.entrySet()) {
            double averageAccuracy = (double) totalNumWeaponAttacksHitMap.get(entry.getKey()) / totalNumWeaponAttacksMap.get(entry.getKey());
            attackAccuracyMap.put(entry.getKey(), averageAccuracy);
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
    protected WeaponAttackResults doWeaponAttack(int enemyArmorClass) {
        // Attack normally.
        int attackRoll = rollD20(false);
        boolean doesAttackHit = determineHit(attackRoll, enemyArmorClass);
        boolean isCriticalHit = false;
        int weaponAttackDamage = 0;
        if (doesAttackHit) {
            // Determine damage.
            isCriticalHit = isCriticalHit(attackRoll);
            weaponAttackDamage = calculateWeaponAttackDamage();
            if (isCriticalHit) {
                weaponAttackDamage += rollDie(weaponDamageDie, false);
            }

            // Accumulate total weapon damage.
            int totalWeaponDamage = totalWeaponDamageMap.get(enemyArmorClass);
            totalWeaponDamageMap.put(enemyArmorClass, totalWeaponDamage + weaponAttackDamage);
            // Accumulate total damage.
            int totalDamage = totalDamageMap.get(enemyArmorClass);
            totalDamageMap.put(enemyArmorClass, totalDamage + weaponAttackDamage);
            // Increment total number of attacks hit.
            int totalAttacksHit = totalNumWeaponAttacksHitMap.get(enemyArmorClass);
            totalNumWeaponAttacksHitMap.put(enemyArmorClass, totalAttacksHit + 1);
        }
        // Increment total number of attacks.
        int totalAttacks = totalNumWeaponAttacksMap.get(enemyArmorClass);
        totalNumWeaponAttacksMap.put(enemyArmorClass, totalAttacks + 1);

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
        for (int i = 0; i < numberWeaponDamageDie; i++) {
            damageRoll += rollDie(damageDie, false);
        }
        return damageRoll + statDamageBonus + damageBonuses;
    }

    protected static int rollDie(int die, boolean advantage) {
        int first = ThreadLocalRandom.current().nextInt(1, die + 1);
        if (!advantage) {
            return first;
        }
        int second = ThreadLocalRandom.current().nextInt(1, die + 1);
        if (second > first) {
            return second;
        } else {
            return first;
        }
    }

    protected static int rollD20(boolean advantage) {
        return rollDie(20, advantage);
    }

    protected boolean determineHit(int attackRoll, int armorClass) {
        return (attackRoll + statBonus) >= armorClass;
    }
}
