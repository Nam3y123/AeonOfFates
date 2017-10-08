package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static java.lang.Math.floor;

public class Suvic extends Unit {
    private int passiveCd;
    private HashMap<Unit, Integer> ab1Hit;
    private int ab2X, ab2Y, ab2Dur;
    private boolean ab4on;
    private int ab4X, ab4Y;

    public Suvic(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 200;
        atk = 16;
        mag = 30;
        hp = maxHp;
        name = "Suvic";
        range = 3;
        charCol = Color.ORANGE;
        ab1Hit = new HashMap<>(3);
        ab2Dur = 0;
        ab4on = false;
        ab4X = 0;
        ab4Y = 0;
        passiveCd = 0;
        particleRegion.getTexture().dispose();
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Suvic_Particles.png")),0, 64, 32, 32);


        abMenu.add(new MenuOption("Power Theft", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Essence Sphere", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!secondary || !primary))
                    if(ab2Dur == 0)
                        setDoing("ab2", false);
                    else
                        ab2();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Tectonic Burst", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Temporal Anomaly", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!secondary || !primary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnSpellHit") {
            private final int maxPassiveCd = 8;

            @Override
            public void effect(Unit target, Unit source) {
                if(passiveCd == 0) {
                    target.takeTrueDmg(10 + (int)(0.2f * mag));
                    passiveCd = maxPassiveCd;
                } else if(passiveCd > 0)
                    passiveCd--;
            }
        });

        maxCd[0] = 1;
        maxCd[1] = 4;
        maxCd[2] = 3;
        maxCd[3] = 16;
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
                        if (ab2Dur > 0 && shiftDown)
                            drawShape(batch, parentAlpha, ab2X, getGridY(ab2Y) * 48, "circle", 3, BLUE, true);
                        else
                            drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        if (ab2Dur > 0 && shiftDown)
                            drawShape(batch, parentAlpha, ab2X, getGridY(ab2Y) * 48, "line", 5, BLUE, true);
                        else
                            drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab4":
                        Tile.setClickPos();
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 15, BLUE, true);
                        if (Tile.hit) {
                            int tX = (int) floor((clickX - 8) / 48);
                            int tY = (int) floor(clickY / 48);
                            drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 2, GREEN, false);
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
        super.clickAct();
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        int dir;
        switch(doing) {
            case "ab1":
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable() && un.getGridX() == cX && un.getGridY() == cY) {
                        boolean wasHit = false;
                        for(int i = 0; i < 3; i++)
                            if(ab1Hit.containsKey(un))
                                wasHit = true;
                        if(!wasHit) {
                            int dmg = 20 + (int)(0.6 * mag);
                            un.takePhysDmg(dmg, this);
                            ArrayList<StatusEffect> se = statusEffect;
                            for (int i = 0; i < se.size(); i++)
                                if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                                    se.get(i).effect(un, Suvic.this, dmg);
                            un.getStatus().add(StatusEffect.slow(1));
                            ab1Hit.put(un, 3);
                            cd[0] = maxCd[0];
                            doing = "Stand";
                            if (secondary)
                                primary = true;
                            else
                                secondary = true;
                            hasMoved = true;
                            MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                            mo.setColor(Color.DARK_GRAY);
                        }
                    }
                break;
            case "ab2":
                particleRegion.setRegion(0, 0, 32, 32);
                int distance = (int)Math.sqrt(Math.pow(cX - getGridX(), 2) + Math.pow(cY - getGridY(), 2));
                dir = (int)Math.ceil(getDirection(cX, getGridX(), cY, getGridY())) + 180;
                getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                        getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, distance) {
                    ArrayList<Unit> hit = new ArrayList<>();

                    @Override
                    public void effect(Unit un) {
                        if(!hit.contains(un)) {
                            int dmg = 17 + (int)(0.65 * mag);
                            un.takePhysDmg(dmg, Suvic.this);
                            ArrayList<StatusEffect> se = statusEffect;
                            for (int i = 0; i < se.size(); i++)
                                if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                                    se.get(i).effect(un, Suvic.this, dmg);
                            hit.add(un);
                        }
                    }

                    @Override
                    public boolean remove() {
                        particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
                            @Override
                            public void increment() {
                                setPosition(ab2X + (48 - particleRegion.getRegionWidth()) / 2,
                                        ab2Y + (48 - particleRegion.getRegionHeight()) / 2);
                            }
                        });
                        return super.remove();
                    }
                });

                if (secondary)
                    primary = true;
                else
                    secondary = true;
                ab2X = cX * 48 + 8;
                ab2Y = cY * 48;
                ab2Dur = 3;
                break;
            case "ab3":
                int sourceX = (int)getX();
                int sourceY = (int)getY();
                if(ab2Dur > 0 && shiftDown) {
                    sourceX = ab2X;
                    sourceY = ab2Y;
                }
                particleRegion.setRegion(0, 0, 32, 32);
                if(ab2Dur > 0 && shiftDown)
                    dir = (int)Math.ceil(getDirection(cX, getGridX(sourceX), cY, getGridY(sourceY))) + 180;
                else
                    dir = (int)Math.ceil(getDirection(cX, getGridX(), cY, getGridY())) + 180;
                getParent().addActor(new Projectile(this, team, dir, sourceX + (48 - particleRegion.getRegionWidth()) / 2,
                        sourceY + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, 5) {
                    boolean firstHit = false;

                    @Override
                    public void effect(Unit un) {
                        int dmg = 20 + (int)(0.7 * mag);
                        un.takePhysDmg(dmg, Suvic.this);
                        ArrayList<StatusEffect> se = statusEffect;
                        for (int i = 0; i < se.size(); i++)
                            if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                                se.get(i).effect(un, Suvic.this, dmg);
                        if(!firstHit)
                            ab3(un, dir % 360);
                        firstHit=true;
                    }
                });
                cd[2] = maxCd[2];
                doing = "Stand";
                hasMoved = true;
                MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                mo.setColor(Color.DARK_GRAY);
                if (secondary)
                    primary = true;
                else
                    secondary = true;
                break;
            case "ab4":
                abMenu.setVisible(false);
                particleRegion.setRegion(32, 0, 240, 240);
                cd[1] = maxCd[1];
                doing = "ab4Anim";
                ab4on = true;
                ab4X = cX * 48 + 8;
                ab4Y = cY * 48;
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                hasMoved = true;
                mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                mo.setColor(Color.DARK_GRAY);

                for (Unit un : units) {
                    int unStX = (int) Math.floor((un.getX() - 8) / 48);
                    int unStY = (int) Math.floor(un.getY() / 48);
                    if (Math.sqrt(Math.pow(cX - unStX, 2) + Math.pow(cY - unStY, 2)) <= 2.5f && un.getTeam() != team && un.getTargetable()) {
                        un.getStatus().add(StatusEffect.slow(1));
                    }
                }

                particles.add(new Particle(particleRegion, ab4X - 96, ab4Y - 96) {
                    @Override
                    public void increment() {
                        this.spr.setOriginCenter();
                        if(duration < 20) {
                            this.spr.rotate(7 - (duration / 4f));
                            this.spr.setAlpha(0.05f * duration);
                        } else
                            this.spr.rotate(2);
                        if(!ab4on) {
                            this.remove();
                            particles.remove(this);
                        }
                        duration++;
                    }
                });
                particleRegion.setRegion(272, 0, 180, 180);
                particles.add(new Particle(particleRegion, ab4X - 66, ab4Y - 66) {
                    @Override
                    public void increment() {
                        this.spr.setOriginCenter();
                        if(duration < 20) {
                            this.spr.rotate(7 - (duration / 4f));
                            this.spr.setAlpha(0.05f * duration);
                        } else {
                            this.spr.rotate(2);
                            this.spr.setAlpha(0.5f + 0.5f * (float)Math.cos(duration / 8f));
                            this.spr.setScale(1 + 0.15f * (float)Math.cos(duration / 8f));
                        }
                        if(!ab4on) {
                            this.remove();
                            particles.remove(this);
                        }
                        duration++;
                    }
                });
                break;
            default:
        }
    }

    private void ab2() {
        int cX = getGridX(ab2X);
        int cY = getGridX(ab2Y);
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && Math.sqrt(Math.pow(cX - un.getGridX(), 2) +
                    Math.pow(cY - un.getGridY(), 2)) <= 2.5) {
                int dmg = 17 + (int)(0.65 * mag);
                un.takePhysDmg(dmg, Suvic.this);
                ArrayList<StatusEffect> se = statusEffect;
                for (int i = 0; i < se.size(); i++)
                    if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                        se.get(i).effect(un, Suvic.this, dmg);
            }
        cd[1] = maxCd[1] - (3 - ab2Dur);
        ab2Dur = 0;
    }

    private void ab3(Unit un, int dir) {
        boolean knockback = false;
        int kbX = (int)un.getX(), kbY = (int)un.getY();
        if(dir == 0)
            kbX -= 96;
        else if(dir == 90)
            kbY -= 96;
        else if(dir == 180)
            kbX += 96;
        else
            kbY += 96;
        while(!knockback) {
            knockback = true;
            int cX = (int)(Math.floor((kbX - 8) / 48));
            int cY = (int)(Math.floor(kbY / 48));

            if(cX > 13 || cY > 10 || cX < 0 || cY < 0)
                knockback = false;
            else if(Tile.walls.getPixel(cX, cY) == Color.rgba8888(Color.WHITE))
                knockback = false;
            else
                for(int i = 0; i < units.size(); i++)
                    if(units.get(i).getX() == kbX && units.get(i).getY() == kbY)
                        knockback = false;

            if(un.getX() == kbX && un.getY() == kbY)
                knockback = true;
            if(!knockback) {
                if(dir == 0)
                    kbX += 48;
                else if(dir == 90)
                    kbY += 48;
                else if(dir == 180)
                    kbX -= 48;
                else
                    kbY -= 48;
            }

        }

        un.addAction(Actions.moveTo(kbX, kbY, 0.075f * (float)Math.sqrt(Math.pow(kbX - un.getX(), 2) + Math.pow(kbY - un.getY(), 2)) / 48f));
    }

    @Override
    public void alTurn() {
        super.alTurn();
        for(Unit un : units)
            if(ab1Hit.containsKey(un)) {
                Integer integer = ab1Hit.get(un);
                integer--;
                if(integer == 0)
                    ab1Hit.remove(un);
                else
                    ab1Hit.put(un, integer);
            }
        if(ab2Dur > 0) {
            ab2Dur--;
            if(ab2Dur == 0)
                ab2();
        }
        if(passiveCd > 0)
            passiveCd--;
        if(ab4on) {
            particleRegion.setRegion(0, 0, 32, 32);
            particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
                private int destX, destY, curY, framesToComplete;
                private float dir;

                @Override
                public void start() {
                    spr.setOriginCenter();
                    spr.setScale(0.75f);

                    destX = ab4X;
                    destY = ab4Y;
                    dir = getDirection(destX, Suvic.this.getX(), destY, Suvic.this.getY());
                    curY = (int)Suvic.this.getY();
                    framesToComplete = (int)(Math.sqrt(Math.pow(Suvic.this.getX() - destX, 2)
                            + Math.pow(Suvic.this.getY() - destY, 2)) / 12f);
                }

                @Override
                public void increment() {
                    if(duration <= framesToComplete) {
                        curY -= (float)Math.sin(Math.toRadians(dir)) * 12f;
                        spr.translateX(-(float)Math.cos(Math.toRadians(dir)) * 12f);
                        spr.setY(curY + (int)Math.pow(framesToComplete / 2, 2) - (int)Math.pow(duration - framesToComplete / 2, 2) / 2);
                    }
                    game.getRoot().setPosition(-spr.getX() + Gdx.graphics.getWidth() / 2 - spr.getWidth() / 2,
                            -curY + Gdx.graphics.getHeight() / 2 - spr.getHeight() / 2);
                    if(duration == framesToComplete) {
                        spr.setAlpha(0);
                        for (Unit un : units) {
                            int unStX = (int) Math.floor((un.getX() - 8) / 48);
                            int unStY = (int) Math.floor(un.getY() / 48);
                            if (Math.sqrt(Math.pow(getGridX(ab4X) - unStX, 2) + Math.pow(getGridY(ab4Y) - unStY, 2))
                                    <= 2.5f && un.getTeam() != team && un.getTargetable()) {
                                un.getStatus().add(StatusEffect.stun);
                                int dmg = 30 + (int)(0.9 * mag);
                                un.takePhysDmg(dmg, Suvic.this);
                                ArrayList<StatusEffect> se = statusEffect;
                                for (int i = 0; i < se.size(); i++)
                                    if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                                        se.get(i).effect(un, Suvic.this, dmg);
                            }
                        }
                    }
                    if(duration >= framesToComplete + 20) {
                        this.remove();
                        particles.remove(this);
                        ab4on = false;
                        doing = "Stand";
                    }
                    duration++;
                }
            });
        }
    }
}
