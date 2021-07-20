package com.company.combat.ronin;

public class IaiFunctionParameters {
    private final int accuracyBonus;
    private final int armorClass;

    public IaiFunctionParameters(int accuracyBonus, int armorClass) {
        this.accuracyBonus = accuracyBonus;
        this.armorClass = armorClass;
    }

    public int getAccuracyBonus() {
        return accuracyBonus;
    }

    public int getArmorClass() {
        return armorClass;
    }
}
