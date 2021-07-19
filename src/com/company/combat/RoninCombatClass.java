package com.company.combat;

import com.company.Stats;

import java.util.concurrent.ThreadLocalRandom;

public class RoninCombatClass {


    private static void executeIaiAttack(int armorClass, int accBonus) {
        int d20Roll = ThreadLocalRandom.current().nextInt(1, 21);
        boolean critHit = d20Roll >= Stats.CRIT_DIE;
        boolean hitSuccess = determineHit(d20Roll, accBonus, armorClass);

        if (hitSuccess) {
            double totalDamageValue = TOTAL_AVG_DAMAGE_MAP.get(armorClass).get(accBonus);
            double totalIaiDamage = TOTAL_IAI_DAMAGE_PER_TURN_MAP.get(armorClass).get(accBonus);
            totalIaiDamage += (Stats.AVG_HINGAN_DMG_PER_CHARGE * energyCharges);
            totalDamageValue += (Stats.AVG_HINGAN_DMG_PER_CHARGE * energyCharges);
            if (critHit) {
                totalIaiDamage += (Stats.AVG_HINGAN_DMG_PER_CHARGE * energyCharges);
                totalDamageValue += (Stats.AVG_HINGAN_DMG_PER_CHARGE * energyCharges);
            }
            TOTAL_AVG_DAMAGE_MAP.get(armorClass).put(accBonus, totalDamageValue);
            TOTAL_IAI_DAMAGE_PER_TURN_MAP.get(armorClass).put(accBonus, totalIaiDamage);
        }
    }
}
