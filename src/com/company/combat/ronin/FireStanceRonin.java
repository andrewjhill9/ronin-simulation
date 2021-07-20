package com.company.combat.ronin;

import com.company.combat.WeaponAttackResults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class FireStanceRonin extends RoninCombatClass {
    protected static final int IGNITE_DAMAGE_DIE = 4;
    protected static final Function<Integer, Integer> igniteNumberOfDie;
    protected static final int HIGANBANA_ID = 0;
    protected final boolean isTargetMindsEye;
    protected final boolean canUseIgnite;
    protected boolean isIgniteUsed = false;
    protected boolean isTargetBurning = false;

    // Outputs
    protected final Map<Integer, Map<Integer, Integer>> totalHiganbanaDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalIgniteDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalTurnsEnemyIgnitedMap = new LinkedHashMap<>();

    private final Function<IaiFunctionParameters, Void> higanbanaFunction;

    static {
        igniteNumberOfDie = (charLevel) -> {
            if (charLevel >= 20) {
                return 6;
            } else if (charLevel >= 15) {
                return 5;
            } else if (charLevel >= 10) {
                return 4;
            } else if (charLevel >= 5) {
                return 3;
            } else if (charLevel >= 2) {
                return 2;
            } else {
                return 0;
            }
        };
    }

    public FireStanceRonin(int characterLevel,
                           int numberWeaponDamageDie,
                           int weaponDamageDie,
                           int statBonus,
                           int critDie,
                           int proficiencyBonus,
                           boolean isTargetMindsEye) {
        super(characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
        this.isTargetMindsEye = isTargetMindsEye;
        this.canUseIgnite = characterLevel >= 2;
        iaiToIaiMinimumCost.put(HIGANBANA_ID, 5);
        iaiToIaiDamageDie.put(HIGANBANA_ID, 12);
        iaiToDiePerCharge.put(HIGANBANA_ID, 1);
        iaiToMinimumLevel.put(HIGANBANA_ID, 3);

        // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            for (int accBonus = 5; accBonus <= 9; accBonus++) {
                totalHiganbanaDamageMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                totalIgniteDamageMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                totalTurnsEnemyIgnitedMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
            }
        }

        // Define Higanbana Function
        higanbanaFunction = iaiFunctionParameters -> {
            int accBonus = iaiFunctionParameters.getAccuracyBonus();
            int armorClass = iaiFunctionParameters.getArmorClass();

            int d20Roll = rollD20(isTargetMindsEye);
            boolean hitSuccess = determineHit(d20Roll, accBonus, armorClass);

            if (hitSuccess) {
                boolean critHit = isCriticalHit((d20Roll));

                int higanbanaDamage = 0;
                for (int i = 0; i < energyCharges; i += iaiToDiePerCharge.get(HIGANBANA_ID)) {
                    if (critHit) {
                        higanbanaDamage += rollDie(iaiToIaiDamageDie.get(HIGANBANA_ID), false) + rollDie(iaiToIaiDamageDie.get(HIGANBANA_ID), false);
                    } else {
                        higanbanaDamage += rollDie(iaiToIaiDamageDie.get(HIGANBANA_ID), false);
                    }
                }
                // Accumulate total damage.
                int totalDamage = totalDamageMap.get(armorClass).get(accBonus);
                totalDamageMap.get(armorClass).put(accBonus, totalDamage + higanbanaDamage);
                // Accumulate total higanbana damage.
                int totalHiganbanaDamage = totalHiganbanaDamageMap.get(armorClass).get(accBonus);
                totalHiganbanaDamageMap.get(armorClass).put(accBonus, totalHiganbanaDamage + higanbanaDamage);

                // Determine if this turn's attack was the largest yet.
                int singleLargestAttackDamage = singleLargestAttackDamageMap.get(armorClass).get(accBonus);
                if(higanbanaDamage > singleLargestAttackDamage) {
                    singleLargestAttackDamageMap.get(armorClass).put(accBonus, higanbanaDamage);
                }

                // Ignite the enemy.
                isTargetBurning = true;
            }
            return null;
        };


        // Add all Iai functions to map.
        iaiIdToIaiExecutionFunction.put(HIGANBANA_ID, higanbanaFunction);
    }

    @Override
    public void doShortRest() {
        super.doShortRest();
        isIgniteUsed = false;
        isTargetBurning = false;
    }

    @Override
    public void doLongRest() {
        super.doLongRest();
        isIgniteUsed = false;
        isTargetBurning = false;
    }

    @Override
    public void doCombatTurn(int accuracyBonus, int enemyArmorClass) {
        // Simulate Ignite damage.
        if (isTargetBurning) {
            // Increment total number of ignite turns.
            int totalTurnsEnemyIgnited = totalTurnsEnemyIgnitedMap.get(enemyArmorClass).get(accuracyBonus);
            totalTurnsEnemyIgnited++;
            totalTurnsEnemyIgnitedMap.get(enemyArmorClass).put(accuracyBonus, totalTurnsEnemyIgnited);

            doIgniteDamage(accuracyBonus, enemyArmorClass);
            int savingThrow = rollD20(false) + 3;
            if (savingThrow >= swordArtDc) {
                isTargetBurning = false;
            }
        }

        // Decide what to do during turn.
        if (energyCharges == 0 && !usedLimitBreak && canUseLimitBreak) {
            // Use Limit Break and then Iai
            usedLimitBreak = true;
            energyCharges += chargesPerLimitBreak;
            int totalEnergyCharges = totalEnergyChargesAccumulatedMap.get(enemyArmorClass).get(accuracyBonus);
            totalEnergyCharges += chargesPerLimitBreak;
            totalEnergyChargesAccumulatedMap.get(enemyArmorClass).put(accuracyBonus, totalEnergyCharges);

            executeIai(enemyArmorClass, accuracyBonus, HIGANBANA_ID);
            energyCharges = 0;

        } else if (energyCharges >= iaiToIaiMinimumCost.get(HIGANBANA_ID)) {
            // Use Iai to avoid over-capping.
            executeIai(enemyArmorClass, accuracyBonus, HIGANBANA_ID);
            energyCharges = 0;

        } else {
            // Attack normally.
            WeaponAttackResults weaponAttackResults = doWeaponAttack(accuracyBonus, enemyArmorClass);
            if (weaponAttackResults.didHit()) {
                energyCharges += 1;
                // Increment total number of elemental charges accumulated.
                int totalEnergyChargesAcc = totalEnergyChargesAccumulatedMap.get(enemyArmorClass).get(accuracyBonus);
                totalEnergyChargesAcc++;
                totalEnergyChargesAccumulatedMap.get(enemyArmorClass).put(accuracyBonus, totalEnergyChargesAcc);

                if (!isTargetBurning && canUseIgnite && !isIgniteUsed) {
                    isIgniteUsed = true;
                    isTargetBurning = true;
                }
            }
        }
    }

    protected void doIgniteDamage(int accuracyBonus, int enemyArmorClass) {
        int igniteDamage = rollDamage(igniteNumberOfDie.apply(characterLevel), IGNITE_DAMAGE_DIE, 0, 0);
        int totalIgniteDamage = totalIgniteDamageMap.get(enemyArmorClass).get(accuracyBonus);
        totalIgniteDamage += igniteDamage;
        totalIgniteDamageMap.get(enemyArmorClass).put(accuracyBonus, totalIgniteDamage);
        // Accumulate total damage.
        int totalDamage = totalDamageMap.get(enemyArmorClass).get(accuracyBonus);
        totalDamageMap.get(enemyArmorClass).put(accuracyBonus, totalDamage + igniteDamage);
    }

    @Override
    public Map<String, Map<Integer, Map<Integer, ?>>> getStatistics(int numberOfTurns) {
        final Map<Integer, Map<Integer, Double>> averageHiganbanaDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalHiganbanaDamageMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                averageHiganbanaDamagePerTurnMap.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), innerEntry.getValue() / (double) numberOfTurns);
            }
        }

        final Map<Integer, Map<Integer, Double>> percentDamageFromHiganbana = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalHiganbanaDamageMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                percentDamageFromHiganbana.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), 100.0 * innerEntry.getValue() / totalDamageMap.get(entry.getKey()).get(innerEntry.getKey()));
            }
        }

        final Map<Integer, Map<Integer, Double>> averageIgniteDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalIgniteDamageMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                averageIgniteDamagePerTurnMap.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), innerEntry.getValue() / (double) numberOfTurns );
            }
        }

        final Map<Integer, Map<Integer, Double>> percentDamageFromIgnite = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalIgniteDamageMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                percentDamageFromIgnite.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), 100.0 * innerEntry.getValue() / totalDamageMap.get(entry.getKey()).get(innerEntry.getKey()));
            }
        }

        final Map<Integer, Map<Integer, Double>> percentageTurnsIgnited = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalTurnsEnemyIgnitedMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                percentageTurnsIgnited.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), 100.0 * innerEntry.getValue() / numberOfTurns);
            }
        }


        Map stats = super.getStatistics(numberOfTurns);

        stats.put("averageHiganbanaDamagePerTurnMap", averageHiganbanaDamagePerTurnMap);
        stats.put("percentDamageFromHiganbana", percentDamageFromHiganbana);
        stats.put("averageIgniteDamagePerTurnMap", averageIgniteDamagePerTurnMap);
        stats.put("percentDamageFromIgnite", percentDamageFromIgnite);
        stats.put("percentageTurnsIgnited", percentageTurnsIgnited);

        return stats;
    }
}
