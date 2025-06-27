package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.mwnlgo.pbo.Screens.GameScreen;

public class EnemyProjectile {
    private Vector2 position;
    private Vector2 velocity;
    private Animation<TextureRegion> animation;
    private float speed = 300f;
    private float damage;
    private GameScreen screen;
    private Player target;
    private Rectangle bounds;
    private boolean active = true;

    private float stateTime = 0f; // Untuk animasi

    private Texture texture;

    public EnemyProjectile(float x, float y, Vector2 targetPosition, float damage, GameScreen screen, Player playerTarget) {
        this.position = new Vector2(x, y);
        this.screen = screen;
        this.target = playerTarget;
        this.damage = damage;

        // Load texture sheet
        this.texture = new Texture("necro/attack_Necro_effect.png"); // 19 kolom x 1 baris

        // Potong menjadi frame-frame
        TextureRegion[][] tmp = TextureRegion.split(texture,
            texture.getWidth() / 19, // width per frame
            texture.getHeight());    // height per frame (1 baris)

        TextureRegion[] frames = new TextureRegion[19];
        for (int i = 0; i < 19; i++) {
            frames[i] = tmp[0][i];
        }

        animation = new Animation<>(0.05f, frames); // 0.05s per frame
        animation.setPlayMode(Animation.PlayMode.LOOP); // bisa LOOP or NORMAL

        // Hitung arah ke target
        Vector2 direction = new Vector2(targetPosition).sub(position).nor();
        this.velocity = direction.scl(speed);

        bounds = new Rectangle(x, y, frames[0].getRegionWidth(), frames[0].getRegionHeight());
    }

    public void update(float delta) {
        if (!active) return;

        // Update posisi
        position.mulAdd(velocity, delta);
        bounds.setPosition(position.x, position.y);

        // Update animasi
        stateTime += delta;

        // Deteksi tabrakan
        if (bounds.overlaps(target.getBounds())) {
            target.takeDamage(damage);
            active = false;
            Gdx.app.log("EnemyProjectile", "Hit player!");
        }

        // Hapus jika keluar layar
        if (position.x < -100 || position.x > screen.getWorldWidth() + 100 ||
            position.y < -100 || position.y > screen.getWorldHeight() + 100) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        TextureRegion currentFrame = animation.getKeyFrame(stateTime);
        batch.draw(currentFrame,
            position.x + 33 - currentFrame.getRegionWidth() / 2f,
            position.y + 20 - currentFrame.getRegionHeight() / 2f);
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }



    public float getDamage() {
        return damage;
    }

    public void setActive(boolean b) {
        this.active = b;
    }
}
