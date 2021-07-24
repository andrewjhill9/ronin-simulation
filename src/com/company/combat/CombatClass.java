package com.company.combat;

import java.util.Map;

public interface CombatClass {
  String getName();

  void doCombatTurn(
      int enemyArmorClass,
      int combatNumberSinceLastRest,
      int combatRound,
      int remainingCombatRounds
  );

  void doShortRest();

  void doLongRest();

  Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns);
}
