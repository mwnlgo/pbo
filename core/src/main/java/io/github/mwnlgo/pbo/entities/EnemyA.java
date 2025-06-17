package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.enums.Direction;
import io.github.mwnlgo.pbo.enums.EnemyState;

import java.util.HashMap;
import java.util.Map;

public class EnemyA extends Enemy{

    private final float detectionRange = 450f;
    private final float attackRange = 50f;
    private float hurtDuration = 0.2f;
    private float hurtTimer = 0f;

    public EnemyA(float x, float y, Player target, GameScreen screen) {
        super(x, y, target, screen);

        this.speed = 120f;
        this.maxHealth = 75f;
        this.currentHealth = this.maxHealth;

        loadAnimations(); // "Isi kaset" ke "mesin" parent

        TextureRegion initialFrame = animations.get(EnemyState.IDLE).get(Direction.DOWN).getKeyFrame(0);
        this.bounds = new Rectangle(x, y, initialFrame.getRegionWidth(), initialFrame.getRegionHeight());

        this.currentState = EnemyState.IDLE;
    }

    private void loadAnimations() {

        Map<Direction, Animation<TextureRegion>> idleAnims = new HashMap<>();
        idleAnims.put(Direction.DOWN, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11,1, 0.25f));
        idleAnims.put(Direction.UP, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11,1, 0.25f));
        idleAnims.put(Direction.LEFT, loadAnimationFromSheet("skeleton/Idle_Skeleton_left.png", 11,1, 0.25f));
        idleAnims.put(Direction.RIGHT, loadAnimationFromSheet("skeleton/Idle_Skeleton_right.png", 11, 1, 0.25f));
        this.animations.put(EnemyState.IDLE, idleAnims);

        Map<Direction, Animation<TextureRegion>> walkAnims = new HashMap<>();
        walkAnims.put(Direction.DOWN, loadAnimationFromSheet("skeleton/Walk_Skeleton_left.png", 13, 1, 0.15f));
        walkAnims.put(Direction.UP, loadAnimationFromSheet("skeleton/Walk_Skeleton_right.png", 13, 1, 0.15f));
        walkAnims.put(Direction.LEFT, loadAnimationFromSheet("skeleton/Walk_Skeleton_left.png", 13, 1, 0.15f));
        walkAnims.put(Direction.RIGHT, loadAnimationFromSheet("skeleton/Walk_Skeleton_right.png", 13, 1, 0.15f));
        this.animations.put(EnemyState.CHASING, walkAnims);
    }

    @Override
    protected void enterHurtState() {
        this.currentState = EnemyState.HURT;
        this.hurtTimer = hurtDuration;
    }

    @Override
    protected void updateAI(float delta) {
        switch (currentState) {
            case IDLE:
                if (isPlayerInRange(detectionRange)) currentState = EnemyState.CHASING;
                break;
            case CHASING:
                chase(delta);
                if (isPlayerInRange(attackRange)) currentState = EnemyState.ATTACKING;
                else if (!isPlayerInRange(detectionRange * 1.2f)) currentState = EnemyState.IDLE;
                break;
            case ATTACKING:
                if (!isPlayerInRange(attackRange)) currentState = EnemyState.CHASING;
                // TODO: Logika serangan Melee
                break;
            case HURT:
                hurtTimer -= delta;
                if (hurtTimer <= 0) currentState = EnemyState.CHASING;
                break;
            case DEAD:
                // Tidak melakukan apa-apa, menunggu GameScreen menghapusnya
                break;
        }
    }

    private boolean isPlayerInRange(float range) {
        return target.getPosition().dst2(this.position) < range * range;
    }

    private void chase(float delta) {
        Vector2 directionToTarget = new Vector2(target.getPosition()).sub(this.position);

        if (Math.abs(directionToTarget.x) > Math.abs(directionToTarget.y)) {
            currentDirection = directionToTarget.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            currentDirection = directionToTarget.y > 0 ? Direction.UP : Direction.DOWN;
        }

        directionToTarget.nor();
        position.mulAdd(directionToTarget, speed * delta);
    }
}
