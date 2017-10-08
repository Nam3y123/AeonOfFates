package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.mygdx.game.*;

import java.util.ArrayList;
import java.util.Objects;

import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static com.mygdx.game.Tile.drawShape;

public class Dancer extends Unit {
    private int ab1Dat[][]; // Bracket 1: ab1 #; Bracket 2: 0 = x, 1 = y
    private short ab1Size;
    private int ab2Dat[][]; // Bracket 1: ab2 #; Bracket 2: 0 = x, 1 = y, 2 = duration left

    public Dancer(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 17;
        def = 15;
        mag = 27;
        hp = maxHp;
        name = "Dancer";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Dancer_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 0.375f, 0.75f, 1f);
        res = 3;
        maxRes = 3;
        resSegmentSize = 1;
        resBigInterval = 4;
        resCol = new Color(0.375f, 0.375f, 0.75f, 1f);
        ab1Dat = new int[3][2];
        ab1Size = 0;
        this.ab2Dat = new int[][]{{x + 3, y + 1, 1}, {x - 3, y + 1, 0}, {0, 0, 0}};


        abMenu.add(new MenuOption("Air Strike", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && res >= 1)
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Captivate", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && res >= 2)
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Roundhouse", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && res >= 2)
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Showtime", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && res >= 1)
                    setDoing("ab4", false);
            }
        });

        particleRegion.setRegion(32, 0, 144, 144);
        particles.add(new Particle(particleRegion, 0, 0) {
            @Override
            public void display(Batch batch, float parentAlpha) {
                for(int i = 0; i < 3; i++)
                    if(ab2Dat[i][2] > 0) {
                        spr.setPosition(ab2Dat[i][0] * 48 - 40, ab2Dat[i][1] * 48 - 48);
                        spr.draw(batch);
                    }
            }
        });

        maxCd[0] = 0;
        maxCd[1] = 2;
        maxCd[2] = 2;
        maxCd[3] = 10;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            switch (doing) {
                case "ab1":
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 4, Color.BLUE, true);
                    Tile.setClickPos();
                    if(Tile.hit) {
                        Color hitCol = Color.GREEN;
                        for(int i = 0; i < ab1Size; i++) {
                            if(ab1Dat[i][0] == getGridX(Tile.clickX) && ab1Dat[i][1] == getGridY(Tile.clickY))
                                hitCol = Color.RED;
                        }
                        drawShape(batch, parentAlpha, getGridX(Tile.clickX) * 48 + 8, getGridY(Tile.clickY) * 48,
                                "circle", 0, hitCol, false);
                    }
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab2":
                    Color drawCol = Color.BLUE;
                    Tile.setClickPos();
                    int destX = getGridX(Tile.clickX);
                    int destY = getGridY(Tile.clickY);
                    boolean tileFree = Tile.walls.getPixel(destX,  destY) != Color.rgba8888(Color.WHITE);
                    for(Unit un: units)
                        if(un.getGridX() == destX && un.getGridY() == destY)
                            tileFree = false;
                    if(!tileFree)
                        drawCol = Color.RED;
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, drawCol, true);
                    if(Tile.hit) {
                        for(int i = 0; i < 3; i++)
                            if(getDistance(getGridX(Tile.clickX), ab2Dat[i][0], getGridY(Tile.clickY), ab2Dat[i][1]) <=
                                    1.5 && ab2Dat[i][2] > 0)
                                drawShape(batch, parentAlpha, ab2Dat[i][0] * 48 + 8, ab2Dat[i][1] * 48, "circle", 1,
                                        Color.GREEN, false);
                    }
                    ret = true;
                    abMenu.setVisible(false);
                    break;
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
        }
    }

    private void ab1() {
        ab1Dat[ab1Size] = new int[]{getGridX(Tile.clickX), getGridY(Tile.clickY)};
        ab1Size++;

        abMenu.setVisible(false);
        doing = "Stand";
        res--;
    }

    private void ab2() {
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
                    + Math.pow(cY - stY, 2))), Actions.run(() -> {
                doing = "Stand";

                for(int i = 0; i < 3; i++)
                    if(ab2Dat[i][2] > 0 && getDistance(getGridX(), ab2Dat[i][0], getGridY(), ab2Dat[i][1]) <= 1.5) {
                        for(Unit un : units)
                            if(un.getTeam() != team && un.getTargetable() && getDistance(un.getGridX(), ab2Dat[i][0],
                                    un.getGridY(), ab2Dat[i][1]) <= 1.5) {
                                int dmg = (int)(10 + level + (0.7 * mag));
                                un.takeMagDmg(dmg, this);
                                ArrayList<StatusEffect> se = statusEffect;
                                passive(un);
                                un.getStatus().add(StatusEffect.stun);
                            }
                        ab2Dat[i][2] = 0;
                    }

                for(int i = 0; i < 3; i++)
                    if(ab2Dat[i][2] == 0) {
                        ab2Dat[i] = new int[]{stX, stY, 3};
                        break;
                    }
            })));


            res -= 2;
            doing = "ab1Anim";
            cd[1] = maxCd[1];
        }
    }

    private void passive(Unit target) {
        if(getDistance(getGridX(), target.getGridX(), getGridY(), target.getGridY()) <= 1.5) {
            int shieldAmt = (int)(7 + (level) + (0.3 * mag));
            addShield(shieldAmt);
            shields.add(shieldAmt);
            getStatus().add(new StatusEffect("Dummy") {
                private final int position = shields.size() - 1;
                @Override
                public void start() {
                    duration = 1;
                }

                @Override
                public void end() {
                    if(shields.size() > position) {
                        addShield(-shields.get(position));
                        shields.remove(position);
                    }
                }
            });
        }
    }

    @Override
    public void alTurn() {
        super.alTurn();
        for(int i = 0; i < ab1Size; i++) {
            int dmg = (int)(12 + (1.5 * level) + (0.8 * mag));
            for(Unit un : units)
                if(un.getTeam() != team && un.getTargetable() && un.getGridX() == ab1Dat[i][0] && un.getGridY() ==
                        ab1Dat[i][1]) {
                    un.takeMagDmg(dmg, this);
                    ArrayList<StatusEffect> se = statusEffect;
                    passive(un);
                }
        }
        ab1Size = 0;

        for(int i = 0; i < 3; i++)
            if(ab2Dat[i][2] > 0)
                ab2Dat[i][2]--;

        res = maxRes;
    }
}
