package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import io.github.mwnlgo.pbo.components.PlayerSpecialAttack;
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
    // HAPUS: private float rotation; // Tidak dibutuhkan lagi

    private float maxHealth;
    private float currentHealth;
    private float speed;
    private boolean isAlive;
    private boolean isMoving;

    // Sistem Animasi Directional
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
        // HAPUS: this.rotation = 0f;
        this.maxHealth = 100f;
        this.currentHealth = this.maxHealth;
        this.speed = 1000f;
        this.isAlive = true;
        this.isMoving = false;

        // Inisialisasi tidak berubah
        loadAnimations();
        loadAttackComponents();

        this.currentState = PlayerState.IDLE;
        this.currentDirection = Direction.DOWN;
        this.stateTimer = 0f;
        this.currentAttack = attackComponents.get(AttackType.MELEE);

        TextureRegion initialFrame = animations.get(PlayerState.IDLE).get(Direction.DOWN).getKeyFrame(0);
        this.bounds = new Rectangle(x, y, initialFrame.getRegionWidth(), initialFrame.getRegionHeight());
    }

    private Animation<TextureRegion> loadAnimationFromSheet(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(path);
        TextureRegion[][] temp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                frames.add(temp[row][col]);
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
    }

    private void loadAttackComponents() {
        this.attackComponents = new HashMap<>();
        attackComponents.put(AttackType.MELEE, new PlayerMeleeAttack(this));
        attackComponents.put(AttackType.THROW, new PlayerDistanceAttack(this));
        attackComponents.put(AttackType.SPECIAL, new PlayerSpecialAttack(this));
    }

    public void update(float delta) {
        if (!isAlive) {
            currentState = PlayerState.DEAD;
            return;
        }
        PlayerState previousState = currentState;
        isMoving = false;
        handleInput(delta);
        currentAttack.update(delta);
        if (isMoving) {
            currentState = PlayerState.WALKING;
        } else {
            currentState = PlayerState.IDLE;
        }
        if (currentState != previousState) {
            stateTimer = 0;
        }
        stateTimer += delta;
        bounds.setCenter(position.x, position.y);
    }

    private void handleInput(float delta) {
        // --- 1. Logika Pergerakan (dari Keyboard) ---
        // Logika ini tetap sama, hanya untuk mengubah posisi
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

        // --- 2. Logika Arah Hadap (dari Mouse) ---
        // Ini adalah logika baru yang menggantikan rotasi
        Vector3 mouseInWorld = screen.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float dx = mouseInWorld.x - position.x;
        float dy = mouseInWorld.y - position.y;

        // Tentukan arah dominan (horizontal atau vertikal)
        if (Math.abs(dx) > Math.abs(dy)) {
            // Arah hadap dominan adalah KIRI atau KANAN
            if (dx > 0) {
                currentDirection = Direction.RIGHT;
            } else {
                currentDirection = Direction.LEFT;
            }
        } else {
            // Arah hadap dominan adalah ATAS atau BAWAH
            if (dy > 0) {
                currentDirection = Direction.UP;
            } else {
                currentDirection = Direction.DOWN;
            }
        }

        // --- 3. Logika Ganti Senjata dan Menyerang ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchAttack(AttackType.MELEE);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchAttack(AttackType.THROW);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) switchAttack(AttackType.SPECIAL);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            currentAttack.attack();
        }
    }

    public void render(SpriteBatch batch) {
        Map<Direction, Animation<TextureRegion>> stateAnimations = animations.get(currentState);
        if (stateAnimations == null) return;
        Animation<TextureRegion> currentAnimation = stateAnimations.get(currentDirection);
        if (currentAnimation == null) return;
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTimer, true);
        float frameWidth = currentFrame.getRegionWidth();
        float frameHeight = currentFrame.getRegionHeight();

        // --- GAMBAR TANPA ROTASI ---
        batch.draw(currentFrame,
            position.x - frameWidth / 2f,
            position.y - frameHeight / 2f);
    }

    public void dispose() {
        Gdx.app.log("Player", "Disposing player textures...");

        // Kita gunakan Array untuk melacak tekstur mana yang sudah di-dispose,
        // untuk mencegah error jika beberapa animasi menggunakan spritesheet yang sama.
        Array<Texture> disposedTextures = new Array<>();

        // Iterasi melalui Map luar (IDLE, WALKING, etc.)
        for (Map<Direction, Animation<TextureRegion>> stateAnims : animations.values()) {
            // Iterasi melalui Map dalam (UP, DOWN, LEFT, RIGHT)
            for (Animation<TextureRegion> animation : stateAnims.values()) {

                // Periksa apakah animasi ini punya frame
                if (animation.getKeyFrames().length > 0) {
                    // Ambil objek Texture (spritesheet utuh) dari frame pertama
                    Texture texture = animation.getKeyFrames()[0].getTexture();

                    // Hanya dispose jika texture ini BELUM PERNAH kita dispose sebelumnya
                    if (!disposedTextures.contains(texture, true)) {
                        texture.dispose(); // Lakukan dispose
                        disposedTextures.add(texture); // Catat bahwa texture ini sudah di-dispose
                        // Gdx.app.log("Player", "Disposed texture for path: [path_anda]"); // Log jika perlu
                    }
                }
            }
        }
    }

    public void switchAttack(AttackType type) {
        PlayerAttackComponent newAttack = attackComponents.get(type);
        if (newAttack != null) {
            this.currentAttack = newAttack;
            Gdx.app.log("Player", "Beralih ke serangan: " + type);
        }
    }

    // --- Implementasi Interface IDamageable ---
    @Override
    public void takeDamage(float amount) {
        if (!isAlive) return;
        this.currentHealth -= amount;
        Gdx.app.log("Player", "Took " + amount + " damage. Health: " + currentHealth);
        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            this.isAlive = false;
            Gdx.app.log("Player", "Player has died!");
        }
    }

    @Override
    public float getHealth() {
        return currentHealth;
    }

    @Override
    public float getMaxHealth() {
        return maxHealth;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    // --- Getter Methods ---
    public Vector2 getPosition() {
        return position;
    }

    // HAPUS: public float getRotation() { return rotation; }

    public GameScreen getScreen() {
        return screen;
    }
}
