package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Objects;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static java.lang.Math.atan;
import static java.lang.Math.floor;
import static java.lang.Math.toDegrees;

public class CrystalSniper extends Unit {
    private Particle[] crystals;
    private Particle crystalSpr;
    private boolean ab2Active;
    private int ab3Dur;
    private int ab4X, ab4Y, ab4Dur;

    public CrystalSniper(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 17;
        def = 18;
        mag = 27;
        hp = maxHp;
        name = "CrystalSniper";
        range = 5;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/CrystalSniper_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 0.375f, 1f, 1f);
        crystals = new Particle[2];
        particleRegion.setRegion(0, 128, 146, 146);
        crystalSpr = new Particle(particleRegion, -146, -146);
        ab3Dur = 0;
        ab4Dur = -1;

        abMenu.add(new MenuOption("Air Strike", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Captivate", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && crystals.length > 0 && (!primary || !secondary))
                    ab2();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Roundhouse", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Showtime", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0)
                    setDoing("ab4", false);
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 3;
        maxCd[2] = 5;
        maxCd[3] = 12;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = true;
            if(num == sel) {
                switch(doing) {
                    case "ab1":
                        particleRegion.setRegion(160, 0, 36, 324);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setSize(24, 216);
                        spr.setOrigin(12, 0);
                        spr.setPosition(getX() + 12, getY() + 20);

                        float rot;
                        Tile.setClickPos();
                        if (clickX >= getX() + 24)
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24));
                        else
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24)) + 3.14159f;
                        spr.setRotation((float) toDegrees(rot) - 90);
                        spr.draw(batch);
                        abMenu.setVisible(false);
                        Tile.hit = true;
                        break;
                    case "ab3":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, BLUE, false);
                        abMenu.setVisible(false);
                        break;
                    case "ab4":
                        drawRect(batch, parentAlpha, 8, 0, Tile.walls.getWidth(), Tile.walls.getHeight(), BLUE, true);
                        drawShape(batch, parentAlpha, getGridX(clickX) * 48 + 8, getGridY(clickY) * 48, "circle", 3, GREEN, false);
                        abMenu.setVisible(false);
                        break;
                    default:
                        ret = false;
                        break;
                }
            }
            return ret;
        }
    }

    @Override
    public void clickAct() {
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        if(!doing.equals("Attack"))
            super.clickAct();
        switch (doing) {
            case "Attack":
                doing = "Stand";
                float dir;
                dir = getDirection(getGridX(), cX, getGridY(), cY);
                particleRegion.setRegion(0, 0, 32, 32);
                getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                        getY() + (48 - particleRegion.getRegionHeight()) / 2 - 4, particleRegion, 4, range) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        atkAct(this);
                    }

                    @Override
                    public void effect(Unit un) {
                        ArrayList<StatusEffect> se = statusEffect;
                        int dmg = atk;
                        int curHp = un.getHp();
                        un.takeMagDmg((int)(5 + (0.5 * dmg) + (0.5 * mag)), CrystalSniper.this);
                        if(hitUnits.size() == 0)
                            for (int i = 0; i < se.size(); i++)
                                if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                                    se.get(i).effect(un, CrystalSniper.this, dmg);
                        while(dmgPopup.getActions().size > 0) {
                            dmgPopup.removeAction(dmgPopup.getActions().get(0));
                        }
                        dmgPopup.setText(Integer.toString(curHp - un.getHp()));
                    }

                    @Override
                    public boolean remove() {
                        Particle crystal = new Particle(particleRegion, (int)this.getX(), (int)this.getY());
                        if(crystals[0] == null)
                            crystals[0] = crystal;
                        else
                            crystals[1] = crystal;
                        particles.add(crystal);
                        return super.remove();
                    }
                });
                doing = "AtkAnim";
                MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
                atk.setColor(Color.DARK_GRAY);
                MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                mo.setColor(Color.DARK_GRAY);
                primary = true;
                hasMoved = true;
                break;
            case "ab1":
                ab1();
                break;
            case "ab3":
                ab3();
                break;
            case "ab4":
                doing = "Stand";
                cd[3] = maxCd[3];
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                hasMoved = true;
                ab4X = getGridX(clickX);
                ab4Y = getGridY(clickY);
                ab4Dur = 2;
                break;
        }
    }

    private void ab1() {
        doing = "Stand";
        float dir;
        dir = getDirection(getX() + 24, Tile.clickX, getY() + 20, Tile.clickY);
        particleRegion.setRegion(32, 32, 32, 32);
        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2 - 4, particleRegion, 4, range) {
            private boolean started = false;

            @Override
            public void act(float delta) {
                super.act(delta);
                atkAct(this);
                if(!started) {
                    setOrigin(getWidth() / 2f, getHeight() / 2f);
                    setRotation(dir);
                    started = true;
                }
            }

            @Override
            public void effect(Unit un) {
                int curHp = un.getHp();
                un.takeMagDmg((int)(12 + (level) + (0.6 * mag)), CrystalSniper.this);
                while(dmgPopup.getActions().size > 0) {
                    dmgPopup.removeAction(dmgPopup.getActions().get(0));
                }
                dmgPopup.setText(Integer.toString(curHp - un.getHp()));
                particleRegion.setRegion(32, 0, 32, 32);
                Particle crystal = new Particle(particleRegion, (int)un.getX() + 8, (int)un.getY() + 4);
                if(crystals[0] == null)
                    crystals[0] = crystal;
                else
                    crystals[1] = crystal;
                particles.add(crystal);
                this.remove();
            }
        });
        doing = "ab1Anim";
        MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        cd[0] = maxCd[0];
        if(secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
    }

    private void ab2() {
        ab2Active = true;
        crystals[0].getSpr().setRegion(96, 0, 32, 32);
        crystalSpr.getSpr().setSize(240, 240);
        doing = "Stand";
        cd[1] = maxCd[1];
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab3() {
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        boolean clear = Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE);
        for(Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY)
                clear = false;
        if(cX == getGridX() && cY == getGridY())
            clear = true;
        if(clear) {
            setPosition(cX * 48 + 8, cY * 48 + 4);
            doing = "Stand";
            if(ab3Dur <= 0) {
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                ab3Dur = 3;
            } else {
                cd[2] = maxCd[2] - 3 + ab3Dur;
                ab3Dur = 0;
            }
        }

    }

    private void ab4() {
        for(Unit un: units) {
            if(un.getTargetable() && un.getTeam() != team && getDistance(ab4X, un.getGridX(), ab4Y, un.getGridY()) <= 3.5) {
                un.takeTrueDmg((int)(17 + (2.5f * level) + (1.2f * mag)), this);
            }
        }
    }

    @Override
    public void drawHUD(Batch batch, float parentAlpha) {
        super.drawHUD(batch, parentAlpha);
        crystalSpr.getSpr().setPosition(-146, -146);
        for(int i = 0; i < 2; i++) {
            if(crystals[i] != null) {
                crystalSpr.getSpr().setPosition(getGridX(crystals[i].getSpr().getX()) * 48 - (ab2Active ? 89 : 41) + game.getRoot().getX(),
                        getGridY(crystals[i].getSpr().getY()) * 48 - (ab2Active ? 97 : 49) + game.getRoot().getY());
                crystalSpr.display(batch, parentAlpha);
            }
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        for(int i = 0; i < 2; i++) {
            if(crystals[i] != null) {
                Particle crystal = crystals[i];
                int crX = getGridX(crystal.getSpr().getX());
                int crY = getGridY(crystal.getSpr().getY());
                for(Unit un : units) {
                    if(un.getTeam() != team && un.getTargetable() && getDistance(crX, un.getGridX(), crY, un.getGridY())
                            <= (ab2Active ? 2.5 : 1.5)) {
                        int dmg = 23 + (2 * level) + (mag);
                        float multiplier = ab2Active ? 1.3f : 1;
                        un.takeMagDmg((int)(dmg * multiplier), this);
                        if(ab2Active)
                            un.getStatus().add(StatusEffect.stun);
                        crystalSpr.getSpr().setSize(146, 146);
                    }
                }
                particles.remove(crystal);
                crystal.remove();
                crystals[i] = null;
            }
        }
        ab2Active = false;
        if(ab3Dur > 0) {
            ab3Dur--;
            if(ab3Dur == 0)
                cd[2] = maxCd[2] - 3;
        }
        if(ab4Dur > 0) {
            ab4Dur--;
            if(ab4Dur == 0)
                ab4();
        }
    }
}
