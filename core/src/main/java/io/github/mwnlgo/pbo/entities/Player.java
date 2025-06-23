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

    private float maxHealth;
    private float currentHealth;
    private float speed;
    private boolean isAlive;
    private boolean isMoving;
    private boolean isAttacking; // Flag untuk melacak apakah pemain sedang menyerang

    // Sistem Animasi Directional
    private Map<PlayerState, Map<Direction, Animation<TextureRegion>>> animations;
    private PlayerState currentState;
    private Direction currentDirection;
    private float stateTimer; // Timer untuk animasi

    // Sistem Multi-Serangan
    private Map<AttackType, PlayerAttackComponent> attackComponents;
    private PlayerAttackComponent currentAttack;

    public Player(float x, float y, GameScreen screen) {
        this.screen = screen;
        this.position = new Vector2(x, y);
        this.maxHealth = 100f;
        this.currentHealth = this.maxHealth;
        this.speed = 1000f;
        this.isAlive = true;
        this.isMoving = false;
        this.isAttacking = false; // Inisialisasi status serangan

        loadAnimations(); // Muat semua animasi
        loadAttackComponents(); // Muat komponen serangan

        this.currentState = PlayerState.IDLE; // Status awal: diam
        this.currentDirection = Direction.DOWN; // Arah awal: bawah
        this.stateTimer = 0f; // Timer animasi dimulai dari 0
        this.currentAttack = attackComponents.get(AttackType.MELEE); // Serangan awal: melee

        // Tentukan batas pemain berdasarkan frame animasi idle awal
        TextureRegion initialFrame = animations.get(PlayerState.IDLE).get(Direction.DOWN).getKeyFrame(0);
        this.bounds = new Rectangle(x, y, initialFrame.getRegionWidth(), initialFrame.getRegionHeight());
    }

    /**
     * Memuat animasi dari sprite sheet.
     * @param path Path ke file gambar sprite sheet.
     * @param cols Jumlah kolom dalam sprite sheet.
     * @param rows Jumlah baris dalam sprite sheet.
     * @param frameDuration Durasi per frame animasi.
     * @return Objek Animation<TextureRegion> yang sudah dimuat.
     */
    private Animation<TextureRegion> loadAnimationFromSheet(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(path);
        TextureRegion[][] temp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                frames.add(temp[row][col]);
            }
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP); // Default LOOPING
    }

    /**
     * Memuat semua animasi pemain (idle, berjalan, menyerang) dari file sprite sheet.
     */
    private void loadAnimations() {
        this.animations = new HashMap<>();

        // Animasi IDLE
        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("player/idle_ayam.png", 2, 2, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("player/idle_back_ayam.png", 2, 2, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("player/idle_left_ayam.png", 2, 2, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("player/idle_right_ayam.png", 2, 2, 0.25f));
        animations.put(PlayerState.IDLE, idleAnims);

        // Animasi WALKING
        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("player/walk_ayam.png", 2, 2, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("player/walk_back_ayam.png", 2, 2, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("player/walk_left_ayam.png", 2, 2, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("player/walk_right_ayam.png", 2, 2, 0.15f));
        animations.put(PlayerState.WALKING, walkAnims);

        // Animasi ATTACKING (PENTING: Gunakan PlayMode.NORMAL untuk animasi sekali putar)
        // Perhatikan bahwa di sini saya memanggil loadAnimationFromSheet, lalu mengubah PlayMode-nya.
        // Atau Anda bisa memodifikasi loadAnimationFromSheet untuk menerima PlayMode sebagai parameter.
        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();
        Animation<TextureRegion> attackDown = loadAnimationFromSheet("player/attack_ayam.png", 2, 2, 0.1f);
        attackDown.setPlayMode(Animation.PlayMode.NORMAL); // Animasi serangan biasanya sekali putar
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
    }

    /**
     * Memuat komponen-komponen serangan yang berbeda.
     */
    private void loadAttackComponents() {
        this.attackComponents = new HashMap<>();
        attackComponents.put(AttackType.MELEE, new PlayerMeleeAttack(this));
        attackComponents.put(AttackType.THROW, new PlayerDistanceAttack(this));
        attackComponents.put(AttackType.SPECIAL, new PlayerSpecialAttack(this));
    }

    /**
     * Memperbarui status pemain setiap frame.
     * @param delta Waktu dalam detik sejak frame terakhir.
     */
    public void update(float delta) {
        if (!isAlive) {
            currentState = PlayerState.DEAD; // Jika mati, set status DEAD
            return;
        }

        PlayerState previousState = currentState; // Simpan state sebelumnya untuk mendeteksi perubahan
        isMoving = false; // Reset status bergerak setiap frame
        // isAttacking tidak di-reset di sini, karena animasinya harus selesai dulu

        handleInput(delta); // Tangani input pemain
        currentAttack.update(delta); // Perbarui logika komponen serangan

        // --- Logika Penentuan State Animasi ---
        if (isAttacking) {
            currentState = PlayerState.ATTACKING;
            // Dapatkan animasi saat ini untuk memeriksa apakah sudah selesai
            Animation<TextureRegion> currentAnim = animations.get(currentState).get(currentDirection);
            if (currentAnim != null && currentAnim.isAnimationFinished(stateTimer)) {
                isAttacking = false; // Animasi serangan selesai
                stateTimer = 0; // Reset timer untuk state berikutnya
                // Logika ini akan secara otomatis membuat pemain beralih ke IDLE/WALKING
                // pada frame berikutnya karena isAttacking menjadi false.
            }
        } else if (isMoving) {
            currentState = PlayerState.WALKING;
        } else {
            currentState = PlayerState.IDLE;
        }

        // Reset stateTimer hanya jika state berubah
        // PENTING: Jika stateTimer direset setiap frame jika tidak ada perubahan state,
        // animasi tidak akan berjalan. Kita hanya mereset jika state berubah.
        if (currentState != previousState) {
            stateTimer = 0;
        } else {
            stateTimer += delta; // Lanjutkan timer jika state tidak berubah
        }

        bounds.setCenter(position.x, position.y); // Perbarui posisi bounds
    }

    /**
     * Menangani input dari pemain (keyboard dan mouse).
     * @param delta Waktu dalam detik sejak frame terakhir.
     */
    private void handleInput(float delta) {
        // --- 1. Logika Pergerakan (dari Keyboard) ---
        Vector2 moveDirection = new Vector2();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveDirection.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDirection.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDirection.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDirection.x += 1;

        if (moveDirection.len() > 0) {
            isMoving = true;
            moveDirection.nor(); // Normalisasi untuk kecepatan yang konsisten di semua arah
            position.mulAdd(moveDirection, speed * delta); // Pindahkan pemain
        }

        // --- 2. Logika Arah Hadap (dari Mouse) ---
        // Ubah koordinat mouse layar ke koordinat dunia game
        Vector3 mouseInWorld = screen.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float dx = mouseInWorld.x - position.x;
        float dy = mouseInWorld.y - position.y;

        // Tentukan arah dominan (horizontal atau vertikal) berdasarkan posisi mouse
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

        // Pemicu serangan hanya jika tombol kiri mouse baru saja ditekan dan pemain tidak sedang menyerang
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !isAttacking) {
            currentAttack.attack(); // Panggil metode serangan dari komponen aktif
            this.isAttacking = true; // Set flag isAttacking ke true
            this.stateTimer = 0; // Reset stateTimer agar animasi serangan dimulai dari frame pertama
            Gdx.app.log("Player", "Attack initiated! Current state: " + currentState);
        }
    }

    /**
     * Menggambar pemain ke layar.
     * @param batch Objek SpriteBatch untuk menggambar.
     */
    public void render(SpriteBatch batch) {
        // Dapatkan map animasi untuk state saat ini (IDLE, WALKING, ATTACKING)
        Map<Direction, Animation<TextureRegion>> stateAnimations = animations.get(currentState);
        if (stateAnimations == null) {
            // Fallback ke IDLE jika state saat ini tidak memiliki animasi (misal DEAD state belum ada animasinya)
            stateAnimations = animations.get(PlayerState.IDLE);
            if (stateAnimations == null) return; // Jika IDLE pun tidak ada, tidak bisa menggambar
        }

        // Dapatkan animasi spesifik untuk arah saat ini
        Animation<TextureRegion> currentAnimation = stateAnimations.get(currentDirection);
        if (currentAnimation == null) {
            // Fallback ke arah DOWN jika arah saat ini tidak memiliki animasi untuk state tersebut
            currentAnimation = stateAnimations.get(Direction.DOWN);
            if (currentAnimation == null) return; // Jika arah DOWN pun tidak ada, tidak bisa menggambar
        }

        // Dapatkan frame saat ini dari animasi.
        // Jika sedang menyerang (ATTACKING), gunakan `false` untuk `looping` agar animasi hanya diputar sekali.
        // Untuk state lain, gunakan `true` untuk `looping`.
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTimer, currentState != PlayerState.ATTACKING);

        float frameWidth = currentFrame.getRegionWidth();
        float frameHeight = currentFrame.getRegionHeight();

        // Gambar frame animasi di posisi tengah pemain
        batch.draw(currentFrame,
            position.x - frameWidth / 2f,
            position.y - frameHeight / 2f);
    }

    /**
     * Membuang (dispose) semua tekstur yang digunakan oleh pemain untuk mencegah memory leak.
     */
    public void dispose() {
        Gdx.app.log("Player", "Disposing player textures...");

        // Gunakan Array untuk melacak tekstur mana yang sudah di-dispose,
        // untuk mencegah error jika beberapa animasi menggunakan spritesheet yang sama.
        Array<Texture> disposedTextures = new Array<>();

        // Iterasi melalui Map luar (IDLE, WALKING, ATTACKING, etc.)
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
                        Gdx.app.log("Player", "Disposed texture: " + texture.toString());
                    }
                }
            }
        }
    }

    /**
     * Mengganti jenis serangan yang aktif.
     * @param type Jenis AttackType yang baru.
     */
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

    public GameScreen getScreen() {
        return screen;
    }
}
