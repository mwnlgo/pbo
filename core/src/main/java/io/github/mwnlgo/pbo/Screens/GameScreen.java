package io.github.mwnlgo.pbo.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.mwnlgo.pbo.Main;
import io.github.mwnlgo.pbo.components.EnemyMeleeAttack;
import io.github.mwnlgo.pbo.components.PlayerAttackComponent;
import io.github.mwnlgo.pbo.components.PlayerMeleeAttack;
import io.github.mwnlgo.pbo.entities.*;
import io.github.mwnlgo.pbo.interfaces.IMeleeAttacker;


public class GameScreen implements Screen {

    private int currentWave = 0;
    private int score = 0;
    private float waveTimer = 0f;
    private float waveInterval = 8f;
    private boolean waitingNextWave = false;
    private BitmapFont waveFont, scoreFont;

    private Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private float worldWidth, worldHeight;

    private Player player;
    private Array<Enemy> allEnemies;
    private Array<Projectile> playerProjectiles;
    private Array<EnemyProjectile> enemyProjectiles;

    private Animation<TextureRegion> playerProjectileAnimation;

    private ShapeRenderer shapeRenderer;
    private Array<EnemyMeleeAttack> hitByEnemyAttacks;

    // (DIUBAH) Aset untuk UI Health Bar
    private Texture healthBarSheet;
    private TextureRegion[] healthBarFrames;

    private Music backgroundMusic;

    private Music loseMusic;

