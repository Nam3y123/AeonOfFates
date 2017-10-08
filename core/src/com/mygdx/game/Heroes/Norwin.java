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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Objects;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class Norwin extends Unit {
    private Image bloodreaver;
    private int brX, brY; // Bloodreaver x & y
    private boolean hasBloodreaver;
    private int ab3X, ab3Y;
    private boolean ab3Active;

    public Norwin(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 220;
        atk = 20;
        def = 20;
        mag = 15;
        hp = maxHp;
        name = "Norwin";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Norwin_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.0f, 0.5f, 1f, 1f);

        hasBloodreaver = true;
        brX = x;
        brY = y;

        abMenu.add(new MenuOption("Spin Slash", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    ab1();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Transfusion", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary)) {
                    if(hasBloodreaver)
                        setDoing("ab2", false);
                    else
                        ab2_in();
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Teleport", combatSkin, "combatMenu") {
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

        maxCd[0] = 3;
        maxCd[1] = 3;
        maxCd[2] = 1;
        maxCd[3] = 12;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(moving) {
            int curX = getGridX();
            int curY = getGridY();
            if(hasBloodreaver) {
                brX = curX;
                brY = curY;
            } else if((curX == brX && curY == brY) || getDistance(curX, brX, curY, brY) > 3.5)
                hasBloodreaver = true;
        }
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = true;
        if(!doing.equals("Attack") && super.drawTile(batch, parentAlpha))
            return true;
        else {
            switch (doing) {
                case "Attack":
                    if(primary) {
                        doing = "Stand";
                        break;
                    } else
                        doing = "passiveAtk";
                case "passiveAtk":
                    Tile.drawShape(batch, parentAlpha, brX * 48 + 8, brY * 48, "circle", range, Color.BLUE, true);
                    break;
                case "ab2":
                    Tile.drawRing(batch, parentAlpha, getX(), getY() - 4, 3, 2, BLUE, true);
                    abMenu.setVisible(false);
                    break;
                case "ab3":
                    Tile.drawShape(batch, parentAlpha, brX * 48 + 8, brY * 48, "circle", 2, Color.BLUE, true);
                    break;
                default:
                    ret = false;
                    break;
            }
        }
        return ret;
    }

    @Override
    public void clickAct() {
        super.clickAct();
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        switch (doing) {
            case "passiveAtk":
                passiveAtk(cX, cY);
                break;
            case "ab2":
                ab2_out(cX, cY);
                break;
            case "ab3":
                ab3(cX, cY);
                break;
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab3Active)
            ab3_move();
    }

    private void passiveAtk(int cX, int cY) {
        Unit target = null;
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && un.getVisibility() < 2 && un.getGridX() == cX && un.getGridY() == cY)
                target = un;
        if(target != null) {
            ArrayList<StatusEffect> se = statusEffect;
            int dmg = atk;
            int curHp = target.getHp();
            target.takePhysDmg(dmg, this);
            for (int i = 0; i < se.size(); i++)
                if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                    se.get(i).effect(target, this, dmg);
            while(dmgPopup.getActions().size > 0) {
                dmgPopup.removeAction(dmgPopup.getActions().get(0));
            }
            dmgPopup.setText(Integer.toString(curHp - target.getHp()));
            doing = "Stand";
            MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
            atk.setColor(Color.DARK_GRAY);
            primary = true;
            if(getDistance(getGridX(), cX, getGridY(), cY) <= 3.5) {
                brX = cX;
                brY = cY;
                hasBloodreaver = false;
            } else {
                brX = getGridX();
                brY = getGridY();
                hasBloodreaver = true;
            }
        }
    }

    private void ab1() {
        for(Unit un : units)
            if(un.getTargetable() && un.getTeam() != team && getDistance(un.getGridX(), brX, un.getGridY(), brY) <= 1.5) {
                int xDif = un.getGridX() - brX;
                int yDif = un.getGridY() - brY;
                boolean clear = Tile.walls.getPixel(un.getGridX() + xDif, un.getGridY() + yDif) != Color.rgba8888(Color.WHITE);
                for(Unit colUn : units)
                    if(colUn.getGridX() == un.getGridX() + xDif && colUn.getGridY() == un.getGridY() + yDif)
                        clear = false;
                if(clear) {
                    un.addAction(Actions.moveBy(xDif * 48, yDif * 48, 0.1f));
                }
                un.takePhysDmg((int)(16 + (1.2 * atk) + (0.875 * level) + (0.03 * un.getMaxHp())), this);
            }
        cd[0] = maxCd[0];
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab2_out(int cX, int cY) {
        float dir = getDirection(getGridX(), cX, getGridY(), cY);
        float dist = getDistance(cX, getGridX(), cY, getGridY());
        particleRegion.setRegion(0, 0, 32, 32);
        getParent().addActor(new Projectile(this, team, dir, getX() + 8, getY() + 4, particleRegion, 3, dist) {
            @Override
            public void effect(Unit un) {
                un.takePhysDmg((int)(8 + (0.8 * atk) + level), Norwin.this);
            }
        });
        brX = cX;
        brY = cY;
        hasBloodreaver = false;
        cd[1] = maxCd[1];
        if(secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
    }

    private void ab2_in() {
        float dir = getDirection(brX, getGridX(), brY, getGridY());
        float dist = getDistance(brX, getGridX(), brY, getGridY());
        particleRegion.setRegion(0, 0, 32, 32);
        getParent().addActor(new Projectile(this, team, dir, brX * 48 + 16, brY * 48 + 8, particleRegion, 3, dist) {
            @Override
            public void effect(Unit un) {
                un.takePhysDmg((int)(8 + (0.8 * atk) + level), Norwin.this);
                if(getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY()) <= 1.5f) {
                    un.getStatus().add(StatusEffect.snare);
                }
            }
        });
        brX = getGridX();
        brY = getGridY();
        hasBloodreaver = true;
        cd[1] = maxCd[1];
        if(secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
    }

    private void ab3(int cX, int cY) {
        ab3X = cX;
        ab3Y = cY;
        ab3Active = true;

        cd[2] = maxCd[2];
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab3_move() {
        boolean clear = Tile.walls.getPixel(ab3X, ab3Y) != Color.rgba8888(Color.WHITE);
        for(Unit un : units)
            if(un.getGridX() == ab3X && un.getGridY() == ab3Y)
                clear = false;
        if(ab3X == getGridX() && ab3Y == getGridY())
            clear = true;
        if(clear) {
            setPosition(ab3X * 48 + 8, ab3Y * 48 + 4);
        }
        ab3Active = false;
    }
}
