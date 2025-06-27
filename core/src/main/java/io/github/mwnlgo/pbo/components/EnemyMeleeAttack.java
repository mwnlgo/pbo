package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import io.github.mwnlgo.pbo.entities.Enemy;
import io.github.mwnlgo.pbo.enums.Direction;

/**
 * Implementasi konkret dari serangan melee untuk musuh.
 * Kelas ini bertanggung jawab untuk membuat dan memposisikan hitbox serangan.
 */
public class EnemyMeleeAttack extends EnemyAttackComponent {

    // Ukuran hitbox bisa disesuaikan per serangan
    private final float attackWidth = 60f;
    private final float attackHeight = 60f;

    private Sound attackSound; // (BARU) Variabel untuk suara serangan


    public EnemyMeleeAttack(Enemy enemy, float damageAmount, float attackDuration, String path) {
        super(enemy, damageAmount, attackDuration);
        this.attackSound = Gdx.audio.newSound(Gdx.files.internal(path));
    }

    @Override
    public void attack() {
        // Panggil metode attack dari induk untuk mengaktifkan status
        super.attack();
        // Jika serangan tidak jadi aktif (misalnya sudah aktif sebelumnya), hentikan.
        if (!isActive) return;

        if (attackSound != null) {
            attackSound.play(0.3f); // Volume 30%
        }

        // Logika untuk menempatkan hitbox di depan musuh
        Rectangle enemyBounds = enemy.getBounds();
        Direction enemyDir = enemy.getCurrentDirection();
        float hitboxX = 0, hitboxY = 0;

        switch (enemyDir) {
            case UP:
                hitboxX = enemyBounds.x + (enemyBounds.width / 2) - (attackWidth / 2);
                hitboxY = enemyBounds.y + enemyBounds.height;
                break;
            case DOWN:
                hitboxX = enemyBounds.x + (enemyBounds.width / 2) - (attackWidth / 2);
                hitboxY = enemyBounds.y - attackHeight;
                break;
            case LEFT:
                hitboxX = enemyBounds.x - attackWidth;
                hitboxY = enemyBounds.y + (enemyBounds.height / 2) - (attackHeight / 2);
                break;
            case RIGHT:
                hitboxX = enemyBounds.x + enemyBounds.width;
                hitboxY = enemyBounds.y + (enemyBounds.height / 2) - (attackHeight / 2);
                break;
        }

        // Atur posisi dan ukuran hitbox serangan
        this.attackHitbox.set(hitboxX, hitboxY, attackWidth, attackHeight);
    }

    public void dispose() {
        if (attackSound != null) {
            attackSound.dispose();
        }
    }
}
