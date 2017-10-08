package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Objects;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.*;
import static com.mygdx.game.Tile.*;

public class Markus extends Unit {
    private int passiveCd;
    private boolean passiveOn;

    public Markus(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 245;
        atk = 18;
        mag = 10;
        def = 20;
        hp = maxHp;
        name = "Malaki";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Malaki_Particles.png")),0, 0, 32, 32);
        charCol = Color.CYAN;

        passiveOn = false;
        passiveCd = 0;

        abMenu.add(new MenuOption("Battle Cry", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    ab1();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Shield Toss", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!secondary || !primary))
                    Markus.this.setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Death Dive", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary))
                    Markus.this.setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Guardian Pact", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!secondary || !primary))
                    Markus.this.setDoing("ab4", false);
            }
        });

        addAction(Actions.run(() -> {
            for(Unit un : units)
                if(un.getTeam() == team && un.getTargetable())
                    un.getStatus().add(new StatusEffect("OnDamaged") {
                        private int healthStore = un.getHp();

                        @Override
                        public void effect(Unit target, Unit source) {
                            float dist = (float)Math.sqrt(Math.pow(un.getGridX() - getGridX(), 2)
                                    + Math.pow(un.getGridY() - getGridY(), 2));
                            if(dist <= 4.5 && un.getHp() <= 0.5f * un.getMaxHp()
                                    && healthStore > 0.5f * un.getMaxHp()) {
                                passiveCd = 4;
                                passiveOn = true;
                            }
                            healthStore = un.getHp();
                        }
                    });
        }));

        statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void effect(Unit target, Unit source) {
                if(passiveOn) {
                    target.getStatus().add(StatusEffect.stun);
                    target.getStatus().add(StatusEffect.mesmerize);
                    passiveOn = false;
                }
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 3;
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
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        Tile.setClickPos();
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 1, BLUE, true);
                        if (Tile.hit) {
                            Color col = RED;
                            for (Unit un : units)
                                if (un.getTeam() != team && un.getTargetable() && getGridX(clickX) == un.getGridX()
                                        && getGridY(clickY) == un.getGridY())
                                    col = GREEN;

                            if (getGridX(clickX) > getGridX())
                                drawRect(batch, parentAlpha, (int) getX() + 48, (int) getY() - 4, 3, 1, col);
                            else if (getGridY(clickY) > getGridY())
                                drawRect(batch, parentAlpha, (int) getX(), (int) getY() + 44, 1, 3, col);
                            else if (getGridX(clickX) < getGridX())
                                drawRect(batch, parentAlpha, (int) getX() - 144, (int) getY() - 4, 3, 1, col);
                            else if (getGridY(clickY) < getGridY())
                                drawRect(batch, parentAlpha, (int) getX(), (int) getY() - 148, 1, 3, col);
                        }
                        ret = true;
                        abMenu.setVisible(false);
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
        int cX = (int)Math.floor((Tile.clickX - 8) / 48);
        int cY = (int)Math.floor(Tile.clickY / 48);
        int stX = (int)Math.floor((getX() - 8) / 48);
        int stY = (int)Math.floor(getY() / 48);
        super.clickAct();
        if(doing.equals("ab2")) {
            cd[1] = maxCd[1];
            doing = "Stand";
            if(secondary)
                primary = true;
            else
                secondary = true;
            abMenu.setVisible(false);
            for(int i = 0; i < units.size(); i++) {
                Unit un = units.get(i);
                if(un.getGridX() == cX && un.getGridY() == cY && un.getTeam() == team && un.getTargetable()) {
                    int shieldAmt = (int)(15 + (0.3 * mag) + (0.3 * def));
                    ArrayList<Integer> shieldArray = un.getShieldArray();
                    un.addShield(shieldAmt);
                    shieldArray.add(shieldAmt);
                    un.getStatus().add(new StatusEffect("OnDamaged") {
                        private int position = shieldArray.size() - 1;
                        @Override
                        public void start() {
                            duration = 1;
                        }

                        @Override
                        public void effect(Unit target, Unit source) {
                            if(shieldArray.size() <= position) {
                                passiveOn = true;
                            }
                        }

                        @Override
                        public void end() {
                            if(shieldArray.size() > position) {
                                un.addShield(-shieldArray.get(position));
                                shieldArray.remove(position);
                            }
                        }
                    });
                }
            }
            hasMoved = true;
            MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
        } else if(doing.equals("ab3")) {
            cd[2] = maxCd[2];
            doing = "ab3Dash";
            primary = true;
            abMenu.setVisible(false);
            for(Unit un : units)
                if(un.getTeam() != team && un.getTargetable() && getGridX(Tile.clickX) == un.getGridX()
                        && getGridY(Tile.clickY) == un.getGridY()) {
                    int dmg = 3 + atk + (2 * level);
                    un.takePhysDmg(dmg);
                    ArrayList<StatusEffect> se = statusEffect;
                    for (int i = 0; i < se.size(); i++)
                        if (Objects.equals(se.get(i).getType(), "OnHit"))
                            se.get(i).effect(un, this);
                    int dir = (int)Math.ceil(getDirection(stX, cX, stY, cY)); // Radians for the cosine & sine
                    switch (dir) {
                        case 0:
                            un.addAction(Actions.sequence(Actions.moveBy(96, 0, 0.15f),
                                    Actions.moveTo(un.getX() + 96, un.getY())));
                            addAction(Actions.sequence(Actions.moveBy(96, 0, 0.15f),
                                    Actions.moveTo(getX() + 96, getY())));
                            break;
                        case 90:
                            un.addAction(Actions.sequence(Actions.moveBy(0, 96, 0.15f),
                                    Actions.moveTo(un.getX(), un.getY() + 96)));
                            addAction(Actions.sequence(Actions.moveBy(0, 96, 0.15f),
                                    Actions.moveTo(getX(), getY() + 96)));
                            break;
                        case 180:
                            un.addAction(Actions.sequence(Actions.moveBy(-96, 0, 0.15f),
                                    Actions.moveTo(un.getX() - 96, un.getY())));
                            addAction(Actions.sequence(Actions.moveBy(-96, 0, 0.15f),
                                    Actions.moveTo(getX() - 96, getY())));
                            break;
                        case 270:
                            un.addAction(Actions.sequence(Actions.moveBy(0, -96, 0.15f),
                                    Actions.moveTo(un.getX(), un.getY() - 96)));
                            addAction(Actions.sequence(Actions.moveBy(0, -96, 0.15f),
                                    Actions.moveTo(getX(), getY() - 96)));
                            break;
                    }
                }
            hasMoved = true;
            MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
        }
    }

    public void ab1() {
        for(Unit un : units)
            if(Math.sqrt(Math.pow(un.getGridX() - getGridX(), 2) + Math.pow(un.getGridY() - getGridY(), 2)) <= 2.5) {
                if(un.getTeam() == team && un.getTargetable()) {
                    un.getStatus().add(StatusEffect.slow(-1));
                    un.getStatus().add(new StatusEffect("OnEnTurn") {
                        private int atk = un.getAtk(), mag = un.getMag();
                        @Override
                        public void effect(Unit target, Unit source) {
                            un.setAtk(atk);
                            un.setMag(mag);
                        }
                    });
                    un.setAtk((int)(un.getAtk() * 1.25f));
                    un.setMag((int)(un.getMag() * 1.25f));
                } else {
                    un.getStatus().add(StatusEffect.slow(1));
                    un.getStatus().add(new StatusEffect("OnEnTurn") {
                        private int atk = un.getAtk(), mag = un.getMag();
                        @Override
                        public void effect(Unit target, Unit source) {
                            un.setAtk(atk);
                            un.setMag(mag);
                        }
                    });
                    un.setAtk((int)(un.getAtk() / 1.25f));
                    un.setMag((int)(un.getMag() / 1.25f));
                }
            }
        cd[0] = maxCd[0];
        abMenu.setVisible(false);
    }
}
