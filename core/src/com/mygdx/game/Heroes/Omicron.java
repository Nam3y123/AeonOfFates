package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.drawShape;

public class Omicron extends Unit {
    private int[][] ghosts;
    private Particle ghostPortal;
    private boolean ghostPortalOpen;

    public Omicron(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 235;
        atk = 12;
        def = 27;
        mag = 20;
        hp = maxHp;
        name = "Omicron";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Omicron_Particles.png")), 32, 0, 144, 144);
        charCol = new Color(0.375f, 0.375f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);

        ghosts = new int[3][3];
        particleRegion.setRegion(32, 0, 144, 144);
        ghostPortal = new Particle(particleRegion, (int)getX() - 48, (int)getY() - 52) {
            @Override
            public void display(Batch batch, float parentAlpha) {
                if(ghostPortalOpen)
                    super.display(batch, parentAlpha);
            }
        };
        particles.add(ghostPortal);
        ghostPortalOpen = true;
        particleRegion.setRegion(32, 288, 144, 144);
        particles.add(new Particle(particleRegion, x * 48 - 40, y * 48 - 48) {
            @Override
            public void start() {
                spr.setOriginCenter();
            }

            @Override
            public void display(Batch batch, float parentAlpha) {
                super.display(batch, parentAlpha);
                spr.rotate(0.5f);
            }
        });
        for(int i = 0; i < 3; i++) {
            particleRegion.setRegion(32, 144, 144, 144);
            final int finalI = i;
            particles.add(new Particle(particleRegion, x * 48 - 40, y * 48 - 48) {
                @Override
                public void start() {
                    spr.setAlpha(0.875f - (0.125f * finalI));
                    spr.setOriginCenter();
                    spr.setScale(0.9f - (0.1f * finalI));
                }

                @Override
                public void display(Batch batch, float parentAlpha) {
                    super.display(batch, parentAlpha);
                    spr.rotate((finalI % 2 == 0 ? -1 : 1) * (finalI + 2) / 2f);
                }
            });
        }
        for(int i = 0; i < 3; i++) {
            particleRegion.setRegion(176, 0, 48, 64);
            final int finalI = i;
            particles.add(new Particle(particleRegion, 0, 0) {
                private boolean setup;

                @Override
                public void display(Batch batch, float parentAlpha) {
                    if(ghosts[finalI][2] > 0) { // If the ghost is in place
                        if(!setup)
                            spr.setPosition(ghosts[finalI][0] * 48 + 8, ghosts[finalI][1] * 48 + 4);
                        super.display(batch, parentAlpha);
                    } else
                        setup = false;
                }
            });
        }

        abMenu.add(new MenuOption("Levitate", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Gravity Field", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0)
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Rewind", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0)
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Spatial Reconfiguration", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0)
                    setDoing("ab4", false);
            }
        });

