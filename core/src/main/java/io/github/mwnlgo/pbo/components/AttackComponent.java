package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.math.Rectangle;

/**
 * KELAS DASAR ABSTRAK untuk semua komponen serangan di dalam game.
 * Mengandung logika umum untuk hitbox serangan sementara.
 */
public abstract class AttackComponent {

    protected float damageAmount;
    protected Rectangle attackHitbox;

    protected boolean isActive;
    protected float attackDuration;
    protected float attackTimer;

    public AttackComponent(float damageAmount, float attackDuration) {
        this.damageAmount = damageAmount;
        this.attackDuration = attackDuration;
        this.attackHitbox = new Rectangle();
        this.isActive = false;
        this.attackTimer = 0f;
    }

    /**
     * Memulai serangan. Subclass harus mendefinisikan bagaimana dan di mana hitbox dibuat.
     */
    public abstract void attack();

    /**
     * Logika umum untuk memperbarui timer. Dipanggil setiap frame.
     * @param delta Waktu sejak frame terakhir.
     */
    public void update(float delta) {
        if (isActive) {
            attackTimer -= delta;
            if (attackTimer <= 0) {
                isActive = false;
            }
        }
    }

    // --- Getters ---
    public boolean isActive() {
        return isActive;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }
}
