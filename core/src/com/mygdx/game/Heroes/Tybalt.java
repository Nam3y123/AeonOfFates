package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.CORAL;
import static com.mygdx.game.Tile.*;

public class Tybalt extends Unit {
    private boolean passive;
    private int ab1X, ab1Y;
    private boolean ab1Active;
    private int ab4X, ab4Y, ab4Dur;
    private HashMap<Unit, StatusEffect> hasPassive;

    public Tybalt(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 23;
        def = 20;
        mag = 7;
        hp = maxHp;
        name = "Tybalt";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Tybalt_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 0.375f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        res = 20;
        maxRes = 20;
        resSegmentSize = 5;
        resBigInterval = 2;
        resCol = Color.YELLOW;
        passive = true;
        ab1X = 0;
        ab1Y = 0;
        ab1Active = false;
        ab4X = 0;
        ab4Y = 0;
        ab4Dur = 0;
        hasPassive = new HashMap<>();

        abMenu.add(new MenuOption("Pain", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && res >= 5 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Broadsword Block", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && res >= 3)
                    ab2();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("SURPRISE!!!", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && res >= 5 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Slash and Burn", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && res >= 5 && (!primary || !secondary))
                    ab4();
            }
        });

        statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void effect(Unit target, Unit source) {
                if(res < 20)
                    res++;
            }
        });

        statusEffect.add(new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source, int dmg) {
                if(hasPassive.containsKey(target)) {
                    float multiplier = (float)(0.5 * Math.pow(0.4, 4 - hasPassive.get(target).getDuration()));
                    target.takeTrueDmg((int)(dmg * multiplier));
                }
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 1;
        maxCd[2] = 4;
        maxCd[3] = 12;
    }

    @Override
    public void showMenu() {
        if(passive) {
            passive = false;
            for(Unit un: units)
                if(!hasPassive.containsKey(un) && un.getTeam() != team && getDistance(getGridX(), un.getGridX(), getGridY(),
                        un.getGridY()) <= 3.5f)
                    passive = true;
        }
        if(passive)
            doing = "passive";
        else
            super.showMenu();
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            if(num == sel) {
                switch(doing) {
                    case "passive":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                        break;
                    case "ab1":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 1, BLUE, true);
                        Tile.setClickPos();
                        if(Tile.hit) {
                            int cX = getGridX(clickX - 4) * 48 + 8;
                            int cY = getGridY(clickY) * 48;
                            Color col = Color.RED;
                            for(Unit un: units) {
                                if(un.getGridX() == getGridX(clickX - 4) && un.getGridY() == getGridY(clickY) &&
                                        un.getTargetable() && un.getTeam() != team)
                                    col = new Color(1, 1, 1, 1);
                            }
                            if(!col.equals(Color.RED))
                                Color.rgba8888ToColor(col, Color.rgba8888(0.33f, 1, 0.33f, 1));
                            drawShape(batch, parentAlpha, cX, cY, "circle", 2, col, true);
                            if(!col.equals(Color.RED))
                                Color.rgba8888ToColor(col, Color.rgba8888(0, 0.83f, 0, 1));
                            drawRing(batch, parentAlpha, cX, cY, 4, 2, col, false);
                        }
                        abMenu.setVisible(false);
                        ret = true;
                        break;
                    case "ab3":
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
        switch (doing) {
            case "passive":
                passive();
                break;
            case "ab1":
                ab1_a();
                break;
            case "ab3":
                ab3();
                break;
        }
    }

    private void passive() {
        Unit hit = null;
        int cGridX = getGridX(clickX - 4);
        int cGridY = getGridY(clickY);
        for(Unit un : units)
            if(un.getGridX() == cGridX && un.getGridY() == cGridY && un.getTargetable() && un.getTeam() != team &&
                    !hasPassive.containsKey(un))
                hit = un;
        if(hit != null) {
            final Unit TARGET = hit;
            hasPassive.put(hit, new StatusEffect("OnAlTurn") {
                @Override
                public void start() {
                    this.duration = 4;
                }

                @Override
                public void end() {
                    TARGET.getStatus().remove(this);
                    hasPassive.remove(TARGET);
                }
            });
            hit.getStatus().add(hasPassive.get(hit));
            passive = false;
            doing = "Stand";
            showMenu();
        }
    }

    private void ab1_a() {
        boolean hit = false;
        int cGridX = getGridX(clickX - 4);
        int cGridY = getGridY(clickY);
        for(Unit un : units)
            if(un.getGridX() == cGridX && un.getGridY() == cGridY && un.getTargetable() && un.getTeam() != team)
                hit = true;
        if(hit) {
            for(Unit un : units)
                if(getDistance(un.getGridX(), cGridX, un.getGridY(), cGridY) <= 2.5f && un.getTargetable() &&
                        un.getTeam() != team)
                    un.takePhysDmg((int)(15 + (atk) + (0.66667 * level)), this);

            particleRegion.setRegion(32, 0, 144, 144);
            particles.add(new Particle(particleRegion, cGridX * 48 - 184, cGridY * 48 - 192) {
                @Override
                public void start() {
                    spr.setSize(432, 432);
                }

                @Override
                public void increment() {
                    super.increment();
                    if(!ab1Active)
                        particles.remove(this);
                }
            });

            hasMoved = true;
            doing = "Stand";
            if(secondary)
                primary = true;
            else
                secondary = true;
            res -= 5;
            cd[0] = maxCd[0];
            ab1X = cGridX;
            ab1Y = cGridY;
            ab1Active = true;
        }
    }

    private void ab1_b() {
        ab1Active = false;
        for(Unit un: units) {
            float dist = getDistance(un.getGridX(), ab1X, un.getGridY(), ab1Y);
            if(dist > 2.5f && dist < 4.5f && un.getTargetable() && un.getTeam() != team) {
                un.takePhysDmg((int)(30 + (atk) + (1.33333 * level)), this);
                un.getStatus().add(StatusEffect.stun);
            }
        }
    }

    private void ab2() {
        cd[1] = maxCd[1];
        passive = true;

        float defBonus = 3 + level;
        for(Unit un: units)
            if(getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 2.5f && un.getTargetable()
                    && un.getTeam() != team)
                defBonus += 3 + (0.25 * level);

        final int FINAL_DEF_BONUS = (int)defBonus;
        statusEffect.add(new StatusEffect("OnAlTurn") {
            @Override
            public void effect(Unit target, Unit source) {
                def -= FINAL_DEF_BONUS;
                statusEffect.remove(this);
            }
        });
        def += FINAL_DEF_BONUS;
    }

    private void ab3() {
        // Search for enemies
        int cGridX = getGridX(clickX - 4);
        int cGridY = getGridY(clickY);
        Unit target = null;
        double dir = Math.toRadians(Math.floor((getDirection(getGridX(), cGridX, getGridY(), cGridY) + 45) / 90f) * 90);
        int locX = getGridX();
        int locY = getGridY();
        for(int i = 0; i < 3; i++) {
            locX += Math.round(Math.cos(dir)); // Just to make absolute sure that it is adding 1 or 0 and not something dumb like 0.99999998
            locY += Math.round(Math.sin(dir)); // ^ What he said
            for(Unit un : units)
                if(un.getGridX() == locX && un.getGridY() == locY && un.getTargetable() && un.getTeam() != team) {
                    target = un;
                }
            if(target != null)
                break;
        }

        // Search for a place to dash to
        boolean locFound = false;
        while(!locFound) {
            locFound = true;
            for(Unit un : units)
                if(un.getGridX() == locX && un.getGridY() == locY)
                    locFound = false;
            if(Tile.walls.getPixel(locX, locY) == Color.rgba8888(Color.WHITE))
                locFound = false;
            if(locX < 0 || locX > Tile.walls.getWidth() || locY < 0 || locY > Tile.walls.getHeight())
                locFound = false;
            if(locX == getGridX() && locY == getGridY())
                locFound = true;
            if(!locFound) {
                locX -= Math.round(Math.cos(dir));
                locY -= Math.round(Math.sin(dir));
            }
        }
        final Unit TARGET = target;
        addAction(Actions.sequence(Actions.moveTo(locX * 48 + 8, locY * 48 + 4, getDistance(getGridX(), locX,
                getGridY(), locY) * 0.15f), Actions.run(() -> {
                    if(TARGET != null) {
                        int dmg = (int)(17 + (0.75 * atk) + (1.3333 * level));
                        if(TARGET.getHp() >= TARGET.getMaxHp() * 0.95f)
                            dmg *= 2;
                        TARGET.takePhysDmg(dmg, Tybalt.this);
                    }
                })));
        hasMoved = true;
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
        res -= 5;
        cd[2] = maxCd[2];
        passive = true;
    }

    private void ab4() {
        for(Unit un : units)
            if(getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 2.5f && un.getTargetable() &&
                    un.getTeam() != team)
                un.takePhysDmg((int)(18 + (1.15 * atk) + (1.25 * level)), this);

        RunnableAction run = new RunnableAction();
        run.setRunnable(new Runnable() {
            int loops = 0;
            @Override
            public void run() {
                spOfsY = 49 - (float)Math.pow(loops - 7, 2);
                loops++;
            }
        });
        addAction(Actions.sequence(Actions.repeat(15, run), Actions.run(() -> {
            ab4X = getGridX();
            ab4Y = getGridY();
            ab4Dur = 3;
            ab4Dmg();
        })));

        hasMoved = true;
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
        res -= 5;
        cd[3] = maxCd[3];
        passive = true;
    }

    private void ab4Dmg() {
        for(Unit un : units)
            if(getDistance(un.getGridX(), ab4X, un.getGridY(), ab4Y) <= 2.5f && un.getTargetable() &&
                    un.getTeam() != team)
                un.takeTrueDmg((int)(10 + (0.45 * atk) + (0.5 * level)), this);
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab1Active)
            ab1_b();
        passive = true;
    }
}
