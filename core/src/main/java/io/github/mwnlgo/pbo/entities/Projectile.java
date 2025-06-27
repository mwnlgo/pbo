package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.mwnlgo.pbo.Screens.GameScreen;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private float speed = 500f;
    private float damage;
    private GameScreen screen;
    private Rectangle bounds;
    private boolean active;
    private float rotation;

    // (DIUBAH) Atribut untuk animasi
    private Animation<TextureRegion> animation;
    private float stateTimer;


    public Projectile(float x, float y, Vector2 directionVector, GameScreen screen, float damageAmount) {
        this.position = new Vector2(x, y);
        this.screen = screen;
        this.active = true;
        this.damage = damageAmount;
        this.stateTimer = 0f;
        Gdx.app.log("Projectile", "Projectile created with " + this.damage + " damage.");

        // (DIUBAH) Muat animasi dari spritesheet
        this.animation = loadAnimationFromSheet("player/projectile_ayam.png", 2, 2, 0.1f);
        this.animation.setPlayMode(Animation.PlayMode.LOOP);

        this.velocity = new Vector2(directionVector).scl(speed);
        this.rotation = velocity.angleDeg();

        // (DIUBAH) Sesuaikan bounds dengan ukuran satu frame animasi
        TextureRegion frame = animation.getKeyFrame(0);
        this.bounds = new Rectangle(x, y, frame.getRegionWidth(), frame.getRegionHeight());
    }

    /**
     * (BARU) Helper method untuk memuat animasi dari spritesheet.
     */
    private Animation<TextureRegion> loadAnimationFromSheet(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] temp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames.add(temp[i][j]);
            }
        }
        return new Animation<>(frameDuration, frames);
    }

    public void update(float delta) {
        if (!active) return;

        // (DIUBAH) Lanjutkan timer animasi
        stateTimer += delta;

        position.mulAdd(velocity, delta);
        bounds.setPosition(position.x - bounds.width / 2f, position.y - bounds.height / 2f);

        // Periksa tabrakan dengan musuh
        Array<Enemy> allEnemies = screen.getAllEnemies();
        for (Enemy enemy : allEnemies) {
            if (active && enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(this.damage);
                active = false; // Nonaktifkan proyektil setelah mengenai target
                Gdx.app.log("Projectile", "Projectile hit enemy for " + this.damage + " damage!");
                break;
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
            // (DIUBAH) Dapatkan frame animasi saat ini
            TextureRegion currentFrame = animation.getKeyFrame(stateTimer, true);

            float frameWidth = currentFrame.getRegionWidth();
            float frameHeight = currentFrame.getRegionHeight();

            // Gunakan metode draw yang sama, tetapi dengan TextureRegion (currentFrame)
            batch.draw(
                currentFrame,
                position.x - frameWidth / 2f,
                position.y - frameHeight / 2f,
                frameWidth / 2f,  // Titik pusat rotasi X
                frameHeight / 2f, // Titik pusat rotasi Y
                frameWidth,
                frameHeight,
                1f, 1f, // Skala X dan Y
                rotation, // Rotasi sprite dalam derajat
                false
            );
        }
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Catatan: Metode dispose() sengaja dihapus.
     * Tekstur untuk animasi ini harus dikelola oleh kelas yang lebih tinggi
     * (misalnya GameScreen atau AssetManager) untuk menghindari error saat
     * beberapa proyektil aktif secara bersamaan.
     */

    public Rectangle getBounds() {
        return bounds;
    }


}
