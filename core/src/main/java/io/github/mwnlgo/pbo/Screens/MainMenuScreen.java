package io.github.mwnlgo.pbo.Screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.mwnlgo.pbo.Main;

public class MainMenuScreen implements Screen {

    private final Main game;

    public MainMenuScreen(Main game) {
        this.game = game;

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.RED);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();
        game.font.draw(game.batch, "Welcome to The Game!!! ", 1, 1.5f);
        game.font.draw(game.batch, "Tap anywhere to begin!", 1, 1);
        game.batch.end();

        if (Gdx.input.justTouched()){
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
