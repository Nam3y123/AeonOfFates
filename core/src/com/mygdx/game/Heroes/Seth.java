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

public class Seth extends Unit {
    private final StatusEffect PASSIVE = new StatusEffect("Dummy");
    private int ab2X, ab2Y;
    private boolean ab2Active;

    public Seth(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 2;
        def = 12;
        mag = 27;
        hp = maxHp;
        name = "Seth";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Lumina_Particles.png")), 0, 0, 32, 32);
        //charCol = new Color(0.375f, 0.375f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        resCol = Color.YELLOW;
        res = 3;
        maxRes = 3;
        resSegmentSize = 1;
        resBigInterval = 4;
        ab2Active = false;

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
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnAttack") {
            @Override
            public void effect(Unit target, Unit source) {
                if(target.getMovementImpared()) {
                    for(Unit un : units)
                        if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), target.getGridX(),
                                un.getGridY(), target.getGridY()) <= 2.5f) {
                            float dmg = 12 + (2 * level) + (0.4f * mag) + (0.5f * atk);
                            if(un.getStatus().contains(PASSIVE))
                                dmg *= 1.5f;
                            un.takeMagDmg((int)dmg, Seth.this);
                        }
                    target.getStatus().add(PASSIVE);
                }
            }
        });
        maxCd[0] = 2;
        maxCd[1] = 3;
        maxCd[2] = 4;
        maxCd[3] = 15;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        boolean ret = false;
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            switch (doing) {
                case "ab1":
                    Tile.drawShape(batch, parentAlpha, (int)getX(), (int)getY() - 4, "line", 3, Color.BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab2":
                    Tile.setClickPos();
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 5, BLUE, true);
                    if (Tile.hit) {
                        int tX = (int) floor((clickX - 8) / 48);
                        int tY = (int) floor(clickY / 48);
                        drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", res, GREEN, false);
                    }
                    ret = true;
                    abMenu.setVisible(false);
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
                if(ab2Active) {
                    cd[1] = maxCd[1];
                    res = 0;
                    ab2Active = false;
                } else {
                    doing = "Stand";
                    if(secondary)
                        primary = true;
                    else
                        secondary = true;
                    ab2X = cX;
                    ab2Y = cY;
                    ab2Active = true;
                }
                break;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(doing.equals("ab1Anim"))
            for(Unit un : units)
                if(un.getTeam() != team && un.getTargetable() && un.getGridX() == getGridX() && un.getGridY() == getGridY())
                    un.getStatus().add(StatusEffect.slow(1));
    }

    private void ab1(int cX, int cY) {
        int abX = getGridX();
        int abY = getGridY();
        if(cX > getGridX())
            abX += 3;
        else if (cX < getGridX())
            abX -= 3;
        else if (cY > getGridY())
            abY += 3;
        else if (cY < getGridY())
            abY -= 3;

        boolean pathOpen = Tile.walls.getPixel(abX, abY) != Color.rgba8888(Color.WHITE);
        for (Unit un : units)
            if(un.getGridX() == abX && un.getGridY() == abY)
                pathOpen = false;
        if(pathOpen) {
            addAction(Actions.sequence(Actions.moveTo(abX * 48 + 8, abY * 48 + 4, 0.25f),
                    Actions.delay(0.15f), Actions.run(() -> doing = "Stand")));
            doing = "ab1Anim";
            cd[0] = maxCd[0];
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private void ab2() {
        if(res == 0) {
            cd[1] = maxCd[1] - 1;
            ab2Active = false;
        } else {
            for(Unit un : units)
                if(un.getTargetable() && un.getTeam() != team && getDistance(ab2X, un.getGridX(), ab2Y, un.getGridY()) <= res + 0.5f)
                    un.takeMagDmg((int)(19 + (0.9 * mag) + (2 * level)), this);
            res--;
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(ab2Active)
            ab2();
        else if(cd[1] == 0)
            res++;
        if(res > maxRes)
            res = maxRes;
    }
}
