package com.company.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface CombatClass {

    int getCumulativeDamage();
    int getCumulativeNumberOfRoundsAttacking();
    int doCombatTurn();
}
