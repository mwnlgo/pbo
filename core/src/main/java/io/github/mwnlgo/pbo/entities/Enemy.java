package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;
import io.github.mwnlgo.pbo.interfaces.IDamageable;

import java.util.HashMap;
import java.util.Map;

public abstract class Enemy implements IDamageable {
    protected GameScreen screen;
    protected Vector2 position;
    protected Rectangle bounds;

    protected float maxHealth;
    protected float currentHealth;
    protected float speed;
    protected boolean isAlive;

    protected Player target;

    // Sistem Animasi Directional
    protected Map<EnemyState, Map<Direction, Animation<TextureRegion>>> animations;
    protected EnemyState currentState;
    protected Direction currentDirection;
    protected float stateTimer;

    public Enemy(float x, float y, Player target, GameScreen screen) {
        this.position = new Vector2(x, y);
        this.target = target;
        this.screen = screen;
        this.isAlive = true;

        // Inisialisasi 'mesin' animasi
        this.animations = new HashMap<>();
        this.stateTimer = 0f;
        this.currentDirection = Direction.DOWN; // Arah hadap default
    }

    public void update(float delta) {
        if (!isAlive) {
            currentState = EnemyState.DEAD;
        }

        updateAI(delta);
        stateTimer += delta;

        if (bounds != null) {
            bounds.setCenter(position.x, position.y);
        }
    }

    public void render(SpriteBatch batch) {
        if (animations.isEmpty()) return;

        Map<Direction, Animation<TextureRegion>> stateAnimations = animations.get(currentState);
        if (stateAnimations == null) return;

        Animation<TextureRegion> currentAnimation = stateAnimations.get(currentDirection);
        if (currentAnimation == null) {
            currentAnimation = stateAnimations.get(Direction.DOWN); // Fallback
        }
        if (currentAnimation == null) return;

        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTimer, true);
        float frameWidth = currentFrame.getRegionWidth();
        float frameHeight = currentFrame.getRegionHeight();

        batch.draw(currentFrame, position.x - frameWidth / 2f, position.y - frameHeight / 2f);
    }

    public void dispose() {
        for (Map<Direction, Animation<TextureRegion>> stateAnims : animations.values()) {
            for (Animation<TextureRegion> animation : stateAnims.values()) {
                if (animation.getKeyFrames().length > 0) {
                    animation.getKeyFrames()[0].getTexture().dispose();
                    break; // Cukup dispose satu, karena semua frame dari spritesheet yang sama
                }
            }
        }
    }

    protected abstract void updateAI(float delta);
    protected void enterHurtState() {}
    protected void enterDeadState() {}

    protected Animation<TextureRegion> loadAnimationFromSheet(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(path);
        TextureRegion[][] tempFrames = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();

        for (int col = 0; col < cols; col++) {
            frames.add(tempFrames[0][col]); // baris 0 karena satu arah = satu sheet
        }

        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    // --- Implementasi Interface IDamageable ---

    @Override
    public void takeDamage(float amount) {
        if (!isAlive) return;
        this.currentHealth -= amount;
        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            this.isAlive = false;
            enterDeadState();
        } else {
            enterHurtState();
        }
    }

    @Override
    public boolean isAlive() { return isAlive; }

    @Override
    public Rectangle getBounds() { return bounds; }

    @Override
    public float getHealth() { return currentHealth; }

    @Override
    public float getMaxHealth() { return maxHealth; }
}
