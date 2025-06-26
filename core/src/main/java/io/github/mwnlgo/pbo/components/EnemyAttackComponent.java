package io.github.mwnlgo.pbo.components;

import io.github.mwnlgo.pbo.entities.Enemy;

/**
 * Komponen serangan abstrak yang spesifik untuk MUSUH.
 * Diturunkan dari AttackComponent.
 */
public abstract class EnemyAttackComponent extends AttackComponent {

    protected Enemy enemy;

    public EnemyAttackComponent(Enemy enemy, float damageAmount, float attackDuration) {
        // Panggil konstruktor kelas dasar
        super(damageAmount, attackDuration);
        this.enemy = enemy;
    }

    @Override
    public void attack() {
        // Logika dasar saat serangan dimulai
        if(isActive) return;
        this.isActive = true;
        this.attackTimer = this.attackDuration;
    }
}
