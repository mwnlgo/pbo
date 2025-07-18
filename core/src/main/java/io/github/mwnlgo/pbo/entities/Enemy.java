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
import java.util.Random;

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

    private static final float SCALE_FACTOR = 2f;

    // Sistem Animasi
    protected Map<EnemyState, Map<Direction, Animation<TextureRegion>>> animations;
    protected EnemyState currentState;
    protected EnemyState previousState;
    protected Direction currentDirection;
    protected float stateTimer;
    private static final Random random = new Random();

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
        // Logika untuk membatasi pergerakan musuh di dalam peta
        float minX = 0;
        float maxX = screen.getWorldWidth();
        float minY = 0;
        float maxY = screen.getWorldHeight();

        position.x = Math.max(minX, Math.min(position.x, maxX));
        position.y = Math.max(minY, Math.min(position.y, maxY));
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

        // (BARU) Hitung lebar dan tinggi yang sudah diskalakan
        float scaledWidth = currentFrame.getRegionWidth() * SCALE_FACTOR;
        float scaledHeight = currentFrame.getRegionHeight() * SCALE_FACTOR;

        // (BARU) Hitung posisi gambar agar sprite tetap di tengah setelah diskalakan
        float drawX = position.x - scaledWidth / 2f;
        float drawY = position.y; // Menggambar dari basis (kaki) karakter agar tidak melayang

        // (DIUBAH) Gunakan versi batch.draw() yang menerima lebar dan tinggi
        batch.draw(currentFrame, drawX, drawY, scaledWidth, scaledHeight);
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

    public void tryDropItem() {

        // Atur persentase drop di sini. 0.1f berarti 10%
        float dropChance = 0.3f;
        if (random.nextFloat() <= dropChance) {
            // Jika beruntung, panggil metode di GameScreen untuk memunculkan item
            screen.spawnItemDrop(this.position.x, this.position.y);
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

            tryDropItem();

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

    public Player getTarget() {
        return target;
    }

    public GameScreen getScreen() {
        return screen;
    }
}
