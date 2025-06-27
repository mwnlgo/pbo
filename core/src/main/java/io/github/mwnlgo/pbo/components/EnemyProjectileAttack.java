package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.mwnlgo.pbo.entities.Enemy;
import io.github.mwnlgo.pbo.entities.EnemyProjectile;
import io.github.mwnlgo.pbo.entities.Player;

/**
 * Komponen yang mengelola logika serangan proyektil untuk musuh.
 * Bertanggung jawab atas cooldown dan peluncuran proyektil.
 */
public class EnemyProjectileAttack {

    private Enemy owner;
    private Player target;
    private float damageAmount;
    private float cooldown;
    private long lastAttackTime;

    private Sound castSound; // (BARU) Variabel untuk suara sihir


    public EnemyProjectileAttack(Enemy owner, float damageAmount, float cooldown) {
        this.owner = owner;
        this.target = owner.getTarget(); // Dapatkan target dari pemilik (Enemy)
        this.damageAmount = damageAmount;
        this.cooldown = cooldown;
        this.lastAttackTime = 0;

        this.castSound = Gdx.audio.newSound(Gdx.files.internal("sound/DemonAttack.wav"));

    }

    /**
     * Memeriksa apakah serangan sudah siap digunakan (cooldown selesai).
     * @return true jika siap, false sebaliknya.
     */
    public boolean isReady() {
        return TimeUtils.millis() - lastAttackTime > cooldown * 1000;
    }

    /**
     * Melakukan serangan dengan meluncurkan proyektil jika cooldown sudah selesai.
     */
    public void attack() {
        if (!isReady()) return;

        if (castSound != null) {
            castSound.play();
        }

        // Tentukan posisi spawn proyektil (dari posisi musuh)
        Vector2 spawnPos = new Vector2(owner.getPosition());

        // Tentukan target proyektil (posisi tengah pemain)
        Vector2 targetPos = new Vector2(
            target.getBounds().x + target.getBounds().width / 2f,
            target.getBounds().y + target.getBounds().height / 2f
        );

        // Buat dan luncurkan proyektil
        EnemyProjectile projectile = new EnemyProjectile(
            spawnPos.x, spawnPos.y,
            targetPos,
            damageAmount,
            owner.getScreen(),
            target
        );
        owner.getScreen().addEnemyProjectile(projectile); // Tambahkan proyektil ke game

        // Reset cooldown
        lastAttackTime = TimeUtils.millis();
    }

    // Metode update tidak melakukan apa-apa karena serangan ini instan (tidak ada hitbox timer)
    public void update(float delta) { }

    public void dispose() {
        if (castSound != null) {
            castSound.dispose();
        }
    }
}
