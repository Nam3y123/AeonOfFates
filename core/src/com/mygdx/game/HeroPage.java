package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class HeroPage extends Actor implements Disposable {
    private final TextureRegion DETAIL_PAGE_TEX = new TextureRegion(new Texture(Gdx.files.internal
            ("Client/HeroDetailPage.png")), 0, 0, 640, 480);
    private final Sprite DETAIL_PAGE = new Sprite(DETAIL_PAGE_TEX);
    private Image splash;
    private Texture splashTex;
    private float time;

    public HeroPage(String heroName) {
        try {
            splashTex = new Texture(Gdx.files.internal("Client/Splash/" + heroName + ".png"));
        } catch (GdxRuntimeException e) {
            splashTex = new Texture(Gdx.files.internal("Client/Splash/Talon.png"));
        }
        splash = new Image(splashTex);
        splash.setOrigin(320, 240);
        splash.setScale(0.8f, 0.8f);
        splash.setColor(1f, 1f, 1f, 0f);
        splash.addAction(Actions.parallel(Actions.scaleTo(1f, 1f, 0.25f), Actions.alpha(1f, 0.25f)));

        DETAIL_PAGE.setOrigin(320, 240);
        DETAIL_PAGE.setScale(1.25f, 1.25f);
        DETAIL_PAGE.setColor(1f, 1f, 1f, 0f);

        time = 0;
    }

    @Override
    public void dispose() {
        splashTex.dispose();
        DETAIL_PAGE_TEX.getTexture().dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        splash.draw(batch, parentAlpha);
        DETAIL_PAGE.draw(batch);
    }

    @Override
    public void act(float delta) {
        splash.act(delta);
        DETAIL_PAGE.setScale(1 / splash.getScaleX(), 1 / splash.getScaleY());
        DETAIL_PAGE.setAlpha(time < 0.25f ? time * 4f : 1f);
        time += delta;
    }
}
