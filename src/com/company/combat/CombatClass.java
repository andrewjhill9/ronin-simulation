package com.company.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface CombatClass {
    String getName();
    void doCombatTurn(int enemyArmorClass);
    void doShortRest();
    void doLongRest();
    Map<String,Map<Integer,?>> getStatistics(int numberOfTurns);

}
