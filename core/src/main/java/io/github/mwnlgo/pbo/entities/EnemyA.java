package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx; // Ditambahkan untuk logging
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;

import java.util.HashMap;
import java.util.Map;

public class EnemyA extends Enemy {

    private final float detectionRange = 450f;
    private final float attackRange = 50f;
    private float hurtDuration = 0.2f;
    private float hurtTimer = 0f;

    // --- Variabel baru untuk Serangan ---
    private final float attackCooldown = 1.0f; // Cooldown antar serangan (detik)
    private float attackTimer = 0f; // Timer untuk cooldown serangan
    private boolean hasAttackedInCurrentAnimation = false; // Flag untuk memastikan damage hanya sekali per animasi

    public EnemyA(float x, float y, Player target, GameScreen screen) {
        super(x, y, target, screen);

        this.speed = 120f;
        this.maxHealth = 75f;
        this.currentHealth = this.maxHealth;

        loadAnimations(); // Muat animasi di konstruktor

        // Setel bounds berdasarkan frame IDLE awal
        TextureRegion initialFrame = animations.get(EnemyState.IDLE).get(Direction.DOWN).getKeyFrame(0);
        this.bounds = new Rectangle(x, y, initialFrame.getRegionWidth(), initialFrame.getRegionHeight());

        this.currentState = EnemyState.IDLE;
        this.currentDirection = Direction.DOWN; // Inisialisasi arah awal
    }

    /**
     * Memuat semua animasi spesifik untuk EnemyA (idle, berjalan, menyerang).
     */
    private void loadAnimations() {
        // Animasi IDLE
        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11, 1, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11, 1, 0.25f)); // Seringkali up sama dengan left/down tapi di-flip atau menggunakan resource yang sama
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11, 1, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("skeleton/Idle_Skeleton_right.png", 11, 1, 0.25f));
        this.animations.put(EnemyState.IDLE, idleAnims);

        // Animasi CHASING (WALK)
        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("skeleton/Walk_Skeleton_left.png", 13, 1, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("skeleton/Walk_Skeleton_right.png", 13, 1, 0.15f)); // Pastikan ini benar untuk arah atas
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("skeleton/Walk_Skeleton_left.png", 13, 1, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("skeleton/Walk_Skeleton_right.png", 13, 1, 0.15f));
        this.animations.put(EnemyState.CHASING, walkAnims);

        // --- Tambahkan Animasi ATTACKING ---
        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();
        // Pastikan Anda memiliki sprite sheet untuk animasi serangan
        // Sesuaikan path, cols, rows, dan frameDuration sesuai aset Anda.
        // Animasi serangan umumnya PlayMode.NORMAL (sekali putar)
        Animation<TextureRegion> attackDown = loadAnimationFromSheet("skeleton/Attack_Skeleton_left.png", 18, 1, 0.08f);
        attackDown.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.DOWN, attackDown);

        Animation<TextureRegion> attackUp = loadAnimationFromSheet("skeleton/Attack_Skeleton_left.png", 18, 1, 0.08f); // Jika tidak ada aset khusus, bisa pakai left/right
        attackUp.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.UP, attackUp);

        Animation<TextureRegion> attackLeft = loadAnimationFromSheet("skeleton/Attack_Skeleton_left.png", 18, 1, 0.08f);
        attackLeft.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.LEFT, attackLeft);

        Animation<TextureRegion> attackRight = loadAnimationFromSheet("skeleton/Attack_Skeleton_right.png", 18, 1, 0.08f);
        attackRight.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, attackRight);

        this.animations.put(EnemyState.ATTACKING, attackAnims);
    }

    @Override
    protected void enterHurtState() {
        this.currentState = EnemyState.HURT;
        this.hurtTimer = hurtDuration;
        this.stateTimer = 0; // Reset state timer untuk memulai animasi hurt dari awal jika ada
    }

    /**
     * Metode utama untuk logika AI musuh. Dipanggil setiap update.
     *
     * @param delta Waktu dalam detik sejak frame terakhir.
     */
    @Override
    protected void updateAI(float delta) {
        // Update cooldown serangan terlepas dari state
        if (attackTimer > 0) {
            attackTimer -= delta;
        }

        switch (currentState) {
            case IDLE:
                if (isPlayerInRange(detectionRange)) {
                    currentState = EnemyState.CHASING;
                    stateTimer = 0; // Reset timer saat beralih state
                }
                break;

            case CHASING:
                chase(delta); // Lakukan pergerakan mengejar
                if (isPlayerInRange(attackRange) && attackTimer <= 0) { // Cek range dan cooldown
                    currentState = EnemyState.ATTACKING;
                    stateTimer = 0; // Reset timer untuk animasi serangan
                    hasAttackedInCurrentAnimation = false; // Reset flag serangan untuk animasi baru
                } else if (!isPlayerInRange(detectionRange * 1.2f)) { // Jika pemain terlalu jauh
                    currentState = EnemyState.IDLE;
                    stateTimer = 0;
                }
                break;

            case ATTACKING:
                // Dapatkan animasi serangan saat ini
                Animation<TextureRegion> currentAttackAnim = animations.get(EnemyState.ATTACKING).get(currentDirection);

                // --- Logika Memicu Damage ---
                // PENTING: Sesuaikan '0.5f' ini dengan titik di mana animasi serangan musuh mengenai target.
                // Ini adalah persentase waktu animasi. Jika animasi 1 detik, 0.5f berarti di 0.5 detik.
                float attackHitTime = currentAttackAnim.getAnimationDuration() * 0.5f;

                if (!hasAttackedInCurrentAnimation && stateTimer >= attackHitTime) {
                    // Hanya menyerang jika belum menyerang di animasi ini dan sudah mencapai 'titik pukul'
                    target.takeDamage(10f); // Contoh: Musuh memberikan 10 damage
                    hasAttackedInCurrentAnimation = true; // Set flag agar tidak menyerang lagi di animasi yang sama
                    Gdx.app.log("EnemyA", "Enemy attacked player for 10 damage!");
                }

                // Cek apakah animasi serangan sudah selesai
                if (currentAttackAnim.isAnimationFinished(stateTimer)) {
                    attackTimer = attackCooldown; // Mulai cooldown setelah serangan selesai
                    if (isPlayerInRange(attackRange)) { // Jika pemain masih dalam jangkauan
                        currentState = EnemyState.ATTACKING; // Langsung coba serang lagi (jika cooldown 0)
                        stateTimer = 0; // Reset timer untuk animasi serangan berikutnya
                        hasAttackedInCurrentAnimation = false; // Reset flag serangan
                    } else if (isPlayerInRange(detectionRange)) { // Jika pemain di luar jangkauan serang tapi masih terdeteksi
                        currentState = EnemyState.CHASING;
                        stateTimer = 0;
                    } else { // Jika pemain benar-benar jauh
                        currentState = EnemyState.IDLE;
                        stateTimer = 0;
                    }
                }

                break;

            case HURT:
                hurtTimer -= delta;
                if (hurtTimer <= 0) {
                    currentState = EnemyState.CHASING; // Kembali mengejar setelah pulih dari hurt
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
        // Menggunakan dst2 (jarak kuadrat) lebih efisien karena tidak melibatkan akar kuadrat.
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

        directionToTarget.nor(); // Normalisasi vektor arah
        position.mulAdd(directionToTarget, speed * delta); // Pindahkan musuh
    }
}
