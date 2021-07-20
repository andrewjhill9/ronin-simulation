package com.company;

import com.company.combat.CombatClass;
import com.company.combat.fighter.ChampionFighter;
import com.company.combat.ronin.FireStanceRonin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    // INPUTS
    private static final Map<Integer, List<Integer>> ARMOR_CLASS_TO_ACC_BONUS = new LinkedHashMap<>();
    private static final int NUMBER_SIM_RUNS = (int) Math.pow(10, 4); // 1k
    protected static final int COMBATS_PER_REST = 3;
    protected static final int ROUNDS_PER_COMBAT = 6;
    protected static final int TOTAL_COMBAT_ROUNDS = COMBATS_PER_REST * ROUNDS_PER_COMBAT;

    static {
        // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            for (int accBonus = 5; accBonus <= 9; accBonus++) {
                ARMOR_CLASS_TO_ACC_BONUS.computeIfAbsent(armorClass, k -> new ArrayList<>()).add(accBonus);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        final CombatClass fireRoninLevel3 = new FireStanceRonin(
                3,
                1,
                8,
                3,
                19,
                2,
                true
        );
        final CombatClass fireRoninLevel5 = new FireStanceRonin(
                5,
                1,
                8,
                4,
                19,
                3,
                true
        );
        final CombatClass championFighterLevel3 = new ChampionFighter(
                3,
                2,
                6,
                3,
                19,
                2
        );
        final CombatClass championFighterLevel5 = new ChampionFighter(
                5,
                2,
                6,
                4,
                19,
                3
        );

        for (int i = 0; i < NUMBER_SIM_RUNS; i++) {
            for (Map.Entry<Integer, List<Integer>> armorClassEntry : ARMOR_CLASS_TO_ACC_BONUS.entrySet()) {
                for (Integer accBonus : armorClassEntry.getValue()) {
                    // Begin simulation for a single run given a specific ACC Bonus and enemy Armor Class.
                    for (int j = 0; j < TOTAL_COMBAT_ROUNDS; j++) {
                        fireRoninLevel3.doCombatTurn(accBonus, armorClassEntry.getKey());
                        fireRoninLevel5.doCombatTurn(accBonus, armorClassEntry.getKey());
                        championFighterLevel3.doCombatTurn(accBonus, armorClassEntry.getKey());
                        championFighterLevel5.doCombatTurn(accBonus, armorClassEntry.getKey());
                    }
                    fireRoninLevel3.doLongRest();
                    fireRoninLevel5.doLongRest();
                    championFighterLevel3.doLongRest();
                    championFighterLevel5.doLongRest();
                    // End simulation.
                }
            }

        }

        String folderName = String.valueOf(System.currentTimeMillis());
        printToCsv(folderName, "fireRoninLevel3-", fireRoninLevel3.getStatistics(NUMBER_SIM_RUNS * TOTAL_COMBAT_ROUNDS));
        printToCsv(folderName, "fireRoninLevel5-", fireRoninLevel5.getStatistics(NUMBER_SIM_RUNS * TOTAL_COMBAT_ROUNDS));
        printToCsv(folderName, "championFighterLevel3-", championFighterLevel3.getStatistics(NUMBER_SIM_RUNS * TOTAL_COMBAT_ROUNDS));
        printToCsv(folderName, "championFighterLevel5-", championFighterLevel5.getStatistics(NUMBER_SIM_RUNS * TOTAL_COMBAT_ROUNDS));
    }

    private static void printToCsv(String folderName, String prefix, Map<String, Map<Integer, Map<Integer, ?>>> statistics) throws IOException {
        Desktop dt = Desktop.getDesktop();
        CsvWriter writer = new CsvWriter();
        String suffix = "";

        for (Map.Entry<String, Map<Integer, Map<Integer, ?>>> stat : statistics.entrySet()) {
            String statName = stat.getKey();
            Map stats = stat.getValue();
            List<String[]> csvStats = readyMapForCsv(stats);

            File statsFile = writer.writeToCsvFile(csvStats, new File("c:\\test\\" + folderName + "\\" + prefix + statName + suffix + ".csv"));

        }

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
