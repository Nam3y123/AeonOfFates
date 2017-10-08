package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.ArrayList;
import java.util.Objects;

public class Monsters extends Unit {
    protected boolean finished;
    protected boolean neutral;
    protected boolean draw;

    public Monsters(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, false);
        neutral = false;
        draw = true;
        charCol = Color.DARK_GRAY;
    }

    public Monsters(int x, int y, Stage game) {
        super(x, y, game, false, false);
        draw = true;
        neutral = true;
    }

    public void onTurnSwap() {
        draw = false;
    }

    @Override
    public void showMenu() {

    }

    @Override
    public void drawHUD(Batch batch, float parentAlpha) {
        if(draw)
            super.drawHUD(batch, parentAlpha);
    }

    public boolean getFinished() {
        if(finished)
            draw = true;
        return finished;
    }

    public static class Guardian extends Monsters {
        private int hpStore;
        private Unit curTarget;
        private int sourceX, sourceY;
        private Runnable onArrival;

        public Guardian(int x, int y, Stage game, boolean team) {
            super(x, y, game, team);
            maxHp = 650;
            hp = maxHp;
            hpStore = maxHp;
            atk = 55;
            mag = 0;
            def = 27;
            range = 1;
            statPerLevel[0] = 6;
            statPerLevel[1] = 6;
            statPerLevel[2] = 35;
            sourceX = getGridX();
            sourceY = getGridY();
            maxRes = (int)(maxHp * 0.2f);
            res = maxRes;
            name = "Core Guardian";
            curTarget = null;
            onArrival = null;

            statusEffect.add(new StatusEffect("OnDamaged") {
                @Override
                public void effect(Unit target, Unit source, int dmg) {
                    res -= dmg;
                    if(res < 0) {
                        hp += res;
                    }
                    curTarget = source;
                }
            });
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if(!moving && onArrival != null) {
                onArrival.run();
                onArrival = null;
            }
        }

        public void alTurn() {
            super.alTurn();
            maxRes = (int)(maxHp * 0.2f);
            res = (int)(maxHp * 0.2f);
        }

        public void enTurn() {
            super.enTurn();
        }

        @Override
        public void onTurnSwap() {
            finished = true;
            draw = false;
            sel = num;
            if(curTurn != team) {
                float targetDist = 10f;
                if(curTarget != null)
                    targetDist = (float)Math.sqrt(Math.pow(curTarget.getGridX() - sourceX, 2)
                            + Math.pow(curTarget.getGridY() - sourceY, 2));
                if(targetDist <= 3.5f) {
                    finished = false;
                    float[] atkDist = new float[4];
                    float defPosX = curTarget.getGridX() - getGridX() + 1;
                    float defPosY = curTarget.getGridY() - getGridY();
                    float[] posX = new float[] {defPosX + 1, defPosX, defPosX - 1, defPosX};
                    float[] posY = new float[] {defPosY, defPosY + 1, defPosY, defPosY - 1};
                    for(int i = 0; i < 4; i++)
                        atkDist[i] = (float)Math.sqrt(Math.pow(posX[i], 2) + Math.pow(posY[i], 2));

                    int lowest = -1;
                    for(int i = 0; i < 4; i++) {
                        boolean canMove = true;
                        for (Unit unit : units) {
                            if (posX[i] == unit.getGridX() && posY[i] == unit.getGridY())
                                canMove = false;
                        }
                        if(canMove && (lowest == -1 || atkDist[lowest] > atkDist[i]))
                            lowest = i;
                    }

                    if(lowest > -1) {
                        calcPath((int)(posX[lowest] + getGridX()) * 48, (int)(posY[lowest] + getGridY()) * 48);
                        if(path.getLength() > 1) {
                            doing = "MoveAnim";
                            curSegment = 1;
                            addAction(Actions.moveTo(path.getSegment(1)[0] * 48 + 8, path.getSegment(1)[1] * 48 + 4, 0.15f));
                            Tile.floorMapInit = false;
                            Tile.drawPath = false;
                        }
                        onArrival = () -> {
                            particleRegion.setRegion(0, 0, 32, 32);
                            float dir = getDirection(getGridX(), curTarget.getGridX(), getGridY(), curTarget.getGridY());
                            getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                                    getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, range){
                                @Override
                                public void effect(Unit un) {
                                    ArrayList<StatusEffect> se = Guardian.this.statusEffect;
                                    un.takePhysDmg(Guardian.this.atk, Guardian.this);
                                    for (int i = 0; i < se.size(); i++)
                                        if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                                            se.get(i).effect(un, Guardian.this);
                                    this.remove();
                                    finished = true;

                                }
                            });
                            doing = "AtkAnim";
                            curTarget = null;
                        };
                    } else
                        rangedAtk();

                } else if(targetDist <= 6.5f)
                    rangedAtk();
                /*
                else {
                    finished = false;
                    calcPath(sourceX * 48 + 24, sourceY * 48 + 24);
                    boolean pathClear = path == null; // If the Core Guardian is at its original position, the path is clear
                    while(!pathClear) {
                        pathClear = true;
                        for (Unit unit : units) {
                            if (path.last()[0] == unit.getGridX() && path.last()[1] == unit.getGridY())
                                pathClear = false;
                        }
                        if(!pathClear) {
                            path = new Tile.Path(path, path.getLength() -1);
                        }
                    }
                    if(path != null && path.getLength() > 1) {
                        doing = "MoveAnim";
                        curSegment = 1;
                        addAction(Actions.moveTo(path.getSegment(1)[0] * 48 + 8, path.getSegment(1)[1] * 48 + 4, 0.15f));
                        Tile.floorMapInit = false;
                        Tile.drawPath = false;
                        onArrival = () -> finished = true;
                    }
                }
                */
            }
        }

        public void rangedAtk() {
            finished = false;
            draw = false;
            calcPath(sourceX * 48 + 24, sourceY * 48 + 24);
            boolean pathClear = path == null; // If the Core Guardian is at its original position, the path is clear
            while(!pathClear) {
                pathClear = true;
                for (Unit unit : units) {
                    if (path.last()[0] == unit.getGridX() && path.last()[1] == unit.getGridY())
                        pathClear = false;
                }
                if(!pathClear) {
                    path = new Tile.Path(path, path.getLength() -1);
                }
            }
            if(path.getLength() > 1) {
                doing = "MoveAnim";
                curSegment = 1;
                addAction(Actions.moveTo(path.getSegment(1)[0] * 48 + 8, path.getSegment(1)[1] * 48 + 4, 0.15f));
                Tile.floorMapInit = false;
                Tile.drawPath = false;
            }
            onArrival = () -> {
                particleRegion.setRegion(0, 0, 32, 32);
                float dir = getDirection(getGridX(), curTarget.getGridX(), getGridY(), curTarget.getGridY());
                float targetDist = (float)Math.sqrt(Math.pow(curTarget.getGridX() - sourceX, 2)
                        + Math.pow(curTarget.getGridY() - sourceY, 2));
                getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                        getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, targetDist){
                    @Override
                    public void effect(Unit un) {
                        un.takeTrueDmg((int)(Guardian.this.atk * 0.8f), Guardian.this);
                    }

                    @Override
                    public boolean remove() {
                        finished = true;
                        return super.remove();
                    }
                });
                doing = "AtkAnim";
                curTarget = null;
            };
        }
    }
}
