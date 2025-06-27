package io.github.mwnlgo.pbo.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        this.worldWidth = 3000;
        this.worldHeight = 3000;
        allEnemies = new Array<>();
        playerProjectiles = new Array<>();
        enemyProjectiles = new Array<>();
        hitByEnemyAttacks = new Array<>();
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        playerProjectileAnimation = loadAnimationFromSheet("player/projectile_ayam.png", 4, 1, 0.1f);
        player = new Player(100, 100, this);

        allEnemies.add(new EnemyA(300, 300, player, this));
        allEnemies.add(new EnemyA(500, 200, player, this));
        allEnemies.add(new EnemyB(700, 400, player, this));
        allEnemies.add(new EnemyC(1000, 100, player, this));
        allEnemies.add(new EnemyC(800, 600, player, this));

        Gdx.input.setInputProcessor(null);
    }

    public void update(float delta) {
        if (!player.isAlive()) return;

        player.update(delta);

        for (int i = allEnemies.size - 1; i >= 0; i--) {
            Enemy enemy = allEnemies.get(i);
            enemy.update(delta);
            if (!enemy.isAlive() && enemy.isDeathAnimationFinished()) {
                enemy.dispose();
                allEnemies.removeIndex(i);
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

        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();
    }

    public void spawnPlayerProjectile(Projectile projectile) {
        playerProjectiles.add(projectile);
    }

    /**
     * (BARU) Method untuk menambahkan proyektil musuh ke dalam game.
     * Dipanggil oleh komponen serangan seperti EnemyProjectileAttack.
     * @param projectile Objek proyektil musuh yang akan ditambahkan.
     */
    public void addEnemyProjectile(EnemyProjectile projectile) {
        if (projectile != null) {
            this.enemyProjectiles.add(projectile);
        }
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
                if (enemyAttack.isActive() && !hitByEnemyAttacks.contains(enemyAttack, true) && enemyAttack.getAttackHitbox().overlaps(player.getBounds())) {
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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Enemy enemy : allEnemies) {
            enemy.render(batch);
        }
        if (player.isAlive()) {
            player.render(batch);
        }
        for (Projectile p : playerProjectiles) {
            p.render(batch);
        }
        for (EnemyProjectile p : enemyProjectiles) {
            p.render(batch);
        }
        batch.end();

        drawDebugShapes();
    }

    private void drawDebugShapes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(player.getBounds().x, player.getBounds().y, player.getBounds().width, player.getBounds().height);

        PlayerAttackComponent playerAttack = player.getCurrentAttack();
        if (playerAttack.isActive() && playerAttack instanceof PlayerMeleeAttack) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(playerAttack.getAttackHitbox().x, playerAttack.getAttackHitbox().y, playerAttack.getAttackHitbox().width, playerAttack.getAttackHitbox().height);
        }

        for (Enemy enemy : allEnemies) {
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.rect(enemy.getBounds().x, enemy.getBounds().y, enemy.getBounds().width, enemy.getBounds().height);

            if (enemy instanceof IMeleeAttacker) {
                EnemyMeleeAttack enemyAttack = ((IMeleeAttacker) enemy).getMeleeAttack();
                if (enemyAttack.isActive()) {
                    shapeRenderer.setColor(Color.ORANGE);
                    shapeRenderer.rect(enemyAttack.getAttackHitbox().x, enemyAttack.getAttackHitbox().y, enemyAttack.getAttackHitbox().width, enemyAttack.getAttackHitbox().height);
                }
            }
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if(shapeRenderer != null) shapeRenderer.dispose();
        if (player != null) player.dispose();

        for (Enemy e : allEnemies) e.dispose();
        allEnemies.clear();

        for (EnemyProjectile p : enemyProjectiles) p.dispose();
        enemyProjectiles.clear();

        if (playerProjectileAnimation != null) {
            Texture sheet = playerProjectileAnimation.getKeyFrames()[0].getTexture();
            sheet.dispose();
        }
        playerProjectiles.clear();

        Gdx.app.log("GameScreen", "Disposed GameScreen resources.");
    }

    public OrthographicCamera getCamera() { return camera; }
    public float getWorldWidth() { return worldWidth; }
    public float getWorldHeight() { return worldHeight; }
    public Array<Enemy> getAllEnemies() { return allEnemies; }
    public Animation<TextureRegion> getPlayerProjectileAnimation() { return playerProjectileAnimation; }
}
