package io.github.mwnlgo.pbo.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ItemDrop {
    private Vector2 position;
    private Rectangle bounds;
    private boolean isCollected;
    private float healAmount;
    private static final float SCALE_FACTOR = 2.3f;

    // --- Perubahan untuk Animasi ---
    private Animation<TextureRegion> animation;
    private Texture itemSheet; // Simpan referensi ke texture sheet untuk di-dispose
    private float stateTimer;
    // -----------------------------

    public ItemDrop(float x, float y) {
        this.position = new Vector2(x, y);
        this.isCollected = false;
        this.stateTimer = 0f;
        this.healAmount = 20f;

        // Ganti "item_spritesheet.png" dengan path ke gambar item Anda
        this.itemSheet = new Texture("item/healing_flask.png");

        // Load animasi dari spritesheet 2x2
        this.animation = loadAnimationFromSheet(this.itemSheet, 2, 2, 0.25f); // 0.25f adalah durasi per frame
        this.animation.setPlayMode(Animation.PlayMode.LOOP);

        // Atur bounds berdasarkan ukuran satu frame
        TextureRegion frame = animation.getKeyFrame(0);
        float scaledWidth = frame.getRegionWidth() * SCALE_FACTOR;
        float scaledHeight = frame.getRegionHeight() * SCALE_FACTOR;
        this.bounds = new Rectangle(x, y, scaledWidth, scaledHeight);

    }

    /**
     * Helper method untuk memuat animasi dari spritesheet.
     */
    private Animation<TextureRegion> loadAnimationFromSheet(Texture sheet, int cols, int rows, float frameDuration) {
        TextureRegion[][] tempFrames = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        Array<TextureRegion> frames = new Array<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                frames.add(tempFrames[row][col]);
            }
        }
        return new Animation<>(frameDuration, frames);
    }

    // Metode baru untuk mengupdate state animasi
    public void update(float delta) {
        stateTimer += delta;
    }

    public void render(SpriteBatch batch) {
        if (!isCollected) {
            // Dapatkan frame animasi saat ini
            TextureRegion currentFrame = animation.getKeyFrame(stateTimer, true);
            float scaledWidth = currentFrame.getRegionWidth() * SCALE_FACTOR;
            float scaledHeight = currentFrame.getRegionHeight() * SCALE_FACTOR;
            batch.draw(currentFrame, position.x, position.y, scaledWidth, scaledHeight);
            batch.draw(currentFrame, position.x, position.y);
        }



    }

    public void dispose() {
        // Dispose texture sheetnya
        if (itemSheet != null) {
            itemSheet.dispose();
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getHealAmount() { // <-- Tambahkan ini
        return healAmount;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }
}
