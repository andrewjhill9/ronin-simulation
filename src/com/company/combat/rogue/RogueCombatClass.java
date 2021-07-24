package com.company.combat.rogue;

import com.company.combat.BaseCombatClass;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class RogueCombatClass extends BaseCombatClass {
  protected static final int SNEAK_ATTACK_DAMAGE_DIE = 6;
  protected final int sneakAttackNumberOfDie;

  // Outputs - Enemy Armor Class to Some result.
  protected final Map<Integer, Integer> totalSneakAttackDamageMap = new LinkedHashMap<>();

  public RogueCombatClass(
      String characterName,
      int characterLevel,
      int numberWeaponDamageDie,
      int weaponDamageDie,
      int statBonus,
      int critDie
  ) {
    super(characterName, characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie);
    if(characterLevel >= 19) {
      this.sneakAttackNumberOfDie = 10;
    } else if(characterLevel >= 17) {
      this.sneakAttackNumberOfDie = 9;
    } else if(characterLevel >= 15) {
      this.sneakAttackNumberOfDie = 8;
    } else if(characterLevel >= 13) {
      this.sneakAttackNumberOfDie = 7;
    } else if(characterLevel >= 11) {
      this.sneakAttackNumberOfDie = 6;
    } else if(characterLevel >= 9) {
      this.sneakAttackNumberOfDie = 5;
    } else if(characterLevel >= 7) {
      this.sneakAttackNumberOfDie = 4;
    } else if(characterLevel >= 5) {
      this.sneakAttackNumberOfDie = 3;
    } else if(characterLevel >= 3) {
      this.sneakAttackNumberOfDie = 2;
    } else {
      this.sneakAttackNumberOfDie = 1;
    }

    // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
    for(int armorClass : ARMOR_CLASSES) {
      totalSneakAttackDamageMap.computeIfAbsent(armorClass, k -> 0);
    }
  }

  @Override
  public void doShortRest() {

  }

  @Override
  public void doLongRest() {

  }

  protected int doSneakAttack(int enemyArmorClass) {
    int sneakAttackDamage = rollDamage(sneakAttackNumberOfDie, SNEAK_ATTACK_DAMAGE_DIE, 0, 0, false);
    // Accumulate total damage.
    int totalDamage = totalDamageMap.get(enemyArmorClass);
    totalDamageMap.put(enemyArmorClass, totalDamage + sneakAttackDamage);
    // Accumulate total damage.
    int totalSneakAttackDamage = totalSneakAttackDamageMap.get(enemyArmorClass);
    totalSneakAttackDamageMap.put(enemyArmorClass, totalSneakAttackDamage + sneakAttackDamage);
    return sneakAttackDamage;
  }

  @Override
  public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
    final Map<Integer, Double> averageSneakAttackDamagePerTurnMap = new LinkedHashMap<>();
    // Average out all damage done over all combat rounds and SIM runs.
    for(Map.Entry<Integer, Integer> entry : totalSneakAttackDamageMap.entrySet()) {
      averageSneakAttackDamagePerTurnMap.put(entry.getKey(), entry.getValue() / (double) numberOfTurns);
    }

    final Map<Integer, Double> percentDamageFromSneakAttack = new LinkedHashMap<>();
    // Average out all attacks done and amount of hits.
    for(Map.Entry<Integer, Integer> entry : totalSneakAttackDamageMap.entrySet()) {
      percentDamageFromSneakAttack.put(entry.getKey(), 100.0 * entry.getValue() / totalDamageMap.get(entry.getKey()));
    }

    Map stats = super.getStatistics(numberOfTurns);

    stats.put("averageSneakAttackDamagePerTurnMap", averageSneakAttackDamagePerTurnMap);
    stats.put("percentDamageFromSneakAttack", percentDamageFromSneakAttack);

    return stats;
  }
}
