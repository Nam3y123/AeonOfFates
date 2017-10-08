package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class Zaniek extends Unit {
    private boolean passiveOn;
    private int[][] abCooldowns;

    public Zaniek(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        atk = 23;
        def = 17;
        mag = 15;
        maxHp = 205;
        hp = maxHp;
        range = 1;
        name = "Zaniek";
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Zaniek_Particles.png")));

        passiveOn = false;

        abMenu.add(new MenuOption("Ab1", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0)
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Ab2", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Ab3", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("The Great Escape!", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = super.drawTile(batch, parentAlpha);
        if(!ret) {
            ret = true;
            switch(doing) {
                case "ab1":
                    float rot;
                    Tile.setClickPos();
                    if (clickX >= getX() + 24)
                        rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24));
                    else
                        rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24)) + 3.14159f;
                    if(passiveOn) {
                        particleRegion.setRegion(196, 0, 216, 108);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setSize(216, 108);
                        spr.setOrigin(108, 0);
                        spr.setPosition(getX() - 84, getY() + 20);
                        spr.setRotation((float) toDegrees(rot) - 90);
                        spr.draw(batch);
                    }
                    particleRegion.setRegion(160, 0, 36, 324);
                    Sprite spr = new Sprite(particleRegion);
                    spr.setSize(12, 108);
                    spr.setOrigin(6, 0);
                    spr.setPosition(getX() + 18, getY() + 20);
                    spr.setRotation((float) toDegrees(rot) - 90);
                    spr.draw(batch);
                    Tile.hit = true;
                    break;
                default:
                    ret = false;
            }
        }
        if(ret)
            abMenu.setVisible(false);
        return ret;
    }

    @Override
    public void clickAct() {
        super.clickAct();
        int cX = getGridX(Tile.clickX - 4);
        int cY = getGridY(Tile.clickY);
        switch(doing) {
            case "ab1":
                passiveOn = true;
                doing = "Stand";
                break;
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        passiveOn = false;
    }

    @Override
    public void enTurn() {
        super.enTurn();
    }
}
