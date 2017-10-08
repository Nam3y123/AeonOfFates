package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;

import static com.mygdx.game.Tile.clickX;
import static com.mygdx.game.Tile.clickY;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

public class Epsilon extends Unit {
    private Pixmap baseMap;
    private int passiveOfs, passiveRange;
    private int passiveX, passiveY;
    private final Sprite baseSpr = new Sprite(new Texture(new Pixmap(768, 768, Pixmap.Format.RGBA8888)));
    private int ab1X, ab1Y, ab1Dur;
    private boolean ab1On;
    private ArrayList<Unit> ab1Targets;
    private final StatusEffect AB1_EFFECT = new StatusEffect("OnDamaged") {
        @Override
        public void effect(Unit target, Unit source, int dmg) {
            if(source == Epsilon.this) {
                target.takeTrueDmg((int)(dmg * 0.25 * ab1Dur));
                ab1Targets.remove(target);
                target.getStatus().remove(this);
            }
        }
    };
    private int ab2X, ab2Y;
    private final StatusEffect.MoveRule AB2_EFFECT = new StatusEffect.MoveRule(){
        private float distanceFromCenter(int x, int y) {
            return (float)Math.sqrt(Math.pow(x - ab2X, 2) + Math.pow(y - ab2Y, 2));
        }

        public boolean viableTile(Unit target, int tileX, int tileY) {
            return distanceFromCenter(tileX, tileY) <= 2.5;
        }
    };
    private Unit ab2Target;
    private int ab3X, ab3Y, ab3Range;
    private boolean ab3On;

