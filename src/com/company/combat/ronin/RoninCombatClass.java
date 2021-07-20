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
    protected final Map<Integer,Integer> iaiToIaiMinimumCost = new LinkedHashMap<>();
    protected final Map<Integer,Integer> iaiToIaiDamageDie = new LinkedHashMap<>();
    protected final Map<Integer,Integer> iaiToDiePerCharge = new LinkedHashMap<>();
    protected final Map<Integer,Integer> iaiToMinimumLevel = new LinkedHashMap<>();
    protected final Map<Integer, Function<IaiFunctionParameters,Void>> iaiIdToIaiExecutionFunction = new LinkedHashMap<>();

    // Outputs
    protected final Map<Integer, Map<Integer, Integer>> totalNumIaiUsesMap = new LinkedHashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> totalEnergyChargesAccumulatedMap = new LinkedHashMap<>();

    public RoninCombatClass(int characterLevel,
                            int numberWeaponDamageDie,
                            int weaponDamageDie,
                            int statBonus,
                            int critDie,
                            int proficiencyBonus) {
        super(characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
        this.canUseLimitBreak = characterLevel >= 5;
        this.maxEnergyCharges = characterLevel * 2;
        this.chargesPerLimitBreak = characterLevel;
        this.swordArtDc = 8 + proficiencyBonus + statBonus;

        // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            for (int accBonus = 5; accBonus <= 9; accBonus++) {
                totalNumIaiUsesMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
                totalEnergyChargesAccumulatedMap.computeIfAbsent(armorClass, k -> new LinkedHashMap<>()).computeIfAbsent(accBonus, k -> 0);
            }
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
    public Map<String, Map<Integer, Map<Integer, ?>>> getStatistics(int numberOfTurns) {
        final Map<Integer, Map<Integer, Double>> averageNumberTurnsPerIai = new LinkedHashMap<>();
        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalNumIaiUsesMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                averageNumberTurnsPerIai.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), (double)numberOfTurns / innerEntry.getValue());
            }
        }

        final Map<Integer, Map<Integer, Double>> averageNumberElementalChargesPerTurn = new LinkedHashMap<>();
        // Average out all attacks done and amount of hits.
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : totalEnergyChargesAccumulatedMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                averageNumberElementalChargesPerTurn.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>()).put(innerEntry.getKey(), innerEntry.getValue() / (double)numberOfTurns);
            }
        }


        Map stats = super.getStatistics(numberOfTurns);

        stats.put("totalNumIaiUsesMap", totalNumIaiUsesMap);
        stats.put("totalEnergyChargesAccumulatedMap", totalEnergyChargesAccumulatedMap);
        stats.put("averageNumberTurnsPerIai", averageNumberTurnsPerIai);
        stats.put("averageNumberElementalChargesPerTurn", averageNumberElementalChargesPerTurn);

        return stats;
    }

    protected void executeIai(int armorClass, int accBonus, int iaiId) {
        // Increment total number of Iai uses.
        int totalIaiUses = totalNumIaiUsesMap.get(armorClass).get(accBonus);
        totalNumIaiUsesMap.get(armorClass).put(accBonus, totalIaiUses+1);

        iaiIdToIaiExecutionFunction.get(iaiId).apply(new IaiFunctionParameters(accBonus, armorClass));
    }
}
