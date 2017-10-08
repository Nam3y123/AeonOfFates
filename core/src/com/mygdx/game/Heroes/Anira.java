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

public class Anira extends Unit {
    private Unit ab1Target;
    private ArrayList<Unit> ab3Targets;
    private final StatusEffect.MoveRule ab3Effect = new StatusEffect.MoveRule(){
        public boolean viableTile(Unit target, int tileX, int tileY) {
            return Math.sqrt(Math.pow(tileX - getGridX(), 2) + Math.pow(tileY - getGridY(), 2)) >= 1.5;
        }
    };

    public Anira(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 13;
        def = 12;
        mag = 27;
        hp = maxHp;
        name = "Anira";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Anira_Particles.png")), 0, 0, 32, 32);
        //charCol = new Color(0.375f, 0.375f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        spr.getTexture().dispose();
        setSpr("Spritesheets/Characters/Anira.atlas", x, y);
        ab1Target = null;
        res = 0;
        maxRes = 500;
        ab3Targets = new ArrayList<>();

        abMenu.add(new MenuOption("Hugs!", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1_a", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Anihilation!", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    ab2();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Keep Away!", combatSkin, "combatMenu") {
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

        statusEffect.add(new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source) {
                StatusEffect hasPassive = null;
                for(StatusEffect s : target.getStatus())
                    if(s.getType().equals("Dummy_AniraPassive"))
                        hasPassive = s;
                int level = 0;
                if(hasPassive != null) {
                    level = hasPassive.getLevel() + 1;
                    hasPassive.setDuration(3);
                    if(level == 3)
                        target.getStatus().add(StatusEffect.slow(1));
                    if(level == 7)
                        level = 6;
                    hasPassive.setLevel(level);
                } else {
                    hasPassive = new StatusEffect("Dummy_AniraPassive") {
                        @Override
                        public void end() {
                            target.getStatus().remove(this);
                        }
                    };
                    hasPassive.setLevel(1);
                    hasPassive.setDuration(3);
                    target.getStatus().add(hasPassive);
                }
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 4;
        maxCd[2] = 4;
        maxCd[3] = 14;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = false;
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            switch (doing) {
                case "ab1_a":
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "line", 3, Color.BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab1_Dash":
                    Tile.drawShape(batch, parentAlpha, (int)ab1Target.getX(), (int)ab1Target.getY() - 4, "circle", 3, Color.BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab3":
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "line", 3, Color.BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
            }
        }
        return ret;
    }

    @Override
    public void clickAct() {
        super.clickAct();
        Tile.setClickPos();
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        switch (doing) {
            case "ab1_a":
                ab1(cX, cY);
                break;
            case "ab1_Dash":
                ab1Dash(cX, cY);
                break;
            case "ab3":
                ab3(cX, cY);
                break;
            default:
                break;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(ab1Target != null && (ab1Target.getMoving() || moving))
            if(getDistance(getGridX(), ab1Target.getGridX(), getGridY(), ab1Target.getGridY()) > 5.5f)
                ab1Target = null;
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab1Target != null) {
            Unit.sel = getUnPosition();
            game.getRoot().addAction(Actions.moveTo(-spr.getX() + Gdx.graphics.getWidth() / 2 - spr.getWidth() / 2,
                    -spr.getY() + Gdx.graphics.getHeight() / 2 - spr.getHeight() / 2, 0.1f));
            for(Unit un : units)
                un.removeListener(un.getClick());
            doing = "ab1_Dash";
        }
        for(Unit un : ab3Targets)
            un.getStatus().remove(ab3Effect);
    }

    private void ab1(int cX, int cY) {
        float dir = getDirection(getGridX(), cX, getGridY(), cY);
        particleRegion.setRegion(32, 0, 32, 32);
        Projectile proj = new Projectile(this, dir, getX() + 8, getY() + 4, particleRegion, 4, 3f, 32) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                spr.setSize(getDistance(Anira.this.getX() + 8, getX(), Anira.this.getY() + 8, getY()) + 24, 32);
                spr.setOrigin(0, 16);
                spr.setRotation(dir);
                spr.setPosition(Anira.this.getX() + 24, Anira.this.getY() + 8);
                spr.draw(batch, parentAlpha);
            }

            @Override
            public void effect(Unit un) {
                if(un.getTeam() != Anira.this.getTeam()) {
                    un.takeMagDmg(12 + (int)(0.4 * mag) + (int)(0.34f * level), sourceUn);
                }
                ab1Target = un;
                particleRegion.setRegion(32, 0, 32, 32);
                particles.add(new Particle(particleRegion, (int)Anira.this.getX() + 24, (int)Anira.this.getY() + 8) {
                    @Override
                    public void start() {
                        spr.setOrigin(0, 16);
                        updatePos();
                    }

                    @Override
                    public void increment() {
                        if(ab1Target == null) {
                            particles.remove(this);
                            return;
                        }
                        if(Anira.this.moving || ab1Target.getMoving()) {
                            updatePos();
                        }
                    }

                    private void updatePos() {
                        Anira an = Anira.this;
                        float dir = an.getDirection(an.getX(), ab1Target.getX(), an.getY(), ab1Target.getY());
                        float dist = getDistance(an.getX(), ab1Target.getX(), an.getY(), ab1Target.getY());
                        spr.setPosition(an.getX() + 24, an.getY() + 8);
                        spr.setRotation(dir);
                        spr.setSize(dist, 32);
                    }
                });
                remove();
            }
        };
        getParent().addActor(proj);
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

    private void ab1Dash(int cX, int cY) {
        boolean pathOpen = Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE);
        for (Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY)
                pathOpen = false;
        if(cX == getGridX() && cY == getGridY())
            pathOpen = true;
        if(pathOpen) {
            doing = "Stand";
            if(ab1Target.getTeam() != team)
                ab1Target.takeMagDmg(12 + (int)(0.4 * mag) + (int)(0.34f * level), this);
            setPosition(cX * 48 + 8, cY * 48 + 4);
            ab1Target = null;
            cd[0] = 0;
        }
    }

    private void ab2() {

    }

    private void ab3(int cX, int cY) {
        int abX = getGridX();
        int abY = getGridY();
        if(cX > getGridX())
            abX += 3;
        else if (cX < getGridX())
            abX -= 3;
        else if (cY > getGridY())
            abY += 3;
        else if (cY < getGridY())
            abY -= 3;

        boolean pathOpen = Tile.walls.getPixel(abX, abY) != Color.rgba8888(Color.WHITE);
        for (Unit un : units)
            if(un.getGridX() == abX && un.getGridY() == abY)
                pathOpen = false;
        if(pathOpen) {
            addAction(Actions.sequence(Actions.moveTo(abX * 48 + 8, abY * 48 + 4, 0.25f),
                    Actions.run(() -> ab3Dmg())));
            doing = "ab3Anim";
            MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
            cd[2] = maxCd[2];
            if(secondary)
                primary = true;
            else
                secondary = true;
            hasMoved = true;
        }
    }

    private void ab3Dmg() {
        for(Unit un : units) {
            if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 2.5f) {
                float[] shortestDistance = new float[] {999, un.getGridX(), un.getGridY()};
                for(int x = -3; x <= 3; x++)
                    for(int y = -3; y <= 3; y++) {
                        float dist = getDistance(x, 0, y, 0);
                        if(dist <= 3.5 && dist > 2.5) {
                            boolean tileOpen = Tile.walls.getPixel(getGridX() + x, getGridY() + y) != Color.rgba8888(Color.WHITE);
                            dist = getDistance(un.getGridX(), getGridX() + x, un.getGridY(), getGridY() + y);
                            if(tileOpen && (dist < shortestDistance[0] || (dist == shortestDistance[0] &&
                                    ((un.getTeam() && getGridX() + x < shortestDistance[1]) || (!un.getTeam() &&
                                            getGridX() + x > shortestDistance[1]))))) { // I give up
                                shortestDistance = new float[] {dist, getGridX() + x, getGridY() + y};
                            }
                        }
                    }
                un.setPosition(shortestDistance[1] * 48 + 8, shortestDistance[2] * 48 + 4);
                un.takeMagDmg((int)(14 + level + (0.65 * mag)), this);
                ab3Targets.add(un);
                un.getStatus().add(ab3Effect);
            }
        }
        doing = "Stand";
    }
}
