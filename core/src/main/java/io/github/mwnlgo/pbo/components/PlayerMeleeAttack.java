package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import io.github.mwnlgo.pbo.entities.Player;

public class PlayerMeleeAttack extends PlayerAttackComponent {
    private static final String TAG = "PlayerMeleeAttack";

    public PlayerMeleeAttack (Player player) {
        super(player);
        setCooldown(0.5f);
    }
    @Override
    public void attack() {
        if (TimeUtils.millis() - getLastAttackTime() > getCooldown() * 1000) {
            Gdx.app.log(TAG, "Melee Attack success!");


            setLastAttackTime(TimeUtils.millis());
        }
    }
}