        maxCd[0] = 4;
        maxCd[1] = 4;
        maxCd[2] = 2;
        maxCd[3] = 10;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(moving && ghostPortalOpen) {
            int portalX = getGridX(ghostPortal.getSpr().getX() + 24) + 1;
            int portalY = getGridY(ghostPortal.getSpr().getY() + 20) + 1;
            if(getDistance(getGridX(), portalX, getGridY(), portalY) > 1.5f) {
                ghostPortalOpen = false;
                boolean ghostFound = false;
                int i = 0;
                while(!ghostFound) {
                    if(ghosts[i][2] == 0) {
                        ghostFound = true;
                        ghosts[i][0] = portalX;
                        ghosts[i][1] = portalY;
                        ghosts[i][2] = 3;
                    }
                    i++;
                    if(i == 3) // If there are already three ghosts in play - should never happen
                        ghostFound = true;
                }
            }
        }
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
                        Tile.drawRing(batch, parentAlpha, getX(), getY() - 4, 5, 2, BLUE, true);
                        if(Tile.hit) {
                            int clickXTile = getGridX(Tile.clickX);
                            int clickYTile = getGridY(Tile.clickY);
                            Color col = Color.RED;
                            for(int i = 0; i < 3; i++) {
                                if(ghosts[i][0] == clickXTile && ghosts[i][1] == clickYTile)
                                    col = Color.GREEN;
                            }
                            int gX = clickXTile * 48 + 8;
                            int gY = clickYTile * 48;
                            drawShape(batch, parentAlpha, gX, gY, "circle", 2, col, true);
                        }
                        abMenu.setVisible(false);
                        ret = true;
                        break;
                    case "ab2":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 1, BLUE, true);
                        Tile.setClickPos();
                        if(Tile.hit) {
                            int gX = getGridX(Tile.clickX) * 48 + 8;
                            int gY = getGridY(Tile.clickY) * 48;
                            drawShape(batch, parentAlpha, gX, gY, "circle", 2, GREEN, true);
                        }
                        abMenu.setVisible(false);
                        ret = true;
                        break;
                    case "ab3":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 8, BLUE, true);
                        Tile.setClickPos();
                        if(Tile.hit) {
                            int clickXTile = getGridX(Tile.clickX);
                            int clickYTile = getGridY(Tile.clickY);
                            Color col = Color.RED;
                            for(int i = 0; i < 3; i++) {
                                if(ghosts[i][0] == clickXTile && ghosts[i][1] == clickYTile)
                                    col = Color.GREEN;
                            }
                            int gX = clickXTile * 48 + 8;
                            int gY = clickYTile * 48;
                            drawShape(batch, parentAlpha, gX, gY, "circle", 2, col, true);
                        }
                        abMenu.setVisible(false);
                        ret = true;
                        break;
                    case "ab4":
                        drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 1, BLUE, true);
                        Tile.setClickPos();
                        if(Tile.hit) {
                            int gX = getGridX(Tile.clickX) * 48 + 8;
                            int gY = getGridY(Tile.clickY) * 48;
                            drawShape(batch, parentAlpha, gX, gY, "circle", 1, GREEN, true);
                        }
                        abMenu.setVisible(false);
                        ret = true;
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
            case "ab2": // TODO: Add this ability
                break;
            case "ab3":
                ab3();
                break;
        }
    }

    private void ab1() {
        int hit = checkGhosts();
        int clickGridX = getGridX(Tile.clickX);
        int clickGridY = getGridY(Tile.clickY);
        if(hit > -1) {
            for(Unit un : units) {
                if(un.getTargetable() && getDistance(clickGridX, un.getGridX(), clickGridY, un.getGridY()) <= 2.5f) {
                    un.setTargetable(false);
                    un.setStunned(true);
                    un.getStatus().add(new StatusEffect(un.getTeam() == team ? "OnAlTurn" : "OnEnTurn") {
                        @Override
                        public void effect(Unit target, Unit source) {
                            un.setTargetable(true);
                            if(un.getTeam() != team)
                                un.takeMagDmg((int)(6 + (level) + (0.45 * mag) + (0.06 * un.getMaxHp())));
                            un.getStatus().remove(this);
                        }
                    });
                }
            }

            doing = "Stand";
            MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
            hasMoved = true;
            cd[0] = maxCd[0];
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private void ab2() {

    }

    private void ab3() {
        int hit = checkGhosts();
        int gX = getGridX(Tile.clickX) * 48 + 8;
        int gY = getGridY(Tile.clickY) * 48 + 4;
        if(hit > -1) {
            setPosition(gX, gY);
            ghosts[hit][2] = 0;
            for(Unit un : units) {
                if(un.getTeam() != team && un.getTargetable() && getDistance(getGridX(), un.getGridX(),
                        getGridY(), un.getGridY()) <= 2.5f) {
                    un.takeMagDmg((int)(8 + (0.75 * level) + (0.5 * mag)), this);
                    un.getStatus().add(StatusEffect.slow(1));
                }
            }

            doing = "Stand";
            cd[2] = maxCd[2];
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private int checkGhosts() {
        int clickXTile = getGridX(Tile.clickX);
        int clickYTile = getGridY(Tile.clickY);
        int hit = -1;
        for(int i = 0; i < 3; i++) {
            if(ghosts[i][0] == clickXTile && ghosts[i][1] == clickYTile)
                hit = i;
        }
        int gX = clickXTile * 48 + 8;
        int gY = clickYTile * 48 + 4;
        return hit;
    }

    @Override
    public void alTurn() {
        super.alTurn();
        ghostPortal.getSpr().setPosition(getX() - 48, getY() - 52);
        ghostPortalOpen = true;
        for(int i = 0; i < 3; i++)
            if(ghosts[i][2] > 0)
                ghosts[i][2]--;
    }
}
