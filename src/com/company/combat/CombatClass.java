package com.company.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface CombatClass {
    void doCombatTurn(int accuracyBonus, int enemyArmorClass);
    void doShortRest();
    void doLongRest();
    Map<String,Map<Integer,Map<Integer,?>>> getStatistics(int numberOfTurns);

}
