package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.*;
import static com.mygdx.game.Tile.*;
import static java.lang.Math.floor;

public class Izuna extends Unit {
	private int stoSegment;
	private int dir;
	private boolean[] ab3Hit;
	
	public Izuna(int x, int y, Stage game, boolean team) {
		super(x, y, game, team, true);
		particleRegion.getTexture().dispose();
		maxHp = 225;
		hp = maxHp;
		atk = 20;
		mag = 32;
		name = "Izuna";
		range = 1;
		stoSegment = 1;
		charCol = Color.YELLOW;
		particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Izuna_Particles.png"))
				, 0, 64, 32, 32);
		items.add(new Item((short)1));


		statusEffect.add(new StatusEffect("OnDamage") {
			@Override
			public void effect(Unit target, Unit source) {
				if(this.level < 3) {
					this.level++;
					Izuna.this.mspeed++;
				}
				if(Izuna.this.res == Izuna.this.maxRes)
					target.takePhysDmg(20 + (int)(0.4 * mag));
				this.duration = 2;
			}
			
			@Override
			public void end() {
				Izuna.this.mspeed -= this.level;
				this.level = 0;
			}
		});
		
		abMenu.add(new MenuOption("Conduction", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[0] == 0 && (!secondary || !primary))
					Izuna.this.setDoing("ab1", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Static Grenade", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[1] == 0 && (!secondary || !primary))
					Izuna.this.setDoing("ab2", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Lightspeed Strike", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[2] == 0 && (!secondary || !primary))
					Izuna.this.setDoing("ab3", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Thunderstorm", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[3] == 0 && (!secondary || !primary))
					Izuna.this.ab4();
				abMenu.getChildren().get(3).setColor(Color.DARK_GRAY);
			}
		});
		
		maxRes = 150;
		res = 100;
		resCol = Color.ORANGE;
		maxCd[0] = 2;
		maxCd[1] = 3;
		maxCd[2] = 3;
		maxCd[3] = 16;
	}
	
