package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.BufferedReader;
import java.io.IOException;

public class GameClient extends Stage {
    private Table clientTable;
    private Skin skin;
    private String user;
    private Sprite heroRect, bg, splashArt;
    private int scrAmt;
    private String[] heroes;

    private final int heroCount = 12;

    public GameClient(Viewport vp) throws IOException {
        super(vp);

        user = "BeansTheMcJeans";

        clientTable = new Table();
        clientTable.setWidth(getWidth());
        clientTable.align(Align.left|Align.top);
        clientTable.setPosition(0, this.getHeight()/2);
        clientTable.setVisible(false);

        skin = new Skin(Gdx.files.internal("CombatSkin.json"));
        clientTable.row();
        clientTable.add(new MenuOption("Sandbox Mode", skin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startGame();
            }
        });

        heroRect = new Sprite(new Texture(Gdx.files.internal("Client/HeroRect.png")));
        bg = new Sprite(new Texture(Gdx.files.internal("Client/DefaultClient.png")));
        splashArt = new Sprite(new Texture(Gdx.files.internal("Client/SplashArt.png")));

        Label nameLabel = new Label(user, skin, "combatMenu");
        nameLabel.setPosition(456 - (nameLabel.getWidth() / 2f), 457);
        Label heroLabel = new Label("temp", skin, "combatMenu") {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                for(int i = 0; i < heroCount; i++) {
                    setText(heroes[i]);
                    setPosition(250 + (154 * (i % 3)) - (getPrefWidth() / 2), 190 - (float)(266 * Math.floor(i / 3f)) -
                            scrAmt);
                    super.draw(batch, parentAlpha);
                }
            }
        };
        heroes = new String[heroCount];
        BufferedReader reader = Gdx.files.internal("Client/HeroList.file").reader(8192);
        for(int i = 0; i < heroCount; i++)
            heroes[heroCount - i - 1] = reader.readLine();

        addActor(heroLabel);
        addActor(new Image(new Texture(Gdx.files.internal("Client/Overlay.png"))));
        getActors().get(getActors().size - 1).addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(x <= 100) { // If the Play button is hit
                    clientTable.setVisible(true);
                    clientTable.setPosition(0, 430 - clientTable.getHeight());
                } else
                    clientTable.setVisible(false);
                return true;
            }
        });
        getActors().get(getActors().size - 1).setPosition(0, 430);
        addActor(clientTable);
        addActor(nameLabel);
        addActor(new HeroPage("Kaiv"));
    }

    @Override
    public void draw() {
        getBatch().begin();
        bg.draw(getBatch());
        heroRect.setBounds(0, 0, 144, 256);
        for(int i = 0; i < heroCount; i++) {
            heroRect.setRegion((heroCount - i > 6) ? 144 : 0, 0, 144, 256);
            heroRect.setBounds(0, 0, 144, 256);
            heroRect.setPosition(178 + (154 * (i % 3)), 164 - (float)(266 * Math.floor(i / 3f)) - scrAmt);
            heroRect.draw(getBatch());

            splashArt.setRegion(132 * (i % 3), (int)(192 * Math.floor(i / 3f)), 132, 192);
            splashArt.setBounds(0, 0, 132, 192);
            splashArt.setPosition(184 + (154 * (i % 3)), 221.5f - (float)(266 * Math.floor(i / 3f)) - scrAmt);
            splashArt.draw(getBatch());
        }
        getBatch().end();
        super.draw();
    }

    @Override
    public void act() {
        getViewport().setScreenWidth(Gdx.graphics.getWidth());
        getViewport().setScreenHeight(Gdx.graphics.getHeight());
        super.act();
    }

    @Override
    public boolean scrolled(int amount) {
        if(amount > 0)
            scrAmt -= 35;
        else
            scrAmt += 35;
        return true;
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch(keyCode) {
            case Input.Keys.DOWN:
                scrAmt -= 35;
                return true;
            case Input.Keys.UP:
                scrAmt += 35;
                return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        heroRect.getTexture().dispose();
        bg.getTexture().dispose();
        splashArt.getTexture().dispose();
        super.dispose();
    }

    public void startGame() {

    }
}
