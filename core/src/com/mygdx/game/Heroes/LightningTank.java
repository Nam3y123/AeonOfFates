package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.*;

import java.util.Objects;

import static com.mygdx.game.Tile.*;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class LightningTank extends Unit {
    private boolean ab2On;
    private float passiveX, passiveY;
    private float oldX, oldY;

    public LightningTank(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 23;
        def = 20;
        mag = 7;
        hp = maxHp;
        name = "LightningTank";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/LT_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.5f, 0.5f, 0f, 1f);
        particleRegion.setRegion(32, 3, 2, 12);

        ab2On = false;
        passiveX = getX();
        passiveY = getY() - 4;
        oldX = getX();
        oldY = getY();

        abMenu.add(new MenuOption("Conduit", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Static Storm", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0)
                    ab2Activate();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Voltage Strike", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Thunderstorm", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        particles.add(new Particle(particleRegion, (int) + 23, (int)passiveY + 18) {
            @Override
            public void increment() {
                spr.setPosition(passiveX + 23, passiveY + 18);
            }
        });
        particleRegion.setRegion(40, 0, 432, 432);
        particles.add(new Particle(particleRegion, (int)passiveX - 192, (int)passiveY - 192) {
            @Override
            public void increment() {
                spr.setPosition(passiveX - 192, passiveY - 192);
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 5;
        maxCd[2] = 1;
        maxCd[3] = 14;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Tile.drawShape(batch, parentAlpha, passiveX, passiveY, "Circle", 4);
        super.draw(batch, parentAlpha);
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            if(num == sel) {
                switch(doing) {
                    case "ab3":
                        particleRegion.setRegion(472, 0, 48, 216);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setSize(48, 216);
                        spr.setOrigin(24, 0);
                        spr.setPosition(passiveX, passiveY + 24);

                        float rot;
                        Tile.setClickPos();
                        if (clickX >= passiveX + 24)
                            rot = (float) atan((clickY - passiveY - 24) / (clickX - passiveX - 24));
                        else
                            rot = (float) atan((clickY - passiveY - 24) / (clickX - passiveX - 24)) + 3.14159f;
                        spr.setRotation((float) toDegrees(rot) - 90);
                        spr.draw(batch);
                        abMenu.setVisible(false);
                        Tile.hit = true;
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
    public void act(float delta) {
        super.act(delta);
        if (ab2On) {
            for (Actor a : getParent().getChildren()) {
                if (a instanceof Projectile && ((Projectile) a).getSourceUn().getTeam() != team) {
                    float distance = getDistance(getX() + 24, a.getX() + a.getWidth() / 2f, getY() + 24, a.getY() + a.getHeight() / 2);
                    if (distance <= 120f) {
                        float pull = 6 + (6 * (120 - distance) / 120f);
                        float dir = (float) Math.toRadians(getDirection(getX() + 24, a.getX() + a.getWidth() / 2f, getY() + 24, a.getY() + a.getHeight() / 2));
                        a.moveBy(-(float) Math.cos(dir) * pull, -(float) Math.sin(dir) * pull);
                    }
                }
            }
        }
        if(moving) {
            while(getDistance(getGridX(), getGridX(passiveX), getGridY(), getGridY(passiveY)) > 4.5f) {
                passiveX -= oldX - getX();
                passiveY -= oldY - getY();
            }
        }
        oldX = getX();
        oldY = getY();
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab2On)
            ab2On = false;
    }

    private void ab2Activate() {
        ab2On = true;
        passiveX = getX();
        passiveY = getY() - 4;
        hasMoved = true;
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
        cd[1] = maxCd[1];
    }
}
