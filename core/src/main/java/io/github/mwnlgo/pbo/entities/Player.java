package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.components.PlayerAttackComponent;
import io.github.mwnlgo.pbo.components.PlayerDistanceAttack;
import io.github.mwnlgo.pbo.components.PlayerMeleeAttack;
import io.github.mwnlgo.pbo.enums.AttackType;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.PlayerState;
import io.github.mwnlgo.pbo.interfaces.IDamageable;

import java.util.HashMap;
import java.util.Map;

public class Player implements IDamageable {
    // --- Atribut ---
    private GameScreen screen;
    private Vector2 position;
    private Rectangle bounds;

    private float hitboxWidth;
    private float hitboxHeight;
    private float hitboxOffsetX;
    private float hitboxOffsetY;

    private float maxHealth;
    private float currentHealth;
    private float speed;
    private boolean isAlive;
    private boolean isMoving;
    private boolean isAttacking;

    // Atribut untuk invincibility setelah diserang
    private boolean isInvincible;
    private final float invincibilityDuration = 2.0f; // Durasi 1 detik
    private float invincibilityTimer;

    // Sistem Animasi
    private Map<PlayerState, Map<Direction, Animation<TextureRegion>>> animations;
    private PlayerState currentState;
    private Direction currentDirection;
    private float stateTimer;

    // Sistem Multi-Serangan
    private Map<AttackType, PlayerAttackComponent> attackComponents;
    private PlayerAttackComponent currentAttack;

    public Player(float x, float y, GameScreen screen) {
        this.screen = screen;
        this.position = new Vector2(x, y);
        this.maxHealth = 100f;
        this.currentHealth = this.maxHealth;
        this.hitboxWidth = 30f;
        this.hitboxHeight = 32f;
        this.hitboxOffsetX = -hitboxWidth / 2f;
        this.hitboxOffsetY = -hitboxHeight / 2f;
        this.speed = 1000f;
        this.isAlive = true;
        this.isMoving = false;
        this.isAttacking = false;

        // Inisialisasi invincibility
        this.isInvincible = false;
        this.invincibilityTimer = 0f;

        loadAnimations();
        loadAttackComponents();

        this.currentState = PlayerState.IDLE;
        this.currentDirection = Direction.DOWN;
        this.stateTimer = 0f;
        this.currentAttack = attackComponents.get(AttackType.MELEE);

        this.bounds = new Rectangle(x + hitboxOffsetX, y + hitboxOffsetY, hitboxWidth, hitboxHeight);
    }

