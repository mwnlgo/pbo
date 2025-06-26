package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import io.github.mwnlgo.pbo.Screens.GameScreen;
import io.github.mwnlgo.pbo.enums.Direction;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private Texture texture;
    private float speed = 500f;
    private float damage; // DIUBAH: Tidak ada nilai default, akan diisi oleh konstruktor
    private GameScreen screen;
    private Rectangle bounds;
    private boolean active;

    /**
     * Konstruktor untuk proyektil.
     * @param x Posisi awal X.
     * @param y Posisi awal Y.
     * @param direction Arah proyektil.
     * @param screen Referensi ke GameScreen untuk interaksi dunia.
     * @param damageAmount Jumlah damage yang dibawa oleh proyektil ini.
     */
    public Projectile(float x, float y, Direction direction, GameScreen screen, float damageAmount) { // DIUBAH: Menambahkan damageAmount
        this.position = new Vector2(x, y);
        this.texture = new Texture("player/player_projectile.png"); // Ganti dengan path aset Anda
        this.screen = screen;
        this.active = true;

        // DIUBAH: Mengisi damage dari parameter konstruktor
        this.damage = damageAmount;
        Gdx.app.log("Projectile", "Projectile created with " + this.damage + " damage.");

        this.velocity = new Vector2();
        switch (direction) {
            case UP:    velocity.set(0, 1).scl(speed); break;
            case DOWN:  velocity.set(0, -1).scl(speed); break;
            case LEFT:  velocity.set(-1, 0).scl(speed); break;
            case RIGHT: velocity.set(1, 0).scl(speed); break;
        }

        this.bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void update(float delta) {
        if (!active) return;

        position.mulAdd(velocity, delta);
        bounds.setPosition(position.x - bounds.width / 2f, position.y - bounds.height / 2f);

        // Periksa tabrakan dengan musuh
        // Asumsi GameScreen memiliki metode untuk mendapatkan daftar musuh
        Array<Enemy> allEnemies = screen.getAllEnemies();
        for (Enemy enemy : allEnemies) {
            if (active && enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(this.damage); // Gunakan 'this.damage'
                active = false; // Proyektil non-aktif setelah mengenai musuh
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
