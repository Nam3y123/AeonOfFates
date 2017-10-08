package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Objects;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static com.mygdx.game.Tile.disp;
import static com.mygdx.game.Tile.drawRect;
import static java.lang.Math.floor;

public class FlameKeeper extends Unit {
    private boolean ab1On, ab2On, ab3On, resGen;
    private Rectangle ab1Rect;
    private boolean ab2Special;
    private int ab2Dmg;
    private ArrayList<Unit> ab3HitUnits;
    private Sprite fire;

    public FlameKeeper(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 220;
        atk = 17;
        mag = 30;
        def = 15;
        hp = maxHp;
        name = "FlameKeeper";
        range = 2;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/FlameKeeper_Particles.png")));
        charCol = new Color(0.5f, 0, 0, 1);
        res = 100;
        maxRes = 100;
        resBigInterval = 2;
        resSegmentSize = 25;
        resCol = Color.SCARLET;
        ab1On = false;
        ab2On = false;
        ab3On = false;
        resGen = true;
        ab2Special = false;
        ab2Dmg = 0;
        ab3HitUnits = new ArrayList<>();
        ab1Rect = new Rectangle();

        fire = new Sprite(particleRegion.getTexture());
        fire.setRegion(176, 0, 144, 144);
        fire.setOrigin(72, 0);
        fire.setSize(144, 144);
        fire.setPosition(-72, 0);

        abMenu.add(new MenuOption("Flame Trail", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary || ab1On))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Ashes to Ashes", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || ab2On)) {
                    if(!ab2On)
                        ab2();
                    else {
                        ab2Special = true;
                        res -= 50;
                    }
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Incinerate", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary || ab3On)) {
                    if(ab3On)
                        ab3Sp();
                    else
                        setDoing("ab3", false);
                }
            }
        });
        abMenu.row();

        statusEffect.add(new StatusEffect("OnSpellHit") {
            @Override
            public void effect(Unit target, Unit source) {
                if(resGen)
                    res+=10;
                if(res > maxRes)
                    res = maxRes;
            }
        });

        maxCd[0] = 4;
        maxCd[1] = 3;
        maxCd[2] = 1;
        maxCd[3] = 14;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        /*fire.setPosition(Gdx.input.getX() - 72 - game.getRoot().getX(), Gdx.graphics.getHeight() - Gdx.input.getY() - game.getRoot().getY());
        fire.setRotation(getDirection(fire.getX(), getX() + 24, fire.getY(), 3 * Gdx.graphics.getHeight() - game.getRoot().getY()) - 90);
        fire.draw(batch);*/
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if (super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            if (num == sel) {
                switch (doing) {
                    case "ab1":
                        if (!ab1On)
                            drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, BLUE, true);
                        else {
                            drawRect(batch, parentAlpha, (int) ab1Rect.x, (int) ab1Rect.y, (int) ab1Rect.width,
                                    (int) ab1Rect.height, BLUE, true);
                        }
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        Tile.setClickPos();
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, BLUE, true);
                        if (Tile.hit) {
                            int tX = (int) floor((clickX - 8) / 48);
                            int tY = (int) floor(clickY / 48);
                            int uX = (int) floor((getX() - 8) / 48);
                            int uY = (int) floor(getY() / 48);

                            if (tX > uX) {
                                drawRect(batch, parentAlpha, (int) getX() + 96, (int) getY() - 52, 2, 3, GREEN);
                                disp(batch, getX() + 48, getY() - 4, GREEN);
                            } else if (tY > uY) {
                                drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() + 92, 3, 2, GREEN);
                                disp(batch, getX(), getY() + 44, GREEN);
                            } else if (tX < uX) {
                                drawRect(batch, parentAlpha, (int) getX() - 144, (int) getY() - 52, 2, 3, GREEN);
                                disp(batch, getX() - 48, getY() - 4, GREEN);
                            } else {
                                drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() - 148, 3, 2, GREEN);
                                disp(batch, getX(), getY() - 52, GREEN);
                            }
                        }

                        ret = true;
                        abMenu.setVisible(false);
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
                if(!ab1On)
                    ab1(cX, cY);
                else {
                    addAction(Actions.moveTo(cX * 48 + 8, cY * 48 + 4));
                    res -= 50;
                    ab1On = false;
                    cd[0] = maxCd[0];
                    doing = "Stand";
                }
                break;
            }
            case "ab2": {
                ab2Sp(cX, cY);
                break;
            }
            case "ab3": {
                int dir = (int)Math.floor((getDirection(getGridX(), cX, getGridY(), cY) + 30) / 90) * 90;
                int enX, enY;
                ab3HitUnits.clear();
                cX = getGridX();
                cY = getGridY();
                for (Unit un : units) {
                    enX = un.getGridX();
                    enY = un.getGridY();
                    if (un.getTeam() != team && un.getTargetable())
                        switch (dir) {
                            case 0:
                                if ((cX + 1 == enX && cY == enY) || (enX > cX + 1 && enX <= cX + 3 && enY >= cY - 1 &&
                                        enY <= cY + 1)) {
                                    un.takeMagDmg(6 + (2 * level) + (int) (0.45 * mag), this);
                                    ab3HitUnits.add(un);
                                }
                                break;
                            case 90:
                                if ((cX == enX && cY + 1 == enY) || (enX >= cX - 1 && enX <= cX + 1 && enY > cY + 1 &&
                                        enY <= cY + 3)) {
                                    un.takeMagDmg(6 + (2 * level) + (int) (0.45 * mag), this);
                                    ab3HitUnits.add(un);
                                }
                                break;
                            case 180:
                                if ((cX - 1 == enX && cY == enY) || (enX < cX - 1 && enX >= cX - 3 && enY >= cY - 1 &&
                                        enY <= cY + 1)) {
                                    un.takeMagDmg(6 + (2 * level) + (int) (0.45 * mag), this);
                                    ab3HitUnits.add(un);
                                }
                                break;
                            case 270:
                                if ((cX == enX && cY - 1 == enY) || (enX >= cX - 1 && enX <= cX + 1 && enY < cY - 1 &&
                                        enY >= cY - 3)) {
                                    un.takeMagDmg(6 + (2 * level) + (int) (0.45 * mag), this);
                                    ab3HitUnits.add(un);
                                }
                                break;
                        }
                }
                ab3On = true;
                if(res < 50)
                    cd[2] = maxCd[2];
                doing = "Stand";
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                hp -= (int)(hp * 0.1);
            }
        }
    }

    private void ab1(int cX, int cY) {
        int dir = (int)Math.floor((getDirection(getGridX(), cX, getGridY(), cY) + 30) / 90) * 90;
        particleRegion.setRegion(128, 0, 48, 48);
        getParent().addActor(new Projectile(this, team, dir, getX(), getY(), particleRegion, 3, 5) {
            private int oldGridX = getGridX(), oldGridY = getGridY();
            private int startX = getGridX(), startY = getGridY();

            @Override
            public void effect(Unit un) {
                un.takeMagDmg(10 + (int)(0.4 * mag) + level, FlameKeeper.this);
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                particleRegion.setRegion(32, 0, 48, 48);
                if(getGridX(getX()) != oldGridX || getGridY(getY()) != oldGridY)
                    particles.add(new Particle(particleRegion, getGridX(getX()) * 48 + 8, getGridY(getY()) * 48) {
                        @Override
                        public void display(Batch batch, float parentAlpha) {
                            super.display(batch, parentAlpha);
                            if(!ab1On) {
                                particles.remove(this);
                                for(Unit un : units)
                                    if(un.getTeam() != team && un.getTargetable() && un.getGridX() == getGridX(spr.getX()) &&
                                            un.getGridY() == getGridY(spr.getY()))
                                        un.takeMagDmg((int)(20 + (0.6 * mag) + (1.75 * level)), FlameKeeper.this);
                            }
                        }
                    });
                oldGridX = getGridX(getX());
                oldGridY = getGridY(getY());
            }

            @Override
            public boolean remove() {
                doing = "Stand";
                switch (dir) {
                    case 0:
                        ab1Rect.set(48 * startX + 56, 48 * startY, 5, 1);
                        break;
                    case 90 :
                        ab1Rect.set(48 * startX + 8, 48 * startY + 48, 1, 5);
                        break;
                    case 180:
                        ab1Rect.set(48 * oldGridX + 8, 48 * oldGridY, 5, 1);
                        break;
                    case 270:
                        ab1Rect.set(48 * oldGridX + 8, 48 * oldGridY, 1, 5);
                        break;
                }
                return super.remove();
            }
        });

        hp -= (int)(hp * 0.15);
        ab1On = true;
        if(res < 50)
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

    private void ab2() {
        /*statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void start() {
                duration = 3;
            }

            @Override
            public void effect(Unit target, Unit source) {
                int targetHp = target.getHp();
                target.takeMagDmg((int)(12 + (0.7 * mag) + (0.75 * level)));
                ab2Dmg = targetHp - target.getHp();
                statusEffect.remove(this);
                cd[1] = maxCd[1];
                if(ab2Special)
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            doing = "ab2";
                            game.getRoot().findActor("CombatMenu").setVisible(false);
                        }
                    }, 0.01f);
            }

            @Override
            public void end() {
                statusEffect.remove(this);
                cd[1] = maxCd[1];
            }
        });*/
        for(Unit un : units) {
            float dist = getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY());
            if(dist <= 2.5f && un.getTeam() != team && un.getTargetable())
                un.takeMagDmg((int)(12 + (0.7 * mag) + (0.75 * level)), this);
        }

        hp -= (int)(hp * 0.1);
        if(hp < 1)
            hp = 1;
        ab2On = true;
        if(res < 50)
            cd[1] = -1;
        primary = true;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
    }

    private void ab2Sp(int cX, int cY) {
        /*for(Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY) {
                un.takeTrueDmg(ab2Dmg);
                ArrayList<StatusEffect> se = statusEffect;
                for (int i = 0; i < se.size(); i++)
                    if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                        se.get(i).effect(un, FlameKeeper.this, ab2Dmg);
                doing = "Stand";
            }*/
        boolean passable = Tile.walls.getPixel(cX, cY) != Color.argb8888(Color.WHITE);
        for(Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY)
                passable = false;
        if(getGridX() == cX && getGridY() == cY)
            passable = true;
        if(passable) {
            doing = "ab2Anim";
            addAction(Actions.sequence(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, 0.25f), Actions.run(this::ab2Dmg)));
        }
    }

    private void ab2Dmg() {
        for(Unit un : units) {
            float dist = getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY());
            if(dist <= 2.5f && un.getTeam() != team && un.getTargetable())
                un.takeTrueDmg((int)(12 + (0.7 * mag) + (0.75 * level)), this);
        }
        doing = "Stand";
    }

    private void ab3Sp() {
        for(Unit un : ab3HitUnits) {
            un.getStatus().add(StatusEffect.slow(1));
            heal((int)(8 + (0.3 * mag) + (level / 3f)));
        }
        res -= 50;
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab1On) {
            ab1On = false;
            if(cd[0] == 0)
                cd[0] = maxCd[0] - 1;
        }
        if(ab2On) {
            ab2On = false;
            if(ab2Special) {
                Unit.sel = getUnPosition();
                game.getRoot().addAction(Actions.moveTo(-spr.getX() + Gdx.graphics.getWidth() / 2 - spr.getWidth() / 2,
                        -spr.getY() + Gdx.graphics.getHeight() / 2 - spr.getHeight() / 2, 0.1f));
                for(Unit un : units)
                    un.removeListener(un.getClick());
                doing = "ab2";
            } else {
                addAction(Actions.sequence(Actions.delay(0.25f), Actions.run(this::ab2Dmg)));
            }
            if(cd[1] <= 0)
                cd[1] = maxCd[1] - 1;
        }
        if(ab3On) {
            ab3On = false;
            if(cd[2] == 0)
                cd[2] = maxCd[2] - 1;
        }
    }
}
