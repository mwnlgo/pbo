package io.github.mwnlgo.pbo.interfaces;

import com.badlogic.gdx.math.Rectangle;

public interface IDamageable {

    // --- Implementasi Interface IDamageable ---
    void takeDamage(float amount);

    boolean isAlive();

    Rectangle getBounds();

    float getHealth();

    float getMaxHealth();
}
