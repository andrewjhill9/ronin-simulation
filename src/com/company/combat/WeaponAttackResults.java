package com.company.combat;

public class WeaponAttackResults {
  private final int damage;
  private final boolean isCrit;
  private final boolean didHit;

  public WeaponAttackResults(
      int damage,
      boolean isCrit,
      boolean didHit
  ) {
    this.damage = damage;
    this.isCrit = isCrit;
    this.didHit = didHit;
  }

  public boolean didHit() {
    return didHit;
  }

  public boolean isCrit() {
    return isCrit;
  }

  public int getDamage() {
    return damage;
  }
}
