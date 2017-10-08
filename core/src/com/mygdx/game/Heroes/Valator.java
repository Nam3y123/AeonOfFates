package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.Objects;

// AND I KNOW IN THAT HOTLINE BLING
// New name found! Valator is now Valator.

public class Valator extends Unit {
    private Unit target;
    private int passiveLvl;
    private boolean ab2On;
    private StatusEffect passiveProc, ab2Activate;
    private Unit ab3Target;
    private boolean ab4On;

    public Valator(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 225;
        atk = 25;
        mag = 10;
        def = 17;
        hp = maxHp;
        name = "Valator";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Drake_Particles.png")),0, 0, 32, 32);
        charCol = Color.MAGENTA;
        passiveLvl = 0;
        ab2On = false;
        ab4On = false;


        abMenu.add(new MenuOption("Flame Blast", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Take Flight", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0) {
                    statusEffect.add(ab2Activate);
                    abMenu.setVisible(false);
                    showMenu();
                    primary = true;
                    secondary = true;
                    hasMoved = true;
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Maul", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || (!secondary && ab2On)))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Chaos Unleashed", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0) {
                    if(ab4On) {
                        ab4On = false;
                        cd[3] = maxCd[3];
                    } else
                        ab4();
                    abMenu.setVisible(false);
                    showMenu();
                }
            }
        });

        passiveProc = new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source) {
                if(Valator.this.target != null && Valator.this.target == target) {
                    passiveLvl++;
                } else {
                    Valator.this.target = target;
                    passiveLvl = 1;
                }

                // This is the claw falling animation
                int xPos, yPos;
                switch (passiveLvl) {
                    case 2:
                        xPos = 16;
                        yPos = 0;
                        break;
                    case 3:
                        xPos = 102;
                        yPos = 8;
                        break;
                    case 4:
                        xPos = 86;
                        yPos = 0;
                        break;
                    default:
                        xPos = 0;
                        yPos = 8;
                        break;
                }
                if((passiveLvl - 1) % 4 + 1 > 2)
                    particleRegion.setRegion(74, 0, 42, 40);
                else
                    particleRegion.setRegion(32, 0, 42, 40);
                final float xOfs = xPos * 4f / 9f, yOfs = yPos * 4f / 9f;
                particles.add(new Particle(particleRegion, (int)target.getX() - 10, (int)target.getY() - 18) {
                    private float stW, stH;

                    @Override
                    public void start() {
                        spr.translate(-spr.getWidth() / 4f + xOfs, -spr.getHeight() / 4f + yOfs);
                        stW = spr.getWidth();
                        stH = spr.getHeight();
                        //spr.setOriginCenter();
                    }

                    @Override
                    public void display(Batch batch, float parentAlpha) {
                        spr.draw(batch);
                        spr.setRegion(spr.getRegionX(), 184, spr.getRegionWidth(), spr.getRegionHeight());
                        spr.setAlpha((10 - duration) / 10f);
                        spr.draw(batch);
                        spr.setRegion(spr.getRegionX(), 0, spr.getRegionWidth(), spr.getRegionHeight());
                        spr.setAlpha(1f);
                    }

                    @Override
                    public void increment() {
                        spr.translate(stW / 30f, stH / 30f);
                        spr.setSize(stW * (15 - duration) / 15f,
                                stH * (15 - duration) / 15f);
                        duration++;
                        if(duration == 10) {
                            if(passiveLvl == 4) {
                                target.takePhysDmg((int)(0.08 * target.getMaxHp()));
                                Valator.this.target = null;
                                passiveLvl = 4;
                            } else if(passiveLvl > 4) {
                                passiveLvl -= 4;
                            }
                            particles.remove(this);
                        }
                    }
                });
            }
        };
        statusEffect.add(passiveProc);

        particleRegion.setRegion(32, 40, 144, 144);
        particles.add(new Particle(particleRegion, (int) getX() - 8, (int) getY() - 16) {
            @Override
            public void start() {
                spr.setOrigin(0, 0);
                spr.setScale(4 / 9f);
            }

            @Override
            public void display(Batch batch, float parentAlpha) {
                super.display(batch, parentAlpha);
                spr.setAlpha((float)Math.sin(Math.PI * duration / 90f) / 2f + 0.5f);
                spr.draw(batch);
                spr.setAlpha(1f);
            }

            @Override
            public void increment() {
                if(target == null)
                    spr.setPosition(-64, -64);
                else
                    spr.setPosition(target.getX() - 8, target.getY() - 16);
                duration++;
            }
        });
        particleRegion.setRegion(176, 0, 144, 192);
        particles.add(new Particle(particleRegion, (int) getX() - 8, (int) getY() - 16) {
            int curLvl;

            @Override
            public void start() {
                spr.setOrigin(0, 0);
                spr.setSize(64, 21.33333f);
                curLvl = -1;
            }

            @Override
            public void increment() {
                if(curLvl != passiveLvl)
                    spr.setRegion(176, 48 * (passiveLvl - 1), 144, 48);
                if(target == null)
                    spr.setPosition(-64, -64);
                else
                    spr.setPosition(target.getX() - 8, target.getY() - 16);
                curLvl = passiveLvl;
            }
        });

        ab2Activate = new StatusEffect("OnDamaged") {
            @Override
            public void start() {
                duration = 1;
            }

            @Override
            public void effect(Unit target, Unit source) {
                cd[1] = maxCd[1];
                addAction(new RunnableAction(){
                    @Override
                    public boolean act(float deltaTime) {
                        statusEffect.remove(ab2Activate);
                        return true;
                    }
                });
            }

            @Override
            public void end() {
                ab2On = true;
                mspeed++;
                range = 2;
                cd[1] = -1;
                spOfsY = 24;
                statusEffect.remove(this);
                statusEffect.add(new StatusEffect("OnAttack") {
                    @Override
                    public void effect(Unit target, Unit source) {
                        ab2(target);
                        statusEffect.remove(this);
                    }
                });
            }
        };

        maxCd[0] = 3;
        maxCd[1] = 3;
        maxCd[2] = 3;
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
                        if(!ab2On)
                            Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, Color.BLUE, true);
                        else
                            Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        if(!ab2On)
                            Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                        else
                            Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, Color.BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3_Dest":
                        Tile.drawShape(batch, parentAlpha, ab3Target.getX(), ab3Target.getY() - 4, "line", 1, Color.BLUE, true);
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
        int cX = (int) Math.floor((Tile.clickX - 8) / 48);
        int cY = (int) Math.floor(Tile.clickY / 48);
        super.clickAct();
        switch (doing) {
            case "ab1": {
                if(!ab2On)
                    ab1_Ground();
                else
                    ab1_Air();
                break;
            }
            case "ab3": {
                abMenu.setVisible(false);
                if(ab2On) {
                    cd[2] = maxCd[2];
                    doing = "Ab1Anim";
                    if(secondary)
                        primary = true;
                    else
                        secondary = true;
                    hasMoved = true;
                    MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                    mo.setColor(Color.DARK_GRAY);

                    float dir = getDirection(getGridX(), cX, getGridY(), cY);
                    float dist = getDistance(getGridX(), cX, getGridY(), cY);
                    particleRegion.setRegion(0, 0, 32, 32);
                    getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                            getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 5, dist){

                        @Override
                        public void effect(Unit un) {
                            un.takePhysDmg(8 + (int)(0.8 * atk) + level, Valator.this);
                        }
                    });

                    MoveToAction moveTo = new MoveToAction(){
                        @Override
                        public boolean act(float delta) {
                            spOfsY = (float)(Math.pow(getTime() / getDuration() * 2 - 1, 2) + 1) * 24f;
                            return super.act(delta);
                        }

                        @Override
                        public void finish() {
                            spOfsY = 0;
                            super.finish();
                        }
                    };
                    moveTo.setPosition(cX * 48 + 8, cY * 48 + 4);
                    moveTo.setDuration(getDistance(cX, getGridX(), cY, getGridY()) / 12f);
                    addAction(Actions.sequence(moveTo, Actions.run(() -> spOfsY = 24)));
                } else
                    for(int i = 0; i < units.size(); i++) {
                        Unit un = units.get(i);
                        if(un.getGridX() == cX && un.getGridY() == cY && un.getTeam() != team && un.getTargetable()) {
                            ab3Target = un;
                            doing = "ab3_Dest";
                        }
                    }
                break;
            }
            case "ab3_Dest": {
                cd[2] = maxCd[2];
                doing = "Ab1Anim";
                primary = true;
                hasMoved = true;
                MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                mo.setColor(Color.DARK_GRAY);

                MoveToAction moveTo = new MoveToAction(){
                    @Override
                    public boolean act(float delta) {
                        spOfsY = (float)(-Math.pow(getTime() / getDuration() * 2 - 1, 2) + 1) * 48f;
                        return super.act(delta);
                    }

                    @Override
                    public void finish() {
                        spOfsY = 0;
                        super.finish();
                    }
                };
                moveTo.setPosition(cX * 48 + 8, cY * 48 + 4);
                moveTo.setDuration(getDistance(cX, getGridX(), cY, getGridY()) / 18f);
                addAction(Actions.sequence(moveTo, Actions.run(() -> {
                    spOfsY = 0;
                    int dmg = 8 + (int)(0.8 * atk) + level;
                    ab3Target.takePhysDmg(dmg, Valator.this);
                    for (int i = 0; i < statusEffect.size(); i++)
                        if (Objects.equals(statusEffect.get(i).getType(), "OnHit"))
                            statusEffect.get(i).effect(ab3Target, Valator.this, dmg);
                })));
            }
        }
    }

    public void ab1_Ground() {
        int cX = (int) Math.floor((Tile.clickX - 8) / 48);
        int cY = (int) Math.floor(Tile.clickY / 48);
        int stX = (int) Math.floor((getX() - 8) / 48);
        int stY = (int) Math.floor(getY() / 48);
        float dir;
        int parY;
        abMenu.setVisible(false);
        dir = getDirection(stX, cX, stY, cY);
        parY = (int)(dir / 90f) * 48;
        particleRegion.setRegion(0, parY, 32, 32);

        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, 5) {
            @Override
            public void effect(Unit un) {
                int dmg = 5 + (int) (0.65 * atk) + level;
                un.takePhysDmg(dmg, Valator.this);
                un.setDef(un.getDef() - 7);
                un.getStatus().add(new StatusEffect("OnEnTurn") {
                    @Override
                    public void effect(Unit source, Unit target) { un.setDef(un.getDef() + 7); }
                });
                int difX = cX - getGridX();
                if(difX != 0)
                    difX = difX / Math.abs(difX);
                int difY = cY - getGridY();
                if(difY != 0)
                    difY = difY / Math.abs(difY);
                for (Unit unit : units)
                    if(unit.getTeam() != team && un.getTargetable() && (unit.getGridX() - un.getGridX() == difX || difX == 0)
                            && (unit.getGridY() - un.getGridY() == difY || difY == 0)
                            && Unit.getDistance(unit.getGridX(), un.getGridX(), unit.getGridY(), un.getGridY())
                            <= 1.5)
                        unit.takePhysDmg(dmg);
                this.remove();
                doing = "Stand";
            }

            @Override
            public boolean remove() {
                return super.remove();
            }
        });

        cd[0] = maxCd[0];
        doing = "Ab1Anim";
        if(secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
    }

    public void ab1_Air() {
        int cX = (int) Math.floor((Tile.clickX - 8) / 48);
        int cY = (int) Math.floor(Tile.clickY / 48);
        int stX = (int) Math.floor((getX() - 8) / 48);
        int stY = (int) Math.floor(getY() / 48);
        float dir;
        int parY;
        abMenu.setVisible(false);
        dir = getDirection(stX, cX, stY, cY);
        parY = (int)(dir / 90f) * 48;
        particleRegion.setRegion(32, 0, 32, 32);

        particles.add(new Particle(particleRegion, (int)spr.getX(), (int)spr.getY() + 12) {
            private float moveX, moveY;
            @Override
            public void start() {
                super.start();
                int destX = cX * 48 + 8;
                int destY = cY * 48;
                moveX = (spr.getX() - destX) / 10f;
                moveY = (spr.getY() - destY) / 10f;
            }

            @Override
            public void increment() {
                spr.translate(-moveX, -moveY);
                if(duration == 10) {
                    int dmg = 5 + (int) (0.65 * atk) + level;
                    for(Unit un : units)
                        if(un.getTeam() != team && un.getTargetable() && Unit.getDistance(un.getGridX(), cX, un.getGridY(), cY) <= 1.5) {
                            if(un.getGridX() == cX && un.getGridY() == cY) {
                                un.takePhysDmg(dmg, Valator.this);
                                un.setDef(un.getDef() - 7);
                                un.getStatus().add(new StatusEffect("OnEnTurn") {
                                    @Override
                                    public void effect(Unit source, Unit target) { un.setDef(un.getDef() + 7); }
                                });
                            } else
                                un.takePhysDmg(dmg);
                        }
                    this.remove();
                    particles.remove(this);
                }
                duration++;
            }
        });

        doing = "Ab1Anim";
        if(secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
    }

    private void ab2(Unit un) {
        int ofsX = 0;
        int ofsY = 0;
        if(un.getX() > getX())
            ofsX = -48;
        else if(un.getY() > getY())
            ofsY = -48;
        else if(un.getX() < getX())
            ofsX = 48;
        else
            ofsY = 48;

        int cX = (int)Math.floor((un.getX() - 8 + ofsX) / 48);
        int cY = (int)Math.floor((un.getY() + ofsY) / 48);
        int stX = (int)Math.floor((getX() - 8) / 48);
        int stY = (int)Math.floor(getY() / 48);

        un.takePhysDmg(8 + (int)(0.3 * atk));
        addAction(Actions.sequence(Actions.moveTo(un.getX() + ofsX, un.getY() + ofsY - spOfsY, getDistance(cX, stX, cY + 0.5f,
                stY) / 16f), Actions.run(() -> {
                    moveBy(0, spOfsY);
                    spOfsY = 0;
                })));
        ab2On = false;
        mspeed--;
        cd[1] = maxCd[1];
        range = 1;
    }

    private void ab4() {
        final float FEAR_RATIO = 0.8f;
        for(Unit un : units) {
            ab4On = true;
            if(un.getTeam() != team && un.getTargetable() && getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY()) < 2.5) {
                un.takePhysDmg(12 + (int)(0.7 * mag) + (int)(1.25 * level));
                un.getStatus().add(new StatusEffect("OnEnTurn") {
                    final int ATK = un.getAtk();
                    final int MAG = un.getMag();

                    @Override
                    public void effect(Unit target, Unit source) {
                        target.setAtk(ATK);
                        target.setMag(MAG);
                    }
                });
                un.setAtk((int)(un.getAtk() * FEAR_RATIO));
                un.setMag((int)(un.getMag() * FEAR_RATIO));
                if(ab2On)
                    un.getStatus().add(StatusEffect.snare);
            }
        }

        getStatus().add(new StatusEffect("OnAttack") {
            @Override
            public void start() {
                duration = 5;
            }

            @Override
            public void effect(Unit target, Unit source, int dmg) {
                if(ab4On) {
                    target.takePhysDmg((int)(0.3 * dmg));
                    passiveProc.effect(target, source);
                } else
                    end();
            }

            @Override
            public void end() {
                if(cd[3] == 0)
                    cd[3] = maxCd[3] + 1;
                ab4On = false;
                statusEffect.remove(this);
            }
        });
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab2On)
            spOfsY = 24;
    }

    @Override
    public void enTurn() {
        super.enTurn();
        if(ab2On)
            spOfsY = 6;
    }
}

