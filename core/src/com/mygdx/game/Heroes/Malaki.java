package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static java.lang.Math.floor;

public class Malaki extends Unit {
    private int ab2X, ab2Y, ab2armor;
    private boolean ab2on, ab4on;
    private final StatusEffect.MoveRule ab4 = new StatusEffect.MoveRule(){
        private float distanceFromMalaki(int x, int y) {
            return (float)Math.sqrt(Math.pow(x - getGridX(), 2) + Math.pow(y - getGridY(), 2));
        }

        public boolean viableTile(Unit target, int tileX, int tileY) {
            if(distanceFromMalaki(target.getGridX(), target.getGridY()) <= 3.5) {
                return distanceFromMalaki(tileX, tileY) <= 3.5;
            }
            return true;
        }
    };

    public Malaki(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 230;
        atk = 15;
        def = 25;
        mag = 15;
        hp = maxHp;
        name = "Malaki";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Malaki_Particles.png")),0, 0, 32, 32);
        charCol = Color.PURPLE;
        ab2X = 0;
        ab2Y = 0;
        ab2armor = 0;
        ab2on = false;


        abMenu.add(new MenuOption("Abyss Spear", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    Malaki.this.setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Field of Despair", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!secondary || !primary))
                    Malaki.this.setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Ground Slam", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary))
                    Malaki.this.setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Abyssal Prison", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!secondary || !primary) && getHp() >= getMaxHp() * 0.2f) {
                    if(ab4on)
                        for(Unit un : units) {
                            if(un.getTeam() != team && un.getTargetable())
                                un.getStatus().remove(ab4);
                        }
                    else {
                        for(Unit un : units) {
                            if(un.getTeam() != team && un.getTargetable())
                                un.getStatus().add(ab4);
                        }
                        cd[3] = maxCd[3];
                    }
                    ab4on = !ab4on;
                }
            }
        });

        statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void effect(Unit target, Unit source) {
                int dir = 270;
                if(getX() < target.getX())
                    dir = 0;
                else if(getY() < target.getY())
                    dir = 90;
                else if(getX() > target.getX())
                    dir = 180;
                Projectile proj = new Projectile(Malaki.this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                        getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, 4) {
                    ArrayList<Unit> hitUnits = new ArrayList<>();

                    @Override
                    public void effect(Unit un) {
                        if(!hitUnits.contains(un)) {
                            un.takePhysDmg(15 + (int)(0.3 * mag));
                            Malaki.this.heal(10 + (int)(0.2 * mag));
                            hitUnits.add(un);
                        }
                    }
                };
                Malaki.this.getParent().addActor(proj);
            }
        });

        maxCd[0] = 2;
        maxCd[1] = 3;
        maxCd[2] = 4;
        maxCd[3] = 3;
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
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 2, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        Tile.setClickPos();
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, BLUE, true);
                        if (Tile.hit) {
                            int tX = (int) floor((clickX - 8) / 48);
                            int tY = (int) floor(clickY / 48);
                            drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 2, GREEN, false);
                        }
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        Tile.setClickPos();
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, BLUE, true);
                        if (Tile.hit) {
                            int tX = (int) floor((clickX - 8) / 48);
                            int tY = (int) floor(clickY / 48);
                            drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 1, GREEN, false);
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
        int cX = (int) Math.floor((Tile.clickX - 8) / 48);
        int cY = (int) Math.floor(Tile.clickY / 48);
        int stX = (int) Math.floor((getX() - 8) / 48);
        int stY = (int) Math.floor(getY() / 48);
        super.clickAct();
        if (doing.equals("ab1")) {
            float dir;
            int parY;
            abMenu.setVisible(false);
            TextureRegion particle = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/TestChar_Particles.png")), 0, 64, 32, 32);
            dir = getDirection(stX, cX, stY, cY);
            parY = 0;
            particle.setRegion(particle.getRegionX(), parY, particle.getRegionWidth(), particle.getRegionHeight());
            cd[0] = maxCd[0];
            getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particle.getRegionWidth()) / 2,
                    getY() + (48 - particle.getRegionHeight()) / 2, particle, 4, 2) {
                @Override
                public void effect(Unit un) {
                    Malaki.this.ab1(un, this);
                    this.remove();
                }
            });
            doing = "ab1Anim";
            if (secondary)
                primary = true;
            else
                secondary = true;
            hasMoved = true;
            MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
        } else if(doing.equals("ab2")) {
            ab2();
            hasMoved = true;
            MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
        } else if(doing.equals("ab3") && Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE)) {
            doing = "ab3Anim";
            if (secondary)
                primary = true;
            else
                secondary = true;
            addAction(Actions.sequence(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, 0.1f * (float)Math.sqrt(Math.pow(cX - stX, 2)
                    + Math.pow(cY - stY, 2))), Actions.run(() -> {
                doing = "Stand";
                ab3();
            })));
            cd[2] = maxCd[2];
        }
    }

    private void ab1(Unit un, Projectile proj) {
        un.takePhysDmg(10 + (int)(0.5 * mag), this);
        un.getStatus().add(StatusEffect.slow(2));
        float dir = (float)Math.ceil(proj.getDirection());
        boolean horizontal = dir == 0 || dir == 180;
        boolean plus = dir == 0 || dir == 90;

        particleRegion.setRegion(0, 0, 32, 32);

        final ArrayList<Unit> hit = new ArrayList<>();

        class Wave extends Projectile {
            private Wave(Unit sourceUn, boolean team, float direction, float x, float y, TextureRegion particle, float speed, float range) {
                super(sourceUn, team, direction, x, y, particle, speed, range);
                hit.add(un);
            }

            @Override
            public void effect(Unit un) {
                if(!hit.contains(un)) {
                    un.takePhysDmg(10 + (int)(0.5 * mag), Malaki.this);
                    un.getStatus().add(StatusEffect.slow(1));
                    hit.add(un);
                }
            }

            @Override
            public boolean remove() {
                Malaki.this.doing = "Stand";
                return super.remove();
            }
        }

        getParent().addActor(new Wave(this, team, dir, proj.getX() + (horizontal ? (plus ? 32 : -32) : 0),
         proj.getY() + (horizontal ? 0 : (plus ? 32 : -32)), particleRegion, 4, 4));
        if(horizontal){
            getParent().addActor(new Wave(this, team, dir, proj.getX() + (plus ? 32 : -32), proj.getY() - 48,
             particleRegion, 4, 4));
            getParent().addActor(new Wave(this, team, dir, proj.getX() + (plus ? 32 : -32), proj.getY() + 48,
             particleRegion, 4, 4));
        } else {
            getParent().addActor(new Wave(this, team, dir, proj.getX() - 48, proj.getY() + (plus ? 32 : -32),
             particleRegion, 4, 4));
            getParent().addActor(new Wave(this, team, dir, proj.getX() + 48, proj.getY() + (plus ? 32 : -32),
             particleRegion, 4, 4));
        }
    }

    private void ab2() {
        ab2X = getGridX(Tile.clickX);
        ab2Y = getGridY(Tile.clickY);
        StatusEffect armorBuff = new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source, int dmg) {
                Malaki.this.def -= ab2armor;
                if(Math.sqrt(Math.pow(target.getGridX() - ab2X, 2) + Math.pow(target.getGridY() - ab2Y, 2)) <= 2.5f)
                    ab2armor += dmg * 0.1f;
                Malaki.this.def += ab2armor;
            }
        };
        for(Unit un : units) {
            if(un.getTeam() != team && un.getTargetable() && Math.sqrt(Math.pow(un.getGridX() - ab2X, 2) + Math.pow(un.getGridY() - ab2Y, 2)) <= 2.5f)
                un.takePhysDmg((int)((0.05 + (0.0002 * mag)) * un.getHp()));
            un.getStatus().add(armorBuff);
        }

        statusEffect.add(new StatusEffect("Dummy") {
            @Override
            public void start() {
                duration = 4;
            }

            @Override
            public void end() {
                for(Unit un : units)
                    un.getStatus().remove(armorBuff);
                Malaki.this.def -= ab2armor;
                ab2armor = 0;
                ab2on = false;
            }
        });

        particleRegion.setRegion(32, 0, 144, 144);
        particles.add(new Particle(particleRegion, ab2X * 48 - 40, ab2Y * 48 - 48) {
            @Override
            public void start() {
                spr.setScale(5 / 3f, 5 / 3f);
            }

            @Override
            public void increment() {
                if(!ab2on)
                    particles.remove(this);
            }
        });
        ab2on = true;
        cd[1] = maxCd[1];
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab3() {
        for(Unit un : units) {
            if(un.getTeam() != team && un.getTargetable()
             && Math.sqrt(Math.pow(getGridX() - un.getGridX(), 2) + Math.pow(getGridY() - un.getGridY(), 2)) <= 1.5f) {
                un.takePhysDmg(13 + (int)(0.3 * def), this);
                un.getStatus().add(new StatusEffect("OnDamaged") {
                    @Override
                    public void start() {
                        duration = 2;
                    }

                    @Override
                    public void effect(Unit target, Unit source) {
                        if(source.equals(Malaki.this)) {
                            target.takePhysDmg(2 + (2 * level));
                            Malaki.this.cd[2]-=2;
                        }
                    }

                    @Override
                    public void end() {
                        un.getStatus().remove(this);
                    }
                });
            }
        }
    }

    @Override
    public void enTurn() {
        super.enTurn();
        if(ab4on) {
            if(getHp() < getMaxHp() * 0.2f){
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable())
                        un.getStatus().remove(ab4);
                cd[3] = maxCd[3];
            }
            else
                takeTrueDmg((int)(getMaxHp() * 0.15f));
        }
    }
}
