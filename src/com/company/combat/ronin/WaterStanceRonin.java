package com.company.combat.ronin;

import com.company.combat.WeaponAttackResults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class WaterStanceRonin extends RoninCombatClass {
    protected static final Function<Integer, Integer> splashNumberOfDie;
    protected static final int RYURYUMAI_ID = 0;
    protected static final int SPLASH_DAMAGE_DIE = 4;
    protected boolean isReflexiveAttackUsed = false;
    protected final boolean isTargetMindsEye;
    protected final boolean canUseIgnite;
    protected final int numberOfSplashEnemies;
    protected final int numberOfRyuRyuMaiEnemies;

    // Outputs - Enemy Armor Class to Some result.
    protected final Map<Integer, Integer> totalRyuRyuDamageMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalSplashDamageMap = new LinkedHashMap<>();

    private final Function<IaiFunctionParameters, Void> ryuRyuFunction;

    static {
        splashNumberOfDie = (charLevel) -> { // equal to proficiency bonus at that level
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

    public WaterStanceRonin(String characterName,
                            int characterLevel,
                            int numberWeaponDamageDie,
                            int weaponDamageDie,
                            int statBonus,
                            int critDie,
                            int proficiencyBonus,
                            boolean isTargetMindsEye,
                            int numberOfSplashEnemies,
                            int numberOfRyuRyuMaiEnemies) {
        super(characterName, characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
        this.isTargetMindsEye = isTargetMindsEye;
        this.canUseIgnite = characterLevel >= 2;
        this.numberOfSplashEnemies = numberOfSplashEnemies;
        this.numberOfRyuRyuMaiEnemies = numberOfRyuRyuMaiEnemies;
        iaiToIaiMinimumCost.put(RYURYUMAI_ID, 6);
        iaiToIaiDamageDie.put(RYURYUMAI_ID, 4);
        iaiToDiePerCharge.put(RYURYUMAI_ID, 1);
        iaiToMinimumLevel.put(RYURYUMAI_ID, 3);

        for (int armorClass : ARMOR_CLASSES) {
            totalRyuRyuDamageMap.computeIfAbsent(armorClass, k -> 0);
            totalSplashDamageMap.computeIfAbsent(armorClass, k -> 0);
        }

        // Define RyuRyuMai Function
        ryuRyuFunction = iaiFunctionParameters -> {
            int armorClass = iaiFunctionParameters.getArmorClass();

            int totalRyuRyuDamageForTurn = 0;

            for (int i = 0; i < numberOfRyuRyuMaiEnemies; i++) {
                int ryuRyuAttackRoll = rollD20(i == 0); // Advantage only on Mind's Eye target.
                boolean hitSuccess = determineHit(ryuRyuAttackRoll, armorClass);
                if (hitSuccess) {
                    boolean critHit = isCriticalHit((ryuRyuAttackRoll));

                    int ryuRyuDamage = rollDamage(
                            iaiToDiePerCharge.get(RYURYUMAI_ID) * energyCharges,
                            iaiToIaiDamageDie.get(RYURYUMAI_ID),
                            statBonus,
                            0
                    );
                    if (critHit) {
                        ryuRyuDamage += rollDamage(
                                iaiToDiePerCharge.get(RYURYUMAI_ID) * energyCharges,
                                iaiToIaiDamageDie.get(RYURYUMAI_ID),
                                0,
                                0
                        );
                    }
                    // Accumulate total damage.
                    int totalDamage = totalDamageMap.get(armorClass);
                    totalDamageMap.put(armorClass, totalDamage + ryuRyuDamage);
                    // Accumulate total RyuRyuMai damage.
                    int totalRyuRyuDamage = totalRyuRyuDamageMap.get(armorClass);
                    totalRyuRyuDamageMap.put(armorClass, totalRyuRyuDamage + ryuRyuDamage);

                    totalRyuRyuDamageForTurn += ryuRyuDamage;
                }
            }
            // Determine if this turn's attack was the largest yet.
            int singleLargestAttackDamage = singleLargestAttackDamageMap.get(armorClass);
            if (totalRyuRyuDamageForTurn > singleLargestAttackDamage) {
                singleLargestAttackDamageMap.put(armorClass, totalRyuRyuDamageForTurn);
            }

            energyCharges = 0;
            return null;
        };

        // Add all Iai functions to map.
        iaiIdToIaiExecutionFunction.put(RYURYUMAI_ID, ryuRyuFunction);
    }

    @Override
    public void doShortRest() {
        super.doShortRest();
        isReflexiveAttackUsed = false;
    }

    @Override
    public void doLongRest() {
        super.doLongRest();
        isReflexiveAttackUsed = false;
    }

    @Override
    public void doCombatTurn(int enemyArmorClass, int combatNumberSinceLastRest, int combatRound, int remainingCombatRounds) {
        // Simulate Mind's Eye target attacking you.
        if (!isReflexiveAttackUsed) {
            isReflexiveAttackUsed = true;
            WeaponAttackResults results = doWeaponAttack(enemyArmorClass, false, false);
            if(results.didHit()) {
                doSplashDamage(enemyArmorClass);
            }
        }

        // Decide what to do during turn.
        if (((maxEnergyCharges - energyCharges) >= chargesPerLimitBreak) && !usedLimitBreak && canUseLimitBreak) {
            // Use Limit Break and then Iai
            usedLimitBreak = true;
            energyCharges += chargesPerLimitBreak;
            int totalEnergyCharges = totalEnergyChargesAccumulatedMap.get(enemyArmorClass);
            totalEnergyCharges += chargesPerLimitBreak;
            totalEnergyChargesAccumulatedMap.put(enemyArmorClass, totalEnergyCharges);
        }

        if (energyCharges >= iaiToIaiMinimumCost.get(RYURYUMAI_ID)) {
            // Use Iai to avoid over-capping.
            executeIai(enemyArmorClass, RYURYUMAI_ID);
        } else {
            // Attack normally.
            WeaponAttackResults weaponAttackResults = doWeaponAttack(enemyArmorClass, false, false);
            if (weaponAttackResults.didHit()) {
                energyCharges += 1;
                // Increment total number of elemental charges accumulated.
                int totalEnergyChargesAcc = totalEnergyChargesAccumulatedMap.get(enemyArmorClass);
                totalEnergyChargesAcc++;
                totalEnergyChargesAccumulatedMap.put(enemyArmorClass, totalEnergyChargesAcc);

                for (int i = 0; i < numberOfSplashEnemies; i++) {
                    doSplashDamage(enemyArmorClass);
                }
            }
        }
    }

    protected void doSplashDamage(int enemyArmorClass) {
        int splashDamage = rollDamage(splashNumberOfDie.apply(characterLevel), SPLASH_DAMAGE_DIE, 0, 0);
        boolean savingThrow = (rollD20(false) + 3) >= swordArtDc;
        if (savingThrow) {
            splashDamage = splashDamage / 2;
        }
        int totalSplashDamage = totalSplashDamageMap.get(enemyArmorClass);
        totalSplashDamage += splashDamage;
        totalSplashDamageMap.put(enemyArmorClass, totalSplashDamage);
        // Accumulate total damage.
        int totalDamage = totalDamageMap.get(enemyArmorClass);
        totalDamageMap.put(enemyArmorClass, totalDamage + splashDamage);
    }

    @Override
    public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
        final Map<Integer, Double> averageRyuRyuMaiDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalRyuRyuDamageMap.entrySet()) {
            averageRyuRyuMaiDamagePerTurnMap.put(entry.getKey(), entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> percentDamageFromRyuRyuMai = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalRyuRyuDamageMap.entrySet()) {
            percentDamageFromRyuRyuMai.put(entry.getKey(), 100.0 * entry.getValue() / totalDamageMap.get(entry.getKey()));
        }

        final Map<Integer, Double> averageSplashDamagePerTurnMap = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalSplashDamageMap.entrySet()) {
            averageSplashDamagePerTurnMap.put(entry.getKey(), entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> percentDamageFromSplash = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalSplashDamageMap.entrySet()) {
            percentDamageFromSplash.put(entry.getKey(), 100.0 * entry.getValue() / totalDamageMap.get(entry.getKey()));
        }


        Map stats = super.getStatistics(numberOfTurns);

        stats.put("averageRyuRyuMaiDamagePerTurnMap", averageRyuRyuMaiDamagePerTurnMap);
        stats.put("percentDamageFromRyuRyuMai", percentDamageFromRyuRyuMai);
        stats.put("averageSplashDamagePerTurnMap", averageSplashDamagePerTurnMap);
        stats.put("percentDamageFromSplash", percentDamageFromSplash);

        return stats;
    }
}
