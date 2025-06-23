package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.entities.Projectile; // Pastikan ini mengacu pada Projectile pemain
import io.github.mwnlgo.pbo.enums.Direction;

public class PlayerDistanceAttack extends PlayerAttackComponent {
    private static final String TAG = "PlayerDistanceAttack";

    public PlayerDistanceAttack(Player player) {
        super(player);
        // Cooldown serangan jarak jauh pemain, bisa disesuaikan.
        // Cooldown 0.5f berarti bisa menyerang setiap 0.5 detik.
        setCooldown(0.5f);
    }

    @Override
    public void attack() {
        // Periksa apakah cooldown sudah berakhir sebelum mengizinkan serangan baru.
        // TimeUtils.millis() memberikan waktu saat ini dalam milidetik.
        // getCooldown() * 1000 mengubah cooldown dari detik ke milidetik.
        if (TimeUtils.millis() - getLastAttackTime() > getCooldown() * 1000) {
            Gdx.app.log(TAG, "Distance Attack initiated!");

            // Dapatkan posisi pemain dari objek Player yang terhubung.
            Vector2 playerPos = getPlayer().getPosition();
            // Dapatkan arah hadap pemain untuk menentukan arah proyektil.
            // PENTING: Pastikan kelas Player memiliki metode public Direction getCurrentDirection()
            Direction playerDir = getPlayer().getCurrentDirection();

            // Buat objek proyektil baru.
            // Proyektil memerlukan posisi awal (posisi pemain), arah tembakan, dan referensi ke GameScreen
            // agar bisa berinteraksi dengan dunia game (misalnya, menambahkan dirinya ke daftar proyektil).
            Projectile playerProjectile = new Projectile(
                playerPos.x,
                playerPos.y,
                playerDir,
                getPlayer().getScreen() // Mengakses GameScreen melalui Player
            );

            // Tambahkan proyektil yang baru dibuat ke daftar proyektil pemain di GameScreen.
            // PENTING: Pastikan GameScreen memiliki metode public void spawnPlayerProjectile(Projectile projectile)
            getPlayer().getScreen().spawnPlayerProjectile(playerProjectile);

            // Setel waktu serangan terakhir ke waktu saat ini untuk memulai cooldown.
            setLastAttackTime(TimeUtils.millis());
        } else {
            // Opsional: Logging untuk debug ketika serangan diblokir oleh cooldown.
            // Gdx.app.log(TAG, "Distance Attack on cooldown. Remaining: " +
            //         (getCooldown() - (TimeUtils.millis() - getLastAttackTime()) / 1000f) + "s");
        }
    }
}
