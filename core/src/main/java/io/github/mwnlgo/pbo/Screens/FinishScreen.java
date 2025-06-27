package io.github.mwnlgo.pbo.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.mwnlgo.pbo.Main;

public class FinishScreen implements Screen {

    private final Main game;
    private final int score, wave;
    private BitmapFont text;
    private OrthographicCamera camera;
    private FitViewport viewport;

    public FinishScreen(Main game, int score, int wave) {
        this.game = game;
        this.score = score;
        this.wave = wave;
        // Inisialisasi kamera dan viewport
        this.camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        viewport.apply();
        // Update kamera untuk memastikan posisi yang benar
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void show() {
        // Implementasi ketika layar ditampilkan
        // Inisialisasi font menggunakan FreeTypeFontGenerator
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/Jersey25-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 72; // Ukuran font yang diinginkan
        text = generator.generateFont(parameter); // Buat font untuk wave
        generator.dispose(); // Buang generator setelah font dibuat

        // Update kamera untuk memastikan posisi yang benar
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        // Warna latar belakang layar
        ScreenUtils.clear(Color.NAVY);
        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);
        game.getBatch().begin();
        // Implementasi rendering layar
        text.draw(game.getBatch(), "Game Over at Wave " + wave + "!\nYour score: " + score, viewport.getWorldWidth() / 2f - 325, viewport.getWorldHeight() / 2f + 50);
        game.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        // Update kamera untuk memastikan posisi yang benar
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void pause() {
        // Implementasi ketika aplikasi dijeda
    }

    @Override
    public void resume() {
        // Implementasi ketika aplikasi dilanjutkan
    }

    @Override
    public void hide() {
        // Implementasi ketika layar disembunyikan
    }

    @Override
    public void dispose() {
        // Implementasi pembuangan sumber daya yang digunakan oleh layar ini
        text.dispose();
        game.dispose();
    }
}
