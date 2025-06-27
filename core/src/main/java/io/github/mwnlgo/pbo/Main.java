package io.github.mwnlgo.pbo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.mwnlgo.pbo.Screens.MainMenuScreen;

/**
 * Kelas utama aplikasi game, mewarisi dari com.badlogic.gdx.Game.
 * Bertanggung jawab untuk inisialisasi SpriteBatch dan mengatur layar awal game.
 */
public class Main extends Game {

    private SpriteBatch batch; // SpriteBatch adalah objek yang digunakan untuk menggambar tekstur/sprite

    /**
     * Metode ini dipanggil saat aplikasi pertama kali dibuat.
     * Di sini kita menginisialisasi SpriteBatch dan mengatur GameScreen sebagai layar aktif.
     */
    @Override
    public void create () {
        // Inisialisasi SpriteBatch. Sebaiknya hanya ada satu instance SpriteBatch
        // dan dilewatkan ke objek-objek yang memerlukannya untuk efisiensi.
        batch = new SpriteBatch();

        // Mengatur layar awal game ke GameScreen.
        // Dengan menggunakan setScreen(), kita memberitahu LibGDX untuk mulai
        // memanggil metode-metode lifecycle (show, render, hide, dispose) dari GameScreen.
        setScreen(new MainMenuScreen(this));
    }

    /**
     * Metode ini dipanggil setiap frame untuk melakukan logika rendering dan update.
     * Karena kita menggunakan kelas Game dan telah mengatur Screen, metode ini
     * secara otomatis akan memanggil metode render dari Screen yang aktif (GameScreen).
     */
    @Override
    public void render () {
        // Metode super.render() sangat penting! Ini yang akan memanggil
        // render() dari Screen yang saat ini aktif (GameScreen).
        super.render();
    }

    /**
     * Metode ini dipanggil saat aplikasi dihancurkan atau ditutup.
     * Sangat penting untuk membuang (dispose) semua resource yang tidak dikelola secara otomatis
     * seperti SpriteBatch, tekstur, audio, dll., untuk mencegah memory leak.
     * Selain itu, metode dispose() dari Screen yang aktif juga akan dipanggil secara otomatis
     * oleh super.dispose().
     */
    @Override
    public void dispose () {
        // Buang SpriteBatch saat aplikasi berakhir
        if (batch != null) {
            batch.dispose();
        }
        // super.dispose() akan memanggil dispose() dari Screen yang aktif.
        // Pastikan GameScreen Anda memiliki metode dispose() yang membuang semua asetnya (map, player, musuh, proyektil).
        super.dispose();
    }

    /**
     * Metode getter untuk mendapatkan instance SpriteBatch.
     * Ini memungkinkan objek lain (seperti Player, Enemy, Projectile)
     * untuk menggunakan SpriteBatch yang sama untuk menggambar.
     * @return Instance SpriteBatch yang digunakan oleh game.
     */
    public SpriteBatch getBatch() {
        return batch;
    }
}
