package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.enums.Direction;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class Projectile { // Nama kelas Projectile karena ini proyektil pemain
    private Vector2 position;
    private Vector2 velocity;
    private Texture texture;
    private float speed = 500f; // Kecepatan proyektil pemain
    private float damage = 25f; // Damage proyektil pemain
    private GameScreen screen;
    private Rectangle bounds;
    private boolean active; // Status aktif proyektil
    private Direction direction; // Arah proyektil

    public Projectile(float x, float y, Direction direction, GameScreen screen) {
        this.position = new Vector2(x, y);
        // Ganti dengan path aset proyektil pemain Anda
        this.texture = new Texture("player/player_projectile.png");
        this.screen = screen;
        this.active = true;
        this.direction = direction;

        // Tentukan kecepatan berdasarkan arah pemain
        this.velocity = new Vector2();
        switch (direction) {
            case UP:
                velocity.set(0, 1).scl(speed);
                break;
            case DOWN:
                velocity.set(0, -1).scl(speed);
                break;
            case LEFT:
                velocity.set(-1, 0).scl(speed);
                break;
            case RIGHT:
                velocity.set(1, 0).scl(speed);
                break;
        }

        this.bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void update(float delta) {
        if (!active) return;

        position.mulAdd(velocity, delta);
        bounds.setPosition(position.x - bounds.width / 2f, position.y - bounds.height / 2f); // Sesuaikan posisi bounds ke tengah

        // Periksa tabrakan dengan musuh
        // Penting: Anda perlu mengelola daftar semua musuh di GameScreen
        // Contoh: Iterasi melalui daftar musuh di GameScreen
        Array<Enemy> allEnemies = screen.getAllEnemies(); // Anda perlu menambahkan metode ini di GameScreen
        for (Enemy enemy : allEnemies) {
            if (active && enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage);
                active = false; // Proyektil non-aktif setelah mengenai musuh
                Gdx.app.log("Projectile", "Projectile hit enemy for " + damage + " damage!");
                break; // Hentikan iterasi setelah mengenai satu musuh
            }
        }

        // Hancurkan proyektil jika keluar layar
        if (position.x < -100 || position.x > screen.getWorldWidth() + 100 ||
            position.y < -100 || position.y > screen.getWorldHeight() + 100) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (active) {
            // Gambar proyektil di tengah posisinya
            batch.draw(texture, position.x - texture.getWidth() / 2f, position.y - texture.getHeight() / 2f);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
