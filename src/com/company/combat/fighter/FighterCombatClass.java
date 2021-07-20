package com.company.combat.fighter;

import com.company.combat.BaseCombatClass;
import com.company.combat.WeaponAttackResults;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class FighterCombatClass extends BaseCombatClass {
    protected final boolean isActionSurgeUnlocked;
    protected final int multiAttackNumber;
    protected boolean usedActionSurge = false;

    // Outputs

    public FighterCombatClass(int characterLevel,
                              int numberWeaponDamageDie,
                              int weaponDamageDie,
                              int statBonus,
                              int critDie,
                              int proficiencyBonus) {
        super(characterLevel, numberWeaponDamageDie, weaponDamageDie, statBonus, critDie, proficiencyBonus);
        this.isActionSurgeUnlocked = characterLevel >= 2;
        if(characterLevel >= 20) {
            this.multiAttackNumber = 4;
        } else if(characterLevel >= 11) {
            this.multiAttackNumber = 3;
        } else if(characterLevel >= 5) {
            this.multiAttackNumber = 2;
        } else {
            this.multiAttackNumber = 1;
        }

        // Populate the AC -> ACC Bonus map plus initialize the output maps with zero values to avoid NPEs.
//        for (int armorClass = 10; armorClass <= 25; armorClass++) {
//            for (int accBonus = 5; accBonus <= 9; accBonus++) {
//            }
//        }
    }

    @Override
    public void doShortRest() {
        usedActionSurge = false;
    }

    @Override
    public void doLongRest() {
        usedActionSurge = false;
    }

    @Override
    public Map<String, Map<Integer, Map<Integer, ?>>> getStatistics(int numberOfTurns) {
        return super.getStatistics(numberOfTurns);
    }

    protected List<WeaponAttackResults> doMultiAttack(int accuracyBonus, int enemyArmorClass) {
        List<WeaponAttackResults> results = new ArrayList<>();
        for(int i = 0; i < multiAttackNumber; i++) {
            results.add(doWeaponAttack(accuracyBonus, enemyArmorClass));
        }
        return results;
    }
}
