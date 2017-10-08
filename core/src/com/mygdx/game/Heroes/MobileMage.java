package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;

public class MobileMage  extends Unit {
    private int[] ab1Pos;
    private boolean ab1Down;
    private ArrayList<Soul> souls;
    private int ab2Dur;

    public MobileMage(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 17;
        def = 12;
        mag = 27;
        hp = maxHp;
        name = "MobileMage";
        range = 2;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/MM_P.png")));
        charCol = new Color(0.825f, 0.375f, 1f, 1f);

        ab1Down = false;
        ab1Pos = new int[2];
        souls = new ArrayList<>();
        ab2Dur = 0;

        abMenu.add(new MenuOption("Putrid Blast", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0) {
                    if(ab1Down)
                        ab1();
                    else if (!primary || !secondary)
                        setDoing("ab1", false);
                }
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Breathtaker", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    ab2();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Repositioning", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Murder's Flight", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        statusEffect.add(new StatusEffect("OnAlTurn") {
            @Override
            public void effect(Unit target, Unit source) {
                for(Unit un : units)
                    if(un.getTargetable() && un.getTeam() != team && getDistance(getGridX(), un.getGridX(), getGridY(), un.getGridY()) <= 2.5f && un.getHp() < un.getMaxHp()) {
                        float dmg = 0.07f * (un.getMaxHp() - un.getHp());
                        un.takeMagDmg((int)dmg, MobileMage.this);
                    }
            }
        });

        maxCd[0] = 1;
        maxCd[1] = 3;
        maxCd[2] = 4;
        maxCd[3] = 17;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha)) {
            if(doing.equals("Move") && ab1Down)
                Tile.disp(batch, (getGridX(Tile.clickX) + ab1Pos[0]) * 48 + 8, (getGridY(Tile.clickY) + ab1Pos[1]) * 48, Color.ORANGE);
            return true;
        }
        else {
            boolean ret = false;
            if(num == sel) {
                switch(doing) {
                    case "ab1":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 4, Color.BLUE, true);
                        ret = true;
                        abMenu.setVisible(false);
                        break;
                    case "ab3":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, Color.BLUE, true);
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
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        switch (doing) {
            case "ab1":
                ab1Down = true;
                ab1Pos = new int[] {cX - getGridX(), cY - getGridY()};
                particleRegion.setRegion(32, 0, 48, 48);
                particles.add(new Particle(particleRegion, cX * 48 + 8, cY * 48) {
                    private int[] offset;

                    @Override
                    public void start() {
                        int xOfs = (int)(cX * 48 + 8 - getX());
                        int yOfs = (int)(cY * 48 - getY());
                        offset = new int[] {xOfs, yOfs};
                    }

                    @Override
                    public void increment() {
                        if(MobileMage.this.moving)
                            spr.setPosition(getX() + offset[0], getY() + offset[1]);
                        if(!ab1Down) {
                            remove();
                            particles.remove(this);
                        }
                    }
                });
                cd[0] = maxCd[0];
                doing = "Stand";
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                hasMoved = true;
                MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                mo.setColor(Color.DARK_GRAY);
                break;
            default:
                break;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(curTurn != team && Unit.sel > -1 && units.get(Unit.sel).getDoing().equals("MoveAnim"))
            for(Soul s : souls)
                s.checkUnit(units.get(Unit.sel));
    }

    @Override
    public void enTurn() {
        super.enTurn();
        for(Soul s : new ArrayList<>(souls)) {
            s.updatePos(getGridX(), getGridY());
            for(Unit un : units)
                s.checkUnit(un);
        }
    }

    private void ab1() {
        for(Unit un : units)
            if(un.getTargetable() && un.getTeam() != team && un.getGridX() == getGridX() + ab1Pos[0] && un.getGridY() == getGridY() + ab1Pos[1]) {
                int dmg = (int)(10 + level + (0.4 * mag));
                if(getDistance(0, ab1Pos[0], 0, ab1Pos[1]) <= 2.5f) // ab1Pos is position relative to MM
                    dmg *= 1.5;
                un.takeMagDmg(dmg, this);
            }
        ab1Down = false;
    }

    private void ab2() {
        for(Unit un : units)
            if(un.getTeam() != team && un.getTargetable()) {
                float dist = getDistance(un.getGridX(), getGridX(), un.getGridY(), getGridY());
                if(dist <= 2.5f) {
                    un.takeMagDmg((int)(17 + (1.25 * level) + (0.65 * mag)), this);
                    souls.add(new Soul(un.getGridX() - getGridX(), un.getGridY() - getGridY(), un));
                }
            }
        ab2Dur = 3;
    }


    private class Soul {
        private int x, y;
        private int xOfs, yOfs;
        private Unit source;
        private int stolenMDef;

        public Soul(int xOfs, int yOfs, Unit source) {
            this.xOfs = xOfs;
            this.yOfs = yOfs;
            this.source = source;
            stolenMDef = (int)(source.getMDef() * 1f);
            source.setMDef(source.getMDef() - stolenMDef);
        }

        public void checkUnit(Unit un) {
            if(un.equals(source) && un.getGridX() == x && un.getGridY() == y) {
                source.setMDef(source.getMDef() + stolenMDef);
                MobileMage.this.souls.remove(this);
            }
        }

        public void end() {
            source.setMDef(source.getMDef() + stolenMDef);
        }

        public void updatePos(int x, int y) {
            this.x = x + xOfs;
            this.y = y + yOfs;
        }
    }
}
