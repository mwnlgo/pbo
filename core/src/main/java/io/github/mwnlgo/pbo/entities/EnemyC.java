package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.components.EnemyProjectileAttack;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;

import java.util.HashMap;
import java.util.Map;

public class EnemyC extends Enemy {

    private final float detectionRange = 450f;
    private final float preferredAttackDistance = 250f;
    private final float stopMovingDistance = 200f;

    private EnemyProjectileAttack projectileAttack;

    public EnemyC(float x, float y, Player target, GameScreen screen) {
        super(x, y, target, screen,
            60f,  // maxHealth
            90f,  // speed
            32f,  // hitboxWidth
            48f,  // hitboxHeight
            -16f, // hitboxOffsetX
            -24f); // hitboxOffsetY

        this.projectileAttack = new EnemyProjectileAttack(this, 15f, 3.0f);
        loadAnimations();
        this.currentState = EnemyState.IDLE;
        this.currentDirection = Direction.DOWN;
    }

    private void loadAnimations() {
        // (Kode loadAnimations Anda sudah baik, tidak perlu diubah)
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
        attackAnims.put(Direction.LEFT, castLeft);
        Animation<TextureRegion> castRight = loadAnimationFromSheet("necro/attacking_Necro_right_animation.png", 13, 1, 0.12f);
        castRight.setPlayMode(Animation.PlayMode.NORMAL);
        attackAnims.put(Direction.RIGHT, castRight);
        attackAnims.put(Direction.UP, castRight);
        this.animations.put(EnemyState.ATTACKING, attackAnims);
        Map<Direction, Animation<TextureRegion>> hurtAnims = new HashMap<>();
        Animation<TextureRegion> hurtAnim = loadAnimationFromSheet("necro/knockback_Necro_Left.png", 5, 1, 0.1f);
        hurtAnim.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.DOWN, hurtAnim);
        hurtAnims.put(Direction.LEFT, hurtAnim);
        Animation<TextureRegion> hurtRightAnim = loadAnimationFromSheet("necro/knockback_Necro_Right.png", 5, 1, 0.1f);
        hurtRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        hurtAnims.put(Direction.RIGHT, hurtRightAnim);
        hurtAnims.put(Direction.UP, hurtRightAnim);
        this.animations.put(EnemyState.HURT, hurtAnims);
        Map<Direction, Animation<TextureRegion>> deadAnims = new HashMap<>();
        Animation<TextureRegion> deadAnim = loadAnimationFromSheet("necro/death_Necro_left_animation.png", 10, 1, 0.1f);
        deadAnim.setPlayMode(Animation.PlayMode.NORMAL);
        deadAnims.put(Direction.DOWN, deadAnim);
        deadAnims.put(Direction.LEFT, deadAnim);
        Animation<TextureRegion> deadRightAnim = loadAnimationFromSheet("necro/death_Necro_right_animation.png", 10, 1, 0.1f);
        deadRightAnim.setPlayMode(Animation.PlayMode.NORMAL);
        deadAnims.put(Direction.RIGHT, deadRightAnim);
        deadAnims.put(Direction.UP, deadRightAnim);
        this.animations.put(EnemyState.DEAD, deadAnims);
    }

    @Override
    protected void updateAI(float delta) {
        // (PERBAIKAN) Logika untuk state non-aktif (HURT/DEAD)
        if (currentState == EnemyState.HURT) {
            // Cek jika animasi HURT sudah selesai
            Animation<TextureRegion> hurtAnimation = animations.get(EnemyState.HURT).get(currentDirection);
            if (hurtAnimation != null && hurtAnimation.isAnimationFinished(stateTimer)) {
                // Kembali ke state IDLE setelah animasi selesai
                currentState = EnemyState.IDLE;
            }
            return; // Hentikan logika AI lainnya jika sedang hurt
        }

        if (currentState == EnemyState.DEAD) {
            // Animasi kematian akan terus diputar.
            // GameScreen akan memeriksa isDeathAnimationFinished() untuk menghapus musuh.
            return; // Hentikan semua logika AI jika sudah mati.
        }


        // Logika AI normal untuk state lainnya
        float distanceToPlayer = target.getPosition().dst(this.position);

        switch (currentState) {
            case IDLE:
            case CHASING:
                updateMovement(distanceToPlayer, delta);
                if (distanceToPlayer <= preferredAttackDistance + 50 && distanceToPlayer >= stopMovingDistance - 50 && projectileAttack.isReady()) {
                    currentState = EnemyState.ATTACKING;
                }
                break;

            case ATTACKING:
                projectileAttack.attack();
                Animation<TextureRegion> attackAnim = animations.get(EnemyState.ATTACKING).get(currentDirection);
                if (attackAnim != null && attackAnim.isAnimationFinished(stateTimer)) {
                    currentState = EnemyState.CHASING;
                }
                break;
        }
    }

    /**
     * (BARU) Metode untuk memeriksa apakah animasi kematian sudah selesai.
     * @return true jika animasi DEAD selesai, false sebaliknya.
     */
    public boolean isDeathAnimationFinished() {
        if (currentState != EnemyState.DEAD) return false;
        Animation<TextureRegion> deadAnimation = animations.get(EnemyState.DEAD).get(currentDirection);
        if (deadAnimation == null) return true; // Jika tidak ada animasi, anggap selesai
        return deadAnimation.isAnimationFinished(stateTimer);
    }

    private void updateMovement(float distanceToPlayer, float delta) {
        if (distanceToPlayer < stopMovingDistance) {
            retreat(delta);
        } else if (distanceToPlayer > preferredAttackDistance) {
            chase(delta);
        } else {
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
