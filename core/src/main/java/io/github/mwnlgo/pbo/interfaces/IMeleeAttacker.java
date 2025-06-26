package io.github.mwnlgo.pbo.interfaces;

import io.github.mwnlgo.pbo.components.EnemyMeleeAttack;

/**
 * Interface untuk menandai entitas yang bisa melakukan serangan melee.
 * Memastikan bahwa entitas tersebut memiliki metode untuk mendapatkan komponen serangannya.
 */
public interface IMeleeAttacker {
    /**
     * Mengembalikan komponen serangan melee yang dimiliki oleh entitas ini.
     * @return Komponen EnemyMeleeAttack.
     */
    EnemyMeleeAttack getMeleeAttack();
}
