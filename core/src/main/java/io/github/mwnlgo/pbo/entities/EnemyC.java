package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.components.EnemyProjectileAttack; // (BARU) Import komponen baru
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;

import java.util.HashMap;
import java.util.Map;

public class EnemyC extends Enemy {

    private final float detectionRange = 450f;
    private final float preferredAttackDistance = 250f;
    private final float stopMovingDistance = 200f;

    // (BARU) Komponen serangan proyektil
    private EnemyProjectileAttack projectileAttack;

    public EnemyC(float x, float y, Player target, GameScreen screen) {
        // Panggil konstruktor kelas dasar dengan nilai spesifik untuk Necromancer
        super(x, y, target, screen,
            60f,  // maxHealth
            90f,  // speed
            32f,  // hitboxWidth
            48f,  // hitboxHeight
            -16f, // hitboxOffsetX
            -24f); // hitboxOffsetY

        // (BARU) Inisialisasi komponen serangan proyektil
        // dengan damage 15 dan cooldown 2 detik.
        this.projectileAttack = new EnemyProjectileAttack(this, 15f, 2.0f);

        loadAnimations(); // Muat animasi spesifik EnemyC

        this.currentState = EnemyState.IDLE;
        this.currentDirection = Direction.DOWN;
    }

    // Metode loadAnimations() Anda sudah baik dan tidak perlu diubah.
    private void loadAnimations() {
        // ... (kode loadAnimations Anda yang sudah ada) ...
        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("necro/idle_Necro_Left.png", 8, 1, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("necro/idle_Necro_Right.png", 8, 1, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("necro/idle_Necro_Left.png", 8, 1, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("necro/idle_Necro_Right.png", 8, 1, 0.25f));
        this.animations.put(EnemyState.IDLE, idleAnims);
        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("necro/walk_Necro_Left.png", 8, 1, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("necro/walk_Necro_Right.png", 8, 1, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("necro/walk_Necro_Left.png", 8, 1, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("necro/walk_Necro_Right.png", 8, 1, 0.15f));
        this.animations.put(EnemyState.CHASING, walkAnims);
        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();
        Animation<TextureRegion> castLeft = loadAnimationFromSheet("necro/attacking_Necro_left_animation.png", 13, 1, 0.12f);
        castLeft.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.DOWN, castLeft);
        attackAnims.put(Direction.UP, castLeft); // Maybe use right animation?
        attackAnims.put(Direction.LEFT, castLeft);
        Animation<TextureRegion> castRight = loadAnimationFromSheet("necro/attacking_Necro_right_animation.png", 13, 1, 0.12f);
        castRight.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, castRight);
        this.animations.put(EnemyState.ATTACKING, attackAnims);
    }

    @Override
    protected void updateAI(float delta) {
        // Abaikan AI jika sedang dalam state non-aktif (HURT atau DEAD)
        if (currentState == EnemyState.HURT || currentState == EnemyState.DEAD) {
            // ... (logika HURT/DEAD Anda sudah baik) ...
            return;
        }

        float distanceToPlayer = target.getPosition().dst(this.position);

        // (PERUBAHAN) State machine sekarang menggunakan komponen serangan
        switch (currentState) {
            case IDLE:
            case CHASING:
                updateMovement(distanceToPlayer, delta); // Pindahkan logika gerak ke metode terpisah
                // Cek apakah bisa menyerang
                if (distanceToPlayer <= preferredAttackDistance + 50 && distanceToPlayer >= stopMovingDistance - 50 && projectileAttack.isReady()) {
                    currentState = EnemyState.ATTACKING;
                }
                break;

            case ATTACKING:
                // Perintahkan komponen untuk meluncurkan proyektil (jika cooldown selesai)
                projectileAttack.attack();

                // Setelah animasi casting selesai, kembali ke state lain
                if (animations.get(EnemyState.ATTACKING).get(currentDirection).isAnimationFinished(stateTimer)) {
                    currentState = EnemyState.CHASING;
                }
                break;
        }
    }

    /**
     * Mengatur pergerakan Necromancer (mengejar atau mundur).
     */
    private void updateMovement(float distanceToPlayer, float delta) {
        if (distanceToPlayer < stopMovingDistance) {
            retreat(delta); // Terlalu dekat, mundur
        } else if (distanceToPlayer > preferredAttackDistance) {
            chase(delta); // Terlalu jauh, maju
        } else {
            // Jarak ideal, berhenti bergerak dan hadap pemain
            updateDirectionToTarget();
        }
    }

    private void chase(float delta) {
        updateDirectionToTarget();
        Vector2 direction = new Vector2(target.getPosition()).sub(this.position).nor();
        position.mulAdd(direction, speed * delta);
    }

    private void retreat(float delta) {
        updateDirectionToTarget();
        Vector2 direction = new Vector2(this.position).sub(target.getPosition()).nor();
        position.mulAdd(direction, speed * delta);
    }

    private void updateDirectionToTarget() {
        if (target.getPosition().x < this.position.x) {
            this.currentDirection = Direction.LEFT;
        } else {
            this.currentDirection = Direction.RIGHT;
        }
    }
}
