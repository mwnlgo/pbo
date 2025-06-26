package io.github.mwnlgo.pbo.components;

import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.interfaces.IDamageable;
import java.util.HashSet;

/**
 * Komponen serangan yang spesifik untuk PEMAIN.
 * Menambahkan logika untuk mencegah satu serangan mengenai musuh yang sama berkali-kali.
 */
public abstract class PlayerAttackComponent extends AttackComponent {

    protected Player player;

    // Ini adalah logika yang spesifik untuk pemain
    protected HashSet<IDamageable> hitEntitiesThisAttack;

    public PlayerAttackComponent(Player player, float damageAmount, float attackDuration) {
        // Panggil konstruktor kelas dasar
        super(damageAmount, attackDuration);
        this.player = player;
        this.hitEntitiesThisAttack = new HashSet<>();
    }

    // Metode spesifik untuk pemain
    public void addHitEntity(IDamageable entity) {
        hitEntitiesThisAttack.add(entity);
    }

    public boolean hasHit(IDamageable entity) {
        return hitEntitiesThisAttack.contains(entity);
    }

    // Saat serangan baru dimulai, bersihkan daftar entitas yang sudah terkena
    @Override
    public void attack() {
        if(isActive) return; // Mencegah serangan tumpang tindih
        this.hitEntitiesThisAttack.clear();
        this.isActive = true;
        this.attackTimer = this.attackDuration;
    }
}
