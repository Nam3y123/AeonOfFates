package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mygdx.game.MenuOption;
import com.mygdx.game.StatusEffect;
import com.mygdx.game.Tile;
import com.mygdx.game.Unit;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static com.mygdx.game.Tile.drawShape;
import static java.lang.Math.floor;

public class RuneMage extends Unit {
    private boolean passiveUp;
    private Unit ab2Target;
    private boolean ab3Active, ab3Dir;
    private int ab3X, ab3Y;

    public RuneMage(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 16;
        def = 18;
        mag = 27;
        hp = maxHp;
        name = "RuneMage";
        range = 2;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Norwin_Particles.png")), 0, 0, 32, 32);

        res = 0;
        maxRes = 5;
        resBigInterval = 6;
        resSegmentSize = 1;

        abMenu.add(new MenuOption("Runesurge", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Entrapment", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary)) {
                    setDoing("ab2", false);
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Runic Barrier", combatSkin, "combatMenu") {
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

        statusEffect.add(new StatusEffect("OnSpellHit") {
            @Override
            public void effect(Unit target, Unit source) {
                if(res < maxRes)
                    res++;
            }
        });
        statusEffect.add(new StatusEffect("OnDamaged") {
            @Override
            public void effect(Unit target, Unit source) {
                if(res > 0) {
                    res--;
                }
            }
        });

        maxCd[0] = 3;
        maxCd[1] = 4;
        maxCd[2] = 4;
        maxCd[3] = 12;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = false;
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            switch (doing) {
                case "ab1":
                    Tile.setClickPos();
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "circle", 3, BLUE, true);
                    if(Tile.hit) {
                        int cX = getGridX(clickX) * 48 + 8;
                        int cY = getGridY(clickY) * 48;
                        Tile.drawShape(batch, parentAlpha, cX, cY, "circle", 2, GREEN, true);
                    }
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab2":
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab3":
                    Tile.setClickPos();
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 4, BLUE, true);
                    if(Tile.hit) {
                        int cX = getGridX(clickX);
                        int cY = getGridY(clickY);
                        int width = cX == getGridX() ? 3 : 1;
                        int height = cY == getGridY() ? 3 : 1;
                        int ofsX = cX == getGridX() ? -1 : 0;
                        int ofsY = cY == getGridY() ? -1 : 0;
                        Tile.drawRect(batch, parentAlpha, (cX + ofsX) * 48 + 8, (cY + ofsY) * 48, width, height, GREEN, false);
                    }
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab4":
                    Tile.setClickPos();
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 5, BLUE, true);
                    break;
            }
        }
        return ret;
    }

    @Override
    public void clickAct() {
        super.clickAct();
        int cX = getGridX(clickX);
        int cY = getGridY(clickY);
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
    public void act(float delta) {
        super.act(delta);
        if(ab2Target != null && sel > -1 && units.get(sel).equals(ab2Target)) {
            if(ab2Target.getDoing().equals("MoveAnim")) {
                ab2Target.silence();
            } else if(!ab2Target.getDoing().equals("Stand"))
                ab2Target.setMoveable(false);
        }
    }

    private void checkPassive() {
        boolean passiveUp = true;
        for(int i = 0; i < 3; i++)
            if(cd[i] == 0)
                passiveUp = false;
        if(passiveUp == this.passiveUp)
            return;
        else if(passiveUp)
            mspeed++;
        else
            mspeed--;
        this.passiveUp = passiveUp;
    }

    private void ab1(int cX, int cY) {
        int startRes = res;
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), cX, un.getGridY(), cY) <= 2.5f)
                un.takeMagDmg((int)((6 + (level / 3f) + (0.45f * mag)  * (startRes + 1))), this);
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
        cd[0] = maxCd[0];
        checkPassive();
    }

    private void ab2(int cX, int cY) {
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable() && un.getGridX() == cX && un.getGridY() == cY) {
                un.takeMagDmg((int)(10 + (level) + (0.6f * mag)), this);
                un.setMovementImpared(true);
                ab2Target = un;
            }
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
        cd[1] = maxCd[1];
        checkPassive();
    }

    private void ab3(int cX, int cY) {
        ab3Dir = cY != getGridY(); // If the wall is vertical to the player, each tile affected is horizontal
        ab3X = cX;
        ab3Y = cY;
        ab3Dmg();
        ab3Active = true;
        doing = "Stand";
        if(secondary)
            primary = true;
        else
            secondary = true;
        cd[2] = maxCd[2];
        checkPassive();
    }

    private void ab3Dmg() {
        for(Unit un : units) {
            int uX = un.getGridX();
            int uY = un.getGridY();
            if(un.getTeam() != team && un.getTargetable() && ((ab3Dir && uX >= ab3X - 1 && uX <= ab3X + 1 && uY == ab3Y) ||
                    (!ab3Dir && uY >= ab3Y - 1 && uY <= ab3Y + 1 && uX == ab3X)))
                un.takeMagDmg((int)(10 + (level) + (0.6f * mag)), this);
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        checkPassive();
        ab2Target = null;
        if(ab3Active) {
            ab3Dmg();
            ab3Active = false;
        }
    }
}
