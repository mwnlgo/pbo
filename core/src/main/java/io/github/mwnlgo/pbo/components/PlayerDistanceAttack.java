package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.entities.Projectile;
import io.github.mwnlgo.pbo.enums.Direction;

/**
 * Komponen serangan jarak jauh yang menembakkan proyektil.
 * Logika serangan ini berbasis cooldown, bukan hitbox sementara.
 */
public class PlayerDistanceAttack extends PlayerAttackComponent {
    private static final String TAG = "PlayerDistanceAttack";

    // Variabel untuk mengelola cooldown serangan jarak jauh
    private float cooldown;
    private long lastAttackTime;

    /**
     * Konstruktor untuk serangan jarak jauh pemain.
     * @param player Pemain yang memiliki serangan ini.
     * @param damageAmount Jumlah damage yang akan diberikan oleh proyektil.
     * @param attackDuration Durasi serangan (tidak digunakan untuk proyektil, bisa diisi 0).
     */
    public PlayerDistanceAttack(Player player, float damageAmount, float attackDuration) {
        // (PERUBAHAN) Panggil konstruktor kelas dasar dengan semua parameter yang diperlukan.
        super(player, damageAmount, attackDuration);

        // Atur cooldown spesifik untuk serangan ini
        this.cooldown = 0.5f; // Bisa menyerang setiap 0.5 detik
        this.lastAttackTime = 0L;
    }

    @Override
    public void attack() {
        // Logika cooldown sekarang dikelola di dalam kelas ini.
        if (TimeUtils.millis() - lastAttackTime > cooldown * 1000) {
            Gdx.app.log(TAG, "Distance Attack initiated!");

            // Panggil metode 'attack' dari kelas dasar untuk membersihkan daftar hit
            // dan menangani status dasar jika diperlukan.
            super.attack();

            Vector2 playerPos = player.getPosition();
            Direction playerDir = player.getCurrentDirection();

            // Buat proyektil baru dengan damage yang sesuai
            Projectile playerProjectile = new Projectile(
                playerPos.x,
                playerPos.y,
                playerDir,
                player.getScreen(),
                this.damageAmount // Menggunakan damageAmount dari kelas dasar
            );

            // Tambahkan proyektil ke dunia game
            player.getScreen().spawnPlayerProjectile(playerProjectile);

            // Perbarui waktu serangan terakhir untuk memulai cooldown
            this.lastAttackTime = TimeUtils.millis();
        }
    }

    @Override
    public void update(float delta) {
        // Metode update dari kelas dasar mengelola timer hitbox, yang tidak kita gunakan di sini.
        // Jadi, kita bisa membiarkannya kosong agar tidak ada logika yang berjalan.
    }
}
