package com.company;

import com.company.combat.CombatClass;
import com.company.combat.fighter.ChampionFighter;
import com.company.combat.rogue.AssassinRogue;
import com.company.combat.ronin.FireStanceRonin;
import com.company.combat.ronin.WaterStanceRonin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {
    // INPUTS
    protected static final List<Integer> ARMOR_CLASSES = new ArrayList<>();
    private static final int NUMBER_SIM_RUNS = (int) Math.pow(10, 4); // 1k
    protected static final int COMBATS_PER_REST = 3;
    protected static final int ROUNDS_PER_COMBAT = 6;
    protected static final int TOTAL_COMBAT_ROUNDS = COMBATS_PER_REST * ROUNDS_PER_COMBAT;

    static {
        for (int armorClass = 10; armorClass <= 25; armorClass++) {
            ARMOR_CLASSES.add(armorClass);
        }
    }

    public static void main(String[] args) throws IOException {
        List<CombatClass> classes = new ArrayList<>();

        final CombatClass fireRoninLevel3 = new FireStanceRonin(
                "Fire Ronin LVL 03",
                3,
                1,
                8,
                3,
                19,
                2,
                true
        );
        final CombatClass fireRoninLevel4 = new FireStanceRonin(
                "Fire Ronin LVL 04",
                4,
                1,
                8,
                3,
                19,
                2,
                true
        );
        final CombatClass fireRoninLevel5 = new FireStanceRonin(
                "Fire Ronin LVL 05",
                5,
                1,
                8,
                4,
                19,
                3,
                true
        );
        final CombatClass fireRoninLevel6 = new FireStanceRonin(
                "Fire Ronin LVL 06",
                6,
                1,
                8,
                4,
                19,
                3,
                true
        );
        final CombatClass fireRoninLevel7 = new FireStanceRonin(
                "Fire Ronin LVL 07",
                7,
                1,
                8,
                4,
                19,
                3,
                true
        );
        final CombatClass fireRoninLevel8 = new FireStanceRonin(
                "Fire Ronin LVL 08",
                8,
                1,
                8,
                5,
                19,
                3,
                true
        );
        final CombatClass fireRoninLevel9 = new FireStanceRonin(
                "Fire Ronin LVL 09",
                9,
                1,
                8,
                5,
                19,
                4,
                true
        );
        final CombatClass fireRoninLevel10 = new FireStanceRonin(
                "Fire Ronin LVL 10",
                10,
                1,
                8,
                5,
                19,
                4,
                true
        );
        final CombatClass fireRoninLevel11 = new FireStanceRonin(
                "Fire Ronin LVL 11",
                11,
                1,
                8,
                5,
                18,
                4,
                true
        );
        final CombatClass waterRoninLevel3 = new WaterStanceRonin(
                "Water Ronin LVL 3",
                3,
                1,
                8,
                3,
                19,
                2,
                true,
                2,
                3
        );
        final CombatClass waterRoninLevel5 = new WaterStanceRonin(
                "Water Ronin LVL 5",
                5,
                1,
                8,
                4,
                19,
                3,
                true,
                2,
                3
        );
        final CombatClass championFighterLevel3 = new ChampionFighter(
                "Champion Fighter LVL 3",
                3,
                2,
                6,
                3,
                19,
                2
        );
        final CombatClass championFighterLevel5 = new ChampionFighter(
                "Champion Fighter LVL 5",
                5,
                2,
                6,
                4,
                19,
                3
        );
        final CombatClass assassinRogueLevel3 = new AssassinRogue(
                "Assassin Rogue LVL 3",
                3,
                2,
                6,
                3,
                20,
                2
        );
        final CombatClass assassinRogueLevel5 = new AssassinRogue(
                "Assassin Rogue LVL 5",
                5,
                2,
                6,
                4,
                20,
                3
        );

        classes.add(fireRoninLevel3);
        classes.add(fireRoninLevel5);
        classes.add(championFighterLevel3);
        classes.add(championFighterLevel5);
        classes.add(assassinRogueLevel3);
        classes.add(assassinRogueLevel5);
        classes.add(fireRoninLevel4);
        classes.add(fireRoninLevel6);
        classes.add(fireRoninLevel7);
        classes.add(fireRoninLevel8);
        classes.add(fireRoninLevel9);
        classes.add(fireRoninLevel10);
        classes.add(fireRoninLevel11);
        classes.add(waterRoninLevel3);
        classes.add(waterRoninLevel5);

        for (int i = 0; i < NUMBER_SIM_RUNS; i++) {
            for (int armorClass : ARMOR_CLASSES) {
                // Begin simulation for a single run given a specific enemy Armor Class.
                for (int combatNumber = 0; combatNumber < COMBATS_PER_REST; combatNumber++) {
                    for (int combatRound = 0; combatRound < ROUNDS_PER_COMBAT; combatRound++) {
                        final int finalCombatNumber = combatNumber;
                        final int finalCombatRound = combatRound;
                        classes.forEach(combatClass -> {
                            combatClass.doCombatTurn(
                                    armorClass,
                                    finalCombatNumber,
                                    finalCombatRound,
                                    (COMBATS_PER_REST * ROUNDS_PER_COMBAT) - (finalCombatNumber * finalCombatRound)
                            );
                        });
                    }
                }
                classes.forEach(CombatClass::doLongRest);
                // End simulation.
            }
        }

        // Swap the keys for the maps around so we print files for each stat that has a CSV of CombatClasses to ArmorClasses
        Map<String, Map<String, Map<Integer, ?>>> characterStatistics = new LinkedHashMap<>();
        classes.forEach(combatClass -> {
            for (Map.Entry<String, Map<Integer, ?>> entry : combatClass.getStatistics(NUMBER_SIM_RUNS * TOTAL_COMBAT_ROUNDS).entrySet()) {
                Map<String, Map<Integer, ?>> innerMap = characterStatistics.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>());
                innerMap.put(combatClass.getName(), entry.getValue());
            }
        });

        // Swap the keys for the maps around so we print files for each stat that has a CSV of CombatClasses to ArmorClasses
        Map<String, Map<String, Map<Integer, ?>>> fireCharacterStatistics = new LinkedHashMap<>();
        List<CombatClass> sortedFire = new ArrayList<>(classes);
        sortedFire.sort(Comparator.comparing(CombatClass::getName));
        sortedFire.forEach(combatClass -> {
            if (!combatClass.getName().contains("Fire")) {
                return;
            }
            for (Map.Entry<String, Map<Integer, ?>> entry : combatClass.getStatistics(NUMBER_SIM_RUNS * TOTAL_COMBAT_ROUNDS).entrySet()) {
                Map<String, Map<Integer, ?>> innerMap = fireCharacterStatistics.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>());
                innerMap.put(combatClass.getName(), entry.getValue());
            }
        });

        String folderName = String.valueOf(System.currentTimeMillis());
        printToCsv(folderName, "", characterStatistics);
        printToCsv(folderName, "fire-", fireCharacterStatistics);

    }

    private static void printToCsv(String folderName, String prefix, Map<String, Map<String, Map<Integer, ?>>> statistics) throws IOException {
        Desktop dt = Desktop.getDesktop();
        CsvWriter writer = new CsvWriter();
        String suffix = "";

        for (Map.Entry<String, Map<String, Map<Integer, ?>>> stat : statistics.entrySet()) {
            String statName = stat.getKey();
            Map stats = stat.getValue();
            List<String[]> csvStats = readyMapForCsv(stats);

            File statsFile = writer.writeToCsvFile(csvStats, new File("c:\\test\\" + folderName + "\\" + prefix + statName + suffix + ".csv"));
        }
    }

    private static List<String[]> readyMapForCsv(Map<?, ?> mapToTabulate) {
        // Each String array is a row from left to right.
        List<String[]> mapToPrint = new ArrayList<>();

        Map<Integer, String[]> rowIdToRowArray = new LinkedHashMap<>();

        // Also header count.
        int totalNumberOfClasses = mapToTabulate.keySet().size();

        // Create Column headers.
        for (Map.Entry entry : mapToTabulate.entrySet()) {
            String[] valuesArray = new String[totalNumberOfClasses + 1];
            valuesArray[0] = "";
            int i = 1;
            for (Object columnHeader : (mapToTabulate).keySet()) {
                valuesArray[i] = String.valueOf(columnHeader);
                i++;
            }
            mapToPrint.add(valuesArray);
            break; // Only need to do this once as only need the column headers once.
        }

        for (int armorClass : ARMOR_CLASSES) {
            String[] row = rowIdToRowArray.computeIfAbsent(armorClass, k -> new String[totalNumberOfClasses + 1]);
            row[0] = String.valueOf(armorClass);
        }

        // Populate Row Arrays.
        List<Map<Integer, ?>> statsList = new ArrayList(mapToTabulate.values());
        for (int i = 0; i < totalNumberOfClasses; i++) {
            Map<Integer, ?> armorClassToStat = statsList.get(i);

            // This for-loop works b/c the map is a LinkedHashMap.
            for (Map.Entry<Integer, ?> armorClassEntry : armorClassToStat.entrySet()) {
                int armorClass = armorClassEntry.getKey();
                Object statValue = armorClassEntry.getValue();
                String[] row = rowIdToRowArray.computeIfAbsent(armorClass, k -> new String[totalNumberOfClasses + 1]);
                row[i + 1] = String.valueOf(statValue);
            }
//
//            String[] rows = new String[totalNumberOfClasses + 1]; // Size of row is equal to number of columns.
//            rows[0] = String.valueOf(entry.getKey()); // Row Header
//            int i = 1;
//            for (Object cellValue : armorClassToStat.values()) {
//                rows[i] = String.valueOf(cellValue);
//                i++;
//            }
//            mapToPrint.add(rows);
        }

        mapToPrint.addAll(rowIdToRowArray.values());

        return mapToPrint;
    }

}
