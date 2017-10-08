package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Objects;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static com.mygdx.game.Tile.clickY;
import static com.mygdx.game.Tile.drawRect;

public class Lumina extends Unit {
    private int passiveArmor;
    private float ab1X, ab1Y;
    private int ab3X, ab3Y, ab3Dur;
    private byte ab3Attach;
    private ArrayList<Unit> ab3Hit, ab3Slow, ab4Units;
    private final Rectangle ab4Area = new Rectangle(), unitArea = new Rectangle(0, 0, 1, 1);
    private int ab4Dur;
    private boolean ab4On;

    public Lumina(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        atk = 13;
        def = 20;
        mag = 16;
        maxHp = 230;
        hp = maxHp;
        range = 1;
        name = "Lumina";
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Lumina_Particles.png")));
        passiveArmor = 0;
        ab3Hit = new ArrayList<>();
        ab3Slow = new ArrayList<>();
        ab4Units = new ArrayList<>();
        ab4Dur = 0;
        ab4On = false;


        abMenu.add(new MenuOption("Illuminate", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Blinding Assault", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Beacon Of Light", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Luminous Guard", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnDamage"){
            @Override
            public void effect(Unit target, Unit source) {
                boolean hasPassive = false;
                for(StatusEffect se : target.getStatus())
                    if(Objects.equals(se.getType(), "LuminaPassive")) {
                        def -= passiveArmor;
                        se.effect(target, source);
                        def += passiveArmor;
                        hasPassive = true;
                    }
                if(!hasPassive) {
                    LuminaPassive passiveMark = new LuminaPassive();
                    target.getStatus().add(passiveMark);
                    passiveMark.setTarget(target);
                    def -= passiveArmor;
                    passiveMark.effect(target, source);
                    def += passiveArmor;
                }
            }
        });

        maxCd[0] = 2;
        maxCd[1] = 3;
        maxCd[2] = 4;
        maxCd[3] = 12;
    }

