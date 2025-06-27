package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.entities.Projectile;

/**
 * Komponen serangan jarak jauh yang menembakkan proyektil ke segala arah (omnidirectional).
 * Logika serangan ini berbasis cooldown.
 */
public class PlayerDistanceAttack extends PlayerAttackComponent {
    private static final String TAG = "PlayerDistanceAttack";

    private float cooldown;
    private long lastAttackTime;

    /**
     * Konstruktor untuk serangan jarak jauh pemain.
     * @param player Pemain yang memiliki serangan ini.
     * @param damageAmount Jumlah damage yang akan diberikan oleh proyektil.
     * @param attackDuration Durasi serangan (tidak digunakan untuk proyektil, bisa diisi 0).
     */
    public PlayerDistanceAttack(Player player, float damageAmount, float attackDuration) {
        super(player, damageAmount, attackDuration);
        this.cooldown = 0.5f; // Bisa menyerang setiap 0.5 detik
        this.lastAttackTime = 0L;
    }

    @Override
    public void attack() {
        if (TimeUtils.millis() - lastAttackTime > cooldown * 1000) {
            Gdx.app.log(TAG, "Distance Attack initiated!");
            super.attack();

            // (PERUBAHAN) Hitung vektor arah dari pemain ke mouse
            Vector3 mouseInWorld = player.getScreen().getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            Vector2 directionVector = new Vector2(mouseInWorld.x - player.getPosition().x, mouseInWorld.y - player.getPosition().y).nor(); // .nor() untuk normalisasi

            // (PERUBAHAN) Buat proyektil baru dengan Vektor Arah, bukan enum Direction
            Projectile playerProjectile = new Projectile(
                player.getPosition().x,
                player.getPosition().y,
                directionVector, // Gunakan vektor arah yang baru dihitung
                player.getScreen(),
                this.damageAmount
            );

            player.getScreen().spawnPlayerProjectile(playerProjectile);
            this.lastAttackTime = TimeUtils.millis();
        }
    }

    @Override
    public void update(float delta) {
        // Metode ini bisa dibiarkan kosong karena serangan proyektil tidak menggunakan hitbox sementara.
    }
}
