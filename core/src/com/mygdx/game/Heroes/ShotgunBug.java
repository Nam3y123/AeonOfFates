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
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static com.mygdx.game.Tile.drawShape;
import static java.lang.Math.atan;
import static java.lang.Math.floor;
import static java.lang.Math.toDegrees;

public class ShotgunBug extends Unit {
    private HashMap<Unit, Integer> passiveTargets;
    private boolean ab1Active;
    private int ab1X, ab1Y;
    private boolean ab2Active;
    private int ab2X, ab2Y, ab2Stacks;

    public ShotgunBug(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 32;
        def = 14;
        mag = 10;
        hp = maxHp;
        name = "ShotgunBug";
        range = 2;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Annah_Particles.png")), 0, 0, 32, 32);
        //charCol = new Color(0.375f, 0.375f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        passiveTargets = new HashMap<>();
        ab2Stacks = 1;

        abMenu.add(new MenuOption("Sandy Shuffle", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Sandstorm", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Alaskan Bull Worm", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Iron Maiden", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0)
                    ab4();
            }
        });

        statusEffect.add(new StatusEffect("OnSpellHit") {
            @Override
            public void effect(Unit target, Unit source) {
                passiveTargets.put(target, 4);
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 5;
        maxCd[2] = 3;
        maxCd[3] = 13;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(ab2Active && sel > -1) {
            Unit target = units.get(sel);
            if(target.getTeam() != team && target.getDoing().equals("MoveAnim") && target.getGridX() == ab2X && target.getGridY() == ab2Y) {
                for(Unit un: units)
                    if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab2X, un.getGridY(), ab2Y) <= 2.5f)
                        un.takeMagDmg((int)(8 + (0.5 * mag) + (1.75 * level)), this);
                ab2Active = false;
            }
        }
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = true;
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            switch (doing) {
                case "ab1":
                    Tile.setClickPos();
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "circle", 7, Color.BLUE, true);
                    if (Tile.hit) {
                        int tX = (int) floor((clickX - 8) / 48);
                        int tY = (int) floor(clickY / 48);
                        drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 2, GREEN, false);
                    }
                    abMenu.setVisible(false);
                    break;
                case "ab2":
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "circle", 2, Color.BLUE, true);
                    abMenu.setVisible(false);
                    break;
                case "ab3":
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "line", 5, Color.BLUE, true);
                    abMenu.setVisible(false);
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
            case "ab1":
                ab1(cX, cY);
                break;
            case "ab2":
                ab2(cX, cY);
                break;
            case "ab3":
                ab3(cX, cY);
                break;
        }
    }

    @Override
    protected void atkEffect(Unit un) {
        if(!passiveTargets.containsKey(un))
            super.atkEffect(un);
        else {
            ArrayList<StatusEffect> se = statusEffect;
            int dmg = atk;
            int curHp = un.getHp();
            dmg *= 1.25f;
            un.takeTrueDmg(dmg, this);
            for (int i = 0; i < se.size(); i++)
                if (Objects.equals(se.get(i).getType(), "OnHit"))
                    se.get(i).effect(un, this, dmg);
            while(dmgPopup.getActions().size > 0) {
                dmgPopup.removeAction(dmgPopup.getActions().get(0));
            }
            dmgPopup.setText(Integer.toString(curHp - un.getHp()));
            passiveTargets.remove(un);
        }
        cd[1] = 0;
        if(ab2Stacks < 2)
            ab2Stacks++;
    }

    protected void atkEffect(Unit un, int dmg) {
        ArrayList<StatusEffect> se = statusEffect;
        int curHp = un.getHp();
        if(passiveTargets.containsKey(un)) {
            dmg *= 1.25f;
            un.takeTrueDmg(dmg, this);
        } else
            un.takePhysDmg(dmg, this);
        for (int i = 0; i < se.size(); i++)
            if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                se.get(i).effect(un, this, dmg);
        while(dmgPopup.getActions().size > 0) {
            dmgPopup.removeAction(dmgPopup.getActions().get(0));
        }
        dmgPopup.setText(Integer.toString(curHp - un.getHp()));
        passiveTargets.remove(un);
        cd[1] = 0;
        if(ab2Stacks < 2)
            ab2Stacks++;
    }

    private void reducePassiveDur() {
        ArrayList<Unit> remove = new ArrayList<>();
        for(Unit u : passiveTargets.keySet()) {
            int dur = passiveTargets.get(u);
            dur--;
            if(dur > 0)
                passiveTargets.put(u, dur);
            else
                remove.add(u);
        }
        for(Unit u : remove)
            passiveTargets.remove(u);
    }

    private void ab1(int cX, int cY) {
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), cX, un.getGridY(), cY) <= 2.5f) {
                un.takePhysDmg((int)(5 + (0.5 * atk) + (0.667 * level)), this);
                un.setVisibility((byte)0);
            }

        ab1Active = true;
        doing = "Stand";
        ab1X = cX;
        ab1Y = cY;
        if(secondary)
            primary = true;
        else
            secondary = true;
        cd[0] = maxCd[0];
    }

    private void ab2(int cX, int cY) {
        boolean clear = Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE);
        for(Unit un : units)
            if(un.getGridX() == cX && un.getGridY() == cY)
                clear = false;
        if(cX == getGridX() && cY == getGridY())
            clear = true;
        if(clear) {
            ab2Active = true;
            ab2X = getGridX();
            ab2Y = getGridY();
            float dashDur = 0.125f;
            addAction(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, dashDur));
            doing = "Stand";
            if(secondary)
                primary = true;
            else
                secondary = true;
            ab2Stacks--;
            if(ab2Stacks == 0)
                cd[1] = maxCd[1];
        }
    }

    private void ab3(int cX, int cY) {
        int dir;
        dir = (int)Math.floor((getDirection(getGridX(), cX, getGridY(), cY) + 1) / 90f) * 90;
        for(Unit un : units) {
            boolean hit = false;
            switch (dir) {
                case 0:
                    if(un.getGridX() > getGridX() && un.getGridX() <= getGridX() + 2 && un.getGridY() == getGridY())
                        hit = true;
                    break;
                case 90:
                    if(un.getGridY() > getGridY() && un.getGridY() <= getGridY() + 2 && un.getGridX() == getGridX())
                        hit = true;
                    break;
                case 180:
                    if(un.getGridX() < getGridX() && un.getGridX() >= getGridX() - 2 && un.getGridY() == getGridY())
                        hit = true;
                    break;
                case 270:
                    if(un.getGridY() < getGridY() && un.getGridY() >= getGridY() - 2 && un.getGridX() == getGridX())
                        hit = true;
                    break;
            }
            if(hit)
                un.takeMagDmg((int)(10 + (0.6 * mag) + (0.75 * level)), this);
        }
        particleRegion.setRegion(0, 0, 32, 32);
        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2 - 4, particleRegion, 4, 5) {
            @Override
            public void act(float delta) {
                super.act(delta);
                atkAct(this);
            }

            @Override
            public void effect(Unit un) {
                atkEffect(un, (int)(3 + (0.45 * atk) + level));
                remove();
            }
        });
        doing = "Stand";
        MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
        atk.setColor(Color.DARK_GRAY);
        MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        primary = true;
        hasMoved = true;
        cd[2] = maxCd[2];
    }

    private void ab4() {
        statusEffect.add(new StatusEffect("OnHit") {
            @Override
            public void start() {
                duration = 3;
            }

            @Override
            public void effect(Unit target, Unit source) {
                if(passiveTargets.containsKey(target)) {
                    for(Unit un : units)
                        if(un.getTeam() != team && un.getTargetable() && getDistance(target.getGridX(), un.getGridX(),
                                target.getGridY(), un.getGridY()) <= 2.5)
                            un.takeMagDmg((int)(0.35 * atk), ShotgunBug.this);
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            passiveTargets.put(target, 4);
                        }
                    }, 2 * Gdx.graphics.getDeltaTime());
                }
            }

            @Override
            public void end() {
                ShotgunBug.this.getStatus().remove(this);
            }
        });
        cd[3] = maxCd[3];
    }

    @Override
    public void alTurn() {
        super.alTurn();
        reducePassiveDur();
        if(ab1Active) {
            for(Unit un : units)
                if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab1X, un.getGridY(), ab1Y) <= 2.5f) {
                    un.takeMagDmg((int)(12 + (0.75 * mag) + (1.5 * level)), this);
                }
            ab1Active = false;
        }
        if(cd[1] == 0 && ab2Stacks == 0)
            ab2Stacks = 1;

    }

    @Override
    public void enTurn() {
        super.enTurn();
        reducePassiveDur();
    }
}