    private Array<ItemDrop> droppedItems;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        this.worldWidth = Gdx.graphics.getWidth();
        this.worldHeight = Gdx.graphics.getHeight();
        allEnemies = new Array<>();
        playerProjectiles = new Array<>();
        droppedItems = new Array<>();
        enemyProjectiles = new Array<>();
        hitByEnemyAttacks = new Array<>();

    }

    @Override
    public void show() {
        // Inisialisasi font menggunakan FreeTypeFontGenerator
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/Jersey25-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50; // Ukuran font yang diinginkan
        waveFont = generator.generateFont(parameter); // Buat font untuk wave
        scoreFont = generator.generateFont(parameter); // Buat font untuk skor
        generator.dispose(); // Buang generator setelah font dibuat

        shapeRenderer = new ShapeRenderer();
        playerProjectileAnimation = loadAnimationFromSheet("player/projectile_ayam.png", 4, 1, 0.1f);

        // (BARU) Memuat spritesheet health bar dengan 7 frame
        healthBarSheet = new Texture("UI/health_bar.png");
        TextureRegion[][] tempFrames = TextureRegion.split(healthBarSheet, healthBarSheet.getWidth() / 7, healthBarSheet.getHeight());
        healthBarFrames = new TextureRegion[7];
        for (int i = 0; i < 7; i++) {
            healthBarFrames[i] = tempFrames[0][i];
        }

        player = new Player(100, 100, this);
        spawnEnemies();

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/GameScreen.ogg"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.2f);
        backgroundMusic.play();

        loseMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/LosingMusic.wav"));
        loseMusic.setVolume(1f);

        Gdx.input.setInputProcessor(null);
    }

    private void spawnWave(int waveNumber) {
        int enemyCount = 3 + waveNumber * 2;
        for (int i = 0; i < enemyCount; i++) {
            float x = 100 + (float)Math.random() * (worldWidth - 200);
            float y = 100 + (float)Math.random() * (worldHeight - 200);

            int type = (int)(Math.random() * 3);
            switch (type) {
                case 0: allEnemies.add(new EnemyA(x, y, player, this)); break;
                case 1: allEnemies.add(new EnemyB(x, y, player, this)); break;
                case 2: allEnemies.add(new EnemyC(x, y, player, this)); break;
            }
        }
        Gdx.app.log("GameScreen", "Wave " + waveNumber + " started with " + enemyCount + " enemies.");
    }

    private void spawnEnemies() {
        allEnemies.add(new EnemyA(300, 300, player, this));
        allEnemies.add(new EnemyA(500, 200, player, this));
        allEnemies.add(new EnemyB(700, 400, player, this));
        allEnemies.add(new EnemyC(1000, 100, player, this));
        allEnemies.add(new EnemyC(800, 600, player, this));
    }

    public void update(float delta) {
        if (!waitingNextWave && allEnemies.size == 0) {
            waitingNextWave = true;
            waveTimer = 0f;
        }

        if (waitingNextWave) {
            waveTimer += delta;
            if (waveTimer >= waveInterval) {
                currentWave++;
                spawnWave(currentWave);
                waitingNextWave = false;
            }
        }

        if (!player.isAlive()) {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.stop();
                loseMusic.play();
            }
            return;
        }

        player.update(delta);

        // (PERBAIKAN) Logika menghapus musuh yang sudah mati disederhanakan
        for (int i = allEnemies.size - 1; i >= 0; i--) {
            Enemy enemy = allEnemies.get(i);
            enemy.update(delta);
            // Cukup panggil method dari kelas dasar Enemy. Ini berlaku untuk semua jenis musuh.
            if (!enemy.isAlive() && enemy.isDeathAnimationFinished()) {
                enemy.dispose();
                allEnemies.removeIndex(i);
                addScore();
            }
        }

        for (int i = droppedItems.size - 1; i >= 0; i--) {
            ItemDrop item = droppedItems.get(i);
            item.update(delta); // <-- TAMBAHKAN BARIS INI untuk menjalankan animasinya

            if (player.getBounds().overlaps(item.getBounds())) {
                item.setCollected(true);
                Gdx.app.log("GameScreen", "Item collected!");
                player.heal(item.getHealAmount()); // Misalnya, menambah HP pemain
                item.dispose();
                droppedItems.removeIndex(i);
            }
        }

        for (int i = playerProjectiles.size - 1; i >= 0; i--) {
            Projectile p = playerProjectiles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                playerProjectiles.removeIndex(i);
            }
        }

        for (int i = enemyProjectiles.size - 1; i >= 0; i--) {
            EnemyProjectile p = enemyProjectiles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                p.dispose();
                enemyProjectiles.removeIndex(i);
            }
        }

        checkCollisions();

        // Membatasi agar kamera tidak keluar dari world
        float halfViewportWidth = camera.viewportWidth / 2f;
        float halfViewportHeight = camera.viewportHeight / 2f;

        float camX = Math.max(halfViewportWidth, Math.min(player.getPosition().x, worldWidth - halfViewportWidth));
        float camY = Math.max(halfViewportHeight, Math.min(player.getPosition().y, worldHeight - halfViewportHeight));

        camera.position.set(camX, camY, 0);
        camera.update();
    }

    public void spawnPlayerProjectile(Projectile projectile) {
        playerProjectiles.add(projectile);
    }

    public void addEnemyProjectile(EnemyProjectile projectile) {
        if (projectile != null) {
            this.enemyProjectiles.add(projectile);
        }
    }

    public void spawnItemDrop(float x, float y) {
        droppedItems.add(new ItemDrop(x, y));
        Gdx.app.log("GameScreen", "Item dropped at: " + x + ", " + y);
    }



    private void checkCollisions() {
        PlayerAttackComponent playerAttack = player.getCurrentAttack();
        if (playerAttack.isActive() && playerAttack instanceof PlayerMeleeAttack) {
            Rectangle attackHitbox = playerAttack.getAttackHitbox();
            for (Enemy enemy : allEnemies) {
                if (enemy.isAlive() && !playerAttack.hasHit(enemy) && attackHitbox.overlaps(enemy.getBounds())) {
                    enemy.takeDamage(playerAttack.getDamageAmount());
                    playerAttack.addHitEntity(enemy);
                }
            }
        }

        for (EnemyProjectile projectile : enemyProjectiles) {
            if (projectile.isActive() && projectile.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(projectile.getDamage());
                projectile.setActive(false);
            }
        }

        for (Enemy enemy : allEnemies) {
            if (enemy instanceof IMeleeAttacker) {
                EnemyMeleeAttack enemyAttack = ((IMeleeAttacker) enemy).getMeleeAttack();

                // --- INILAH PERUBAHAN YANG PALING PENTING ---
                if (enemyAttack.isHitboxLive() && !hitByEnemyAttacks.contains(enemyAttack, true) && enemyAttack.getAttackHitbox().overlaps(player.getBounds())) {
                    player.takeDamage(enemyAttack.getDamageAmount());

                    hitByEnemyAttacks.add(enemyAttack);
                }
                if (!enemyAttack.isActive() && hitByEnemyAttacks.contains(enemyAttack, true)) {
                    hitByEnemyAttacks.removeValue(enemyAttack, true);
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Langkah 1: Gambar Dunia Game (menggunakan kamera yang bergerak) ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Enemy enemy : allEnemies) {
            enemy.render(batch);
        }
        player.render(batch); // Render pemain bahkan jika mati, untuk animasi kematian
        for (Projectile p : playerProjectiles) {
            p.render(batch);
        }
        for (EnemyProjectile p : enemyProjectiles) {
            p.render(batch);
        }
        for (ItemDrop item : droppedItems) { // <-- Tambahkan ini
            item.render(batch);
        }

        batch.end();

        // --- Langkah 2: Gambar UI / HUD (menggunakan kamera statis dari viewport) ---
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Logika untuk memilih frame health bar yang benar
        float healthPercentage = player.getHealth() / player.getMaxHealth();
        int frameIndex;

        if (healthPercentage <= 0) {
            frameIndex = 6; // Frame ke-7 (index 6) adalah untuk kondisi mati
        } else if (healthPercentage == 1) {
            frameIndex = 0; // Frame pertama (index 0) untuk HP penuh
        } else {
            // Map persentase HP (0-100%) ke 5 frame sisanya (index 1-5)
            frameIndex = 6 - (int) Math.ceil(healthPercentage * 6);
            frameIndex = Math.max(1, Math.min(5, frameIndex)); // Pastikan index antara 1 dan 5
        }

        TextureRegion currentHealthFrame = healthBarFrames[frameIndex];
        float barX = player.getPosition().x - currentHealthFrame.getRegionWidth() / 2f;
        float barY = player.getPosition().y + player.getBounds().height + 10f;

        batch.draw(currentHealthFrame, barX, barY);
        batch.end();

        batch.begin();
        waveFont.draw(batch, "Wave " + currentWave, 50, viewport.getWorldHeight() - 50);
        scoreFont.draw(batch, "Score: " + score, viewport.getWorldWidth() - 220, viewport.getWorldHeight() - 50);
        batch.end();

        // --- Langkah 3: Gambar Bentuk Debug (opsional) ---
        drawDebugShapes();
    }

    // Metode untuk memperbarui skor ketika musuh mati
    private void addScore() {
        // Log setiap kali skor bertambah
        Gdx.app.log("GameScreen", "Score updated: " + ++score);
    }

    private void drawDebugShapes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.RED);
        if (player.isAlive()) {
            shapeRenderer.rect(player.getBounds().x, player.getBounds().y, player.getBounds().width, player.getBounds().height);
        }

        PlayerAttackComponent playerAttack = player.getCurrentAttack();
        if (playerAttack.isActive() && playerAttack instanceof PlayerMeleeAttack) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(playerAttack.getAttackHitbox().x, playerAttack.getAttackHitbox().y, playerAttack.getAttackHitbox().width, playerAttack.getAttackHitbox().height);
        }

        for (Enemy enemy : allEnemies) {
            // Gambar hitbox dasar musuh
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.rect(enemy.getBounds().x, enemy.getBounds().y, enemy.getBounds().width, enemy.getBounds().height);

            // Gambar hitbox serangan musuh dengan logika warna baru
            if (enemy instanceof IMeleeAttacker) {
                EnemyMeleeAttack enemyAttack = ((IMeleeAttacker) enemy).getMeleeAttack();

                // Jika hitbox serangan sedang LIVE, gambar kotak MERAH
                if (enemyAttack.isHitboxLive()) {
                    shapeRenderer.setColor(Color.RED); // Warna untuk hitbox aktif
                    shapeRenderer.rect(enemyAttack.getAttackHitbox().x, enemyAttack.getAttackHitbox().y, enemyAttack.getAttackHitbox().width, enemyAttack.getAttackHitbox().height);
                }
                // Jika animasi serangan berjalan TAPI hitbox BELUM live, gambar KUNING
                else if (enemyAttack.isActive()) {
                    shapeRenderer.setColor(Color.YELLOW); // Warna untuk masa wind-up/persiapan
                    // Kita tetap perlu memposisikan hitbox-nya agar tahu di mana seharusnya ia berada
                    Rectangle rect = enemyAttack.getAttackHitbox();
                    shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
                }
            }
        }

        shapeRenderer.setColor(Color.PINK);
        for (ItemDrop item : droppedItems) {
            shapeRenderer.rect(item.getBounds().x, item.getBounds().y, item.getBounds().width, item.getBounds().height);
        }



        shapeRenderer.setColor(Color.CYAN);
        for (Projectile projectile : playerProjectiles) {
            if (projectile.isActive()) {
                shapeRenderer.rect(projectile.getBounds().x, projectile.getBounds().y, projectile.getBounds().width, projectile.getBounds().height);
            }
        }

        shapeRenderer.setColor(Color.MAGENTA);
        for (EnemyProjectile projectile : enemyProjectiles) {
            if (projectile.isActive()) {
                shapeRenderer.rect(projectile.getBounds().x, projectile.getBounds().y, projectile.getBounds().width, projectile.getBounds().height);
            }
        }

        shapeRenderer.end();
    }

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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) backgroundMusic.pause();
    }

    @Override
    public void resume() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) backgroundMusic.play();
    }

    @Override
    public void hide() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (player != null) player.dispose();

        for (ItemDrop item : droppedItems) { // <-- Tambahkan ini
            item.dispose();
        }
        droppedItems.clear();

        for (Enemy e : allEnemies) e.dispose();
        allEnemies.clear();

        for (EnemyProjectile p : enemyProjectiles) p.dispose();
        enemyProjectiles.clear();

        if (playerProjectileAnimation != null) {
            Texture sheet = playerProjectileAnimation.getKeyFrames()[0].getTexture();
            if (sheet != null) sheet.dispose();
        }
        playerProjectiles.clear();

        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }

        // (BARU) Buang spritesheet health bar
        if (healthBarSheet != null) {
            healthBarSheet.dispose();
        }

        if (waveFont != null) {
            waveFont.dispose();
        }

        Gdx.app.log("GameScreen", "Disposed GameScreen resources.");
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public Array<Enemy> getAllEnemies() {
        return allEnemies;
    }

    public Animation<TextureRegion> getPlayerProjectileAnimation() {
        return playerProjectileAnimation;
    }
}
