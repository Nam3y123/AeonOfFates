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

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static java.lang.Math.*;

public class DragonRider extends Unit {
    private boolean usingStamina;
    private int passiveCd;
    private boolean ab1Active;
    private boolean ab2Charged;
    private int ab2Dir;
    private int ab3Cd;
    private int hpSto;

    public DragonRider(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 230;
        atk = 25;
        mag = 14;
        def = 17;
        hp = maxHp;
        name = "DragonRider";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Tera_Particles.png")),0, 0, 32, 32);
        charCol = Color.GREEN;

        res = 5;
        maxRes = 5;
        resSegmentSize = 1;
        resBigInterval = 6;
        resCol = Color.ORANGE;
        usingStamina = false;
        passiveCd = 0;
        ab1Active = false;
        ab2Charged = false;
        ab2Dir = 0;
        ab3Cd = 0;
        hpSto = hp;

        abMenu.add(new MenuOption("Dodge Roll", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary) && res >= 1)
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Take Flight", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!secondary || !primary) && res >= 2)
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Piercing Flames", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0)
                    ab3();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Dragon's Call", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnAttack") {
            @Override
            public void effect(Unit target, Unit source, int dmgTaken) {
                res--;
                usingStamina = true;
                if(ab3Cd == 0) {
                    target.takeTrueDmg((int)(0.7 * atk + 3 + level));
                    ab3Cd = 3;
                }
            }
        });

        maxCd[0] = 1;
        maxCd[1] = 3;
        maxCd[2] = 4;
        maxCd[3] = 15;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(!((res == 0 || ab1Active) && doing.equals("Attack")) && super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            if(num == sel) {
                switch(doing) {
                    case "ab1Attack":
                    case "Attack":
                        if (res == 0 || primary) {
                            doing = "Stand";
                            ret = false;
                            showMenu();
                        } else {
                            if (doing.equals("Attack"))
                                doing = "ab1Attack";
                            particleRegion.setRegion(32, 0, 36, 336);
                            Sprite spr = new Sprite(particleRegion);
                            spr.setSize(18, 168);
                            spr.setOrigin(9, 0);
                            spr.setPosition(getX() + 14, getY() + 20);

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
                        }
                        break;
                    case "ab1":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, BLUE, true);
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
            case "ab1Attack":
                ab1Attack();
                break;
            case "ab2":
                ab2();
                break;
        }
    }

    private void ab1() {
        int cX = getGridX(clickX);
        int cY = getGridY(clickY);
        int stX = getGridX();
        int stY = getGridY();
        boolean unitHit = false;
        for(Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY)
                unitHit = true;

        if(Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE) && !unitHit) {
            addAction(Actions.sequence(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, 0.075f * (float)Math.sqrt(Math.pow(cX - stX, 2)
                    + Math.pow(cY - stY, 2))), Actions.run(() -> doing = "Stand")));
            res--;
            usingStamina = true;
            ab1Active = true;
            doing = "ab1Anim";
            if (secondary)
                primary = true;
            else
                secondary = true;
            cd[0] = maxCd[0];
        }
    }

    private void ab1Attack() {
        float dir = getDirection(getX() + 24, Tile.clickX, getY() + 24, Tile.clickY);
        abMenu.setVisible(false);

        particleRegion.setRegion(1, 128, 36, 36);

        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 2.5f, 3) {
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
                int dmg = 5 + level + (int)(1.35f * atk);
                un.takePhysDmg(dmg, DragonRider.this);
                if(hitUnits.size() == 0)
                    for (int i = 0; i < se.size(); i++)
                        if (se.get(i).getType().equals("OnHit") || se.get(i).getType().equals("OnAttack"))
                            se.get(i).effect(un, DragonRider.this, dmg);
                hitUnits.add(un);
            }

            @Override
            public boolean remove() {
                doing = "Stand";
                return super.remove();
            }
        });

        doing = "AttackAnim";
        primary = true;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
        atk.setColor(Color.DARK_GRAY);
    }

    private void ab2() {
        ab2Dir = (int)Math.floor((getDirection(getX() + 24, Tile.clickX, getY() + 24, Tile.clickY) + 45) / 90f);
        ab2Charged = true;

        doing = "Stand";
        res -= 2;
        usingStamina = true;
        primary = true;
        secondary = true;
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
        atk.setColor(Color.DARK_GRAY);
    }

    private void ab2Dash() {
        int cX = getGridX();
        int cY = getGridY();
        int stX = getGridX();
        int stY = getGridY();
        Unit target = null;
        boolean[] unavailable = new boolean[5];
        int dist = 4;
        for(int i = 0; i < 5; i++) {
            switch (ab2Dir) {
                case 0:
                    cX++;
                    break;
                case 1:
                    cY++;
                    break;
                case 2:
                    cX--;
                    break;
                case 3:
                    cY--;
                    break;
            }

            for(Unit un : units) {
                if(un.getGridX() == cX && un.getGridY() == cY)
                    if(un.getTeam() != team && un.getTargetable()){
                        target = un;
                        dist = i;
                        i = 5;
                    } else {
                        unavailable[i] = true;
                    }
            }
        }
        for(int j = dist; j >= 0; j--) {
            switch (ab2Dir) {
                case 0:
                    cX--;
                    break;
                case 1:
                    cY--;
                    break;
                case 2:
                    cX++;
                    break;
                case 3:
                    cY++;
                    break;
            }
            if(Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE) && (j == 0 || !unavailable[j - 1])) {
                final Unit finalTarget = target;
                addAction(Actions.sequence(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, 0.075f * (float)Math.sqrt(Math.pow(cX - stX, 2)
                        + Math.pow(cY - stY, 2))), Actions.run(() -> {
                    doing = "Stand";
                    if(finalTarget != null) {
                        finalTarget.takePhysDmg((int)(1.2 * atk + 7 + 1.5 * level), DragonRider.this);
                        finalTarget.getStatus().add(StatusEffect.snare);
                        res++;
                    }
                })));
                j = -1;
            }
        }
        ab2Charged = false;
    }

    private void ab3() {
        ab3Cd = 0;
        cd[2] = maxCd[2];
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(shiftDown && team != curTurn && passiveCd == 0) {
            float curReduction = getDmgReduction();
            addAction(Actions.sequence(Actions.delay(0.15f), Actions.run(() -> setDmgReductionFlat(0f)),
                    Actions.delay(0.5f), Actions.run(() -> setDmgReductionFlat(curReduction))));
            passiveCd = 3;
        }
        if(hpSto > hp) {
            ab3Cd = curTurn == team ? 3 : 4;
        }
        hpSto = hp;
    }

    @Override
    public void enTurn() {
        super.enTurn();
        if(passiveCd > 0)
            passiveCd--;
        ab1Active = false;
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab2Charged)
            ab2Dash();
        if(!usingStamina)
            res = 5;
        if(ab3Cd > 0)
            ab3Cd--;
        usingStamina = false;
    }
}
