package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;

import java.util.HashMap;
import java.util.Map;

public class EnemyC extends Enemy {

    private final float detectionRange = 450f;
    // Jarak ideal Necromancer dari pemain untuk melancarkan serangan (tidak terlalu dekat/jauh)
    private final float preferredAttackDistance = 250f;
    // Jarak di mana Necromancer akan berhenti bergerak atau mulai mundur jika pemain terlalu dekat
    private final float stopMovingDistance = 200f;
    private float hurtDuration = 0.2f;
    private float hurtTimer = 0f;

    // --- Variabel untuk Serangan Jarak Jauh ---
    private final float attackCooldown = 2.0f; // Cooldown antar serangan proyektil (detik)
    private float attackTimer = 0f; // Timer untuk cooldown serangan
    private boolean hasAttackedInCurrentAnimation = false; // Flag untuk memastikan proyektil hanya diluncurkan sekali per animasi

    public EnemyC(float x, float y, Player target, GameScreen screen) {
        super(x, y, target, screen);

        this.speed = 90f; // Kecepatan Necromancer
        this.maxHealth = 60f; // Health Necromancer
        this.currentHealth = this.maxHealth;

        loadAnimations(); // Muat animasi

        TextureRegion initialFrame = animations.get(EnemyState.IDLE).get(Direction.DOWN).getKeyFrame(0);
        this.bounds = new Rectangle(x, y, initialFrame.getRegionWidth(), initialFrame.getRegionHeight());

        this.currentState = EnemyState.IDLE;
        this.currentDirection = Direction.DOWN;
    }

