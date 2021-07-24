package com.company.combat.rogue;

import com.company.combat.WeaponAttackResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssassinRogue extends RogueCombatClass {

    public AssassinRogue(String characterName,
                         int characterLevel,
                         int numberWeaponDamageDie,
                         int weaponDamageDie,
                         int statBonus,
                         int critDie,
                         int proficiencyBonus) {
        super(characterName, characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
    }

    @Override
    public void doCombatTurn(int enemyArmorClass, int combatNumberSinceLastRest, int combatRound, int remainingCombatRounds) {
        List<WeaponAttackResults> resultsList = new ArrayList<>();
        if(combatRound == 0) {
            // Do assassinate damage.
            resultsList.add(doWeaponAttack(enemyArmorClass, true, false, false));
        } else {
            // Attack normally.
            resultsList.add(doWeaponAttack(enemyArmorClass, false, false, false));
        }

        int totalDamageInTurn = 0;
        for(WeaponAttackResults result : resultsList) {
            totalDamageInTurn += result.getDamage();
            if(result.didHit()) {
                int sneakAttackDamage = doSneakAttack(enemyArmorClass);
                totalDamageInTurn += sneakAttackDamage;
            }
        }

        // Determine if this turn's attack was the largest yet.
        int singleLargestAttackDamage = singleLargestAttackDamageMap.get(enemyArmorClass);
        if(totalDamageInTurn > singleLargestAttackDamage) {
            singleLargestAttackDamageMap.put(enemyArmorClass, totalDamageInTurn);
        }
    }

    @Override
    public void doShortRest() {
    }

    @Override
    public void doLongRest() {
    }

    @Override
    public Map<String, Map<Integer, ?>> getStatistics(int numberOfTurns) {
        return super.getStatistics(numberOfTurns);
    }
}