    @Override
    public void openAbMenu() {
        super.openAbMenu();
        if((primary && secondary) || cd[0] > 0)
            abMenu.getChildren().get(1).setColor(Color.DARK_GRAY);
        if((primary && secondary) || cd[1] > 0)
            abMenu.getChildren().get(2).setColor(Color.DARK_GRAY);
        if((primary && secondary) || cd[2] > 0)
            abMenu.getChildren().get(3).setColor(Color.DARK_GRAY);
        if((primary && secondary) || cd[3] > 0)
            abMenu.getChildren().get(4).setColor(Color.DARK_GRAY);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(sel > -1) {
            Unit selUn = units.get(sel);
            if(selUn.getTeam() != team && selUn.getTargetable() && selUn.getMoving() && getDistance(selUn.getGridX(), ab3X, selUn.getGridY(), ab3Y) <= 2.5)
                ab3Damage(selUn);
        }
        if(moving) {
            if(ab3Attach == 1) {
                ab3X = getGridX();
                ab3Y = getGridY();
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y) <= 2.5) {
                        ab3Damage(un);
                    }
            } else if(getGridX() == ab3X && getGridY() == ab3Y)
                ab3Attach = 1;
            if(getDistance(getGridX(), ab3X, getGridY(), ab3Y) <= 2.5)
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y) <= 2.5) {
                        ab3Damage(un);
                    }
        }
        for(Unit un : units)
            if(ab4Area.contains(un.getGridX(), un.getGridY()) && !ab4Units.contains(un)) {
                if(un.getTeam() == team && un.getTargetable())
                    un.setDmgReduction(0.85f);
                else
                    un.setDmgReduction(1.15f);
                ab4Units.add(un);
            } else if(!ab4Area.contains(un.getGridX(), un.getGridY()) && ab4Units.contains(un)) {
                if(un.getTeam() == team && un.getTargetable())
                    un.setDmgReduction(1 / 0.85f);
                else
                    un.setDmgReduction(1 / 1.15f);
                ab4Units.remove(un);
            }
        if(ab4Dur == 0)
            ab4On = false;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = super.drawTile(batch, parentAlpha);
        if(!ret)
            switch(doing) {
                case "ab1":
                    drawShape(batch, parentAlpha, (int) getX(), (int) getY() - 4, "line", 5, BLUE, true);
                    ret = true;
                    break;
                case "ab2":
                    drawShape(batch, parentAlpha, (int) getX(), (int) getY() - 4, "line", 5, BLUE, true);
                    ret = true;
                    break;
                case "ab3":
                    Tile.setClickPos();
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, BLUE, true);
                    if (Tile.hit) {
                        int tX = getGridX(clickX - 4);
                        int tY = getGridY(clickY);
                        drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 2, GREEN, false);
                    }
                    ret = true;
                    break;
                case "ab4":
                    clickX = (int) (input.getX() / (graphics.getWidth() / 640f)) - (int) game.getRoot().getX() - 4;
                    clickY = (int) ((graphics.getHeight() - input.getY()) / (graphics.getHeight() / 480f)) - (int) game.getRoot().getY();
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 1, BLUE, true);
                    if (Tile.hit) {
                        if (getGridX(clickX) > getGridX())
                            drawRect(batch, parentAlpha, (int) getX() + 48, (int) getY() - 52, 1, 3, GREEN);
                        else if (getGridY(clickY) > getGridY())
                            drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() + 44, 3, 1, GREEN);
                        else if (getGridX(clickX) < getGridX())
                            drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() - 52, 1, 3, GREEN);
                        else if (getGridY(clickY) < getGridY())
                            drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() - 52, 3, 1, GREEN);
                    }
                    ret = true;
                    abMenu.setVisible(false);
                    break;
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
                ab1(cX, cY);
                break;
            case "ab2":
                ab2(cX, cY);
                break;
            case "ab3":
                ab3(cX, cY);
                break;
            case "ab4":
                ab4(cX, cY);
                break;
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab3Dur > 0) {
            ab3Dur--;
            ab3Hit.clear();
            ab3Slow.clear();
            if(ab3Dur > 0) {
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y) <= 2.5) {
                        ab3Damage(un);
                    }
            } else {
                ab3X = -5;
                ab3Y = -5;
                ab3Attach = 0;
            }
        }
        if(ab4Dur > 0) {
            ab4Dur--;
            if(ab4Dur == 0) {
                ab4Area.setSize(0, 0);
            }
        }
    }

    private void ab1(int cX, int cY) {
        particleRegion.setRegion(64, 0, 32, 32);
        float dir = getDirection(getGridX(), cX, getGridY(), cY);
        getParent().addActor(new Projectile(this, team, dir, getX() + 8, getY() + 4, particleRegion, 4, 5) {
            @Override
            public void act(float delta) {
                float move = 48 / speed;
                this.moveBy((float)Math.cos(Math.toRadians(direction))*move, (float)Math.sin(Math.toRadians(direction)) * move);
                spr.setPosition(getX(), getY());
                spr.setOriginCenter();
                spr.rotate(3);
                for (Unit un : units) {
                    if (getDistance(getX() + 16, un.getX() + 24, getY() + 16, un.getY() + 24) <= 32
                            && !hitUnits.contains(un) && un.getTeam() != team && un.getTargetable()) {
                        effect(un);
                        hitUnits.add(un);
                    }
                }
                if(ab3Dur > 0 && getGridX(getX()) == ab3X && getGridY(getY()) == ab3Y)
                    ab3Attach = 2;
                if(ab3Attach == 2) {
                    ab3X = getGridX(getX());
                    ab3Y = getGridY(getY());
                    for(Unit un : units)
                        if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y) <= 2.5) {
                            ab3Damage(un, this);
                        }
                }
                duration--;
                if(duration == 0)
                    remove();
            }

            @Override
            public boolean remove() {
                particles.add(new Particle(particleRegion, (int)this.getX(), (int)this.getY(), "MaxRangeShield") {
                    @Override
                    public void increment() {
                        spr.setOriginCenter();
                        spr.rotate(3);
                    }
                });
                ab1X = this.getX() - 8;
                ab1Y = this.getY() - 4;
                if(ab3Attach == 2) {
                    ab3X = getGridX(ab1X);
                    ab3Y = getGridY(ab1Y);
                }

                particleRegion.setRegion(32, 16, 32, 32);
                particles.add(new Particle(particleRegion, (int)Lumina.this.getX(), (int)Lumina.this.getY(), "MaxRangeShield") {
                    @Override
                    public void start() {
                        spr.setOrigin(0, 16);
                        spr.setPosition(Lumina.this.getX() + 24, Lumina.this.getY() + 4);
                    }

                    @Override
                    public void increment() {
                        spr.setPosition(Lumina.this.getX() + 24, Lumina.this.getY() + 4);
                        spr.setSize(getDistance(Lumina.this.getX(), ab1X, Lumina.this.getY(), ab1Y), 32);
                        spr.setRotation(Lumina.this.getDirection(Lumina.this.getX(), ab1X, Lumina.this.getY(), ab1Y));
                    }
                });

                return super.remove();
            }

            @Override
            public void effect(Unit un) {
                un.takePhysDmg((int)(6.5 + (0.4 * mag) + (1.5 * level)), sourceUn);
            }
        });
        statusEffect.add(new StatusEffect("Dummy") {
            @Override
            public void start() {
                duration = 1;
            }

            @Override
            public void end() {
                statusEffect.remove(this);
                int i = 0;
                while(i < particles.size())
                    if(particles.get(i).getTag().equals("MaxRangeShield"))
                        particles.remove(particles.get(i));
                    else
                        i++;
                float direction = getDirection(ab1X, getX(), ab1Y, getY());
                float distance = getDistance(ab1X, getX(), ab1Y, getY()) / 48f;
                Unit.sel = units.indexOf(Lumina.this);
                particleRegion.setRegion(32, 0, 32, 16);
                getParent().addActor(new Projectile(Lumina.this, team, direction, ab1X + 8, ab1Y + 4, particleRegion, 3, distance) {
                    @Override
                    public void act(float delta) {
                        float move = 48 / speed;
                        this.moveBy((float)Math.cos(Math.toRadians(direction)) * move, (float)Math.sin(Math.toRadians(direction)) * move);
                        spr.setPosition(getX(), getY());
                        spr.setOriginCenter();
                        spr.rotate(3);
                        for (Unit un : units) {
                            if (getDistance(getX() + 16, un.getX() + 24, getY() + 16, un.getY() + 24) <= 32
                                    && !hitUnits.contains(un) && un.getTeam() != team && un.getTargetable()) {
                                effect(un);
                                hitUnits.add(un);
                            }
                        }
                        if(ab3Dur > 0 && getGridX(getX()) == ab3X && getGridY(getY()) == ab3Y)
                            ab3Attach = 2;
                        if(ab3Attach == 2) {
                            ab3X = getGridX(getX());
                            ab3Y = getGridY(getY());
                            for(Unit un : units)
                                if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y) <= 2.5) {
                                    ab3Damage(un, this);
                                }
                        }
                        duration--;
                        if(duration == 0)
                            remove();
                    }

                    @Override
                    public void effect(Unit un) {
                        un.takePhysDmg((int)(6.5 + (0.4 * mag) + (0.3 * def) + (1.5 * level)), sourceUn);
                        un.getStatus().add(StatusEffect.stun);
                    }
                });
            }
        });
        cd[0] = maxCd[0];
        if(secondary)
            primary = true;
        else
            secondary = true;
        doing = "ab1Anim";
    }

    private void ab2(int cX, int cY) {
        particleRegion.setRegion(0, 0, 0, 0);
        float dir = (float)Math.floor((getDirection(getGridX(), cX, getGridY(), cY) + 30) / 90) * 90;
        getParent().addActor(new Projectile(this, team, dir, getX(), getY(), particleRegion, 3, 5) {
            private int dur = 0;
            private final float startY = Lumina.this.getY();
            private final float startX = Lumina.this.getX();
            @Override
            public void act(float delta) {
                float move = 48 / speed;
                this.moveBy((float)Math.cos(Math.toRadians(direction))*move, (float)Math.sin(Math.toRadians(direction))*move);
                spr.setPosition(getX(), getY());
                spr.setOriginCenter();
                spr.rotate(3);
                dur++;
                int width = (int)Math.ceil((dur - 1) / 6) * 2 - 1; // -1 accommodates for the wave starting atop Lumina
                if(direction % 180 == 90) { // Moving vertically (90 or 270 degree angle)
                    setWidth(width * 48f);
                    setX(startX - (width - 1) * 24);
                } else { // Moving horizontally
                    setHeight(width * 48f);
                    setY(startY - (width - 1) * 24);
                }
                for (Unit un : units) {
                    if (un.getX() <= getX() + getWidth() && un.getX() + 48 >= getX()
                            && un.getY() <= getY() + getHeight() && un.getY() + 48 >= getY()
                            && !hitUnits.contains(un) && un.getTeam() != team && un.getTargetable()) {
                        effect(un);
                        hitUnits.add(un);
                    }
                }
                duration--;
                if(duration == 0)
                    remove();
            }

            @Override
            public void effect(Unit un) {
                un.takePhysDmg((int)(6.5 + (0.4 * mag) + (1.5 * level)), sourceUn);
                un.setVisibility((byte)0);
            }
        });
        cd[1] = maxCd[1];
        if(secondary)
            primary = true;
        else
            secondary = true;
        doing = "ab2Anim";
    }

    private void ab3(int cX, int cY) {
        ab3X = cX;
        ab3Y = cY;
        ab3Dur = 3;
        ab3Attach = 0; // 0 = No attachment, 1 = Lumina, 2 = Light Disc
        cd[2] = maxCd[2];
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y) <= 2.5) {
                ab3Damage(un);
            }
        if(secondary)
            primary = true;
        else
            secondary = true;
        doing = "Stand";
    }

    private void ab3Damage(Unit un) {
        if(!ab3Hit.contains(un)) {
            un.takePhysDmg(7 + (int)(0.4  * mag) + level, this);
            ab3Hit.add(un);
        }
        if(!ab3Slow.contains(un)) {
            if(getDistance(getGridX(), ab3X, getGridY(), ab3Y) <= 2.5) {
                if(curTurn == getTeam())
                    un.getStatus().add(StatusEffect.slow(1));
                else {
                    un.setMspeed(un.getMspeed() - 1);
                    un.getStatus().add(new StatusEffect("OnEnTurn") {
                        @Override
                        public void effect(Unit target, Unit source) {
                            un.setMspeed(un.getMspeed() + 1);
                        }
                    });
                }
                ab3Slow.add(un);
            }
        }
    }

    private void ab3Damage(Unit un, Projectile ab1) {
        if(!ab3Hit.contains(un)) {
            un.takePhysDmg(7 + (int)(0.4  * mag) + level, this);
            ab3Hit.add(un);
        }
        if(!ab3Slow.contains(un)) {
            if(getDistance(getGridX(), ab3X, getGridY(), ab3Y) <= 2.5 ||
                    getDistance(getGridX(ab1.getX()), ab3X, getGridY(ab1.getY()), ab3Y) <= 2.5) {
                if(curTurn == getTeam())
                    un.getStatus().add(StatusEffect.slow(1));
                else {
                    un.setMspeed(un.getMspeed() - 1);
                    un.getStatus().add(new StatusEffect("OnEnTurn") {
                        @Override
                        public void effect(Unit target, Unit source) {
                            un.setMspeed(un.getMspeed() + 1);
                        }
                    });
                }
                ab3Slow.add(un);
            }
        }
    }

    private void ab4(int cX, int cY) {
        int dir = (int)Math.floor((getDirection(getGridX(), cX, getGridY(), cY) + 30) / 90) * 90;
        switch(dir) {
            case 0:
                ab4Area.setPosition(getGridX()- 4, getGridY() - 1);
                ab4Area.setSize(5, 3);
                break;
            case 90:
                ab4Area.setPosition(getGridX() - 1, getGridY() - 4);
                ab4Area.setSize(3, 5);
                break;
            case 180:
                ab4Area.setPosition(getGridX()- 4, getGridY() - 1);
                ab4Area.setSize(5, 3);
                break;
            case 270:
                ab4Area.setPosition(getGridX() - 1, getGridY() - 4);
                ab4Area.setSize(3, 5);
                break;
        }
        for(Unit un : units) {
            if(ab4Area.contains(un.getGridX(), un.getGridY())) {
                if(un.getTeam() == team && un.getTargetable())
                    un.setDmgReduction(0.85f);
                else
                    un.setDmgReduction(1.15f);
                ab4Units.add(un);
            }
        }
        ab4Dur = 3;
        ab4On = true;
        cd[3] = maxCd[3];
        if(secondary)
            primary = true;
        else
            secondary = true;
        doing = "Stand";
    }

    private class LuminaPassive extends StatusEffect {
        Unit target;

        private LuminaPassive() {
            super("LuminaPassive");
        }

        @Override
        public void end() {
            def -= passiveArmor;
            passiveArmor -= level;
            def += passiveArmor;
            level = 0;
            target.getStatus().remove(this);
        }

        @Override
        public void effect(Unit target, Unit source) {
            if(level < 3) {
                level++;
                passiveArmor++;
            }
            duration = 3;
        }

        public void setTarget(Unit target) {
            this.target = target;
        }
    }
}
