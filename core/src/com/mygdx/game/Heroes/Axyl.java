package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.MenuOption;
import com.mygdx.game.Particle;
import com.mygdx.game.StatusEffect;
import com.mygdx.game.Unit;

public class Axyl  extends Unit {
    private int[] ab1X, ab1Y, ab1Dur;

    public Axyl(int x, int y, Stage game, boolean team) {
        super(x, y, game, team, true);
        maxHp = 215;
        atk = 15;
        def = 15;
        mag = 15;
        hp = maxHp;
        name = "Axyl";
        range = 1;
        particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Axyl_Particles.png")), 0, 0, 32, 32);
        charCol = new Color(0.375f, 1f, 1f, 1f);
        particleRegion.setRegion(0, 128, 146, 146);


        ab1X = new int[2];
        ab1Y = new int[2];
        ab1Dur = new int[2];

        abMenu.add(new MenuOption("Deflection Zone", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[0] == 0 && (!primary || !secondary))
                    ab1();
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Pulsar Blast", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[1] == 0 && (!primary || !secondary))
                    setDoing("ab2", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Redirection", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[2] == 0 && (!primary || !secondary))
                    setDoing("ab3", false);
            }
        });
        abMenu.row();
        abMenu.add(new MenuOption("Role Reversal", combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cd[3] == 0 && (!primary || !secondary))
                    setDoing("ab4", false);
            }
        });

        maxCd[0] = 2;
        maxCd[1] = 1;
        maxCd[2] = 4;
        maxCd[3] = 14;
    }

    private void ab1() {
        statusEffect.add(new StatusEffect("OnAttack") {
            @Override
            public void effect(Unit target, Unit source) {
                int dmg = (int)(7 + (0.3 * atk) + (0.5 * mag) + level);
                int gX = target.getGridX();
                int gY = target.getGridY();
                for(Unit un : units) {
                    if(un.getTargetable() && un.getTeam() != team && getDistance(un.getGridX(), gX, un.getGridY(), gY) <= 2.5) {
                        un.takeMagDmg(dmg);
                    }
                }

                final int CUR_FIELD = ab1Dur[0] <= 0 ? 0 : 1;
                ab1X[CUR_FIELD] = gX;
                ab1Y[CUR_FIELD] = gY;
                ab1Dur[CUR_FIELD] = 3;
                particleRegion.setRegion(32, 0, 240, 240);
                particles.add(new Particle(particleRegion, (int)target.getX(), (int)target.getY()) {
                    @Override
                    public void start() {
                        duration = 0;
                    }

                    @Override
                    public void increment() {
                        super.increment();
                        if(duration <= 5) {
                            spr.setSize(duration * 48f, duration * 48f);
                            spr.setPosition(ab1X[CUR_FIELD] * 48 + 8 - (19.2f * duration), ab1Y[CUR_FIELD] * 48 -
                                    (19.2f * duration));
                        }
                        if(ab1Dur[CUR_FIELD] <= 0) {
                            particles.remove(this);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void alTurn() {
        super.alTurn();
        for(int i = 0; i < 2; i++)
            ab1Dur[i]--;
    }
}
