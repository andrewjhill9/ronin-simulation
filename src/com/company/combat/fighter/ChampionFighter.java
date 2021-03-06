package com.company.combat.fighter;

import com.company.combat.WeaponAttackResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChampionFighter extends FighterCombatClass {

  public ChampionFighter(
      String characterName,
      int characterLevel,
      int numberWeaponDamageDie,
      int weaponDamageDie,
      int statBonus,
      int critDie
  ) {
    super(characterName, characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie);
  }

  @Override
  public void doCombatTurn(
      int enemyArmorClass,
      int combatNumberSinceLastRest,
      int combatRound,
      int remainingCombatRounds
  ) {
    List<WeaponAttackResults> resultsList = new ArrayList<>();
    if(! usedActionSurge && isActionSurgeUnlocked) {
      // Use Limit Break and then Iai
      usedActionSurge = true;
      resultsList.addAll(doMultiAttack(enemyArmorClass, true));
      resultsList.addAll(doMultiAttack(enemyArmorClass, true));
    } else {
      // Attack normally.
      resultsList.addAll(doMultiAttack(enemyArmorClass, true));
    }

    int totalDamageInTurn = 0;
    for(WeaponAttackResults result : resultsList) {
      totalDamageInTurn += result.getDamage();
    }

    // Determine if this turn's attack was the largest yet.
    int singleLargestAttackDamage = singleLargestAttackDamageMap.get(enemyArmorClass);
    if(totalDamageInTurn > singleLargestAttackDamage) {
      singleLargestAttackDamageMap.put(enemyArmorClass, totalDamageInTurn);
    }
  }

  @Override
  public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
    return super.getStatistics(numberOfTurns);
  }
}
