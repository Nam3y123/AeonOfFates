package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.MenuOption;
import com.mygdx.game.Projectile;
import com.mygdx.game.Tile;
import com.mygdx.game.Unit;

import java.util.ArrayList;
import java.util.HashMap;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.mygdx.game.Tile.*;
import static com.mygdx.game.Tile.drawShape;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class Annah extends Unit {
    private float[] ab1X, ab1Y;
    private Unit[] ab1Unit;
    private boolean ab1On;
    private Rectangle ab2Rect;
    private int ab2Dur;

    public Annah(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 8;
        def = 13;
        mag = 27;
        hp = maxHp;
        name = "Annah";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Annah_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 1f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        ab1On = false;
        ab2Dur = 0;

        abMenu.add(new MenuOption("Featherstorm", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Broadsword Block", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("SUPRISE!!!", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Slash and Burn", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 1;
        maxCd[2] = 4;
        maxCd[3] = 10;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            if(num == sel) {
                switch(doing) {
                    case "ab1":
                        float rot;
                        Tile.setClickPos();
                        if (clickX >= getX() + 24)
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24));
                        else
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24)) + 3.14159f;

                        particleRegion.setRegion(32, 12, 324, 36);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setSize(120, 12);
                        spr.setOrigin(0, 6);
                        spr.setPosition(getX() + 24 + (24 * (float)Math.cos(rot)), getY() + 16 + (24 * (float)Math.sin(rot)));

                        for(int i = 0; i < 6; i++) {
                            spr.setRotation((float) toDegrees(rot) - 30 + (12 * i));
                            spr.draw(batch);
                        }
                        Tile.hit = true;
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, BLUE, true);
                        abMenu.setVisible(false);
                        ret = true;
                        break;
                    default:
                        break;
                }
            }
            return ret;
        }
    }

    @Override
    public void clickAct() {
        super.clickAct();
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        switch (doing) {
            case "ab1":
                ab1_Out();
                break;
            case "ab2":
                ab2(cX, cY);
                break;
        }
    }

    private void ab1_Out() {
        float cX = Tile.clickX;
        float cY = Tile.clickY;
        ab1Unit = new Unit[6];
        ab1X = new float[6];
        ab1Y = new float[6];
        float dir;
        if (cX >= getX() + 24)
            dir = (float) atan((cY - getY() - 24) / (cX - getX() - 24));
        else
            dir = (float) atan((cY - getY() - 24) / (cX - getX() - 24)) + 3.14159f;
        for(int i = 0; i < 6; i++) {
            final int FINAL_I = i;
            particleRegion.setRegion(44, 0, 12, 12);
            getParent().addActor(new Projectile(this, getTeam(), (float)Math.toDegrees(dir) - 30 + (12 * i),
                    getX() + 18 + (24 * (float)Math.cos(dir)), getY() + 14 + (24 * (float)Math.sin(dir)), particleRegion, 4, 2f) {
                @Override
                public void effect(Unit un) {
                    ab1Unit[FINAL_I] = un;
                    super.remove();
                }

                @Override
                public boolean remove() {
                    ab1X[FINAL_I] = getX();
                    ab1Y[FINAL_I] = getY();
                    return super.remove();
                }
            });
        }
        ab1On = true;
        doing = "ab1OutAnim";
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                doing = "Stand";
            }
        }, 0.5f);
        if(secondary)
            primary = true;
        else
            secondary = true;
        cd[0] = maxCd[0];
    }

    private void ab1_In() {
        float dmg = 2 + (0.2f * mag) + (0.2f * level);
        HashMap<Unit, Integer> hitUnits = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            if(ab1Unit[i] != null) {
                ab1X[i] = ab1Unit[i].getX() + 18;
                ab1Y[i] = ab1Unit[i].getY() + 14;
                if(hitUnits.containsKey(ab1Unit[i]))
                    hitUnits.put(ab1Unit[i], hitUnits.get(ab1Unit[i]) + 1);
                else
                    hitUnits.put(ab1Unit[i], 1);
            }
            final int FINAL_I = i;
            float dir = getDirection(ab1X[i], getX() + 18, ab1Y[i], getY() + 14);
            float dist = getDistance(ab1X[i], getX() + 18, ab1Y[i], getY() + 14) / 48f;
            getParent().addActor(new Projectile(this, getTeam(), dir, ab1X[i], ab1Y[i], particleRegion, 3, dist) {
                @Override
                public void effect(Unit un) {
                    if(!un.equals(ab1Unit[FINAL_I]))
                        un.takeMagDmg((int)(dmg * 2), Annah.this);
                }
            });
        }
        for(Unit un : hitUnits.keySet())
            un.takeMagDmg((int)(dmg * hitUnits.get(un)), this);
        ab1On = false;
        doing = "ab1InAnim";
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                doing = "Stand";
            }
        }, 0.5f);
    }

    private void ab2(int cX, int cY) {
        boolean clear = Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE);
        for(Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY)
                clear = false;
        if(cX == getGridX() && cY == getGridY())
            clear = true;
        if(clear) {
            float dashDur = 0.125f;
            float dir = (int)Math.floor((getDirection(getGridX(), cX, getGridY(), cY) + 1) / 90f) * 90;
            Rectangle rect;
            if(dir == 0 || dir == 180) {
                rect = new Rectangle(getGridX() - 3, getGridY() - 1, 6, 2);
            } else
                rect = new Rectangle(getGridX() - 1, getGridY() - 3, 2, 6);
            for(Unit un : units) {
                if(un.getTeam() != team && un.getTargetable() && rect.contains(un.getGridX(), un.getGridY()))
                    un.takeMagDmg((int)(10 + (0.6 * mag) + (0.8 * level)), this);
            }
            addAction(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, dashDur));
            doing = "Stand";
            if(secondary)
                primary = true;
            else
                secondary = true;
            ab2Dur = 3;
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab1On) {
            sel = num;
            ab1_In();
        }
    }
}
