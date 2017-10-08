package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import static com.badlogic.gdx.graphics.Color.RED;
import static com.mygdx.game.Tile.*;
import static java.lang.Math.*;

public class Tera extends Unit {
    private int ab1ChargeDur;
    private boolean ab2Fall, ab2InStunRange;
    private boolean ab4Active;
    private Unit ab4Target;
    private int ab4Duration;

    public Tera(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 230;
        atk = 25;
        mag = 10;
        def = 17;
        hp = maxHp;
        name = "Tera";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Tera_Particles.png")),0, 0, 32, 32);
        charCol = Color.GREEN;

        ab1ChargeDur = 0;
        ab2Fall = false;
        ab4Active = false;
        ab4Duration = 0;
        ab4Target = null;

        abMenu.add(new MenuOption("Sneak Attack", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(ab1ChargeDur > 0)
                    setDoing("ab1", false);
                else if(cd[0] == 0 && !primary) {
                    ab1ChargeDur = 3;
                    doing = "Ab1Anim";
                    primary = true;
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Overhead Strike", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!secondary || !primary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Spear Sweep", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Toxic Spear", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary)) {
                    if(!ab4Active)
                        setDoing("ab4", false);
                    else {
                        cd[3] = maxCd[3] - (5 - ab4Duration);
                        ab4Active = false;
                        primary = true;
                    }
                }
            }
        });

