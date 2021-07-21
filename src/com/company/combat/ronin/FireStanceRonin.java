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

    // Outputs - Enemy Armor Class to Some result.
    protected final Map<Integer, Integer> totalHiganbanaDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalIgniteDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalTurnsEnemyIgnitedMap = new LinkedHashMap<>();

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

    public FireStanceRonin(String characterName,
                           int characterLevel,
                           int numberWeaponDamageDie,
                           int weaponDamageDie,
                           int statBonus,
                           int critDie,
                           int proficiencyBonus,
                           boolean isTargetMindsEye) {
        super(characterName, characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
        this.isTargetMindsEye = isTargetMindsEye;
        this.canUseIgnite = characterLevel >= 2;
        iaiToIaiMinimumCost.put(HIGANBANA_ID, 5);
        iaiToIaiDamageDie.put(HIGANBANA_ID, 12);
        iaiToDiePerCharge.put(HIGANBANA_ID, 1);
        iaiToMinimumLevel.put(HIGANBANA_ID, 3);

        for (int armorClass : ARMOR_CLASSES) {
            totalHiganbanaDamageMap.computeIfAbsent(armorClass, k -> 0);
            totalIgniteDamageMap.computeIfAbsent(armorClass, k -> 0);
            totalTurnsEnemyIgnitedMap.computeIfAbsent(armorClass, k -> 0);
        }

        // Define Higanbana Function
        higanbanaFunction = iaiFunctionParameters -> {
            int armorClass = iaiFunctionParameters.getArmorClass();

            int higanbanaAttackRoll = rollD20(isTargetMindsEye);
            boolean hitSuccess = determineHit(higanbanaAttackRoll, armorClass);

            if (hitSuccess) {
                boolean critHit = isCriticalHit((higanbanaAttackRoll));

                int higanbanaDamage = 0;
                for (int i = 0; i < energyCharges; i += iaiToDiePerCharge.get(HIGANBANA_ID)) {
                    if (critHit) {
                        higanbanaDamage += rollDie(iaiToIaiDamageDie.get(HIGANBANA_ID), false) + rollDie(iaiToIaiDamageDie.get(HIGANBANA_ID), false);
                    } else {
                        higanbanaDamage += rollDie(iaiToIaiDamageDie.get(HIGANBANA_ID), false);
                    }
                }
                // Accumulate total damage.
                int totalDamage = totalDamageMap.get(armorClass);
                totalDamageMap.put(armorClass, totalDamage + higanbanaDamage);
                // Accumulate total higanbana damage.
                int totalHiganbanaDamage = totalHiganbanaDamageMap.get(armorClass);
                totalHiganbanaDamageMap.put(armorClass, totalHiganbanaDamage + higanbanaDamage);

                // Determine if this turn's attack was the largest yet.
                int singleLargestAttackDamage = singleLargestAttackDamageMap.get(armorClass);
                if (higanbanaDamage > singleLargestAttackDamage) {
                    singleLargestAttackDamageMap.put(armorClass, higanbanaDamage);
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
    public void doCombatTurn(int enemyArmorClass) {
        // Simulate Ignite damage.
        if (isTargetBurning) {
            // Increment total number of ignite turns.
            int totalTurnsEnemyIgnited = totalTurnsEnemyIgnitedMap.get(enemyArmorClass);
            totalTurnsEnemyIgnited++;
            totalTurnsEnemyIgnitedMap.put(enemyArmorClass, totalTurnsEnemyIgnited);

            doIgniteDamage(enemyArmorClass);
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
            int totalEnergyCharges = totalEnergyChargesAccumulatedMap.get(enemyArmorClass);
            totalEnergyCharges += chargesPerLimitBreak;
            totalEnergyChargesAccumulatedMap.put(enemyArmorClass, totalEnergyCharges);

            executeIai(enemyArmorClass, HIGANBANA_ID);
            energyCharges = 0;

        } else if (energyCharges >= iaiToIaiMinimumCost.get(HIGANBANA_ID)) {
            // Use Iai to avoid over-capping.
            executeIai(enemyArmorClass, HIGANBANA_ID);
            energyCharges = 0;

        } else {
            // Attack normally.
            WeaponAttackResults weaponAttackResults = doWeaponAttack(enemyArmorClass);
            if (weaponAttackResults.didHit()) {
                energyCharges += 1;
                // Increment total number of elemental charges accumulated.
                int totalEnergyChargesAcc = totalEnergyChargesAccumulatedMap.get(enemyArmorClass);
                totalEnergyChargesAcc++;
                totalEnergyChargesAccumulatedMap.put(enemyArmorClass, totalEnergyChargesAcc);

                if (!isTargetBurning && canUseIgnite && !isIgniteUsed) {
                    isIgniteUsed = true;
                    isTargetBurning = true;
                }
            }
        }
    }

    protected void doIgniteDamage(int enemyArmorClass) {
        int igniteDamage = rollDamage(igniteNumberOfDie.apply(characterLevel), IGNITE_DAMAGE_DIE, 0, 0);
        int totalIgniteDamage = totalIgniteDamageMap.get(enemyArmorClass);
        totalIgniteDamage += igniteDamage;
        totalIgniteDamageMap.put(enemyArmorClass, totalIgniteDamage);
        // Accumulate total damage.
        int totalDamage = totalDamageMap.get(enemyArmorClass);
        totalDamageMap.put(enemyArmorClass, totalDamage + igniteDamage);
    }

    @Override
    public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
        final Map<Integer, Double> averageHiganbanaDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalHiganbanaDamageMap.entrySet()) {
            averageHiganbanaDamagePerTurnMap.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> percentDamageFromHiganbana = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalHiganbanaDamageMap.entrySet()) {
            percentDamageFromHiganbana.put(entry.getKey(),  100.0 * entry.getValue() / totalDamageMap.get(entry.getKey()));
        }

        final Map<Integer, Double> averageIgniteDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalIgniteDamageMap.entrySet()) {
            averageIgniteDamagePerTurnMap.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> percentDamageFromIgnite = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalIgniteDamageMap.entrySet()) {
            percentDamageFromIgnite.put(entry.getKey(),  100.0 * entry.getValue() / totalDamageMap.get(entry.getKey()));
        }

        final Map<Integer, Double> percentageTurnsIgnited = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalTurnsEnemyIgnitedMap.entrySet()) {
            percentageTurnsIgnited.put(entry.getKey(),  100.0 * entry.getValue() / numberOfTurns);
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
