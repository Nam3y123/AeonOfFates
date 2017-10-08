package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.MenuOption;
import com.mygdx.game.StatusEffect;
import com.mygdx.game.Tile;
import com.mygdx.game.Unit;

import java.util.ArrayList;
import java.util.Objects;

import static com.mygdx.game.Tile.drawShape;

public class Igor extends Unit {

    public Igor(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 195;
        atk = 14;
        def = 12;
        mag = 31;
        hp = maxHp;
        name = "Igor";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Igor_Particles.png")), 0, 0, 32, 32);
        charCol = Color.GREEN;
        res = 5;
        maxRes = 5;
        resSegmentSize = 1;
        resBigInterval = 6;


        abMenu.add(new MenuOption("Arcane Artillery", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if((cd[0] == 0 || res == 5) && (!secondary || !primary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Scout Trap", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if((cd[1] == 0 || res == 5) && (!secondary || !primary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Anglefly", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if((cd[2] == 0 || res == 5) && (!secondary || !primary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Young Lavaling", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!secondary || !primary))
                    setDoing("ab4", false);
            }
        });

        maxCd[0] = 1;
        maxCd[1] = 3;
        maxCd[2] = 4;
        maxCd[3] = 16;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = false;
            switch (doing) {
                case "ab1":
                case "ab3":
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
                case "ab2":
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 2, Color.BLUE, true);
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
        int destX = getGridX(Tile.clickX);
        int destY = getGridY(Tile.clickY);
        boolean tileFree = Tile.walls.getPixel(destX,  destY) != Color.rgba8888(Color.WHITE);
        for(Unit un: units)
            if(un.getGridX() == destX && un.getGridY() == destY)
                tileFree = false;

        if(tileFree) {
            ArcaneArtillery add = new ArcaneArtillery(destX, destY, game, team);
            units.add(add);
            updateVisibility();
            updatePosition();
            Igor.this.getParent().addActor(add);

            abMenu.setVisible(false);
            doing = "Stand";
            if(cd[0] == 0)
                cd[0] = maxCd[0];
            else
                res = 0;
            hasMoved = true;
            MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private void ab2() {
        int destX = getGridX(Tile.clickX);
        int destY = getGridY(Tile.clickY);
        boolean tileFree = Tile.walls.getPixel(destX,  destY) != Color.rgba8888(Color.WHITE);
        for(Unit un: units)
            if(un.getGridX() == destX && un.getGridY() == destY)
                tileFree = false;

        if(tileFree) {
            ScoutTrap add = new ScoutTrap(destX, destY, game, team);
            units.add(add);
            updateVisibility();
            updatePosition();
            Igor.this.getParent().addActor(add);

            abMenu.setVisible(false);
            doing = "Stand";
            if(cd[1] == 0)
                cd[1] = maxCd[1];
            else
                res = 0;
            hasMoved = true;
            MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
            mo.setColor(Color.DARK_GRAY);
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private void lifespringSpawn(float sourceX, float sourceY) {
        float dir = getDirection(getGridX(), getGridX(sourceX), getGridY(), getGridY(sourceY));
        float posX = sourceX + (float)Math.cos(Math.toRadians(dir)) * 48;
        float posY = sourceY + (float)Math.sin(Math.toRadians(dir)) * 48;
        getParent().addActor(new Lifespring(getGridX(sourceX), getGridY(sourceY), getGridX(posX + 24), getGridY(posY + 20)));
        posX = sourceX - (float)Math.cos(Math.toRadians(dir)) * 48;
        posY = sourceY - (float)Math.sin(Math.toRadians(dir)) * 48;
        getParent().addActor(new Lifespring(getGridX(sourceX), getGridY(sourceY), getGridX(posX + 24), getGridY(posY + 20)));
    }


    //Igor's Summons
    private class Lifespring extends Actor {
        private Sprite region;
        private boolean spawnAnim;
        private float age;

        Lifespring(int startX, int startY, int destX, int destY) {
            setBounds(startX * 48 + 15, startY * 48 + 7, 48, 48);
            addAction(Actions.moveTo(destX * 48 + 15, destY * 48 + 7, 0.15f));
            region = new Sprite(particleRegion);
            region.setRegion(32, 0, 38, 38);
            age = 0;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            region.draw(batch);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            age += delta;
            region.setPosition(getX(), getY());
            if(age > 0.175f)
                for(Unit un : units)
                    if(un.getGridX() == getGridX(getX()) && un.getGridY() == getGridX(getY())) {
                        if(un.equals(Igor.this)) {
                            heal(5 + (int)(0.4 * mag) + (int)(1.5 * level));
                            res++;
                            getParent().removeActor(this);
                        } else if(un.getTeam() != team && un.getTargetable())
                            getParent().removeActor(this);
                    }
        }
    }

    private class ArcaneArtillery extends Unit {
        private final Igor creator = Igor.this;
        private final ArrayList<Unit> hitUnits = new ArrayList<>();

        private ArcaneArtillery(int x, int y, Stage game, boolean team) {
            super(x, y, game, team, false);
            maxHp = 95 + (8 * creator.level);
            atk = (int)(8 + (creator.level) + (0.45 * creator.mag));
            def = (int)(7 + (0.8 * creator.level) + (0.2 * creator.mag));
            mag = 0;
            hp = maxHp;
            name = "Arcane Artillery";
            range = 3;
            particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Igor_Particles.png")), 0, 0, 32, 32);
            atlas = new TextureAtlas(Gdx.files.internal("Spritesheets/Characters/Igor/Artillery.atlas"));
            spr.setTexture(new Texture(Gdx.files.internal("Spritesheets/Characters/Igor/Artillery.png")));
            spr.setPosition(getX(), getY());
            anim = "Stand";
            visionRange = 2;
            res = 3;
            maxRes = 3;
            resSegmentSize = 1;
            resBigInterval = 4;
            hasMoved = true;

            statusEffect.add(new StatusEffect("OnDamaged") {

                @Override
                public void effect(Unit target, Unit source, int dmg) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            if(hp <= 0) {
                                lifespringSpawn(getX(), getY());
                                units.remove(ArcaneArtillery.this);
                                getParent().removeActor(ArcaneArtillery.this);
                                updatePosition();
                                updateVisibility();
                                remove();
                            }
                        }
                    }, 0.01f);
                }
            });
        }

        @Override
        public boolean drawTile(Batch batch, float parentAlpha) {
            boolean ret = false;
            if(num != sel || res == 0)
                doing = "Stand";
            switch (doing) {
                case "Attack":
                    drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, Color.BLUE, true);
                    ret = true;
                    abMenu.setVisible(false);
                    break;
            }
            return ret;
        }

        @Override
        public void clickAct() {
            if(!doing.equals("Attack"))
                super.clickAct();
            else {
                for(Unit un : units)
                    if(un.getTeam() != team && un.getTargetable() && un.getGridX() == getGridX(Tile.clickX) && un.getGridY() == getGridY(Tile.clickY)) {
                        int dmg = atk;
                        if(hitUnits.contains(un))
                            dmg = atk / 2;
                        un.takePhysDmg(dmg, this);
                        ArrayList<StatusEffect> se = statusEffect;
                        for (int i = 0; i < se.size(); i++)
                            if (Objects.equals(se.get(i).getType(), "OnSpellHit"))
                                se.get(i).effect(un, Igor.this, dmg);
                        for(Unit aAUn: units)
                            if(aAUn.getTeam() == team && aAUn.getTargetable() && aAUn instanceof ArcaneArtillery)
                                aAUn.setRes(aAUn.getRes() - 1);
                        hitUnits.add(un);
                        if(res == 0)
                            doing = "Stand";
                        if (secondary)
                            primary = true;
                        else
                            secondary = true;
                        hasMoved = true;
                        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
                        mo.setColor(Color.DARK_GRAY);
                    }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
        }

        @Override
        public void alTurn() {
            super.alTurn();
            hasMoved = true;
            res = 3;
            setExp(0);
            hitUnits.clear();
        }

        @Override
        public void enTurn() {
            super.enTurn();
            setExp(0);
        }
    }

    private class ScoutTrap extends Unit {
        private final Igor creator = Igor.this;
        private int dur;
        private boolean active;
        private final Group parent = Igor.this.getParent();

        ScoutTrap(int x, int y, Stage game, boolean team) {
            super(x, y, game, team, false);
            maxHp = 20 + (3 * creator.level);
            atk = 0;
            def = 0;
            mag = (int)(12 + (1.5 * level) + (0.75 * creator.mag));
            hp = maxHp;
            name = "Scout Trap";
            particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Igor_Particles.png")), 0, 0, 32, 32);
            atlas = new TextureAtlas(Gdx.files.internal("Spritesheets/Characters/Igor/ScoutTrap.atlas"));
            spr.setTexture(particleRegion.getTexture());
            spr.setPosition(getX(), getY());
            anim = "Stand";
            visionRange = 2;
            visibility = 3;
            dur = 1;
            hasMoved = true;
            active = true;

            statusEffect.add(new StatusEffect("OnDamaged") {

                @Override
                public void effect(Unit target, Unit source, int dmg) {
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            if(hp <= 0) {
                                lifespringSpawn(getX(), getY());
                                units.remove(ScoutTrap.this);
                                getParent().removeActor(ScoutTrap.this);
                                updatePosition();
                                updateVisibility();
                                remove();
                            }
                        }
                    }, 0.01f);
                }
            });
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            targetable = visibility != 3;
            for(Unit un : units)
                if(active && un.getGridX() == getGridX(getX()) && un.getGridY() == getGridX(getY())) {
                    if(un.getTeam() != team && un.getTargetable()) {
                        for(Unit dmgUn : units)
                            if(dmgUn.getTeam() != team && dmgUn.getTargetable() && getDistance(getGridX(),
                                    dmgUn.getGridX(), getGridY(), dmgUn.getGridY()) <= 2.5)
                                dmgUn.takeMagDmg(mag, creator);
                        Timer.schedule(new Timer.Task() {
                            @Override
                            public void run() {
                                lifespringSpawn(getX(), getY());
                                units.remove(ScoutTrap.this);
                                parent.removeActor(ScoutTrap.this);
                                updatePosition();
                                updateVisibility();
                                remove();
                            }
                        }, 0.01f);
                        active = false;
                    }
                }
        }

        @Override
        public void alTurn() {
            super.alTurn();
            setStunned(true);
            setExp(0);
            dur++;
            if(dur == 8) {
                lifespringSpawn(getX(), getY());
                units.remove(ScoutTrap.this);
                getParent().removeActor(ScoutTrap.this);
                updatePosition();
                updateVisibility();
                remove();
            }
        }

        @Override
        public void enTurn() {
            super.enTurn();
            setStunned(true);
            setExp(0);
        }
    }
}
