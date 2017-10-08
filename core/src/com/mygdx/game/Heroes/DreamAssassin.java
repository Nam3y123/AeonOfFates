package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;

public class DreamAssassin extends Unit {
    private int passiveCd;
    private int ab2Dur, ab2X, ab2Y;
    private int ab3Dur;
    private Unit ab3Target;

    private final int maxPassiveCd = 3;
    private final ArrayList<Unit> ab2HitUnits = new ArrayList<>();

    public DreamAssassin(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 205;
        atk = 17;
        def = 12;
        mag = 32;
        hp = maxHp;
        name = "DreamAssassin";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/DreamAssassin_Particles.png")),0, 0, 32, 32);
        charCol = Color.VIOLET;

        passiveCd = 0;
        ab2Dur = 0;

        abMenu.add(new MenuOption("Fractured Reality", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Dreamscape", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0) {
                    if(ab2Dur == 0)
                        ab2Cast();
                    else if((!secondary || !primary))
                        ab2();
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Mental Scars", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary)) {
                    if(ab3Dur > 0)
                        setDoing("ab3", false);
                    else
                        ab3();
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("TBA", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void effect(Unit target, Unit source, int dmgTaken) {
                if(passiveCd == 0) {
                    int dmg = (int)(5 + (0.5 * level) + (0.3 * mag));
                    float hpRatio = 1 - (target.getHp() / (float)target.getMaxHp());
                    target.takeMagDmg(dmg + (int)(2 * dmg * hpRatio));
                    passiveCd = maxPassiveCd;
                }
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 5;
        maxCd[2] = 3;
        maxCd[3] = 12;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(doing.equals("ab2Anim"))
            for(Unit un: units)
                if(un.getTeam() != team && un.getTargetable() && !ab2HitUnits.contains(un) && getDistance(getX(), un.getX(), getY(),
                        un.getY()) <= 48) {
                    ab2HitUnits.add(un);
                    un.takeMagDmg((int)(8 + (0.6 * mag) + (1.25 * level)));
                }
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
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 4, Color.BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3Sel":
                        Tile.drawShape(batch, parentAlpha, ab3Target.getX(), ab3Target.getY() - 4, "circle", 1,
                                Color.BLUE, true);
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
        super.clickAct();
        switch(doing) {
            case "ab1":
                ab1();
                break;
            case "ab3":
                for(Unit un: units)
                    if(un.getGridX() == getGridX(Tile.clickX) && un.getGridY() == getGridY(Tile.clickY)) {
                        ab3Target = un;
                        doing = "ab3Sel";
                    }
                break;
            case "ab3Sel":
                ab3Sel();
                break;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(ab2Dur > 0) {
            spr.setPosition(-getX() + (96 * ab2X) + 16, -getY() + (96 * ab2Y) + 8);
            spr.setAlpha(0.5f);
            spr.draw(batch);
            spr.setAlpha(1f);
            spr.setPosition(getX(), getY());
        }
    }

    private void ab1() {
        float dir = getDirection(getGridX(), getGridX(Tile.clickX), getGridY(), getGridY(Tile.clickY));
        particleRegion.setRegion(0, (int)(dir * 32 / 90f), 32, 32);
        getParent().addActor(new Projectile(this, team, dir, getX() +  96 * (float)Math.cos(Math.toRadians(dir)),
                getY() +  96 * (float)Math.sin(Math.toRadians(dir)), particleRegion, 4, 2) {

            @Override
            public void effect(Unit un) {
                int sourceX = un.getGridX() - (int)Math.cos(Math.toRadians(dir));
                int sourceY = un.getGridY() - (int)Math.sin(Math.toRadians(dir));
                for(Unit areaUn : units) {
                    if(areaUn.getTeam() != team && un.getTargetable() && getDistance(sourceX, areaUn.getGridX(), sourceY, areaUn.getGridY())
                            <= 1.5) {
                        areaUn.takeMagDmg((int)(6 + (0.45 * mag) + (0.375 * level)), DreamAssassin.this);
                        int destX = 2 * areaUn.getGridX() - sourceX;
                        int destY = 2 * areaUn.getGridY() - sourceY;
                        boolean tileFree = Tile.walls.getPixel(destX,  destY) != Color.rgba8888(Color.WHITE);
                        for(Unit tileUn: units)
                            if(tileUn.getGridX() == destX && tileUn.getGridY() == destY)
                                tileFree = false;
                        if(tileFree)
                            areaUn.addAction(Actions.moveTo(destX * 48 + 8, destY * 48 + 4, 0.15f));
                    }
                }
                doing = "Stand";
                this.remove();
            }
        });

        for(Unit un: units)
            if(un.getGridX() == getGridX(getX() + 48 * (float)Math.cos(Math.toRadians(dir)))
                    && un.getGridY() == getGridY(getY() + 48 * (float)Math.sin(Math.toRadians(dir))))
                un.takeMagDmg((int)(12 + (0.9 * mag) + (0.75 * level)), this);

        doing = "ab1Anim";
        cd[0] = maxCd[0];
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab2Cast() {
        ab2Dur = 3;
        ab2X = getGridX();
        ab2Y = getGridY();
        abMenu.setVisible(false);
        showMenu();

        particleRegion.setRegion(32, 0, 32, 32);
        particles.add(new Particle(particleRegion, ab2X * 48 + 16, ab2Y * 48 + 8) {

            @Override
            public void increment() {
                super.increment();
                if(ab2Dur == 0) {
                    particles.remove(this);
                }
            }
        });
    }

    private void ab2() {
        int destX = getGridX() - 2 * (getGridX() - ab2X);
        int destY = getGridY() - 2 * (getGridY() - ab2Y);
        boolean tileFree = Tile.walls.getPixel(destX,  destY) != Color.rgba8888(Color.WHITE);
        for(Unit un: units)
            if(un.getGridX() == destX && un.getGridY() == destY)
                tileFree = false;
        if(tileFree) {
            ab2HitUnits.clear();
            addAction(Actions.sequence(Actions.moveTo(destX * 48 + 8, destY * 48 + 4, 0.025f * getDistance(destX,
                    getGridX(), destY, getGridY())), Actions.run(() -> doing = "Stand")));

            abMenu.setVisible(false);
            doing = "ab2Anim";
            cd[1] = -1;
            hasMoved = true;
            MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private void ab3() {
        for(Unit un: units)
            if(un.getTeam() != team && un.getTargetable() && getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY()) <= 1.5f) {
                un.takeMagDmg((int)(8 + (0.5 * mag) + level));
                un.getStatus().add(new StatusEffect("DA_ab3Dummy") {
                    @Override
                    public void start() {
                        duration = 3;
                    }

                    @Override
                    public void end() {
                        un.getStatus().remove(this);
                        remove();
                    }
                });
            }
        ab3Dur = 3;

        abMenu.setVisible(false);
        showMenu();
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab3Sel() {
        int destX = getGridX(Tile.clickX);
        int destY = getGridY(Tile.clickY);
        boolean tileFree = Tile.walls.getPixel(destX,  destY) != Color.rgba8888(Color.WHITE);
        for(Unit un: units)
            if(un.getGridX() == destX && un.getGridY() == destY)
                tileFree = false;
        if(tileFree) {
            addAction(Actions.moveTo(destX * 48 + 8, destY * 48 + 4));
            ab3Target.takeMagDmg((int)(8 + (0.5 * mag) + level));

            abMenu.setVisible(false);
            doing = "Stand";
            showMenu();
            cd[2] = maxCd[2] - 3 + ab3Dur;
            if(secondary)
                primary = true;
            else
                secondary = true;
            for(Unit un: units)
                if(un.getTeam() != team && un.getTargetable())
                    for(int i = 0; i < un.getStatus().size(); i++)
                        if(i < un.getStatus().size() && un.getStatus().get(i).getType().equals("DA_ab3Dummy")) {
                            un.getStatus().remove(i);
                            break;
                        }
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(cd[1] == -1 && ab2Dur > 0)
            cd[1] = 0;
        if(ab2Dur > 0) {
            ab2Dur--;
            if(ab2Dur == 0)
                cd[1] = maxCd[1] - 3;
        }
        if(ab3Dur > 0)
            ab3Dur--;
    }
}