    private Animation<TextureRegion> loadAnimationFromSheet(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(path);
        TextureRegion[][] temp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();
        for (TextureRegion[] row : temp) {
            for (TextureRegion col : row) {
                frames.add(col);
            }
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    private void loadAnimations() {
        this.animations = new HashMap<>();

        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("player/idle_ayam.png", 2, 2, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("player/idle_back_ayam.png", 2, 2, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("player/idle_left_ayam.png", 2, 2, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("player/idle_right_ayam.png", 2, 2, 0.25f));
        animations.put(PlayerState.IDLE, idleAnims);

        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("player/walk_ayam.png", 2, 2, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("player/walk_back_ayam.png", 2, 2, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("player/walk_left_ayam.png", 2, 2, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("player/walk_right_ayam.png", 2, 2, 0.15f));
        animations.put(PlayerState.WALKING, walkAnims);

        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();
        Animation<TextureRegion> attackDown = loadAnimationFromSheet("player/attack_ayam.png", 2, 2, 0.1f);
        attackDown.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.DOWN, attackDown);
        Animation<TextureRegion> attackUp = loadAnimationFromSheet("player/attack_back_ayam.png", 2, 2, 0.1f);
        attackUp.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.UP, attackUp);
        Animation<TextureRegion> attackLeft = loadAnimationFromSheet("player/attack_left_ayam.png", 2, 2, 0.1f);
        attackLeft.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.LEFT, attackLeft);
        Animation<TextureRegion> attackRight = loadAnimationFromSheet("player/attack_right_ayam.png", 2, 2, 0.1f);
        attackRight.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, attackRight);
        animations.put(PlayerState.ATTACKING, attackAnims);

        Map<Direction, Animation<TextureRegion>> hurtAnims = new HashMap<>();
        Animation<TextureRegion> knockDown = loadAnimationFromSheet("player/knock_ayam.png", 5, 1, 0.1f);
        knockDown.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.DOWN, knockDown);
        Animation<TextureRegion> KnockUp = loadAnimationFromSheet("player/knock_back_ayam.png", 5, 1, 0.1f);
        KnockUp.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.UP, KnockUp);
        Animation<TextureRegion> KnockLeft = loadAnimationFromSheet("player/knock_left_ayam.png", 5, 1, 0.1f);
        KnockLeft.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.LEFT, KnockLeft);
        Animation<TextureRegion> knockRight = loadAnimationFromSheet("player/knock_right_ayam.png", 5, 1, 0.1f);
        knockRight.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.RIGHT, knockRight);
        animations.put(PlayerState.HURT, hurtAnims);
    }

    private void loadAttackComponents() {
        this.attackComponents = new HashMap<>();
        attackComponents.put(AttackType.MELEE, new PlayerMeleeAttack(this, 25f, 0.2f));
        attackComponents.put(AttackType.THROW, new PlayerDistanceAttack(this, 15f, 0.2f));
    }

    public void update(float delta) {
        if (!isAlive) {
            currentState = PlayerState.DEAD;
            return;
        }

        // Update timer invincibility
        if (isInvincible) {
            invincibilityTimer -= delta;
            if (invincibilityTimer <= 0) {
                isInvincible = false;
            }
        }

        PlayerState previousState = currentState;
        isMoving = false;

        handleInput(delta);
        currentAttack.update(delta);

        // Logika state machine diperbarui untuk menangani HURT
        if (currentState == PlayerState.HURT) {
            Animation<TextureRegion> hurtAnim = animations.get(currentState).get(currentDirection);
            if (hurtAnim != null && hurtAnim.isAnimationFinished(stateTimer)) {
                // Setelah animasi hurt selesai, kembali ke IDLE
                currentState = PlayerState.IDLE;
            }
        } else if (isAttacking) {
            currentState = PlayerState.ATTACKING;
            Animation<TextureRegion> currentAnim = animations.get(currentState).get(currentDirection);
            if (currentAnim != null && currentAnim.isAnimationFinished(stateTimer)) {
                isAttacking = false;
            }
        } else if (isMoving) {
            currentState = PlayerState.WALKING;
        } else {
            currentState = PlayerState.IDLE;
        }

        if (currentState != previousState) {
            stateTimer = 0;
        } else {
            stateTimer += delta;
        }

        this.bounds.x = this.position.x + this.hitboxOffsetX;
        this.bounds.y = this.position.y + this.hitboxOffsetY;
    }

    private void handleInput(float delta) {
        // Kunci input jika pemain sedang diserang (HURT) atau menyerang
        if (isAttacking || currentState == PlayerState.HURT) return;

        Vector2 moveDirection = new Vector2();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveDirection.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDirection.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDirection.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDirection.x += 1;

        if (moveDirection.len() > 0) {
            isMoving = true;
            moveDirection.nor();
            position.mulAdd(moveDirection, speed * delta);
        }

        Vector3 mouseInWorld = screen.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float dx = mouseInWorld.x - position.x;
        float dy = mouseInWorld.y - position.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            currentDirection = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            currentDirection = dy > 0 ? Direction.UP : Direction.DOWN;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) switchAttack(AttackType.MELEE);
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) switchAttack(AttackType.THROW);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            currentAttack.attack();
            this.isAttacking = true;
            this.stateTimer = 0;
        }
    }

    public void render(SpriteBatch batch) {
        Map<Direction, Animation<TextureRegion>> stateAnimations = animations.get(currentState);
        if (stateAnimations == null) stateAnimations = animations.get(PlayerState.IDLE);

        Animation<TextureRegion> currentAnimation = stateAnimations.get(currentDirection);
        if (currentAnimation == null) currentAnimation = stateAnimations.get(Direction.DOWN);

        if (currentAnimation == null) return;

        boolean isLooping = (currentState != PlayerState.ATTACKING && currentState != PlayerState.HURT);
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTimer, isLooping);

        float frameWidth = currentFrame.getRegionWidth();
        float frameHeight = currentFrame.getRegionHeight();

        // Logika untuk membuat sprite berkedip saat kebal
        if (isInvincible) {
            // Berkedip setiap 0.1 detik dengan mengubah alpha
            if ((int)(invincibilityTimer * 10) % 2 == 0) {
                batch.setColor(1, 1, 1, 0.5f); // Setengah transparan
            } else {
                batch.setColor(1, 1, 1, 1f); // Normal
            }
        }

        batch.draw(currentFrame, position.x - frameWidth / 2f, position.y - frameHeight / 2f);

        // Kembalikan warna batch ke normal setelah menggambar
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        Gdx.app.log("Player", "Disposing player textures...");
        Array<Texture> disposedTextures = new Array<>();
        for (Map<Direction, Animation<TextureRegion>> stateAnims : animations.values()) {
            for (Animation<TextureRegion> animation : stateAnims.values()) {
                if (animation.getKeyFrames().length > 0) {
                    Texture texture = animation.getKeyFrames()[0].getTexture();
                    if (!disposedTextures.contains(texture, true)) {
                        texture.dispose();
                        disposedTextures.add(texture);
                    }
                }
            }
        }
    }

    public void switchAttack(AttackType type) {
        if (isAttacking || currentState == PlayerState.HURT) return;
        PlayerAttackComponent newAttack = attackComponents.get(type);
        if (newAttack != null) {
            this.currentAttack = newAttack;
            Gdx.app.log("Player", "Beralih ke serangan: " + type);
        }
    }

    @Override
    public void takeDamage(float amount) {
        // Jangan terima damage jika sedang kebal
        if (!isAlive || isInvincible) return;

        this.currentHealth -= amount;
        Gdx.app.log("Player", "Took " + amount + " damage. Health: " + currentHealth);

        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            this.isAlive = false;
            this.currentState = PlayerState.DEAD;
            Gdx.app.log("Player", "Player has died!");
        } else {
            // Masuk ke state HURT untuk animasi
            this.currentState = PlayerState.HURT;
            this.stateTimer = 0;

            // Aktifkan mode kebal
            this.isInvincible = true;
            this.invincibilityTimer = this.invincibilityDuration;
        }
    }

    @Override
    public float getHealth() { return currentHealth; }
    @Override
    public float getMaxHealth() { return maxHealth; }
    @Override
    public boolean isAlive() { return isAlive; }
    @Override
    public Rectangle getBounds() { return bounds; }
    public Vector2 getPosition() { return position; }
    public GameScreen getScreen() { return screen; }
    public Direction getCurrentDirection() { return currentDirection; }
    public PlayerAttackComponent getCurrentAttack() { return this.currentAttack;}
}
