package io.github.mwnlgo.pbo.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.mwnlgo.pbo.Main;

public class MainMenuScreen implements Screen {

    private final Main game; // Ubah ke MainGame
    private OrthographicCamera camera; // Kamera untuk viewport
    private Viewport viewport; // Viewport untuk skala layar
    private BitmapFont font; // Font untuk teks
    // Kita tidak perlu mendeklarasikan SpriteBatch di sini karena akan diambil dari game.getBatch()

    private Music backgroundMusic;

    public MainMenuScreen(Main game) { // Ubah konstruktor ke MainGame
        this.game = game;

        // Inisialisasi kamera dan viewport
        camera = new OrthographicCamera();
        // Gunakan ukuran layar saat ini sebagai basis viewport.
        // FitViewport akan memastikan rasio aspek tetap terjaga.
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0); // Posisikan kamera di tengah
        viewport.apply(true); // Terapkan viewport agar kamera terpusat dengan benar

        // Inisialisasi font menggunakan FreeTypeFontGenerator
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/Jersey25-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 72; // Ukuran font yang diinginkan
        font = generator.generateFont(parameter); // Buat font dengan parameter yang telah ditentukan
        generator.dispose(); // Buang generator setelah font dibuat
    }

    @Override
    public void show() {
        // Metode ini dipanggil ketika MainMenuScreen menjadi layar aktif
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/MainMenu.ogg"));
        backgroundMusic.setLooping(true); // Atur agar musik berulang
        backgroundMusic.setVolume(0.2f);  // Atur volume (0.0 sampai 1.0)
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        // Hapus layar dengan warna merah (atau warna lain yang Anda inginkan)
        ScreenUtils.clear(Color.NAVY);

        // Terapkan viewport agar drawing disesuaikan dengan skala layar
        // true: center the camera (biasanya true untuk menu)
        viewport.apply(true);

        // Set projection matrix batch agar sesuai dengan kamera viewport
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);

        game.getBatch().begin();
        // Gambar teks. Posisi (x, y) relatif terhadap viewport.
        // Sesuaikan posisi (x, y) dan ukuran viewport jika teks tidak terlihat atau di tempat yang salah.
        // Misalnya, jika viewport 800x480, (1, 1.5f) mungkin terlalu kecil/di pojok.
        // Gunakan posisi relatif atau hitungan dari viewport.getWorldWidth()/2 dan viewport.getWorldHeight()/2
        font.draw(game.getBatch(), "Welcome to The Game!!!\nTap anywhere to begin!", viewport.getWorldWidth() / 2 - 325, viewport.getWorldHeight() / 2 + 50);
        game.getBatch().end();

        // Cek jika layar disentuh/diklik
        if (Gdx.input.justTouched()){
            // Beralih ke GameScreen
            game.setScreen(new GameScreen(game));
            // Penting: Buang resource MainMenuScreen setelah tidak digunakan lagi
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        // Perbarui viewport ketika ukuran jendela berubah
        viewport.update(width, height, true); // true: center the camera
    }

    @Override
    public void pause() {
        // Dipanggil saat game dijeda (misalnya, aplikasi berpindah ke latar belakang)
    }

    @Override
    public void resume() {
        // Dipanggil saat game dilanjutkan dari keadaan jeda
    }

    @Override
    public void hide() {
        // Dipanggil ketika layar ini tidak lagi menjadi layar aktif
        // (misalnya, ketika setScreen() dipanggil)
    }

    @Override
    public void dispose() {
        // Penting: Buang resource yang dibuat di layar ini
        // Jika font dibuat di sini, buang di sini
        if (font != null) {
            font.dispose();
        }

        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }

        Gdx.app.log("MainMenuScreen", "Disposed MainMenuScreen resources.");
    }
}
