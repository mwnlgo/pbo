package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.enums.Direction;

/**
 * Implementasi serangan jarak dekat (melee) untuk pemain.
 * Hitbox serangan ini akan muncul secara dinamis di depan pemain berdasarkan arah hadap.
 */
public class PlayerMeleeAttack extends PlayerAttackComponent {

    private static final String TAG = "PlayerMeleeAttack";

    // Ukuran hitbox serangan
    private final float MELEE_ATTACK_WIDTH = 100f; // Lebar ayunan pedang
    private final float MELEE_ATTACK_HEIGHT = 100f; // Tinggi ayunan pedang

    private Sound slashSound;
    /**
     * Konstruktor untuk serangan melee.
     * @param player Pemain yang memiliki serangan.
     * @param damageAmount Jumlah damage yang dihasilkan.
     * @param attackDuration Durasi aktifnya hitbox serangan ini.
     */
    public PlayerMeleeAttack(Player player, float damageAmount, float attackDuration) {
        // (PERUBAHAN) Panggil konstruktor kelas dasar dengan semua parameter yang diperlukan.
        super(player, damageAmount, attackDuration);
        this.slashSound = Gdx.audio.newSound(Gdx.files.internal("sound/PlayerAttack.wav"));
    }

    @Override
    public void attack() {
        // Panggil metode 'attack' dari kelas dasar untuk membersihkan daftar hit
        // dan mengaktifkan status serangan.
        super.attack();
        if(!isActive) return; // Jika super.attack() mencegah serangan, hentikan.

        if (slashSound != null) {
            slashSound.play(0.3f); // Angka 0.8f adalah volume (opsional)
        }

        Gdx.app.log(TAG, "Melee attack performed towards " + player.getCurrentDirection());

        // --- Logika Hitbox Dinamis Berdasarkan Arah ---
        Direction dir = player.getCurrentDirection();
        Rectangle playerBounds = player.getBounds(); // Gunakan bounds pemain sebagai acuan
        float hitboxX = 0, hitboxY = 0;

        // Hitung posisi hitbox berdasarkan arah pemain
        switch (dir) {
            case UP:
                hitboxX = playerBounds.x + (playerBounds.width / 2) - (MELEE_ATTACK_WIDTH / 2);
                hitboxY = playerBounds.y + playerBounds.height; // Tepat di atas pemain
                break;
            case DOWN:
                hitboxX = playerBounds.x + (playerBounds.width / 2) - (MELEE_ATTACK_WIDTH / 2);
                hitboxY = playerBounds.y - MELEE_ATTACK_HEIGHT; // Tepat di bawah pemain
                break;
            case LEFT:
                hitboxX = playerBounds.x - MELEE_ATTACK_WIDTH; // Tepat di kiri pemain
                hitboxY = playerBounds.y + (playerBounds.height / 2) - (MELEE_ATTACK_HEIGHT / 2);
                break;
            case RIGHT:
                hitboxX = playerBounds.x + playerBounds.width; // Tepat di kanan pemain
                hitboxY = playerBounds.y + (playerBounds.height / 2) - (MELEE_ATTACK_HEIGHT / 2);
                break;
        }

        // Atur posisi dan ukuran hitbox serangan
        this.attackHitbox.set(hitboxX, hitboxY, MELEE_ATTACK_WIDTH, MELEE_ATTACK_HEIGHT);
    }

    @Override
    public void update(float delta) {
        // Panggil update dari kelas dasar untuk mengelola timer dan status 'isActive'
        super.update(delta);
    }
}
