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
import com.badlogic.gdx.Input; // Tambahkan import ini

public class FinishScreen implements Screen {

    private final Main game;
    private final int score, wave;
    private BitmapFont text;
    private BitmapFont replayText;
    private BitmapFont menuText;
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

        parameter.size = 72; // Ukuran font yang diinginkan untuk teks utama
        text = generator.generateFont(parameter); // Buat font untuk wave

        parameter.size = 48; // Ukuran font yang lebih kecil untuk teks replay
        replayText = generator.generateFont(parameter); // Buat font untuk teks replay

        parameter.size = 32; // Ukuran font yang lebih kecil untuk teks Menu
        menuText = generator.generateFont(parameter); // Buat font untuk teks Menu

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

        // Teks utama "Game Over"
        text.draw(game.getBatch(), "Game Over at Wave " + wave + "!\nYour score: " + score, viewport.getWorldWidth() / 2f - 325, viewport.getWorldHeight() / 2f + 50);

        // Teks "Enter SPACE for replay"
        // Sesuaikan posisi Y agar berada di bawah teks utama
        replayText.draw(game.getBatch(), "Enter SPACE for play again", viewport.getWorldWidth() / 2f - 325, viewport.getWorldHeight() / 2f - 100);

        menuText.draw(game.getBatch(), "Enter (ESC) to Back", viewport.getWorldWidth() / 2f - 325, viewport.getWorldHeight() / 2f - 150);


        game.getBatch().end();

        // Cek input tombol spasi
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Ganti layar ke PlayScreen atau MainScreen untuk memulai ulang permainan
            // Asumsi Main memiliki metode untuk mengatur layar ke PlayScreen
            game.setScreen(new GameScreen(game)); // Ganti PlayScreen dengan nama layar permainanmu
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Ganti layar ke PlayScreen atau MainScreen untuk memulai ulang permainan
            // Asumsi Main memiliki metode untuk mengatur layar ke PlayScreen
            game.setScreen(new MainMenuScreen(game)); // Ganti PlayScreen dengan nama layar permainanmu
        }
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
        replayText.dispose(); // Buang font replay juga
        // game.dispose(); // Hapus ini, karena Main tidak boleh di-dispose di sini
    }
}