        statusEffect.add(new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source, int dmgTaken) {
                float dist = (float)Math.sqrt(Math.pow(getGridX() - target.getGridX(), 2) + Math.pow(getGridY()
                        - target.getGridY(), 2));
                if(dist > 2.5f)
                    target.takePhysDmg(8 + (level) + (int)(0.4 * atk));
            }
        });

        maxCd[0] = 4;
        maxCd[1] = 3;
        maxCd[2] = 3;
        maxCd[3] = 12;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(!(ab4Active && doing.equals("Attack")) && super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            if(num == sel) {
                switch(doing) {
                    case "ab4Attack":
                    case "Attack":
                        if (ab4Active && !primary) {
                            Tile.setClickPos();
                            if (doing.equals("Attack"))
                                doing = "ab4Attack";
                            Color col = BLUE;
                            if (getDistance(getGridX(), ab4Target.getGridX(), getGridY(), ab4Target.getGridY()) > 10.5f)
                                col = RED;
                            drawShape(batch, parentAlpha, ab4Target.getX(), ab4Target.getY() - 4, "circle", 2, col, true);
                            if (Tile.hit) {
                                boolean enHit = false;
                                for (Unit un : units)
                                    if (un.getGridX() == getGridX(clickX) && un.getGridY() == getGridY(clickY) &&
                                            un.getTeam() != team && un.getTargetable()) {
                                        enHit = true;

                                        particleRegion.setRegion(32, 0, 36, 504);
                                        Sprite spr = new Sprite(particleRegion);
                                        spr.setOrigin(18, 0);
                                        spr.setPosition(getX() + 6, getY() + 20);
                                        spr.setSize(36, getDistance(getX(), un.getX(), getY(), un.getY()));

                                        float rot;
                                        Tile.setClickPos();
                                        if (un.getX() >= getX() + 24)
                                            rot = (float) atan((un.getY() - getY()) / (un.getX() - getX()));
                                        else
                                            rot = (float) atan((un.getY() - getY()) / (un.getX() - getX())) + 3.14159f;
                                        spr.setRotation((float) toDegrees(rot) - 90);
                                        spr.draw(batch);
                                    }
                                Tile.hit = enHit;
                            }
                        } else if (primary)
                            doing = "Stand";
                        break;
                    case "ab1":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 5, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, BLUE, true);
                        for (Unit un : units)
                            if (un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 2.5) {
                                int xDif = un.getGridX() - getGridX();
                                int yDif = un.getGridY() - getGridY();
                                float dist = getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY());
                                float xEnd = un.getGridX() + (xDif / dist) + 0.5f;
                                float yEnd = un.getGridY() + (yDif / dist) + 0.5f;
                                disp(batch, (int) floor(xEnd) * 48 + 8, (int) floor(yEnd) * 48, GREEN);
                            }
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab4":
                        particleRegion.setRegion(32, 0, 36, 504);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setOrigin(18, 0);
                        spr.setPosition(getX() + 6, getY() + 20);

                        float rot;
                        Tile.setClickPos();
                        if (clickX >= getX() + 24)
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24));
                        else
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24)) + 3.14159f;
                        spr.setRotation((float) toDegrees(rot) - 90);
                        spr.draw(batch);
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
        switch (doing) {
            case "ab1":
                ab1();
                break;
            case "ab2":
                ab2();
                break;
            case "ab3":
                ab3();
                break;
            case "ab4":
                ab4();
                break;
            case "ab4Attack":
                ab4Attack();
                break;
        }
    }

    private void ab1() {
        float dir;
        int parY;
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        abMenu.setVisible(false);
        if (cX > getGridX()) {
            dir = 0;
            parY = 0;
        } else if (cY > getGridX()) {
            dir = 90;
            parY = 32;
        } else if (cX < getGridX()) {
            dir = 180;
            parY = 64;
        } else {
            dir = 270;
            parY = 96;
        }
        particleRegion.setRegion(0, parY, 32, 32);
        final boolean ab1Charged = ab1ChargeDur < 3;

        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, 5) {
            @Override
            public void effect(Unit un) {
                int dmg = 10 + (level) + (int)(0.7 * atk);
                if (ab1Charged)
                    dmg *= 2;
                un.takePhysDmg(dmg, Tera.this);
                this.remove();
                doing = "Stand";
            }

            @Override
            public boolean remove() {
                return super.remove();
            }
        });

        cd[0] = maxCd[0] + ab1ChargeDur - 3;
        ab1ChargeDur = 0;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
    }

    private void ab2() {
        abMenu.setVisible(false);
        particleRegion.setRegion(0, 128, 38, 38);
        cd[1] = maxCd[1];
        doing = "ab1Anim";
        if (secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
        ab2Fall = false;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);

        particles.add(new Particle(particleRegion, (int) getX(), (int) getY()) {
            private int destX, destY, startX, startY;
            private boolean visible;

            @Override
            public void start() {
                spr.setOrigin(72, 0);
                spr.setScale(0.75f);

                destX = (int) Math.floor((Tile.clickX - 8) / 48) * 48 + 8;
                destY = (int) Math.floor(Tile.clickY / 48) * 48 + 4;
                startX = getGridX();
                startY = getGridY();
                visible = true;
            }

            @Override
            public void display(Batch batch, float parentAlpha) {
                if(visible)
                    super.display(batch, parentAlpha);
            }

            @Override
            public void increment() {
                spr.translateY(duration <= 15 ? 24 : -24);
                if(Math.sqrt(Math.pow(startX - getGridX(destX), 2) + Math.pow(startY
                        - getGridY(destY), 2)) > 2.5f && duration == 14)
                    doing = "Stand";
                if(duration == 15) {
                    spr.setX(destX);
                }
                if(duration == 30) {
                    spr.setAlpha(0);
                    for(Unit un : units)
                        if(un.getTeam() != team && un.getTargetable() && un.getGridX() == getGridX(destX)
                                && un.getGridY() == getGridY(destY)) {
                            un.takePhysDmg(15 + (int)(0.6 * atk), Tera.this);
                            if(ab2InStunRange)
                                un.getStatus().add(StatusEffect.stun);
                        }
                    this.remove();
                    particles.remove(this);
                    doing = "Stand";
                }
                if(Math.sqrt(Math.pow(startX - getGridX(destX), 2) + Math.pow(startY
                        - getGridY(destY), 2)) <= 2.5f || duration < 15 || ab2Fall) {
                    duration++;
                    visible = true;
                } else {
                    visible = false;
                    ab2InStunRange = true;
                }
            }
        });
    }

    private void ab3() {
        for (Unit un : units)
            if (un.getTeam() != team && un.getTargetable() && Unit.getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 2.5) {
                un.takePhysDmg((int) (0.65 * mag), this);
                int xDif = un.getGridX() - getGridX();
                int yDif = un.getGridY() - getGridY();
                float dist = Unit.getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY());
                int xEnd = (int)Math.floor(un.getGridX() + (xDif / dist) + 0.5f);
                int yEnd = (int)Math.floor(un.getGridY() + (yDif / dist) + 0.5f);
                if (Tile.walls.getPixel(xEnd, yEnd) != Color.rgba8888(Color.WHITE))
                    un.addAction(Actions.moveTo(xEnd * 48 + 8, yEnd * 48 + 4, 0.15f));
            }
        doing = "Ab3Anim";
        if (secondary)
            primary = true;
        else
            secondary = true;
        cd[2] = maxCd[2];
    }

    private void ab4() {
        float dir = getDirection(getX() + 24, Tile.clickX, getY() + 24, Tile.clickY);
        abMenu.setVisible(false);

        particleRegion.setRegion(1, 128, 36, 36);

        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 2.5f, 10) {
            boolean rotSet = false;

            @Override
            public void act(float delta) {
                float move = 48 / speed;
                this.moveBy((float)Math.cos(Math.toRadians(direction))*move, (float)Math.sin(Math.toRadians(direction))*move);

                if(hitUnits == null)
                    hitUnits = new ArrayList<Unit>();
                if(!rotSet) {
                    this.spr.rotate(dir);
                    rotSet = true;
                }

                spr.setPosition(getX(), getY());
                for(int i = 0; i < units.size(); i++) {
                    Unit un = units.get(i);
                    float distance = (float)Math.sqrt(Math.pow(un.getX() - getX(), 2) + Math.pow(un.getY() - getY(), 2));
                    if(distance < 36 && !hitUnits.contains(un) && un.getTeam() != team && un.getTargetable())
                        effect(un);
                }
                duration--;
                if(duration == 0)
                    remove();
            }

            @Override
            public void effect(Unit un) {
                ArrayList<StatusEffect> se = getStatus();
                int dmg = atk;
                un.takePhysDmg(dmg, Tera.this);
                for (int i = 0; i < se.size(); i++)
                    if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                        se.get(i).effect(un, Tera.this, dmg);
                ab4Target = un;
                ab4Active = true;
                ab4Duration = 5;

                particleRegion.setRegion(68, 0, 240, 240);
                particles.add(new Particle(particleRegion, (int)un.getX() - 96, (int)un.getY() - 100) {
                    @Override
                    public void start() {
                        spr.setOriginCenter();
                    }

                    @Override
                    public void increment() {
                        spr.setPosition(un.getX() - 96, un.getY() - 100);
                        if(duration <= 10)
                            spr.setScale((float)(-Math.pow(duration - 7, 2) / .49f + 118.367346939f) / 100f);
                        duration++;

                        if(!ab4Active) {
                            this.remove();
                            particles.remove(this);
                        }
                    }
                });

                this.remove();
                doing = "Stand";
            }

            @Override
            public boolean remove() {
                return super.remove();
            }
        });

        doing = "Ab4Anim";
        primary = true;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
        atk.setColor(Color.DARK_GRAY);
    }

    private void ab4Attack() {
        for(Unit un : units)
            if(un.getGridX() == getGridX(Tile.clickX) && un.getGridY() == getGridY(Tile.clickY) &&
                    un.getTeam() != team && un.getTargetable()) {

                float dist = getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY());
                float dir = getDirection(getX(), un.getX(), getY(), un.getY());
                particleRegion.setRegion(1, 128, 36, 36);

                getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                        getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 2.5f, dist) {
                    @Override
                    public void effect(Unit un) {
                        ArrayList<StatusEffect> se = statusEffect;
                        int dmg = atk;
                        un.takePhysDmg(dmg, Tera.this);
                        for (int i = 0; i < se.size(); i++)
                            if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                                se.get(i).effect(un, Tera.this, dmg);
                        doing = "Stand";
                        this.remove();
                    }
                });

                doing = "AtkAnim";
                MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
                atk.setColor(Color.DARK_GRAY);
                primary = true;
            }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab1ChargeDur > 0)
            ab1ChargeDur--;
        if(ab2InStunRange)
            ab2Fall = true;
        if(ab4Active) {
            ab4Duration--;
            if(ab4Duration == 0) {
                ab4Active = false;
                cd[3] = maxCd[3] - 5;
            }
        }
    }
}
