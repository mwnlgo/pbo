package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private float speed;
    private Texture texture;
    private Rectangle bounds;
    private boolean active; // Untuk menandai apakah proyektil ini masih aktif

    public Projectile(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.speed = 400f; // Kecepatan proyektil, misalnya 400 piksel/detik
        this.active = true;
        this.texture = new Texture("projectile.png"); // Ganti dengan gambar proyektil Anda

        this.bounds = new Rectangle(startX, startY, texture.getWidth(), texture.getHeight());
    }

    public void update(float delta) {
        if (!active) return;

        // Gerakkan proyektil sesuai kecepatan dan arahnya
        position.mulAdd(velocity, delta);
        bounds.setCenter(position);

        // TODO: Tambahkan logika untuk menonaktifkan proyektil
        // jika keluar dari layar atau mengenai sesuatu.
        // Contoh: if (position.x > Gdx.graphics.getWidth()) { active = false; }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(texture, bounds.x, bounds.y);
    }

    public void dispose() {
        texture.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getSpeed() {
        return speed;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
