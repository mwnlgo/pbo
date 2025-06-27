package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.components.EnemyMeleeAttack;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;
import io.github.mwnlgo.pbo.interfaces.IMeleeAttacker;

import java.util.HashMap;
import java.util.Map;

// Implementasikan interface IMeleeAttacker
public class EnemyB extends Enemy implements IMeleeAttacker {

    private final float detectionRange = 400f;
    private final float attackRange = 100f; // Demon punya jangkauan sedikit lebih jauh

    // Komponen yang akan mengelola logika serangan melee
    private EnemyMeleeAttack meleeAttack;

    private boolean hasAttackedThisPhase = false;

    public EnemyB(float x, float y, Player target, GameScreen screen) {
        // Panggil konstruktor kelas dasar dengan nilai spesifik untuk EnemyB
        super(x, y, target, screen,
            80f,   // maxHealth
            80f,   // speed
            80f,    // hitboxWidth
            80f,    // hitboxHeight
            -40f,   // hitboxOffsetX
            -80f);  // hitboxOffsetY

        // Inisialisasi komponen serangan dengan damage 15 dan durasi hitbox 0.5 detik
        this.meleeAttack = new EnemyMeleeAttack(this, 15f, 1f, "sound/DemonAttack.wav");

        loadAnimations();

        this.currentState = EnemyState.IDLE;
        this.currentDirection = Direction.DOWN;
        this.deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/DemonDead.wav"));

    }



    private void loadAnimations() {
        // Animasi IDLE
        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("demon/idle_demon_right.png", 6, 1, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("demon/idle_demon_left.png", 6, 1, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("demon/idle_demon_left.png", 6, 1, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("demon/idle_demon_right.png", 6, 1, 0.25f));
        this.animations.put(EnemyState.IDLE, idleAnims);

        // Animasi CHASING (WALK)
        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("demon/walk_demon_left.png", 12, 1, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("demon/walk_demon_right.png", 12, 1, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("demon/walk_demon_left.png", 12, 1, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("demon/walk_demon_right.png", 12, 1, 0.15f));
        this.animations.put(EnemyState.CHASING, walkAnims);

        // --- Tambahkan Animasi ATTACKING untuk Demon ---
        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();
        Animation<TextureRegion> attackLeft = loadAnimationFromSheet("demon/attack_demon_left.png", 15, 1, 0.1f);
        attackLeft.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.DOWN, attackLeft); // Asumsi animasi sama
        attackAnims.put(Direction.LEFT, attackLeft);

        Animation<TextureRegion> attackRight = loadAnimationFromSheet("demon/attack_demon_right.png", 15, 1, 0.1f);
        attackRight.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, attackRight);
        attackAnims.put(Direction.UP, attackLeft);
        this.animations.put(EnemyState.ATTACKING, attackAnims);

        // Animasi HURT
        Map<Direction, Animation<TextureRegion>> hurtAnims = new HashMap<>();
        Animation<TextureRegion> hurtAnim = loadAnimationFromSheet("demon/knockback_demon_left.png", 5, 1, 0.1f);
        hurtAnim.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.DOWN, hurtAnim);
        hurtAnims.put(Direction.LEFT, hurtAnim);

        Animation<TextureRegion> hurtRightAnim = loadAnimationFromSheet("demon/knockback_demon_right.png", 5, 1, 0.1f);
        hurtRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.RIGHT, hurtRightAnim);
        hurtAnims.put(Direction.UP, hurtRightAnim);

        this.animations.put(EnemyState.HURT, hurtAnims);

        // Animasi DEAD
        Map<Direction, Animation<TextureRegion>> deadAnims = new HashMap<>();
        Animation<TextureRegion> deadAnim = loadAnimationFromSheet("demon/death_demon_left_animation.png", 26, 1, 0.1f);
        deadAnim.setPlayMode(Animation.PlayMode.NORMAL);
        deadAnims.put(Direction.DOWN, deadAnim);
        deadAnims.put(Direction.LEFT, deadAnim);

        Animation<TextureRegion> deadRightAnim = loadAnimationFromSheet("demon/death_demon_right_animation.png", 26, 1, 0.1f);
        deadRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        deadAnims.put(Direction.RIGHT, deadRightAnim);
        deadAnims.put(Direction.UP, deadRightAnim);

        this.animations.put(EnemyState.DEAD, deadAnims);
    }

    @Override
    protected void updateAI(float delta) {
        // Update komponen serangan (untuk mengelola timer hitbox-nya)
        meleeAttack.update(delta);

        // Abaikan AI jika sedang dalam state non-aktif (HURT atau DEAD)
        if (currentState == EnemyState.HURT || currentState == EnemyState.DEAD) {
            // Periksa jika animasi HURT sudah selesai
            if (currentState == EnemyState.HURT && animations.get(currentState).get(currentDirection) != null && animations.get(currentState).get(currentDirection).isAnimationFinished(stateTimer)) {
                currentState = EnemyState.IDLE;
            }
            return;
        }

        updateDirection();

        // Logika state machine yang menggunakan komponen serangan
        switch (currentState) {
            case IDLE:
            case CHASING:
                if (isPlayerInRange(attackRange) && !meleeAttack.isActive()) {
                    currentState = EnemyState.ATTACKING;
                } else if (isPlayerInRange(detectionRange)) {
                    currentState = EnemyState.CHASING;
                    chase(delta);
                } else {
                    currentState = EnemyState.IDLE;
                }
                break;

            case ATTACKING:
                if (!hasAttackedThisPhase) {
                    meleeAttack.attack(); // ✅ hanya sekali
                    hasAttackedThisPhase = true;
                }

                Animation<TextureRegion> attackAnim = animations.get(EnemyState.ATTACKING).get(currentDirection);
                if (attackAnim != null && attackAnim.isAnimationFinished(stateTimer)) {
                    hasAttackedThisPhase = false; // reset untuk attack berikutnya

                    if (isPlayerInRange(detectionRange)) {
                        currentState = EnemyState.CHASING;
                    } else {
                        currentState = EnemyState.IDLE;
                    }
                }
                break;
        }
    }

    private boolean isPlayerInRange(float range) {
        return target.getPosition().dst2(this.position) < range * range;
    }

    private void chase(float delta) {
        // Berhenti bergerak sedikit sebelum mencapai jangkauan serangan
        if (!isPlayerInRange(attackRange * 0.8f)) {
            Vector2 directionToTarget = new Vector2(target.getPosition()).sub(this.position).nor();
            position.mulAdd(directionToTarget, speed * delta);
        }
    }


    private void updateDirection() {
        float dx = target.getPosition().x - this.position.x;
        float dy = target.getPosition().y - this.position.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            this.currentDirection = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            this.currentDirection = dy > 0 ? Direction.UP : Direction.DOWN;
        }
}
    @Override
    public EnemyMeleeAttack getMeleeAttack() {
        return this.meleeAttack;
    }
    @Override
    public void dispose() {
        super.dispose(); // Panggil dispose dari kelas dasar untuk membersihkan deathSound
        if (meleeAttack != null) {
            meleeAttack.dispose();
        }
    }
}
