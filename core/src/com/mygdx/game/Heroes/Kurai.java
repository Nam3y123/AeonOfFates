package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Kurai extends Unit {
    private boolean inCombat;
    private int baseAtk;

    public Kurai(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 32;
        def = 14;
        mag = 10;
        hp = maxHp;
        name = "Kurai";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Annah_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 0.375f, 0.375f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        res = 9;
        maxRes = 9;
        resCol = new Color(0.25f, 0.25f, 0.75f, 1);
        resSegmentSize = 1;
        resBigInterval = 3;
        inCombat = false;
        baseAtk = atk;

        abMenu.add(new MenuOption("Sandy Shuffle", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary) && res >= 3)
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Smoke Bomb", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary) && res >= 5)
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Shadow Slash", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary) && res >= 4)
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Shadow Puppet", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0)
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void effect(Unit target, Unit source) {
                if(res <= 6) {
                    target.takePhysDmg((int)(7 + (0.4 * atk) + level));
                    res += 3;
                }
            }
        });
        statusEffect.add(new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source) {
                inCombat = true;
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 4;
        maxCd[2] = 4;
        maxCd[3] = 18;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha)) {
            return true;
        } else {
            boolean ret = true;
            switch (doing) {
                case "ab1":
                    Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, Color.BLUE, true);
                    break;
                default:
                    ret = false;
            }
            if(ret)
                abMenu.setVisible(false);
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
                ab1(cX, cY);
                break;
        }
    }

    private void ab1(int cX, int cY) {
        float dir = getDirection(getGridX(), cX, getGridY(), cY);
        particleRegion.setRegion(0, 0, 32, 32);
        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2 - 4, particleRegion, 4, 5) {
            private boolean hasHitEnemy = false;

            @Override
            public void effect(Unit un) {
                if(un.getTargetable() && un.getTeam() != team) {
                    float dmg = 12 + (0.8f * (atk - baseAtk)) + (1.25f * level);
                    if(!hasHitEnemy)
                        dmg *= 1.5f;
                    un.takePhysDmg((int)dmg, Kurai.this);
                    if(!hasHitEnemy) {
                        ArrayList<StatusEffect> se = statusEffect;
                        for (int i = 0; i < se.size(); i++)
                            if (Objects.equals(se.get(i).getType(), "OnHit"))
                                se.get(i).effect(un, Kurai.this, (int)dmg);
                    }
                    hasHitEnemy = true;
                }
            }

            @Override
            public boolean remove() {
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        doing = "Stand";
                    }
                }, 0.25f);
                return super.remove();
            }
        });
        res -= 3;
        doing = "ab1Anim";
        primary = true;
        cd[0] = maxCd[0];
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(inCombat)
            res--;
        else
            res++;
        inCombat = false;
    }
}
