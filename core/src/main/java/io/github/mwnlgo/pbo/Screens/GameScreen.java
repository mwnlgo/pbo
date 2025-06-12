package io.github.mwnlgo.pbo.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import io.github.mwnlgo.pbo.Main;
import io.github.mwnlgo.pbo.entities.Enemy;
import io.github.mwnlgo.pbo.entities.EnemyA;
import io.github.mwnlgo.pbo.entities.Player;
import io.github.mwnlgo.pbo.entities.Projectile;

import java.util.Iterator;

public class GameScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    // Aktor utama di dalam panggung
    private final Player player;
    private final Array<Enemy> enemies;
    private final Array<Projectile> projectiles;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Atur kamera untuk melihat dunia game
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Ciptakan para aktor
        this.player = new Player(400, 300, this);
        this.enemies = new Array<>();
        this.projectiles = new Array<>();

        // Panggil beberapa musuh untuk pertama kali
        spawnEnemies();
    }

    private void spawnEnemies() {
        // Contoh memunculkan beberapa musuh di posisi acak
        enemies.add(new EnemyA(100, 500, player, this));
        enemies.add(new EnemyA(700, 100, player, this));
        // TODO: Tambahkan EnemyB dan EnemyC di sini nanti
    }

    // Metode ini akan dipanggil oleh PlayerAttackComponent
    public void spawnEntity(Projectile projectile) {
        projectiles.add(projectile);
    }

    @Override
    public void render(float delta) {
        // --- LOGIKA UPDATE (Update semua "otak" aktor) ---

        // Update player
        player.update(delta);

        // Update semua musuh & hapus yang mati
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(delta);
            if (!enemy.isAlive()) {
                enemy.dispose();
                enemyIterator.remove();
            }
        }

        // Update semua proyektil & hapus yang tidak aktif
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile p = projectileIterator.next();
            p.update(delta);
            if (!p.isActive()) {
                p.dispose();
                projectileIterator.remove();
            }
        }

        // TODO: Logika deteksi tabrakan (misal: proyektil mengenai musuh)


        // --- LOGIKA RENDER (Gambar semua aktor ke layar) ---

        // Bersihkan layar
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Atur kamera agar mengikuti player
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Mulai menggambar
        batch.begin();

        player.render(batch);
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
        for (Projectile p : projectiles) {
            p.render(batch);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        // Hancurkan semua resource untuk mencegah memory leak!
        batch.dispose();
        player.dispose();
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        for (Projectile p : projectiles) {
            p.dispose();
        }
    }

    // --- Metode lain yang bisa diabaikan untuk saat ini ---
    @Override
    public void show() { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    // --- Getter untuk Kamera (dibutuhkan oleh Player) ---
    public OrthographicCamera getCamera() {
        return camera;
    }

}
