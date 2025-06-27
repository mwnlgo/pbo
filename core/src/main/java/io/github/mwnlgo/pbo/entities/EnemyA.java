package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.components.EnemyMeleeAttack;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;
import io.github.mwnlgo.pbo.interfaces.IMeleeAttacker; // (BARU) Import interface

import java.util.HashMap;
import java.util.Map;

// (PERUBAHAN) Tambahkan 'implements IMeleeAttacker'
public class EnemyA extends Enemy implements IMeleeAttacker {

    private final float detectionRange = 400f;
    private final float attackRange = 80f; // Jangkauan untuk mulai menyerang

    // Komponen yang akan mengelola logika serangan melee
    private EnemyMeleeAttack meleeAttack;

    private boolean hasAttackedThisPhase = false;


    public EnemyA(float x, float y, Player target, GameScreen screen) {
        // Panggil konstruktor kelas dasar dengan nilai spesifik untuk EnemyA
        super(x, y, target, screen,
            50f,    // maxHealth
            120f,   // speed
            32f,    // hitboxWidth
            48f,    // hitboxHeight
            -16f,   // hitboxOffsetX
            0f);  // hitboxOffsetY

        // Inisialisasi komponen serangan dengan damage dan durasi hitbox
        this.meleeAttack = new EnemyMeleeAttack(this, 10f, 0.4f, "sound/DemonAttack.wav", 0.35f, 0.2f);

        loadAnimations(); // Muat animasi spesifik untuk EnemyA

        this.currentState = EnemyState.IDLE;
        this.currentDirection = Direction.DOWN;

        this.deathSound = Gdx.audio.newSound(Gdx.files.internal("sound/SkeletonDead.wav"));
    }

    private void loadAnimations() {
        // Animasi IDLE
        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11, 1, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11, 1, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11, 1, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("skeleton/Idle_Skeleton_right.png", 11, 1, 0.25f));
        this.animations.put(EnemyState.IDLE, idleAnims);

        // Animasi CHASING
        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("skeleton/Walk_Skeleton_left.png", 13, 1, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("skeleton/Walk_Skeleton_right.png", 13, 1, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("skeleton/Walk_Skeleton_left.png", 13, 1, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("skeleton/Walk_Skeleton_right.png", 13, 1, 0.15f));
        this.animations.put(EnemyState.CHASING, walkAnims);

        // Animasi ATTACKING
        Map<Direction, Animation<TextureRegion>> attackAnims = new HashMap<>();
        Animation<TextureRegion> attackAnim = loadAnimationFromSheet("skeleton/Attack_Skeleton_left.png", 18, 1, 0.08f);
        attackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.DOWN, attackAnim);
        attackAnims.put(Direction.UP, attackAnim);
        attackAnims.put(Direction.LEFT, attackAnim);
        Animation<TextureRegion> attackRightAnim = loadAnimationFromSheet("skeleton/Attack_Skeleton_right.png", 18, 1, 0.08f);
        attackRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, attackRightAnim);
        this.animations.put(EnemyState.ATTACKING, attackAnims);

        // Animasi HURT
        Map<Direction, Animation<TextureRegion>> hurtAnims = new HashMap<>();
        Animation<TextureRegion> hurtAnim = loadAnimationFromSheet("skeleton/Hit_Skeleton_left.png", 8, 1, 0.1f);
        hurtAnim.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.DOWN, hurtAnim);
        hurtAnims.put(Direction.UP, hurtAnim);
        hurtAnims.put(Direction.LEFT, hurtAnim);
        Animation<TextureRegion> hurtRightAnim = loadAnimationFromSheet("skeleton/Hit_Skeleton_right.png", 8, 1, 0.1f);
        hurtRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.RIGHT, hurtRightAnim);
        this.animations.put(EnemyState.HURT, hurtAnims);

        // Animasi DEAD
        Map<Direction, Animation<TextureRegion>> deadAnims = new HashMap<>();
        Animation<TextureRegion> deadAnim = loadAnimationFromSheet("skeleton/Dead_Skeleton_left_animation.png", 21, 1, 0.1f);
        deadAnim.setPlayMode(Animation.PlayMode.NORMAL);
        deadAnims.put(Direction.DOWN, deadAnim);
        deadAnims.put(Direction.UP, deadAnim);
        deadAnims.put(Direction.LEFT, deadAnim);
        Animation<TextureRegion> deadRightAnim = loadAnimationFromSheet("skeleton/Dead_Skeleton_right_animation.png", 21, 1, 0.1f);
        deadRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        deadAnims.put(Direction.RIGHT, deadRightAnim);
        this.animations.put(EnemyState.DEAD, deadAnims);
    }

    @Override
    protected void updateAI(float delta) {
        meleeAttack.update(delta);

        if (currentState == EnemyState.HURT || currentState == EnemyState.DEAD) {
            if (currentState == EnemyState.HURT && animations.get(currentState).get(currentDirection).isAnimationFinished(stateTimer)) {
                currentState = EnemyState.IDLE;
            }
            return;
        }

        updateDirection();

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
                    meleeAttack.attack();
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
        Vector2 directionToTarget = new Vector2(target.getPosition()).sub(this.position).nor();
        position.mulAdd(directionToTarget, speed * delta);
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

    /**
     * Implementasi metode dari interface IMeleeAttacker.
     * Ini penting agar GameScreen bisa mendapatkan komponen serangan musuh ini.
     *
     * @return Komponen serangan melee musuh.
     */
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
