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
    protected final boolean canGainEnergyFromFear;
    protected final boolean canGainEnergyFromCrit;
    protected final boolean canGainEnergyFromDamagingFeared;
    protected boolean isIgniteUsed = false;
    protected boolean isTargetBurning = false;
    protected boolean hasEnemyBeenFeared = false;
    protected boolean isEnemyFeared = false;

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
        this.canGainEnergyFromFear = characterLevel >= 6;
        this.canGainEnergyFromDamagingFeared = characterLevel >= 10;
        this.canGainEnergyFromCrit = characterLevel >= 15;
        iaiToIaiMinimumCost.put(HIGANBANA_ID, 4);
        iaiToIaiBaseDieNumber.put(HIGANBANA_ID, 4);
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
                // Number of damage die is equal to the base # of dice
                // plus how many dice are awarded for each charge spent over the minimum cost.
                int numDamageDie = iaiToIaiBaseDieNumber.get(HIGANBANA_ID)
                        + (iaiToDiePerCharge.get(HIGANBANA_ID) * (energyCharges - iaiToIaiMinimumCost.get(HIGANBANA_ID)));

                int higanbanaDamage = rollDamage(
                        numDamageDie,
                        iaiToIaiDamageDie.get(HIGANBANA_ID),
                        statBonus,
                        0
                );
                if (critHit) {
                    higanbanaDamage += rollDamage(
                            numDamageDie,
                            iaiToIaiDamageDie.get(HIGANBANA_ID),
                            0,
                            0
                    );
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

            energyCharges = 0;
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
        hasEnemyBeenFeared = false;
        isEnemyFeared = false;
    }

    @Override
    public void doLongRest() {
        super.doLongRest();
        isIgniteUsed = false;
        isTargetBurning = false;
        hasEnemyBeenFeared = false;
        isEnemyFeared = false;
    }

    @Override
    public void doCombatTurn(int enemyArmorClass, int combatNumberSinceLastRest, int combatRound, int remainingCombatRounds) {
        if(combatRound == 0) {
            isEnemyFeared = false;
            hasEnemyBeenFeared = false;
            isTargetBurning = false;
        }

        // Enemy gets to try to break fear.
        if(isEnemyFeared) {
            int wisdomSavingThrow = rollD20(false) + 3;
            if (wisdomSavingThrow >= swordArtDc) {
                isEnemyFeared = false;
            }
        }

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
        if (((maxEnergyCharges - energyCharges) >= chargesPerLimitBreak) && !usedLimitBreak && canUseLimitBreak) {
            // Use Limit Break.
            usedLimitBreak = true;
            energyCharges += chargesPerLimitBreak;
            int totalEnergyCharges = totalEnergyChargesAccumulatedMap.get(enemyArmorClass);
            totalEnergyCharges += chargesPerLimitBreak;
            totalEnergyChargesAccumulatedMap.put(enemyArmorClass, totalEnergyCharges);
        }

        // TODO Put more thorough logic in here about using charges appropriately.

//        if (energyCharges == maxEnergyCharges ||
//                (energyCharges >= iaiToIaiMinimumCost.get(HIGANBANA_ID) && remainingCombatRounds == 0)
        if(energyCharges >= iaiToIaiMinimumCost.get(HIGANBANA_ID)) {
            // Use Iai to avoid over-capping.
            executeIai(enemyArmorClass, HIGANBANA_ID);
        } else {

            // Attack normally.
            WeaponAttackResults weaponAttackResults = doWeaponAttack(enemyArmorClass, false, false);
            if (weaponAttackResults.didHit()) {
                if(isEnemyFeared && canGainEnergyFromDamagingFeared) {
                    energyCharges+=1;
                    // Increment total number of elemental charges accumulated.
                    totalEnergyChargesAccumulatedMap.put(enemyArmorClass,
                            totalEnergyChargesAccumulatedMap.get(enemyArmorClass) + 1);
                    // Increment total number of elemental charges accumulated from Lvl 10 feature.
                    totalEnergyChargesLvl10AccumulatedMap.put(enemyArmorClass,
                            totalEnergyChargesLvl10AccumulatedMap.get(enemyArmorClass) + 1);
                }


                energyCharges += 1;
                // Increment total number of elemental charges accumulated from Lvl 3 feature.
                totalEnergyChargesLvl3AccumulatedMap.put(enemyArmorClass,
                        totalEnergyChargesLvl3AccumulatedMap.get(enemyArmorClass) + 1);
                // Increment total number of elemental charges accumulated.
                totalEnergyChargesAccumulatedMap.put(enemyArmorClass,
                        totalEnergyChargesAccumulatedMap.get(enemyArmorClass) + 1);


                if(weaponAttackResults.isCrit()) {
                    energyCharges += 1;
                    // Increment total number of elemental charges accumulated from Lvl 15 feature.
                    totalEnergyChargesLvl15AccumulatedMap.put(enemyArmorClass,
                            totalEnergyChargesLvl15AccumulatedMap.get(enemyArmorClass) + 1);
                    // Increment total number of elemental charges accumulated.
                    totalEnergyChargesAccumulatedMap.put(enemyArmorClass,
                            totalEnergyChargesAccumulatedMap.get(enemyArmorClass) + 1);
                }


                if (!isTargetBurning && canUseIgnite && !isIgniteUsed) {
                    isIgniteUsed = true;
                    isTargetBurning = true;
                }


                if(!hasEnemyBeenFeared) {
                    int wisdomSavingThrow = rollD20(false) + 3;
                    if (wisdomSavingThrow < swordArtDc) {
                        hasEnemyBeenFeared = true;
                        isEnemyFeared = true;

                        if(canGainEnergyFromFear) {
                            energyCharges+=1;
                            // Increment total number of elemental charges accumulated.
                            totalEnergyChargesAccumulatedMap.put(enemyArmorClass,
                                    totalEnergyChargesAccumulatedMap.get(enemyArmorClass) + 1);
                            // Increment total number of elemental charges accumulated from Lvl 6 feature.
                            totalEnergyChargesLvl6AccumulatedMap.put(enemyArmorClass,
                                    totalEnergyChargesLvl6AccumulatedMap.get(enemyArmorClass) + 1);
                        }
                    }
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
