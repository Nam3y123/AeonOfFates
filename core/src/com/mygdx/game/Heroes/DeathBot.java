package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static com.mygdx.game.Tile.drawShape;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class DeathBot  extends Unit {
    private boolean passiveUp;
    private float[] ab1X, ab1Y;
    private boolean[] ab1Out;
    private boolean ab2Charge;

    public DeathBot(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 245;
        atk = 22;
        def = 22;
        mag = 15;
        hp = maxHp;
        name = "DeathBot";
        range = 2;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Tera_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 1f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        passiveUp = true;
        res = (int)(maxHp * 0.25f);
        maxRes = (int)(maxHp * 0.25f);
        resSegmentSize = (int)(maxHp * 0.25f);
        resCol = Color.YELLOW;

        ab1X = new float[3];
        ab1Y = new float[3];
        ab1Out = new boolean[3];

        abMenu.add(new MenuOption("Deflection Zone", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Pulsar Blast", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if((!primary || !secondary))
                    ab2();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Redirection", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    ab3();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Role Reversal", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnAttack") {
            @Override
            public void effect(Unit target, Unit source) {
                if(passiveUp) {
                    target.takeMagDmg((int)(maxHp * 0.03f));
                }
            }
        });
        statusEffect.add(new StatusEffect("OnDamaged") {
            @Override
            public void effect(Unit target, Unit source, int dmg) {
                res -= dmg;
                if(res <= 0) {
                    res = 0;
                    source.takeMagDmg(25 + mag + (2 * level));
                    resCol = Color.GRAY;
                    maxRes = 4;
                    passiveUp = false;
                }
            }
        });

        maxCd[0] = 1;
        maxCd[1] = 5;
        maxCd[2] = 3;
        maxCd[3] = 11;
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(!passiveUp) {
            res++;
            if(res == 4) {
                passiveUp = true;
                res = (int)(maxHp * 0.25f);
                maxRes = (int)(maxHp * 0.25f);
                resCol = Color.YELLOW;
            }
        } else {
            res = maxRes;
        }
        if(ab2Charge) {
            ab2Charge = false;
            visibility = 3;
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
                        particleRegion.setRegion(32, 0, 36, 162);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setSize(48, 216);
                        spr.setOrigin(24, 0);

                        float rot;
                        boolean threeAxes = true;
                        for(int i = 0; i < 3; i++)
                            if(!ab1Out[i])
                                threeAxes = false;
                        if(!threeAxes) {
                            spr.setPosition(getX(), getY() + 20);

                            Tile.setClickPos();
                            if (clickX >= getX() + 24)
                                rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24));
                            else
                                rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24)) + 3.14159f;
                            spr.setRotation((float) toDegrees(rot) - 90);
                            spr.draw(batch);
                        }

                        for(int i = 0; i < 3; i++) {
                            if(ab1Out[i]) {
                                spr.setPosition(ab1X[i], ab1Y[i] + 20);

                                Tile.setClickPos();
                                if (clickX >= ab1X[i] + 24)
                                    rot = (float) atan((clickY - ab1Y[i] - 24) / (clickX - ab1X[i] - 24));
                                else
                                    rot = (float) atan((clickY - ab1Y[i] - 24) / (clickX - ab1X[i] - 24)) + 3.14159f;
                                spr.setRotation((float) toDegrees(rot) - 90);
                                spr.draw(batch);
                            }
                        }
                        Tile.hit = true;
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
            default:
                break;
        }
    }

    private void ab1() {
        doing = "Stand";
        float dir;
        boolean threeAxes = true;
        for(int i = 0; i < 3; i++)
            if(!ab1Out[i])
                threeAxes = false;
        float[] startX = {getX(), ab1X[0], ab1X[1], ab1X[2]};
        float[] startY = {getY() - 4, ab1Y[0], ab1Y[1], ab1Y[2]};
        boolean[] startOut = {!threeAxes, ab1Out[0], ab1Out[1], ab1Out[2]};
        for(int i = 0; i < 4; i++) {
            if(startOut[i]) {
                if(i > 0)
                    ab1Out[i - 1] = false;
                dir = getDirection(startX[i] + 24, Tile.clickX, startY[i] + 24, Tile.clickY);
                particleRegion.setRegion(0, 0, 32, 32);
                getParent().addActor(new Projectile(this, team, dir, startX[i] + (48 - particleRegion.getRegionWidth()) / 2,
                        startY[i] + (48 - particleRegion.getRegionHeight()) / 2 - 4, particleRegion, 4, 4) {
                    private boolean started = false;

                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        atkAct(this);
                        if(!started) {
                            setOrigin(getWidth() / 2f, getHeight() / 2f);
                            setRotation(this.direction);
                            started = true;
                        }
                    }

                    @Override
                    public void effect(Unit un) {
                        un.takePhysDmg((int)(12 + (0.75 * atk) + (0.75 * level)), DeathBot.this);
                    }

                    @Override
                    public boolean remove() {
                        int axeNum;
                        int open = 0;
                        boolean found = false;
                        while(!found && open != 3) {
                            found = !ab1Out[open];
                            if(!found)
                                open++;
                        }
                        ab1Out[open] = true;
                        ab1X[open] = getX();
                        ab1Y[open] = getY();
                        axeNum = open;
                        particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
                            @Override
                            public void increment() {
                                if(!ab1Out[axeNum]) {
                                    particles.remove(this);
                                    remove();
                                }
                            }
                        });
                        return super.remove();
                    }
                });
            }
        }
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
        boolean enemiesNear = false;
        for(Unit un : units)
            if(un.getTeam() != team && getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY()) <= 4.5f)
                enemiesNear = true;
        if(!enemiesNear) {
            particleRegion.setRegion(68, 0, 240, 240);
            particles.add(new Particle(particleRegion, (int)getX() - 192, (int)getY() - 196) {
                @Override
                public void start() {
                    spr.setSize(432, 432);
                }

                @Override
                public void increment() {
                    if(!ab2Charge)
                        particles.remove(this);
                }
            });
            ab2Charge = true;
            cd[1] = -1;
        }
    }

    private void ab3() {
        for(Unit un : units)
            if(getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY()) <= 1.5f)
                un.takePhysDmg((int)(10 + (0.8 * atk) + (0.75 * atk)));
        for(int i = 0; i < 3; i++) {
            if(ab1Out[i]) {
                ab1Out[i] = false;
                float dir = getDirection(ab1X[i], getX(), ab1Y[i], getY() - 4);
                float dist = getDistance(ab1X[i], getX(), ab1Y[i], getY() - 4) / 48f;
                particleRegion.setRegion(0, 0, 32, 32);
                getParent().addActor(new Projectile(this, team, dir, ab1X[i] + (48 - particleRegion.getRegionWidth()) / 2,
                        ab1Y[i] + (48 - particleRegion.getRegionHeight()) / 2 - 4, particleRegion, 4, dist) {
                    private boolean started = false;

                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        atkAct(this);
                        if(!started) {
                            setOrigin(getWidth() / 2f, getHeight() / 2f);
                            setRotation(this.direction);
                            started = true;
                        }
                    }

                    @Override
                    public void effect(Unit un) {
                        un.takePhysDmg((int)(12 + (0.75 * atk) + (0.75 * level)));
                    }
                });
            }
        }
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
