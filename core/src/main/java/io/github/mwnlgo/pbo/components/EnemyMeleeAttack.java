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

    private float internalTimer; // Timer internal untuk melacak durasi serangan
    private final float hitboxActivationDelay; // Waktu tunda sebelum hitbox aktif
    private final float hitboxActiveDuration;  // Berapa lama hitbox aktif
    private boolean isHitboxLive = false; // Penanda apakah hitbox sedang aktif untuk memberi damage
    // ------------------------------------


    private Sound attackSound; // (BARU) Variabel untuk suara serangan


    public EnemyMeleeAttack(Enemy enemy, float damageAmount, float attackDuration, String path, float hitboxActivationDelay, float hitboxActiveDuration) {
        super(enemy, damageAmount, attackDuration);
        this.attackSound = Gdx.audio.newSound(Gdx.files.internal(path));
        this.hitboxActivationDelay = hitboxActivationDelay;
        this.hitboxActiveDuration = hitboxActiveDuration;

    }

    @Override
    public void attack() {
        // Panggil metode attack dari induk untuk mengaktifkan status
        super.attack();
        // Jika serangan tidak jadi aktif (misalnya sudah aktif sebelumnya), hentikan.
        if (!isActive) return;
        this.internalTimer = 0f; // Reset timer internal setiap kali serangan baru dimulai
        this.isHitboxLive = false; // Pastikan hitbox tidak aktif di awal

        if (attackSound != null) {
            attackSound.play(0.3f); // Volume 30%
        }

        // Logika untuk menempatkan hitbox di depan musuh
        Rectangle enemyBounds = enemy.getBounds();
        Direction enemyDir = enemy.getCurrentDirection();
        float hitboxX = 0, hitboxY = 0;

//        switch (enemyDir) {
//            case UP:
//                hitboxX = enemyBounds.x + (enemyBounds.width / 2) - (attackWidth / 2);
//                hitboxY = enemyBounds.y + enemyBounds.height;
//                break;
//            case DOWN:
//                hitboxX = enemyBounds.x + (enemyBounds.width / 2) - (attackWidth / 2);
//                hitboxY = enemyBounds.y - attackHeight;
//                break;
//            case LEFT:
//                hitboxX = enemyBounds.x - attackWidth;
//                hitboxY = enemyBounds.y + (enemyBounds.height / 2) - (attackHeight / 2);
//                break;
//            case RIGHT:
//                hitboxX = enemyBounds.x + enemyBounds.width;
//                hitboxY = enemyBounds.y + (enemyBounds.height / 2) - (attackHeight / 2);
//                break;
//        }
//
//        // Atur posisi dan ukuran hitbox serangan
//        this.attackHitbox.set(hitboxX, hitboxY, attackWidth, attackHeight);
    }
    @Override
    public void update(float delta) {
        super.update(delta); // Ini akan mengelola timer utama dan menonaktifkan `isActive` saat selesai

        if (isActive) {
            internalTimer += delta;

            // Cek apakah sudah waktunya untuk mengaktifkan hitbox
            if (internalTimer >= hitboxActivationDelay && internalTimer < (hitboxActivationDelay + hitboxActiveDuration)) {
                if (!isHitboxLive) {
                    // Aktifkan hitbox dan posisikan
                    isHitboxLive = true;
                    positionHitbox();
                }
            } else {
                // Nonaktifkan hitbox jika di luar jendela waktu
                isHitboxLive = false;
            }
        } else {
            isHitboxLive = false; // Pastikan hitbox mati jika serangan tidak aktif
        }
    }

    /**
     * (BARU) Metode untuk memposisikan hitbox.
     * Logika ini dipisahkan dari `attack()` agar bisa dipanggil saat timer tepat.
     */
    private void positionHitbox() {
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
        this.attackHitbox.set(hitboxX, hitboxY, attackWidth, attackHeight);
    }

    /**
     * (BARU) Getter untuk memeriksa apakah hitbox sedang live.
     * @return true jika hitbox aktif dan bisa memberi damage.
     */
    public boolean isHitboxLive() {
        return isHitboxLive;
    }


    public void dispose() {
        if (attackSound != null) {
            attackSound.dispose();
        }
    }
}