	@Override
	public void openAbMenu() {
		super.openAbMenu();
		if((primary && secondary) || cd[3] > 0)
			abMenu.getChildren().get(4).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[0] > 0)
			abMenu.getChildren().get(1).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[1] > 0)
			abMenu.getChildren().get(2).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[2] > 0)
			abMenu.getChildren().get(3).setColor(Color.DARK_GRAY);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if(stoSegment != curSegment && res < maxRes && num == sel) {
			res+=6;
			stoSegment = curSegment;
		}
		if(doing.equals("ab3Anim"))
			ab3();
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
						Tile.setClickPos();
						drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, BLUE, true);
						if (Tile.hit) {
							int tX = (int) floor((clickX - 8) / 48);
							int tY = (int) floor(clickY / 48);
							int uX = (int) floor((getX() - 8) / 48);
							int uY = (int) floor(getY() / 48);

							if (tX > uX) {
								drawRect(batch, parentAlpha, (int) getX() + 96, (int) getY() - 52, 2, 3, GREEN);
								disp(batch, getX() + 48, getY() - 4, GREEN);
							} else if (tY > uY) {
								drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() + 92, 3, 2, GREEN);
								disp(batch, getX(), getY() + 44, GREEN);
							} else if (tX < uX) {
								drawRect(batch, parentAlpha, (int) getX() - 144, (int) getY() - 52, 2, 3, GREEN);
								disp(batch, getX() - 48, getY() - 4, GREEN);
							} else {
								drawRect(batch, parentAlpha, (int) getX() - 48, (int) getY() - 148, 3, 2, GREEN);
								disp(batch, getX(), getY() - 52, GREEN);
							}
						}

						ret = true;
						abMenu.setVisible(false);
						break;
					case "ab2":
						Tile.setClickPos();
						drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, BLUE, true);
						if (Tile.hit) {
							int tX = (int) floor((clickX - 8) / 48);
							int tY = (int) floor(clickY / 48);
							drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 2, GREEN, false);
						}
						ret = true;
						abMenu.setVisible(false);
						break;
					case "ab3":
						Tile.setClickPos();
						drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, BLUE, true);
						if (Tile.hit) {
							int tX = (int) floor((clickX - 8) / 48);
							int tY = (int) floor(clickY / 48);
							int uX = (int) floor((getX() - 8) / 48);
							int uY = (int) floor(getY() / 48);

							Color col = GREEN;
							if (walls.getPixel(tX, tY) != rgba8888(BLACK))
								col = RED;

							if (tX > uX) {
								drawRect(batch, parentAlpha, (int) getX(), (int) getY() - 4, tX - uX + 2, 1, col);
							} else if (tY > uY) {
								drawRect(batch, parentAlpha, (int) getX(), (int) getY() - 4, 1, tY - uY + 2, col);
							} else if (tX < uX) {
								drawRect(batch, parentAlpha, (int) getX() - (uX - tX + 1) * 48, (int) getY() - 4, uX - tX + 2, 1, col);
							} else {
								drawRect(batch, parentAlpha, (int) getX(), (int) getY() - 4 - (uY - tY + 1) * 48, 1, uY - tY + 2, col);
							}
						}

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
		int cX = (int)Math.floor((Tile.clickX - 8) / 48);
		int cY = (int)Math.floor(Tile.clickY / 48);
		int stX = (int)Math.floor((getX() - 8) / 48);
		int stY = (int)Math.floor(getY() / 48);
		super.clickAct();
		if(doing.equals("ab1")) {
			float x, y;
			abMenu.setVisible(false);
			if(cX > stX) {
				x = getX() - 24;
				y = getY() + 24;
				dir = 0;
			}
			else if(cY > stY) {
				x = getX() - 48;
				y = getY() + 48;
				dir = 90;
			}
			else if(cX < stX) {
				x = getX() - 72;
				y = getY() + 24;
				dir = 180;
			}
			else {
				x = getX() - 48;
				y = getY();
				dir = 270;
			}
			particleRegion.setRegion(32, 0, 144, 128);
			cd[0] = maxCd[0];
			ab1();
			doing = "ab1Anim";
			if(secondary)
				primary = true;
			else
				secondary = true;
			hasMoved = true;
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
			
			particles.add(new Particle(particleRegion, (int)x, (int)y) {
				private int thisDir;
				
				@Override
				public void start() {
					spr.setOrigin(72, 0);
					spr.setRotation(Izuna.this.dir - 90);
					thisDir = Izuna.this.dir;
				}
				
				@Override
				public void increment() {
					if(thisDir == 0)
						spr.translateX(3f);
					else if(thisDir == 90)
						spr.translateY(3f);
					else if(thisDir == 180)
						spr.translateX(-3f);
					else
						spr.translateY(-3f);
					spr.setScale(0.1f * duration);
					if(duration >= 8)
						spr.setAlpha(1 - (float)Math.pow(0.4 * (duration - 7), 2));
					if(duration >= 10) {
						this.remove();
						Izuna.this.particles.remove(this);
						Izuna.this.doing = "Stand";
						if(res == 150)
							res = 0;
					}
					duration++;
				}
			});
		} else if (doing.equals("ab2")) {
			abMenu.setVisible(false);
			dir = 0;
			particleRegion.setRegion(0, 128, 38, 38);
			cd[1] = maxCd[1];
			doing = "ab1Anim";
			if(secondary)
				primary = true;
			else
				secondary = true;
			hasMoved = true;
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
			
			particles.add(new Particle(particleRegion, (int)getX(), (int)getY()) {
				private int destX, destY, curY;
				private float trajectoryX, trajectoryY;
				
				@Override
				public void start() {
					spr.setOrigin(72, 0);
					spr.setScale(0.75f);
					
					destX = (int)Math.floor((Tile.clickX - 8) / 48) * 48 + 8;
					destY = (int)Math.floor(Tile.clickY / 48) * 48 + 4;
					trajectoryX = (getX() - destX) / 15;
					trajectoryY = (getY() - destY) / 15;
					curY = (int)getY();
				}
				
				@Override
				public void increment() {
					curY -= trajectoryY;
					spr.translateX(-trajectoryX);
					spr.setY(curY + 32 - (int)Math.pow(duration - 8, 2) / 2);
					if(duration == 15) {
						spr.setAlpha(0);
						ab2();
					}
					if(duration >= 20) {
						this.remove();
						particles.remove(this);
						doing = "Stand";
					}
					duration++;
				}
			});
		} else if(doing.equals("ab3") && Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE)) {
			if(cX > stX)
				dir = 0;
			else if(cY > stY)
				dir = 90;
			else if(cX < stX)
				dir = 180;
			else
				dir = 270;
			ab3Hit = new boolean[units.size()];
			for(int i = 0; i < units.size(); i++)
				ab3Hit[i] = false;
			addAction(Actions.sequence(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, 0.075f * (float)Math.sqrt(Math.pow(cX - stX, 2)
			 + Math.pow(cY - stY, 2))), Actions.run(() -> {
                 doing = "Stand";
                 if(res == 150)
                     res = 0;
             })));
			doing = "ab3Anim";
			cd[2] = maxCd[2];
			if(secondary)
				primary = true;
			else
				secondary = true;
		}
	}
	
	private void ab1() {
		int cX, cY, enX, enY;
		cX = getGridX();
		cY = getGridY();
		for (Unit un : units) {
			enX = un.getGridX();
			enY = un.getGridY();
			if (un.getTeam() != team && un.getTargetable())
				switch (dir) {
					case 0:
						if (cX + 1 == enX && cY == enY) {
							un.takePhysDmg(12 + (3 * level) + (int) (0.9 * mag), this);
							cd[0] = 1;
						} else if (enX > cX + 1 && enX <= cX + 3 && enY >= cY - 1 && enY <= cY + 1)
							un.takePhysDmg(8 + (2 * level) + (int) (0.6 * mag), this);
						break;
					case 90:
						if (cX == enX && cY + 1 == enY) {
							un.takePhysDmg(12 + (3 * level) + (int) (0.9 * mag), this);
							cd[0] = 1;
						} else if (enX >= cX - 1 && enX <= cX + 1 && enY > cY + 1 && enY <= cY + 3)
							un.takePhysDmg(8 + (2 * level) + (int) (0.6 * mag), this);
						break;
					case 180:
						if (cX - 1 == enX && cY == enY) {
							un.takePhysDmg(12 + (3 * level) + (int) (0.9 * mag), this);
							cd[0] = 1;
						} else if (enX < cX - 1 && enX >= cX - 3 && enY >= cY - 1 && enY <= cY + 1)
							un.takePhysDmg(8 + (2 * level) + (int) (0.6 * mag), this);
						break;
					case 270:
						if (cX == enX && cY - 1 == enY) {
							un.takePhysDmg(12 + (3 * level) + (int) (0.9 * mag), this);
							cd[0] = 1;
						} else if (enX >= cX - 1 && enX <= cX + 1 && enY < cY - 1 && enY >= cY - 3)
							un.takePhysDmg(8 + (2 * level) + (int) (0.6 * mag), this);
						break;
				}
		}
	}
	
	private void ab2() {
		int tX = (int)Math.floor((Tile.clickX - 8) / 48);
		int tY = (int)Math.floor(Tile.clickY / 48);
		int stoRes = res;
		int useRes = res;
		for (Unit un : units) {
			int unStX = (int) Math.floor((un.getX() - 8) / 48);
			int unStY = (int) Math.floor(un.getY() / 48);
			if (Math.sqrt(Math.pow(tX - unStX, 2) + Math.pow(tY - unStY, 2)) <= 2.5f && un.getTeam() != team && un.getTargetable()) {
				res = useRes;
				un.takePhysDmg(15 + (int) (0.7 * mag), this);
				un.getStatus().add(StatusEffect.slow(1));
				stoRes += 10;
			}
		}

		if(useRes == 150)
			res = 0;
		else
			res = stoRes;
	}
	
	private void ab3() {
		for(int i = 0; i < units.size(); i++) {
			Unit un = units.get(i);
			if(un.getTeam() != team && un.getTargetable() && !ab3Hit[i]) {
				boolean hit;
				switch(dir) {
				case 0:
					hit = (un.getX() > getX() && un.getX() < getX() + 96 && un.getY() == getY());
					break;
				case 90:
					hit = (un.getY() > getY() && un.getY() < getY() + 96 && un.getX() == getX());
					break;
				case 180:
					hit = (un.getX() < getX() && un.getX() > getX() - 48 && un.getY() == getY());
					break;
				default:
					hit = (un.getY() < getY() && un.getY() > getY() - 48 && un.getX() == getX());
					break;
				}
				if(hit) {
					un.takePhysDmg(10 + (int)(0.6 * mag) + (2 * level), this);
					ab3Hit[i] = true;
				}
			}
		}
	}
	
	private void ab4() {
		for (Unit un : units) {
			int stX = (int) Math.floor((getX() - 8) / 48);
			int stY = (int) Math.floor(getY() / 48);
			int unStX = (int) Math.floor((un.getX() - 8) / 48);
			int unStY = (int) Math.floor(un.getY() / 48);
			if (Math.sqrt(Math.pow(stX - unStX, 2) + Math.pow(stY - unStY, 2)) <= 4.5f && un.getTeam() != team && un.getTargetable()) {
				un.takePhysDmg(25 + (int) (0.9 * mag), this);
			}
		}
		cd[3] = maxCd[3];
		if(res == 150)
			res = 0;
	}
}
