package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.entities.Projectile;

public class PlayerDistanceAttack extends PlayerAttackComponent {
    private static final String TAG = "PlayerDistanceAttack";
    public PlayerDistanceAttack(Player player) {
        super(player);
        setCooldown(1.0f);
    }


    @Override
    public void attack() {
        if (TimeUtils.millis() - getLastAttackTime() > getCooldown() * 1000) {
            Gdx.app.log(TAG, "Distance Attack success!");

            // Menggunakan getter untuk mendapatkan informasi dari player
            Projectile projectile = new Projectile(
                getPlayer().getPosition().x,
                getPlayer().getPosition().y
            );

            getPlayer().getScreen().spawnEntity(projectile);

            setLastAttackTime(TimeUtils.millis());
        }
    }
}
