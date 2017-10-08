package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.*;

import java.util.ArrayList;

public class Vynn extends Unit {

    public Vynn(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 17;
        def = 12;
        mag = 27;
        hp = maxHp;
        name = "Vynn";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Vynn_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 0.375f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        res = 100;
        maxRes = 100;

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
                if(cd[1] == 0 && (!primary || !secondary))
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
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnAttack") {
            @Override
            public void effect(Unit target, Unit source) {
                StatusEffect hasPassive = null;
                for(StatusEffect s : target.getStatus())
                    if(s.getType().equals("Dummy_VynnPassive"))
                        hasPassive = s;
                int level = 0;
                if(hasPassive != null) {
                    level = hasPassive.getLevel();
                    hasPassive.setLevel(level + 1);
                    hasPassive.setDuration(3);
                } else {
                    hasPassive = new StatusEffect("Dummy_VynnPassive") {
                        @Override
                        public void end() {
                            target.getStatus().remove(this);
                        }
                    };
                    hasPassive.setLevel(1);
                    hasPassive.setDuration(3);
                    target.getStatus().add(hasPassive);
                }
                res += 10 + (5 * level);
            }
        });

        maxCd[0] = 1;
        maxCd[1] = 4;
        maxCd[2] = 4;
        maxCd[3] = 14;
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
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, Color.BLUE, true);
                        Tile.setClickPos();
                        if(Tile.hit) {
                            if(getGridX(Tile.clickX) < getGridX())
                                Tile.drawRect(batch, parentAlpha, getGridX(Tile.clickX) * 48 + 56, getGridY(Tile.clickY) * 48, 5, 1, Color.GREEN);
                            else if(getGridX(Tile.clickX) > getGridX())
                                Tile.drawRect(batch, parentAlpha, getGridX(Tile.clickX) * 48 - 232, getGridY(Tile.clickY) * 48, 5, 1, Color.GREEN);
                            else if(getGridY(Tile.clickY) < getGridY())
                                Tile.drawRect(batch, parentAlpha, getGridX(Tile.clickX) * 48 + 8, getGridY(Tile.clickY) * 48 + 48, 1, 5, Color.GREEN);
                            else if(getGridY(Tile.clickY) > getGridY())
                                Tile.drawRect(batch, parentAlpha, getGridX(Tile.clickX) * 48 + 8, getGridY(Tile.clickY) * 48 - 240, 1, 5, Color.GREEN);
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
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        super.clickAct();
        switch (doing) {
            case "ab1":
                boolean hit = false;
                for(int i = 0; i < units.size(); i++) {
                    Unit un = units.get(i);
                    if(un.getGridX() == cX && un.getGridY() == cY && un.getTeam() != team && un.getTargetable()) {
                        hit = true;
                    }
                }
                if(hit)
                    ab1();
                break;
            case "ab3":
                break;
            default:
                break;
        }
    }

    private void ab1() {
        cd[0] = maxCd[0];
        if(secondary)
            primary = true;
        else
            secondary = true;
        res -= 20 + (5 * level);
        particleRegion.setRegion(32, 0, 32, 32);
        particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
            private int destX, destY, curY;
            private float trajectoryX, trajectoryY;

            @Override
            public void start() {
                spr.setOriginCenter();
                spr.setScale(0.75f);

                destX = getGridX(Tile.clickX) * 48;
                destY = getGridY(Tile.clickY) * 48;
                trajectoryX = (getX() - destX) / 15;
                trajectoryY = (getY() - destY) / 15;
                curY = (int)getY();
            }

            @Override
            public void increment() {
                curY -= trajectoryY;
                spr.translateX(-trajectoryX);
                spr.rotate(15);
                spr.setY(curY + 32 - (int)Math.pow(duration - 8, 2) / 2);
                if(duration == 15) {
                    spr.setAlpha(0);
                    ab1Dmg();
                }
                if(duration >= 20) {
                    this.remove();
                    particles.remove(this);
                    doing = "Stand";
                }
                duration++;
            }
        });
    }

    private void ab1Dmg() {
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), cX, un.getGridY(), cY) <= 1.5f) {
                StatusEffect hasPassive = null;
                for(StatusEffect s : un.getStatus())
                    if(s.getType().equals("Dummy_VynnPassive"))
                        hasPassive = s;
                int passiveLvl = 0;
                if(hasPassive != null)
                    passiveLvl = hasPassive.getLevel();
                un.takeMagDmg((int)(7 + (0.4 * mag) + (2 * level) + (passiveLvl * (4 + (0.1 * mag) + (0.5 * level)))), this);
            }
    }

    private void ab2() {
        int shieldAmt = (int)(17 + (0.4 * mag) + (3 * level));
        addShield(shieldAmt);
        shields.add(shieldAmt);
        int bonusDef = 3 + (int)(0.5 * level);
        int bonusMDef = 3 + (int)(0.5 * level);
        def += bonusDef;
        mdef += bonusMDef;
        getStatus().add(new StatusEffect("OnDamaged") {
            private int position = shields.size() - 1;

            @Override
            public void start() {
                duration = 1;
            }

            @Override
            public void end() {
                if(shields.size() > position) {
                    addShield(-shields.get(position));
                    shields.remove(position);
                }
                def -= bonusDef;
                mdef -= bonusMDef;
            }
        });
    }
}
