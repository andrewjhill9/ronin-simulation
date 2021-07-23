package com.company.combat.ronin;

import com.company.combat.BaseCombatClass;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class RoninCombatClass extends BaseCombatClass {
    protected final int swordArtDc;
    protected final boolean canUseLimitBreak;
    protected boolean usedLimitBreak = false;
    protected int energyCharges = 0;
    protected final int maxEnergyCharges;
    protected final int chargesPerLimitBreak;
    protected final Map<Integer, Integer> iaiToIaiMinimumCost = new LinkedHashMap<>();
    protected final Map<Integer, Integer> iaiToIaiBaseDieNumber = new LinkedHashMap<>();
    protected final Map<Integer, Integer> iaiToIaiDamageDie = new LinkedHashMap<>();
    protected final Map<Integer, Integer> iaiToDiePerCharge = new LinkedHashMap<>();
    protected final Map<Integer, Integer> iaiToMinimumLevel = new LinkedHashMap<>();
    protected final Map<Integer, Function<IaiFunctionParameters, Void>> iaiIdToIaiExecutionFunction = new LinkedHashMap<>();

    // Outputs - Enemy Armor Class to Some result.
    protected final Map<Integer, Integer> totalNumIaiUsesMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalEnergyChargesAccumulatedMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalEnergyChargesLvl3AccumulatedMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalEnergyChargesLvl6AccumulatedMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalEnergyChargesLvl10AccumulatedMap = new LinkedHashMap<>();
    protected final Map<Integer, Integer> totalEnergyChargesLvl15AccumulatedMap = new LinkedHashMap<>();

    public RoninCombatClass(String characterName,
                            int characterLevel,
                            int numberWeaponDamageDie,
                            int weaponDamageDie,
                            int statBonus,
                            int critDie,
                            int proficiencyBonus) {
        super(characterName, characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
        this.canUseLimitBreak = characterLevel >= 5;
        this.maxEnergyCharges = characterLevel * 2;
        this.chargesPerLimitBreak = characterLevel;
        this.swordArtDc = 8 + proficiencyBonus + statBonus;

        for (int armorClass: ARMOR_CLASSES) {
            totalNumIaiUsesMap.computeIfAbsent(armorClass, k -> 0);
            totalEnergyChargesAccumulatedMap.computeIfAbsent(armorClass, k -> 0);
            totalEnergyChargesLvl3AccumulatedMap.computeIfAbsent(armorClass, k -> 0);
            totalEnergyChargesLvl6AccumulatedMap.computeIfAbsent(armorClass, k -> 0);
            totalEnergyChargesLvl10AccumulatedMap.computeIfAbsent(armorClass, k -> 0);
            totalEnergyChargesLvl15AccumulatedMap.computeIfAbsent(armorClass, k -> 0);
        }
    }

    @Override
    public void doShortRest() {
        usedLimitBreak = false;
    }

    @Override
    public void doLongRest() {
        usedLimitBreak = false;
        energyCharges = 0;
    }

    @Override
    public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
        final Map<Integer, Double> averageNumberTurnsPerIai = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Integer> entry : totalNumIaiUsesMap.entrySet()) {
            averageNumberTurnsPerIai.put(entry.getKey(),  (double) numberOfTurns / entry.getValue());
        }

        final Map<Integer, Double> averageNumberElementalChargesPerTurn = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalEnergyChargesAccumulatedMap.entrySet()) {
            averageNumberElementalChargesPerTurn.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> averageNumberElementalChargesLvl3PerTurn = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalEnergyChargesLvl3AccumulatedMap.entrySet()) {
            averageNumberElementalChargesLvl3PerTurn.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> averageNumberElementalChargesLvl6PerTurn = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalEnergyChargesLvl6AccumulatedMap.entrySet()) {
            averageNumberElementalChargesLvl6PerTurn.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> averageNumberElementalChargesLvl10PerTurn = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalEnergyChargesLvl10AccumulatedMap.entrySet()) {
            averageNumberElementalChargesLvl10PerTurn.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        final Map<Integer, Double> averageNumberElementalChargesLvl15PerTurn = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Integer> entry : totalEnergyChargesLvl15AccumulatedMap.entrySet()) {
            averageNumberElementalChargesLvl15PerTurn.put(entry.getKey(),  entry.getValue() / (double) numberOfTurns);
        }

        Map stats = super.getStatistics(numberOfTurns);

        stats.put("totalNumIaiUsesMap", totalNumIaiUsesMap);
        stats.put("totalEnergyChargesAccumulatedMap", totalEnergyChargesAccumulatedMap);
        stats.put("averageNumberTurnsPerIai", averageNumberTurnsPerIai);
        stats.put("averageNumberElementalChargesPerTurn", averageNumberElementalChargesPerTurn);
        stats.put("averageNumberElementalChargesLvl3PerTurn", averageNumberElementalChargesLvl3PerTurn);
        stats.put("averageNumberElementalChargesLvl6PerTurn", averageNumberElementalChargesLvl6PerTurn);
        stats.put("averageNumberElementalChargesLvl10PerTurn", averageNumberElementalChargesLvl10PerTurn);
        stats.put("averageNumberElementalChargesLvl15PerTurn", averageNumberElementalChargesLvl15PerTurn);

        return stats;
    }

    protected void executeIai(int armorClass, int iaiId) {
        // Increment total number of Iai uses.
        int totalIaiUses = totalNumIaiUsesMap.get(armorClass);
        totalNumIaiUsesMap.put(armorClass, totalIaiUses + 1);

        iaiIdToIaiExecutionFunction.get(iaiId).apply(new IaiFunctionParameters(armorClass));
    }
}