    /**
     * Memuat semua animasi spesifik untuk EnemyC (Necromancer: idle, berjalan, menyerang/casting).
     */
    private void loadAnimations() {
        // Animasi IDLE
        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("necro/idle_Necro_Left.png", 8, 1, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("necro/idle_Necro_Right.png", 8, 1, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("necro/idle_Necro_Left.png", 8, 1, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("necro/idle_Necro_Right.png", 8, 1, 0.25f));
        this.animations.put(EnemyState.IDLE, idleAnims);

        // Animasi CHASING (WALK)
        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("necro/walk_Necro_Left.png", 8, 1, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("necro/walk_Necro_Right.png", 8, 1, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("necro/walk_Necro_Left.png", 8, 1, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("necro/walk_Necro_Right.png", 8, 1, 0.15f));
        this.animations.put(EnemyState.CHASING, walkAnims);

        // --- Animasi ATTACKING (Casting/Meluncurkan Sihir) untuk Necromancer ---
        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();

        // Pastikan path, cols, rows, dan frameDuration sesuai aset Anda.
        Animation<TextureRegion> castDown = loadAnimationFromSheet("necro/attacking_Necro_left_animation.png", 13, 1, 0.12f);
        castDown.setPlayMode(Animation.PlayMode.NORMAL); // Animasi serangan hanya diputar sekali
        attackAnims.put(Direction.DOWN, castDown);

        Animation<TextureRegion> castUp = loadAnimationFromSheet("necro/attacking_Necro_right_animation.png", 13, 1, 0.12f);
        castUp.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.UP, castUp);

        Animation<TextureRegion> castLeft = loadAnimationFromSheet("necro/attacking_Necro_left_animation.png", 13, 1, 0.12f);
        castLeft.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.LEFT, castLeft);

        Animation<TextureRegion> castRight = loadAnimationFromSheet("necro/attacking_Necro_right_animation.png", 13, 1, 0.12f);
        castRight.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, castRight);

        this.animations.put(EnemyState.ATTACKING, attackAnims);
    }

    @Override
    protected void enterHurtState() {
        this.currentState = EnemyState.HURT;
        this.hurtTimer = hurtDuration;
        this.stateTimer = 0; // Reset state timer untuk memulai animasi hurt dari awal
    }

    /**
     * Metode utama untuk logika AI musuh Necromancer.
     * Mengatur perilaku Necromancer berdasarkan jarak ke pemain (target).
     *
     * @param delta Waktu dalam detik sejak frame terakhir.
     */
    @Override
    protected void updateAI(float delta) {
        // Update cooldown serangan
        if (attackTimer > 0) {
            attackTimer -= delta;
        }

        float distanceToPlayer = target.getPosition().dst(this.position); // Jarak aktual ke pemain

        switch (currentState) {
            case IDLE:
                if (distanceToPlayer < detectionRange) {
                    currentState = EnemyState.CHASING;
                    stateTimer = 0;
                }
                break;

            case CHASING:
                // Logika AI Necromancer (musuh jarak jauh)
                if (distanceToPlayer < stopMovingDistance) { // Pemain terlalu dekat, mundur
                    retreat(delta);
                    // Necromancer bisa menyerang sambil mundur jika cooldown habis
                    if (attackTimer <= 0) {
                        currentState = EnemyState.ATTACKING;
                        stateTimer = 0;
                        hasAttackedInCurrentAnimation = false;
                    }
                } else if (distanceToPlayer > preferredAttackDistance) { // Pemain terlalu jauh, maju
                    chase(delta);
                } else { // Pemain dalam jangkauan ideal untuk menyerang
                    // Berhenti bergerak dan coba serang
                    if (attackTimer <= 0) {
                        currentState = EnemyState.ATTACKING;
                        stateTimer = 0;
                        hasAttackedInCurrentAnimation = false;
                    }
                    // Jika cooldown belum habis, Necromancer hanya akan diam di posisi ini
                }

                // Kembali IDLE jika pemain terlalu jauh
                if (distanceToPlayer > detectionRange * 1.2f) { // Tambahkan buffer untuk menghindari flapping
                    currentState = EnemyState.IDLE;
                    stateTimer = 0;
                }
                break;

            case ATTACKING:
                // Dapatkan animasi serangan (casting) saat ini
                Animation<TextureRegion> currentAttackAnim = animations.get(EnemyState.ATTACKING).get(currentDirection);

                // --- Logika Memicu Peluncuran Proyektil ---
                // Sesuaikan '0.6f' ini dengan titik di mana animasi casting meluncurkan proyektil.
                float launchTime = currentAttackAnim.getAnimationDuration() * 0.6f;

                if (!hasAttackedInCurrentAnimation && stateTimer >= launchTime) {
                    // Hanya meluncurkan proyektil jika belum diluncurkan di animasi ini dan sudah mencapai 'titik launch'
                    launchProjectile(); // Panggil metode untuk meluncurkan proyektil
                    hasAttackedInCurrentAnimation = true; // Set flag agar tidak meluncurkan lagi di animasi yang sama
                    Gdx.app.log("EnemyC", "Necromancer launched a projectile!");
                }

                // Cek apakah animasi serangan (casting) sudah selesai
                if (currentAttackAnim.isAnimationFinished(stateTimer)) {
                    attackTimer = attackCooldown; // Mulai cooldown setelah serangan selesai

                    // Setelah menyerang, tentukan state berikutnya berdasarkan jarak ke pemain
                    if (distanceToPlayer < stopMovingDistance || distanceToPlayer > preferredAttackDistance) {
                        currentState = EnemyState.CHASING; // Kembali mengejar/mundur untuk menyesuaikan posisi
                        stateTimer = 0;
                    } else {
                        // Jika pemain masih dalam jangkauan ideal, kembali ke IDLE
                        // (atau bisa juga langsung coba serang lagi jika cooldown 0 dan ingin agresi tinggi)
                        currentState = EnemyState.IDLE;
                        stateTimer = 0;
                    }
                }
                break;

            case HURT:
                hurtTimer -= delta;
                if (hurtTimer <= 0) {
                    currentState = EnemyState.CHASING; // Kembali mengejar/beradaptasi setelah pulih dari hurt
                    stateTimer = 0;
                }
                break;

            case DEAD:
                // Tidak melakukan apa-apa, menunggu GameScreen menghapusnya
                break;
        }
    }

    /**
     * Memeriksa apakah pemain berada dalam jangkauan tertentu.
     *
     * @param range Jarak jangkauan.
     * @return true jika pemain dalam jangkauan, false sebaliknya.
     */
    private boolean isPlayerInRange(float range) {
        // Menggunakan dst2 (jarak kuadrat) lebih efisien
        return target.getPosition().dst2(this.position) < range * range;
    }

    /**
     * Logika untuk mengejar target (pemain).
     *
     * @param delta Waktu dalam detik sejak frame terakhir.
     */
    private void chase(float delta) {
        Vector2 directionToTarget = new Vector2(target.getPosition()).sub(this.position);

        // Menentukan arah visual (animasi) berdasarkan arah dominan ke target
        if (Math.abs(directionToTarget.x) > Math.abs(directionToTarget.y)) {
            currentDirection = directionToTarget.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            currentDirection = directionToTarget.y > 0 ? Direction.UP : Direction.DOWN;
        }

        directionToTarget.nor();
        position.mulAdd(directionToTarget, speed * delta);
    }

    /**
     * Logika untuk mundur dari target (pemain) jika terlalu dekat.
     *
     * @param delta Waktu dalam detik sejak frame terakhir.
     */
    private void retreat(float delta) {
        Vector2 directionFromTarget = new Vector2(this.position).sub(target.getPosition());

        // Menentukan arah visual (animasi) saat mundur
        if (Math.abs(directionFromTarget.x) > Math.abs(directionFromTarget.y)) {
            currentDirection = directionFromTarget.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            currentDirection = directionFromTarget.y > 0 ? Direction.UP : Direction.DOWN;
        }

        directionFromTarget.nor();
        position.mulAdd(directionFromTarget, speed * delta);
    }

    /**
     * Meluncurkan proyektil ke arah pemain.
     * Proyektil dibuat dan ditambahkan ke GameScreen.
     */
    private void launchProjectile() {
        // Posisi target (player)
        Vector2 playerPos = new Vector2(target.getBounds().x + target.getBounds().width / 2f,
            target.getBounds().y + target.getBounds().height / 2f);

        // Posisi spawn proyektil (di atas player, bukan di badan necromancer)
        float spawnX = playerPos.x;
        float spawnY = playerPos.y + 150f; // Tambahkan offset ke atas

        EnemyProjectile projectile = new EnemyProjectile(
            spawnX, spawnY,
            playerPos, // Target tengah player
            15f, // Damage proyektil
            screen,
            target
        );
        screen.addEnemyProjectile(projectile);
    }
}
