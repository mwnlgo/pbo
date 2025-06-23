package io.github.mwnlgo.pbo.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.mwnlgo.pbo.Main;
import io.github.mwnlgo.pbo.entities.*;

public class GameScreen implements Screen {

    private Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private float worldWidth, worldHeight;

    private Player player;
    private Array<Enemy> allEnemies;
    private Array<EnemyProjectile> enemyProjectiles;
    private Array<Projectile> playerProjectiles;

    private boolean assetsLoaded = false;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        this.worldWidth = viewport.getWorldWidth();
        this.worldHeight = viewport.getWorldHeight();

        allEnemies = new Array<>();
        enemyProjectiles = new Array<>();
        playerProjectiles = new Array<>();
    }

    @Override
    public void show() {
        if (!assetsLoaded) {
            Gdx.app.log("GameScreen", "Loading assets...");

            player = new Player(100, 100, this);

            allEnemies.add(new EnemyA(300, 300, player, this));
            allEnemies.add(new EnemyA(500, 200, player, this));
            allEnemies.add(new EnemyB(700, 400, player, this));
            allEnemies.add(new EnemyC(1000, 100, player, this));
            allEnemies.add(new EnemyC(800, 600, player, this));

            assetsLoaded = true;
            Gdx.app.log("GameScreen", "Assets loaded.");
        }
        Gdx.input.setInputProcessor(null);
    }

    public void addEnemyProjectile(EnemyProjectile projectile) {
        Gdx.app.log("GameScreen", "Enemy projectile added at: " + projectile.getBounds().x + ", " + projectile.getBounds().y);
        enemyProjectiles.add(projectile);
    }

    public void spawnPlayerProjectile(Projectile projectile) {
        playerProjectiles.add(projectile);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!assetsLoaded) return;

        player.update(delta);

        for (int i = allEnemies.size - 1; i >= 0; i--) {
            Enemy enemy = allEnemies.get(i);
            enemy.update(delta);
            if (!enemy.isAlive()) {
                enemy.dispose();
                allEnemies.removeIndex(i);
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

        for (int i = playerProjectiles.size - 1; i >= 0; i--) {
            Projectile p = playerProjectiles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                p.dispose();
                playerProjectiles.removeIndex(i);
            }
        }

        // Update camera mengikuti pemain
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.position.x = Math.max(viewport.getWorldWidth() / 2, Math.min(worldWidth - viewport.getWorldWidth() / 2, camera.position.x));
        camera.position.y = Math.max(viewport.getWorldHeight() / 2, Math.min(worldHeight - viewport.getWorldHeight() / 2, camera.position.y));
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        player.render(batch);

        for (Enemy enemy : allEnemies) {
            enemy.render(batch);
        }

        for (EnemyProjectile p : enemyProjectiles) {
            p.render(batch);
        }

        for (Projectile p : playerProjectiles) {
            p.render(batch);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (player != null) player.dispose();

        for (Enemy e : allEnemies) e.dispose();
        allEnemies.clear();

        for (EnemyProjectile p : enemyProjectiles) p.dispose();
        enemyProjectiles.clear();

        for (Projectile p : playerProjectiles) p.dispose();
        playerProjectiles.clear();

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
}
