package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound; // (BARU) Import Sound
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

    protected float hitboxWidth;
    protected float hitboxHeight;
    protected float hitboxOffsetX;
    protected float hitboxOffsetY;

    protected float maxHealth;
    protected float currentHealth;
    protected float speed;
    protected boolean isAlive;

    protected Player target;

    // Sistem Animasi
    protected Map<EnemyState, Map<Direction, Animation<TextureRegion>>> animations;
    protected EnemyState currentState;
    protected EnemyState previousState;
    protected Direction currentDirection;
    protected float stateTimer;

    protected Array<Texture> managedTextures;

    // (BARU) Variabel untuk suara kematian. Protected agar bisa diakses subclass.
    protected Sound deathSound;

    public Enemy(float x, float y, Player target, GameScreen screen,
                 float initialHealth, float initialSpeed,
                 float hitboxW, float hitboxH, float hitboxOX, float hitboxOY) {
        this.position = new Vector2(x, y);
        this.target = target;
        this.screen = screen;
        this.maxHealth = initialHealth;
        this.currentHealth = initialHealth;
        this.speed = initialSpeed;
        this.isAlive = true;

        this.hitboxWidth = hitboxW;
        this.hitboxHeight = hitboxH;
        this.hitboxOffsetX = hitboxOX;
        this.hitboxOffsetY = hitboxOY;
        this.bounds = new Rectangle(position.x + hitboxOffsetX, position.y + hitboxOffsetY, hitboxWidth, hitboxHeight);

        this.animations = new HashMap<>();
        this.stateTimer = 0f;
        this.currentDirection = Direction.DOWN;
        this.currentState = EnemyState.IDLE;
        this.previousState = EnemyState.IDLE;

        this.managedTextures = new Array<>();
    }

    public void update(float delta) {
        if (!isAlive) {
            if (currentState != EnemyState.DEAD) {
                currentState = EnemyState.DEAD;
                stateTimer = 0;
            }
            stateTimer += delta;
            return;
        }

        this.previousState = this.currentState;
        updateAI(delta);

        if (currentState != previousState) {
            stateTimer = 0f;
        } else {
            stateTimer += delta;
        }

        this.bounds.setPosition(this.position.x + this.hitboxOffsetX, this.position.y + this.hitboxOffsetY);
    }

    public void render(SpriteBatch batch) {
        if (animations.isEmpty()) return;

        Map<Direction, Animation<TextureRegion>> stateAnimations = animations.get(currentState);
        if (stateAnimations == null) {
            stateAnimations = animations.get(EnemyState.IDLE);
            if (stateAnimations == null) return;
        }

        Animation<TextureRegion> currentAnimation = stateAnimations.get(currentDirection);
        if (currentAnimation == null) {
            currentAnimation = stateAnimations.get(Direction.DOWN);
        }
        if (currentAnimation == null) return;

        boolean looping = (currentState != EnemyState.ATTACKING && currentState != EnemyState.DEAD && currentState != EnemyState.HURT);
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTimer, looping);

        float frameWidth = currentFrame.getRegionWidth();
        float frameHeight = currentFrame.getRegionHeight();

        batch.draw(currentFrame, position.x - frameWidth / 2f, position.y - frameHeight / 2f);
    }

    public void dispose() {
        Gdx.app.log("Enemy", "Disposing enemy textures...");
        for (Texture texture : managedTextures) {
            texture.dispose();
        }
        managedTextures.clear();

        // (BARU) Pastikan suara kematian juga di-dispose
        if (deathSound != null) {
            deathSound.dispose();
        }
    }

    protected abstract void updateAI(float delta);

    protected void enterHurtState() {
        currentState = EnemyState.HURT;
    }

    protected void enterDeadState() {
        currentState = EnemyState.DEAD;
    }

    protected Animation<TextureRegion> loadAnimationFromSheet(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(path);
        managedTextures.add(sheet);
        TextureRegion[][] tempFrames = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                frames.add(tempFrames[row][col]);
            }
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void takeDamage(float amount) {
        if (!isAlive || currentState == EnemyState.HURT) {
            return;
        }

        this.currentHealth -= amount;

        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            this.isAlive = false;

            // (BARU) Putar suara kematian sebelum mengubah state
            if (deathSound != null) {
                deathSound.play();
            }

            this.currentState = EnemyState.DEAD;
            this.stateTimer = 0;
            Gdx.app.log("Enemy", "Enemy has died!");
        } else {
            this.currentState = EnemyState.HURT;
            this.stateTimer = 0f;
            Gdx.app.log("Enemy", "Enemy took damage, entering HURT state.");
        }
    }

    public boolean isDeathAnimationFinished() {
        if (this.currentState != EnemyState.DEAD) {
            return false;
        }
        Animation<TextureRegion> deadAnimation = animations.get(EnemyState.DEAD).get(currentDirection);
        if (deadAnimation == null) {
            return true;
        }
        return deadAnimation.isAnimationFinished(stateTimer);
    }

    @Override
    public boolean isAlive() { return isAlive; }
    @Override
    public Rectangle getBounds() { return bounds; }
    @Override
    public float getHealth() { return currentHealth; }
    @Override
    public float getMaxHealth() { return maxHealth; }

    public Vector2 getPosition() { return position; }
    public Direction getCurrentDirection() { return currentDirection; }
    public EnemyState getCurrentState() { return currentState; }

    public Player getTarget() {
        return target;
    }

    public GameScreen getScreen() {
        return screen;
    }
}
