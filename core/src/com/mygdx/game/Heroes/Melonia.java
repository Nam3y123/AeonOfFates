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
import java.util.Objects;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;

// Celestica is now Melonia! Keep this in mind pls
public class Melonia extends Unit {
    private Unit ab1Target;
    private boolean actuallyHasMoved;
    private boolean[] ab3Hit;
    private int ab2Dur;
    private Unit ab2Target;

    public Melonia(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 216;
        hp = maxHp;
        atk = 16;
        def = 19;
        mag = 24;
        name = "Melonia";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Melonia_Particles.png")),0, 0, 32, 32);
        charCol = Color.GOLDENROD;

        actuallyHasMoved = false;
        ab2Dur = 0;

        abMenu.add(new MenuOption("Dark Matter", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Wormhole", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0) {
                    if(ab2Dur == 0)
                        setDoing("ab2", false);
                    else if (getDistance(ab2Target.getGridX(), getGridX(), ab2Target.getGridY(), getGridY()) <= 8.5) {
                        Melonia.this.addAction(Actions.moveTo(ab2Target.getX(), ab2Target.getY(), 0.05f));
                        ab2Target.addAction(Actions.moveTo(Melonia.this.getX(), Melonia.this.getY(), 0.05f));
                        cd[1] = maxCd[1];
                        ab2Dur = 0;
                    }
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Starsurge", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Star Shield", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!secondary || !primary))
                    setDoing("ab4", false);
            }
        });
        abMenu.row();

        statusEffect.add(new StatusEffect("OnSpellHit") {
            @Override
            public void effect(Unit target, Unit source) {
                boolean hasDebuff = false;
                for(StatusEffect se : target.getStatus())
                    if(se.getType().equals("MeloniaDebuff"))
                        hasDebuff = true;
                if(!hasDebuff)
                    setDebuff(target);
                else
                    passive(target);
            }
        });

        maxCd[0] = 2;
        maxCd[1] = 6;
        maxCd[2] = 3;
        maxCd[3] = 14;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret =  super.drawTile(batch, parentAlpha);
        if(!ret) {
            switch(doing) {
                case "ab1":
                    drawShape(batch, parentAlpha, (int) getX(), (int) getY() - 4, "line", 4, BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab2":
                    drawShape(batch, parentAlpha, (int) getX(), (int) getY() - 4, "circle", 3, BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab3":
                    Tile.setClickPos();
                    drawShape(batch, parentAlpha, (int) getX(), (int) getY() - 4, "line", 3, BLUE, true);
                    if (Tile.hit) {
                        if (getGridX() > getGridX(clickX) || getGridY() > getGridY(clickY))
                            drawRect(batch, parentAlpha, getGridX(clickX) * 48 + 8,
                                    getGridY(clickY) * 48, getGridX() - getGridX(clickX - 8) + 1,
                                    getGridY() - getGridY(clickY) + 1, GREEN);
                        else
                            drawRect(batch, parentAlpha, (int) getX(), (int) getY() - 4, getGridX(clickX - 8)
                                    - getGridX() + 1, getGridY(clickY) - getGridY() + 1, GREEN);
                    }
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab4":
                    drawShape(batch, parentAlpha, (int) getX(), (int) getY() - 4, "circle", 3, BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
            }
        }
        return ret;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(doing.equals("MoveAnim"))
            actuallyHasMoved = true;
        if(doing.equals("ab3Anim")) {
            for(Unit un : units)
                if(un.getTeam() != team && un.getTargetable() && getDistance(getX(), un.getX(), getY(), un.getY()) <= 48
                        && !ab3Hit[units.indexOf(un)]) {
                    int dmg = 10 + (int)(0.6 * mag);
                    un.takePhysDmg(dmg, this);
                    for (int i = 0; i < statusEffect.size(); i++)
                        if (Objects.equals(statusEffect.get(i).getType(), "OnSpellHit"))
                            statusEffect.get(i).effect(un, Melonia.this, dmg);
                    ab3Hit[units.indexOf(un)] = true;
                }
        }
    }

    @Override
    public void clickAct() {
        int cX = (int) Math.floor((Tile.clickX - 8) / 48);
        int cY = (int) Math.floor(Tile.clickY / 48);
        int stX = (int) Math.floor((getX() - 8) / 48);
        int stY = (int) Math.floor(getY() / 48);
        super.clickAct();
        switch (doing) {
            case "ab1":
                float dir;
                abMenu.setVisible(false);
                dir = getDirection(stX, cX, stY, cY);
                particleRegion.setRegion(4, 132, 24, 24);

                getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                        getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 6, 4) {
                    @Override
                    public void effect(Unit un) {
                        ArrayList<StatusEffect> se = statusEffect;
                        int dmg = 6 + (int)(0.4 * mag) + level;
                        un.takePhysDmg(dmg, Melonia.this);
                        ab1Target = un;
                        for (int i = 0; i < se.size(); i++)
                            if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                                se.get(i).effect(un, Melonia.this, dmg);
                        if(ab1Target == null) {
                            this.remove();
                            doing = "Stand";
                        }
                    }
                });

                cd[0] = maxCd[0];
                doing = "ab1Anim";
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                hasMoved = true;
                MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                mo.setColor(Color.DARK_GRAY);
                break;
            case "ab2":
                for(int i = 0; i < units.size(); i++) {
                    Unit un = units.get(i);
                    if (un.getGridX() == cX && un.getGridY() == cY && un.getTeam() == team && un.getTargetable()) {
                        ab2Target = un;
                        ab2Dur = 3;
                        doing = "Stand";
                        cd[1] = -1;

                        StatusEffect ab2Damage = new StatusEffect("OnDamage") {
                            @Override
                            public void effect(Unit target, Unit source) {
                                if(ab2Dur > 0 && (source.equals(Melonia.this) || source.equals(ab2Target))) {
                                    target.takePhysDmg((int)((0.02 + (0.0003 * mag)) * target.getMaxHp()));
                                }
                                else
                                    source.getStatus().remove(this);
                            }
                        };
                        statusEffect.add(ab2Damage);
                        un.getStatus().add(ab2Damage);

                        particleRegion.setRegion(32, 0, 120, 64);
                        particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {

                            @Override
                            public void start() {
                                spr.setOrigin(60, -336);
                                spr.setPosition(getX() - 36, getY() + 360);
                            }

                            @Override
                            public void increment() {
                                spr.setPosition(getX() - 36, getY() + 360);
                                spr.setRotation(getDirection(getX(), ab2Target.getX(), getY(), ab2Target.getY()) - 90);
                                if(-Math.pow((384 - getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()))/ 64f, 2) + 1.5 > 0)
                                    if(-Math.pow((384 - getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()))/ 64f, 2) + 1.5 < 1)
                                        spr.setAlpha(-(float)Math.pow((384 - getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()))/ 64f, 2) + 1.5f);
                                    else
                                        spr.setAlpha(1);
                                else
                                    spr.setAlpha(0);
                                if(getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()) > 384)
                                    spr.setAlpha(1);
                                if(ab2Dur == 0) {
                                    this.remove();
                                    particles.remove(this);
                                }
                            }
                        });
                        particleRegion.setRegion(0, 160, 48, 8);
                        particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
                            Color col;
                            @Override
                            public void start() {
                                spr.setOrigin(0, 4);
                                spr.setPosition(getX() + 24, getY() + 24);
                                col = new Color(1, 0, 0, 1);
                            }

                            @Override
                            public void increment() {
                                spr.setPosition(getX() + 24, getY() + 24);
                                spr.setSize(getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()), 12);
                                spr.setRotation(getDirection(getX(), ab2Target.getX(), getY(), ab2Target.getY()));
                                if(1 - (getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()) / 384f) > 0)
                                    col.g = 1 - (getDistance(getX(), ab2Target.getX(), getY(), ab2Target.getY()) / 384f);
                                else
                                    col.g = 0;
                                spr.setColor(col);
                                if(ab2Dur == 0) {
                                    this.remove();
                                    particles.remove(this);
                                }
                            }
                        });
                    }
                }
                break;
            case "ab3":
                if(Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE)) {
                    if(cX > stX)
                        dir = 0;
                    else if(cY > stY)
                        dir = 90;
                    else if(cX < stX)
                        dir = 180;
                    else
                        dir = 270;
                    ab3Hit = new boolean[units.size()];
                    addAction(Actions.sequence(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, 0.075f * (float)Math.sqrt(Math.pow(cX - stX, 2)
                            + Math.pow(cY - stY, 2))), Actions.run(() -> {
                        doing = "Stand";
                        for(Unit un : units) {
                            if(un.getTeam() == team && un.getTargetable() && getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 2.5) {
                                int shieldAmt = (int)(20 + (0.035 * maxHp) + (2 * level));
                                ArrayList<Integer> shieldArray = un.getShieldArray();
                                un.addShield(shieldAmt);
                                shieldArray.add(shieldAmt);
                                un.getStatus().add(new StatusEffect("OnDamaged") {
                                    private int position = shieldArray.size() - 1;

                                    @Override
                                    public void start() {
                                        duration = 3;
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
                    })));
                    doing = "ab3Anim";
                    cd[2] = maxCd[2];
                    if(secondary)
                        primary = true;
                    else
                        secondary = true;
                    break;
                }
            case "ab4":
                for(int i = 0; i < units.size(); i++) {
                    Unit un = units.get(i);
                    if (un.getGridX() == cX && un.getGridY() == cY && un.getTeam() == team && un.getTargetable()) {
                        doing = "Stand";
                        cd[3] = maxCd[3];
                        if(secondary)
                            primary = true;
                        else
                            secondary = true;
                        un.setTargetable(false);
                        un.setStunned(true);
                        statusEffect.add(new StatusEffect("OnAlTurn") {
                            @Override
                            public void effect(Unit target, Unit source) {
                                un.setTargetable(true);
                                statusEffect.remove(this);
                            }
                        });
                    }
                }
        }
    }

    private void setDebuff(Unit un) {
        StatusEffect passiveDebuff = new StatusEffect("MeloniaDebuff") {
            @Override
            public void start() {
                duration = 3;
            }

            @Override
            public void end() {
                un.getStatus().remove(this);
            }
        };
        un.getStatus().add(passiveDebuff);
    }

    private void passive(Unit target) {
        switch(doing) {
            case "ab1Anim":
                int stX = ab1Target.getGridX();
                int stY = ab1Target.getGridY();
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable() && getDistance(stX, un.getGridX(), stY, un.getGridY()) <= 2.5) {
                        un.getStatus().add(StatusEffect.snare);
                        un.takePhysDmg(8 + (int)(0.45 * mag));
                    }
                ab1Target = null;
                break;
            case "ab3Anim":
                cd[2] = 0;
                break;
            case "ab4Anim":
                int dmg = 15 + (int)(0.6 * mag);
                target.takePhysDmg(dmg);
                break;
        }
        for(Unit un : units)
            if(un.getTeam() == team && un.getTargetable() && getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY()) <= 2.5) {
                un.setMspeed(un.getMspeed() + 1);
                un.getStatus().add(new StatusEffect("OnAlTurn") {
                    @Override
                    public void effect(Unit target, Unit source) {
                        un.setMspeed(un.getMspeed() - 1);
                        un.getStatus().remove(this);
                    }
                });
            }
        if(!actuallyHasMoved)
            hasMoved = false;
        for(int i = 0; i < target.getStatus().size(); i++)
            if(target.getStatus().get(i).getType().equals("MeloniaDebuff")) {
                target.getStatus().remove(i);
                break;
            }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        actuallyHasMoved = false;
        if(cd[1] == -1)
            cd[1] = 0;
        if(ab2Dur > 0) {
            ab2Dur--;
            if(ab2Dur == 0)
                cd[1] = maxCd[1];
        }
    }
}