    public Epsilon(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 210;
        atk = 15;
        def = 10;
        mag = 28;
        hp = maxHp;
        name = "Epsilon";
        range = 3;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Epsilon_Particles.png")),0, 0, 32, 32);
        charCol = Color.PINK;
        //
        passiveOfs = 0;
        passiveRange = 1;
        passiveX = (int)getX();
        passiveY = (int)getY();
        ab1On = false;
        ab1Dur = 0;
        ab3On = false;

        abMenu.add(new MenuOption("Magic Field", combatSkin, "combatMenu") { // WOW this name is trash, TODO: rename pls
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!secondary || !primary))
                    setDoing("ab1", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Psychic Chains", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!secondary || !primary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Dismemberment", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!secondary || !primary))
                    setDoing("ab3_a", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Arcane Artillery", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!secondary || !primary))
                    setDoing("ab4", false);
            }
        });

        baseMap = new Pixmap(768, 768, Pixmap.Format.RGBA8888);
        Texture texture = particleRegion.getTexture();
        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();
        }
        Pixmap pixmap = texture.getTextureData().consumePixmap();
        particleRegion.setRegion(256, 0, 256, 256);
        for(int ix = 0; ix < 768; ix++)
            for(int iy = 0; iy < 768; iy++) {
                int col = pixmap.getPixel(256 + (ix % 256), iy % 256);
                baseMap.drawPixel(ix, iy, col);
            }

        maxCd[0] = 3;
        maxCd[1] = 5;
        maxCd[2] = 3;
        maxCd[3] = 2;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        passiveOfs++;
        if(passiveOfs == 512)
            passiveOfs = 0;
        if(moving && getDistance(getX(), passiveX, getY(), passiveY) > 24 + passiveRange * 48)
            passiveRange = 0;
    }

    @Override
    public boolean drawTile(Batch batch, float parentAlpha) {
        if(passiveRange > 0) {
            Epsilon ep = Epsilon.this;
            Pixmap filterMap = new Pixmap(48 + 96 * passiveRange, 48 + 96 * passiveRange, Pixmap.Format.RGBA8888);
            int xOfs = ((int)Math.abs(game.getRoot().getX() / 2f + ep.passiveX + passiveOfs / 2)) % 256;
            int yOfs = ((int)Math.abs(1056 - game.getRoot().getY() / 2f - ep.passiveY + passiveOfs / 2)) % 256;
            filterMap.drawPixmap(baseMap, -xOfs, -yOfs);
            filterMap.setColor(0, 0, 0, 0);
            for(int ix = 0; ix < 1 + 2 * passiveRange; ix++)
                for(int iy = 0; iy < 1 + 2 * passiveRange; iy++) {
                    float dist = getDistance(ix, passiveRange, iy, passiveRange);
                    if(dist > passiveRange + 0.5f) {
                        filterMap.fillRectangle(ix * 48, iy * 48, 48, 48);
                    }
                }

            if(baseSpr.getTexture() != null)
                baseSpr.getTexture().dispose();
            baseSpr.setTexture(new Texture(filterMap));
            baseSpr.setSize(48 + 96 * passiveRange, 48 + 96 * passiveRange);
            baseSpr.setPosition(ep.passiveX - 48 * passiveRange, ep.passiveY - 4 - 48 * passiveRange);
            baseSpr.draw(batch);

            if(shiftDown)
                Gdx.app.log("", "");
        }

        if(super.drawTile(batch, parentAlpha))
            return true;
        else {
            boolean ret = true;
            if(num == sel) {
                switch(doing) {
                    case "ab1":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", passiveRange > 0 ? 6 : 4, Color.BLUE, true);
                        abMenu.setVisible(false);
                        break;
                    case "ab2":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", passiveRange > 0 ? 5 : 3, Color.BLUE, true);
                        abMenu.setVisible(false);
                        break;
                    case "ab3_a":
                        Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", passiveRange > 0 ? 5 : 3, Color.BLUE, true);
                        abMenu.setVisible(false);
                        break;
                    case "ab3_b":
                        Tile.setClickPos();
                        Tile.drawShape(batch, parentAlpha, ab3X * 48 + 8, ab3Y * 48, "circle", 3, Color.BLUE, true);
                        if(Tile.hit) {
                            int cX = getGridX(Tile.clickX);
                            int cY = getGridY(Tile.clickY);
                            ab3Range = (int)Math.floor(getDistance(cX, ab3X, cY, ab3Y) + 0.5f);
                            if(ab3Range < 1)
                                ab3Range = 1;
                            Tile.drawShape(batch, parentAlpha, ab3X * 48 + 8, ab3Y * 48, "circle", ab3Range, Color.GREEN, false);
                        }
                        abMenu.setVisible(false);
                        break;
                    case "ab4":
                        particleRegion.setRegion(160, 0, 36, 324);
                        Sprite spr = new Sprite(particleRegion);
                        spr.setSize(36, passiveRange > 0 ? 408 : 312);
                        spr.setOrigin(12, 0);
                        spr.setPosition(getX() + 12, getY() + 26);

                        float rot;
                        Tile.setClickPos();
                        if (clickX >= getX() + 24)
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24));
                        else
                            rot = (float) atan((clickY - getY() - 24) / (clickX - getX() - 24)) + 3.14159f;
                        spr.setRotation((float) toDegrees(rot) - 90);
                        spr.draw(batch);
                        abMenu.setVisible(false);
                        Tile.hit = true;
                        break;
                    default:
                        ret = false;
                        break;
                }
            }
            return ret;
        }
    }

    @Override
    public void clickAct() {
        super.clickAct();
        switch(doing) {
            case "ab1":
                ab1();
                break;
            case "ab2":
                ab2();
                break;
            case "ab3_a":
                ab3X = getGridX(Tile.clickX);
                ab3Y = getGridY(Tile.clickY);
                doing = "ab3_b";
                break;
            case "ab3_b":
                ab3On = true;
                doing = "Stand";
                cd[2] = maxCd[2];
                hasMoved = true;
                if(secondary)
                    primary = true;
                else
                    secondary = true;
                break;
            case "ab4":
                ab4();
                break;
        }
    }

    private void ab1() {
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        abMenu.setVisible(false);
        float dist = getDistance(cX, getGridX(), cY, getGridY());
        float dir = getDirection(cX , getGridX(), cY, getGridY())  + 180;
        particleRegion.setRegion(32, 0, 24, 24);
        doing = "ab1Anim";

        getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
                getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, dist) {
            private boolean hitEn = false;

            @Override
            public void effect(Unit un) {
                int dmg = 10 + (level) + (int)(0.7 * mag);
                for(Unit un2 : units) {
                    float dist = getDistance(un2.getGridX(), un.getGridX(), un2.getGridY(), un.getGridY());
                    if(dist <= 2.5f && un2.getTeam() != team && un2.getTargetable())
                        un2.takeMagDmg(dmg, Epsilon.this);
                }
                ab1Targets = new ArrayList<>();
                ab1Targets.add(un);
                ab1Dur = 1;
                un.getStatus().add(AB1_EFFECT);
                hitEn = true;
                this.remove();
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                this.spr.rotate(30);
            }

            @Override
            public boolean remove() {
                if(!hitEn) {
                    ab1On = true;
                    ab1X = cX;
                    ab1Y = cY;
                    particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
                        @Override
                        public void increment() {
                            if(!ab1On)
                                particles.remove(this);
                        }
                    });
                }

                return super.remove();
            }
        });

        cd[0] = maxCd[0];
        hasMoved = true;
        MenuOption mo = (MenuOption) ((Table) game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        if(secondary)
            primary = true;
        else
            secondary = true;
    }

    private void ab2() {
        int cX = getGridX(Tile.clickX);
        int cY = getGridY(Tile.clickY);
        ab2Target = null;
        for(Unit un : units)
            if(un.getGridY() == cY && un.getGridX() == cX && un.getTeam() != team && un.getTargetable())
                ab2Target = un;
        if(ab2Target != null) {
            abMenu.setVisible(false);
            ab2Target.takeMagDmg((int)(3 + (level / 3f) + (0.45 * mag)), this);
            ab2Target.getStatus().add(AB2_EFFECT);
            ab2X = cX;
            ab2Y = cY;

            doing = "Stand";
            cd[1] = maxCd[1];
            hasMoved = true;
            if(secondary)
                primary = true;
            else
                secondary = true;
        }
    }

    private void ab4() {
        doing = "Stand";
        float dir;
        dir = getDirection(getX() + 24, Tile.clickX, getY() + 26, Tile.clickY);
        particleRegion.setRegion(32, 0, 24, 24);
        int range = passiveRange > 0 ? 8 : 6;
        Projectile proj = new Projectile(this, team, dir, getX() + 6,
                getY() + 2, particleRegion, 3, range) {
            private boolean started = false;

            @Override
            public void act(float delta) {
                super.act(delta);
                atkAct(this);
                if(!started) {
                    setOrigin(getWidth() / 2f, getHeight() / 2f);
                    setRotation(dir);
                    started = true;
                }
            }

            @Override
            public void effect(Unit un) {
                int curHp = un.getHp();
                un.takeMagDmg((int)(20 + (1.25 * level) + (0.7 * mag)), Epsilon.this);
            }
        };
        proj.setSize(36, 36);
        getParent().addActor(proj);
        doing = "ab1Anim";
        MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
        mo.setColor(Color.DARK_GRAY);
        cd[0] = maxCd[0];
        if(secondary)
            primary = true;
        else
            secondary = true;
        hasMoved = true;
    }

    @Override
    public void dispose() {
        super.dispose();
        baseMap.dispose();
    }

    @Override
    public void alTurn() {
        super.alTurn();
        if(passiveRange == 0) {
            passiveX = (int)getX();
            passiveY = (int)getY();
            passiveRange = 1;
        } else if(passiveRange < 3)
            passiveRange++;
        if(ab1Dur > 0) {
            ab1Dur--;
            if(ab1Dur == 0) {
                for(Unit un : ab1Targets)
                    un.getStatus().remove(AB1_EFFECT);
                ab1Targets = null;
            }
        }
        if(ab1On) {
            ab1On = false;
            int dmg = 10 + (level) + (int)(0.7 * mag);
            ab1Targets = new ArrayList<>();
            ab1Dur = 2;
            for(Unit un : units) {
                float dist = getDistance(un.getGridX(), ab1X, un.getGridY(), ab1Y);
                if(dist <= 2.5f && un.getTeam() != team && un.getTargetable()) {
                    un.takeMagDmg(dmg, Epsilon.this);
                    un.getStatus().add(StatusEffect.slow(3));
                    ab1Targets.add(un);
                    un.getStatus().add(AB1_EFFECT);
                }
            }
        }
        if(ab2Target != null) {
            ab2Target.getStatus().remove(AB2_EFFECT);
            ab2Target = null;
        }
        if(ab3On) {
            for(Unit un : units) {
                float dist = getDistance(un.getGridX(), ab3X, un.getGridY(), ab3Y);
                float dmg = 25 + (1.25f * level) + (0.75f * mag);
                if(dist <= ab3Range + 0.5f && un.getTeam() != team && un.getTargetable())
                    un.takeMagDmg((int)((1.25 - (0.25 * ab3Range)) * dmg), this);
            }
            ab3On = false;
        }
    }
}
