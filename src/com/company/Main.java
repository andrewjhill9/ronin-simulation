package com.company;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final double NUMBER_SIM_RUNS = Math.pow(10, 5); // 10k
    private static boolean usedLimitBreak = false;
    private static double energyCharges = 0;
    private static int meditationCharges = Stats.MEDITATION_CHARGES;

    // INPUTS
    private static final Map<Integer, List<Integer>> ARMOR_CLASS_TO_ACC_BONUS = new LinkedHashMap<>();

    // Outputs
    private static final Map<Integer, Map<Integer, Double>> TOTAL_TECH_2HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Integer>> TURN_COUNT_TECH_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> AVG_TECH_2HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> TOTAL_TECH_3HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> AVG_TECH_3HIT_ACCURACY_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> TOTAL_EM_PER_TURN_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> AVG_EM_PER_TURN_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> TOTAL_TECH_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> AVG_TECH_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> TOTAL_IAI_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> AVG_IAI_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();

    private static final Map<Integer, Map<Integer, Double>> TOTAL_AVG_DAMAGE_MAP = new LinkedHashMap<>();
    private static final Map<Integer, Map<Integer, Double>> AVG_DAMAGE_PER_TURN_MAP = new LinkedHashMap<>();

    static {
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

    public static void main(String[] args) throws IOException {

        for (int i = 0; i < NUMBER_SIM_RUNS; i++) {
            for (Map.Entry<Integer, List<Integer>> armorClassEntry : ARMOR_CLASS_TO_ACC_BONUS.entrySet()) {
                for (Integer accBonus : armorClassEntry.getValue()) {
                    // Begin simulation for a single run given a specific ACC Bonus and enemy Armor Class.
                    for (int j = 0; j < Stats.TOTAL_COMBAT_ROUNDS; j++) {
                        if (energyCharges == 0 && !usedLimitBreak) {
                            // Use Limit Break and then Iai
                            usedLimitBreak = true;
                            energyCharges = Stats.EM_CHARGES_PER_LIMIT_BREAK;
                            double totalEnergyCharges = TOTAL_EM_PER_TURN_MAP.get(armorClassEntry.getKey()).get(accBonus);
                            totalEnergyCharges += Stats.EM_CHARGES_PER_LIMIT_BREAK;
                            TOTAL_EM_PER_TURN_MAP.get(armorClassEntry.getKey()).put(accBonus, totalEnergyCharges);

                            executeIaiAttack(armorClassEntry.getKey(), accBonus);
                            energyCharges = 0;

                        } else if (energyCharges >= Stats.IAI_EM_COST) {
                            // Use Iai to avoid over-capping
                            executeIaiAttack(armorClassEntry.getKey(), accBonus);
                            energyCharges = 0;

                        } else {
                            // Use Technique Combo
                            executeTechniqueCombo(0, armorClassEntry.getKey(), accBonus);
                        }
                    }
                    usedLimitBreak = false;
                    energyCharges = 0;
                    meditationCharges = Stats.MEDITATION_CHARGES;
                    // End simulation.
                }
            }

        }

        // Average out all damage done over all combat rounds and SIM runs.
        for (Map.Entry<Integer, Map<Integer, Double>> entry : TOTAL_TECH_2HIT_ACCURACY_MAP.entrySet()) {
            for (Map.Entry<Integer, Double> innerEntry : entry.getValue().entrySet()) {
                AVG_TECH_2HIT_ACCURACY_MAP.get(entry.getKey()).put(innerEntry.getKey(), innerEntry.getValue() / TURN_COUNT_TECH_MAP.get(entry.getKey()).get(innerEntry.getKey()));
            }
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : TOTAL_TECH_3HIT_ACCURACY_MAP.entrySet()) {
            for (Map.Entry<Integer, Double> innerEntry : entry.getValue().entrySet()) {
                AVG_TECH_3HIT_ACCURACY_MAP.get(entry.getKey()).put(innerEntry.getKey(), innerEntry.getValue() / TURN_COUNT_TECH_MAP.get(entry.getKey()).get(innerEntry.getKey()));
            }
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : TOTAL_EM_PER_TURN_MAP.entrySet()) {
            for (Map.Entry<Integer, Double> innerEntry : entry.getValue().entrySet()) {
                AVG_EM_PER_TURN_MAP.get(entry.getKey()).put(innerEntry.getKey(), innerEntry.getValue() / (Stats.TOTAL_COMBAT_ROUNDS * NUMBER_SIM_RUNS));
            }
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : TOTAL_TECH_DAMAGE_PER_TURN_MAP.entrySet()) {
            for (Map.Entry<Integer, Double> innerEntry : entry.getValue().entrySet()) {
                AVG_TECH_DAMAGE_PER_TURN_MAP.get(entry.getKey()).put(innerEntry.getKey(), innerEntry.getValue() / (Stats.TOTAL_COMBAT_ROUNDS * NUMBER_SIM_RUNS));
            }
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : TOTAL_IAI_DAMAGE_PER_TURN_MAP.entrySet()) {
            for (Map.Entry<Integer, Double> innerEntry : entry.getValue().entrySet()) {
                AVG_IAI_DAMAGE_PER_TURN_MAP.get(entry.getKey()).put(innerEntry.getKey(), innerEntry.getValue() / (Stats.TOTAL_COMBAT_ROUNDS * NUMBER_SIM_RUNS));
            }
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : TOTAL_AVG_DAMAGE_MAP.entrySet()) {
            for (Map.Entry<Integer, Double> innerEntry : entry.getValue().entrySet()) {
                AVG_DAMAGE_PER_TURN_MAP.get(entry.getKey()).put(innerEntry.getKey(), innerEntry.getValue() / (Stats.TOTAL_COMBAT_ROUNDS * NUMBER_SIM_RUNS));
            }
        }

        printToCsv();
    }

    private static void executeTechniqueCombo(int attackNum, int armorClass, int accBonus) {
        if (attackNum >= 3) {
            return;
        }
        int d20Roll = ThreadLocalRandom.current().nextInt(1, 21);
        int advD20Roll = ThreadLocalRandom.current().nextInt(1, 21);

        if(meditationCharges > 0) {
            meditationCharges--;
            if(advD20Roll > d20Roll) {
                d20Roll = advD20Roll;
            }
        }

        boolean hitSuccess = determineHit(d20Roll, accBonus, armorClass);
        boolean critHit = d20Roll >= Stats.CRIT_DIE;

        if (hitSuccess) {
            double totalTechniqueDamage = TOTAL_TECH_DAMAGE_PER_TURN_MAP.get(armorClass).get(accBonus);
            double totalDamageValue = TOTAL_AVG_DAMAGE_MAP.get(armorClass).get(accBonus);
            if (critHit) {
                totalTechniqueDamage += ((Stats.AVG_WPN_DMG + Stats.AVG_WPN_DMG + Stats.STAT_DMG_BONUS + Stats.STANCE_DMG_BONUS) * Stats.TECH_DMG_MOD);
                totalDamageValue += ((Stats.AVG_WPN_DMG + Stats.AVG_WPN_DMG + Stats.STAT_DMG_BONUS + Stats.STANCE_DMG_BONUS) * Stats.TECH_DMG_MOD);
            } else {
                totalTechniqueDamage += ((Stats.AVG_WPN_DMG + Stats.STAT_DMG_BONUS + Stats.STANCE_DMG_BONUS) * Stats.TECH_DMG_MOD);
                totalDamageValue += ((Stats.AVG_WPN_DMG + Stats.STAT_DMG_BONUS + Stats.STANCE_DMG_BONUS) * Stats.TECH_DMG_MOD);
            }
            TOTAL_TECH_DAMAGE_PER_TURN_MAP.get(armorClass).put(accBonus, totalTechniqueDamage);
            TOTAL_AVG_DAMAGE_MAP.get(armorClass).put(accBonus, totalDamageValue);
            if (attackNum >= 0) {
                if (energyCharges < Stats.MAX_ENERGY_CHARGES) {
                    // Keep track of energy charges over all sim runs.
                    double totalEnergyCharges = TOTAL_EM_PER_TURN_MAP.get(armorClass).get(accBonus);
                    totalEnergyCharges += 1;
                    TOTAL_EM_PER_TURN_MAP.get(armorClass).put(accBonus, totalEnergyCharges);

                    // Update currently held charges.
                    energyCharges++;
                }
            }

            executeTechniqueCombo(attackNum + 1, armorClass, accBonus); // Recursive
        }

        // Do this after due to recursive nature of function.
        double total2TechniqueHits = TOTAL_TECH_2HIT_ACCURACY_MAP.get(armorClass).get(accBonus);
        double total3TechniqueHits = TOTAL_TECH_3HIT_ACCURACY_MAP.get(armorClass).get(accBonus);
        int totalTechniqueUsageCount = TURN_COUNT_TECH_MAP.get(armorClass).get(accBonus);

        if (attackNum == 0) {
            totalTechniqueUsageCount += 1;
        }
        if (hitSuccess) {
            if (attackNum == 1) {
                total2TechniqueHits += 1;
            } else if (attackNum == 2) {
                total3TechniqueHits += 1;
            }
        }
        TOTAL_TECH_2HIT_ACCURACY_MAP.get(armorClass).put(accBonus, total2TechniqueHits);
        TOTAL_TECH_3HIT_ACCURACY_MAP.get(armorClass).put(accBonus, total3TechniqueHits);
        TURN_COUNT_TECH_MAP.get(armorClass).put(accBonus, totalTechniqueUsageCount);
    }

    private static void printToCsv() throws IOException {
        List<String[]> avg2HitTechAccuracy = readyMapForCsv(AVG_TECH_2HIT_ACCURACY_MAP);
        List<String[]> avg3HitTechAccuracy = readyMapForCsv(AVG_TECH_3HIT_ACCURACY_MAP);
        List<String[]> avgEmPerTurn = readyMapForCsv(AVG_EM_PER_TURN_MAP);
        List<String[]> avgTechDamagePerTurn = readyMapForCsv(AVG_TECH_DAMAGE_PER_TURN_MAP);
        List<String[]> avgIaiDamagePerTurn = readyMapForCsv(AVG_IAI_DAMAGE_PER_TURN_MAP);
        List<String[]> avgDamagePerTurn = readyMapForCsv(AVG_DAMAGE_PER_TURN_MAP);

        CsvWriter writer = new CsvWriter();
        String suffix = "";

        String folderName = String.valueOf(ThreadLocalRandom.current().nextInt(1, 9999999));
        File avg2HitTechAccuracyFile = writer.writeToCsvFile(avg2HitTechAccuracy, new File("c:\\test\\" + folderName + "\\avg2HitTechAccuracy" + suffix + ".csv"));
        File avg3HitTechAccuracyFile = writer.writeToCsvFile(avg3HitTechAccuracy, new File("c:\\test\\" + folderName + "\\avg3HitTechAccuracy" + suffix + ".csv"));
        File avgEmPerTurnFile = writer.writeToCsvFile(avgEmPerTurn, new File("c:\\test\\" + folderName + "\\avgEmPerTurn" + suffix + ".csv"));
        File avgTechDamagePerTurnFile = writer.writeToCsvFile(avgTechDamagePerTurn, new File("c:\\test\\" + folderName + "\\avgTechDamagePerTurn" + suffix + ".csv"));
        File avgIaiDamagePerTurnFile = writer.writeToCsvFile(avgIaiDamagePerTurn, new File("c:\\test\\" + folderName + "\\avgIaiDamagePerTurn" + suffix + ".csv"));
        File avgDamagePerTurnFile = writer.writeToCsvFile(avgDamagePerTurn, new File("c:\\test\\" + folderName + "\\avgDamagePerTurn" + suffix + ".csv"));

        Desktop dt = Desktop.getDesktop();
        dt.open(avgDamagePerTurnFile);
    }

    private static List<String[]> readyMapForCsv(Map<?, ?> mapToTabulate) {
        List<String[]> mapToPrint = new ArrayList<>();

        for (Map.Entry entry : mapToTabulate.entrySet()) {
            String[] valuesArray = new String[((Map) entry.getValue()).keySet().size() + 1];
            valuesArray[0] = "";
            int i = 1;
            for (Object columnHeader : ((Map) entry.getValue()).keySet()) {
                valuesArray[i] = String.valueOf(columnHeader);
                i++;
            }
            mapToPrint.add(valuesArray);
            break; // Only need to do this once as only need the column headers once.
        }

        for (Map.Entry entry : mapToTabulate.entrySet()) {
            String[] valuesArray = new String[((Map) entry.getValue()).values().size() + 1];
            valuesArray[0] = String.valueOf(entry.getKey());
            int i = 1;
            for (Object cellValue : ((Map) entry.getValue()).values()) {
                valuesArray[i] = String.valueOf(cellValue);
                i++;
            }
            mapToPrint.add(valuesArray);
        }

        return mapToPrint;
    }

}
