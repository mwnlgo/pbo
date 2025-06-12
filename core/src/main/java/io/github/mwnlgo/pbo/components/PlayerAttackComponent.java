package io.github.mwnlgo.pbo.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.mwnlgo.pbo.entities.Player;

public abstract class PlayerAttackComponent {
    private Player player;
    private float cooldown;
    private float lastAttackTime;

    public PlayerAttackComponent(Player player) {
        this.player = player;
    }

    public abstract void attack();

    public void update(float delta) {

    }

    public void render(SpriteBatch batch) {

    }

    public Player getPlayer() {
        return player;
    }

    public float getCooldown() {
        return cooldown;
    }

    public void setCooldown(float cooldown) {
        if (cooldown < 0) {
            this.cooldown = 0;
        } else {
        this.cooldown = cooldown;
        }
    }

    public float getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(float lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }
}
