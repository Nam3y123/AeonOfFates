package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.*;

import java.util.ArrayList;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;
import static com.badlogic.gdx.graphics.Color.BLUE;
import static com.badlogic.gdx.graphics.Color.GREEN;
import static com.mygdx.game.Tile.*;
import static java.lang.Math.*;

public class Syrilith extends Unit {
	private int passiveStacks;
	private int normAtk;
	private StatusEffect hasBurn;
	private boolean secondAb2;
	private int ab2X, ab2Y;
	private float rot;
	private float spin;

	public Syrilith(int x, int y, Stage game, boolean team) {
		super(x, y, game, team, true);
		maxHp = 205;
		atk = 30;
		normAtk = atk;
		mag = 15;
		hp = maxHp;
		name = "Syrilith";
		range = 3;
		passiveStacks = 0;
		secondAb2 = false;
		particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/Rachel_Particles.png")),0, 64, 32, 32);
		spin = 0;
		charCol = Color.RED;

		resCol = Color.RED;
		res = 0;
		maxRes = 3;

		statPerLevel[0] = 3;
		statPerLevel[1] = 1;
		statPerLevel[2] = 2;
		statPerLevel[3] = 25;
		
		abMenu.add(new MenuOption("Cluster Shot", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[0] == 0 && (!secondary || !primary))
					Syrilith.this.setDoing("ab1", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Flame Tornado", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[1] == 0 && (!secondary || !primary))
					Syrilith.this.setDoing("ab2", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Incinerate", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[2] == 0 && (!secondary || !primary))
					Syrilith.this.setDoing("ab3", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Flame-X", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[3] == 0 && (!secondary || !primary))
					Syrilith.this.setDoing("ab4", false);
			}
		});

		hasBurn = new StatusEffect("Dummy");
		statusEffect.add(new StatusEffect("OnHit") {
			@Override
			public void effect(Unit target, Unit source) {
				if(passiveStacks >= 3){
					target.getStatus().add(burn(target));
					target.takePhysDmg(7 + (int)(0.15 * Syrilith.this.atk));
				}
			}
		});
		statusEffect.add(new StatusEffect("OnDamage") {
			@Override
			public void effect(Unit target, Unit source) {
				if(passiveStacks < 3)
					passiveStacks++;
				atk = (int)(normAtk * (1 + 0.1 * passiveStacks));
				if(target.getStatus().contains(hasBurn) && cd[0] > 0)
					cd[0]--;
				duration = 3;
				res = passiveStacks;
			}

			@Override
			public void end() {
				passiveStacks = 0;
			}
		});
		
		maxCd[0] = 4;
		maxCd[1] = 3;
		maxCd[2] = 4;
		maxCd[3] = 12;
	}

	@Override
	public void openAbMenu() {
		super.openAbMenu();
		if((primary && secondary) || cd[0] > 0)
			abMenu.getChildren().get(0).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[1] > 0)
			abMenu.getChildren().get(1).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[2] > 0)
			abMenu.getChildren().get(2).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[3] > 0)
			abMenu.getChildren().get(3).setColor(Color.DARK_GRAY);
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
						Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, Color.BLUE, true);
						ret = true;
						abMenu.setVisible(false);
						break;
					case "ab2":
						if(!secondAb2) {
							Tile.setClickPos();
							drawShape(batch, parentAlpha, getX(), getY() - 4, "circle", 3, BLUE, true);
							if (Tile.hit) {
								int tX = (int) floor((clickX - 8) / 48);
								int tY = (int) floor(clickY / 48);
								drawShape(batch, parentAlpha, tX * 48 + 8, tY * 48, "circle", 1, GREEN, false);

								particleRegion.setRegion(155, 0, 120, 120);
								Sprite spr = new Sprite(particleRegion);
								spr.setScale(2.5f);
								spr.setPosition(tX * 48 - 28, tY * 48 - 36);
								spr.draw(batch);
							}
						} else {
							particleRegion.setRegion(35, 0, 120, 120);
							Sprite spr = new Sprite(particleRegion);
							spr.setScale(2.5f);
							spr.setPosition(ab2X - 36, ab2Y - 36);
							spr.draw(batch);

							particleRegion.setRegion(0, 128, 120, 48);
							spr = new Sprite(particleRegion);
							spr.setOrigin(0, 24);
							spr.setScale(1.25f);
							spr.setPosition(ab2X + 24, ab2Y);

							Tile.setClickPos();
							if (clickX >= ab2X + 24)
								rot = (float) atan((clickY - ab2Y - 24) / (float) (clickX - ab2X - 24));
							else
								rot = (float) atan((clickY - ab2Y - 24) / (float) (clickX - ab2X - 24)) + 3.14159f;
							spr.setRotation((float) toDegrees(rot));
							spr.draw(batch);
							Tile.hit = true;
						}
						ret = true;
						abMenu.setVisible(false);
						break;
					case "ab3":
						Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 3, Color.BLUE, true);
						ret = true;
						abMenu.setVisible(false);
						break;
					case "ab4":
						Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, Color.BLUE, true);
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
			float dir;
			int parY;
			abMenu.setVisible(false);
			if(cX > stX) {
				dir = 0;
				parY = 0;
			}
			else if(cY > stY) {
				dir = 90;
				parY = 32;
			}
			else if(cX < stX) {
				dir = 180;
				parY = 64;
			}
			else {
				dir = 270;
				parY = 96;
			}
			particleRegion.setRegion(0, parY, 32, 32);

			getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
					getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, 5) {
				@Override
				public void effect(Unit un) {
					ab1(this.getX(), this.getY(), this.getDirection());
					this.remove();
					doing = "Stand";
				}

				@Override
				public boolean remove() {
					cd[0] = maxCd[0];
					return super.remove();
				}
			});

			doing = "Ab1Anim";
			if(secondary)
				primary = true;
			else
				secondary = true;
			hasMoved = true;
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
		} else if (doing.equals("ab2")) {
			if(secondAb2) {
				particleRegion.setRegion(0, 176, 0, 0);
				doing = "ab2Anim";
				cd[1] = maxCd[1];
				if(secondary)
					primary = true;
				else
					secondary = true;
				secondAb2 = false;
				getParent().addActor(new Projectile(this, (int)Math.toDegrees(rot), ab2X, ab2Y, particleRegion, 6, 2.5f, 48) {
					private ArrayList<Unit> hitUnits;
					private Tornado tornado;

					@Override
					public void act(float delta) {
						float move = 48 / speed;
						this.moveBy((float)Math.cos(Math.toRadians(direction))*move, (float)Math.sin(Math.toRadians(direction))*move);

						if(hitUnits == null)
							hitUnits = new ArrayList<Unit>();
						if(tornado == null)
							for(int i = 0; i < getParent().getChildren().size; i++)
								if(getParent().getChildren().get(i).getClass() == Tornado.class)
									tornado = (Tornado)getParent().getChildren().get(i);
						tornado.setPosition(getX(), getY());

						spr.setPosition(getX(), getY());
						for(int i = 0; i < units.size(); i++) {
							Unit un = units.get(i);
							float distance = (float)Math.sqrt(Math.pow(un.getX() - getX(), 2) + Math.pow(un.getY() - getY(), 2));
							if(distance < 48 && !hitUnits.contains(un))
								effect(un);
						}
						duration--;
						if(duration == 0)
							remove();
					}

					@Override
					public void effect(Unit un) {
						if(un.getTeam() != sourceUn.getTeam()) {
							un.takePhysDmg(20 + (int)(0.7 * mag), sourceUn);
						} else if (un.equals(sourceUn)) {
							mspeed++;
							statusEffect.add(new StatusEffect("OnEnTurn") {
								@Override
								public void effect(Unit target, Unit source) {
									target.setMspeed(target.getMspeed() - 1);
									target.getStatus().remove(this);
								}
							});
						}
						duration += 6;
						hitUnits.add(un);
					}

					@Override
					public boolean remove() {
						tornado.kill(direction);
						return super.remove();
					}
				});
			} else {
				abMenu.setVisible(false);
				particleRegion.setRegion(0, 176, 50, 26);
				cd[1] = 1;
				doing = "Stand";
				secondAb2 = true;
				if(secondary)
					primary = true;
				else
					secondary = true;
				hasMoved = true;
				ab2X = cX * 48 + 8;
				ab2Y = cY * 48;
				getParent().addActor(new Tornado(ab2X, ab2Y, particleRegion));
				MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
				mo.setColor(Color.DARK_GRAY);
				ab2();
			}
		} else if(doing.equals("ab3")) {
			float dir;
			int parY;
			abMenu.setVisible(false);
			if(cX > stX) {
				dir = 0;
				parY = 64;
			}
			else if(cY > stY) {
				dir = 90;
				parY = 96;
			}
			else if(cX < stX) {
				dir = 180;
				parY = 0;
			}
			else {
				dir = 270;
				parY = 32;
			}
			particleRegion.setRegion(0, parY, 32, 32);

			getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
					getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, 3) {
				@Override
				public void effect(Unit un) {
					ab3(un, (int)direction);
					this.remove();
					doing = "Stand";
				}
			});

			doing = "ab3Anim";
			if(secondary)
				primary = true;
			else
				secondary = true;
			hasMoved = true;
			cd[2] = maxCd[2];
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
			mo.setColor(Color.DARK_GRAY);
		}else if(doing.equals("ab4")) {
			float dir;
			int parY;
			abMenu.setVisible(false);
			if(cX > stX) {
				dir = 0;
				parY = 64;
			}
			else if(cY > stY) {
				dir = 90;
				parY = 96;
			}
			else if(cX < stX) {
				dir = 180;
				parY = 0;
			}
			else {
				dir = 270;
				parY = 32;
			}
			particleRegion.setRegion(0, parY, 32, 32);

			getParent().addActor(new Projectile(this, team, dir, getX() + (48 - particleRegion.getRegionWidth()) / 2,
					getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 3, 5) {
				boolean[] hitUnit = new boolean[units.size()];

				@Override
				public void effect(Unit un) {
					if(!hitUnit[units.indexOf(un)]) {
						un.takePhysDmg(25 + (int)(0.8 * mag), Syrilith.this);
						un.getStatus().add(StatusEffect.snare);
						hitUnit[units.indexOf(un)] = true;
					}
				}

				@Override
				public boolean remove() {
					doing = "Stand";
					return super.remove();
				}
			});

			doing = "ab4Anim";
			if(secondary)
				primary = true;
			else
				secondary = true;
			hasMoved = true;
			cd[3] = maxCd[3];
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
		}
	}

	private StatusEffect burn(Unit target) {
		return new StatusEffect("OnEnTurn") {
			private Unit en;

			@Override
			public void start() {
				duration = 3;
				en = target;
				en.getStatus().add(hasBurn);
			}

			@Override
			public void effect(Unit target, Unit source) {
				target.takePhysDmg(7 + (int)(0.15 * Syrilith.this.atk));
			}

			@Override
			public void end() {
				en.getStatus().remove(hasBurn);
				en.getStatus().remove(this);
			}
		};
	}

	private void ab1(float pX, float pY, float dir) {
		for(int i = 0; i < units.size(); i++) {
			Unit un = units.get(i);
			if (un.getTeam() != team && un.getTargetable()) {
				float range = (float) Math.sqrt(Math.pow(pX - un.getX(), 2) + Math.pow(pY - un.getY(), 2));
				boolean behind = false;
				switch ((int) dir) {
					case 0:
						if (un.getX() >= pX)
							behind = true;
						break;
					case 90:
						if (un.getY() >= pY)
							behind = true;
						break;
					case 180:
						if (un.getX() <= pX)
							behind = true;
						break;
					default:
						if (un.getY() <= pY)
							behind = true;
						break;
				}
				if (behind && range <= 120) {
					un.takePhysDmg(15 + (int) (0.85 * atk), this);
					un.getStatus().add(burn(un));
				}
			}
		}
		cd[0] = maxCd[0];
	}

	private void ab2() {
		int tX = (int)Math.floor((Tile.clickX - 8) / 48);
		int tY = (int)Math.floor(Tile.clickY / 48);
		for(int i = 0; i < units.size(); i++){
			Unit un = units.get(i);
			int unStX = (int)Math.floor((un.getX() - 8) / 48);
			int unStY = (int)Math.floor(un.getY() / 48);
			if(Math.sqrt(Math.pow(tX - unStX, 2) + Math.pow(tY - unStY, 2)) <= 1.5f && un.getTeam() != team && un.getTargetable()) {
				un.takePhysDmg(15 + (int)(0.7 * atk), this);
			}
		}
	}

	private void ab3(Unit un, int dir) {
		un.takePhysDmg(20 + (int)(0.6 * mag) + (int)(0.08f * (un.getMaxHp() - un.getHp())), this);
		boolean knockback = false;
		int kbX = (int)un.getX(), kbY = (int)un.getY();
		if(dir == 0)
			kbX += 144;
		else if(dir == 90)
			kbY += 144;
		else if(dir == 180)
			kbX -= 144;
		else
			kbY -= 144;
		while(!knockback) {
			knockback = true;
			int cX = (int)(Math.floor((kbX - 8) / 48));
			int cY = (int)(Math.floor(kbY / 48));

			if(cX > 63 || cY > 47 || cX < 0 || cY < 0)
				knockback = false;
			else if(Tile.walls.getPixel(cX, cY) == Color.rgba8888(Color.WHITE))
				knockback = false;
			else
				for(int i = 0; i < units.size(); i++)
					if(units.get(i).getX() == kbX && units.get(i).getY() == kbY)
						knockback = false;

			if(un.getX() == kbX && un.getY() == kbY)
				knockback = true;
			if(!knockback) {
				if(dir == 0)
					kbX -= 48;
				else if(dir == 90)
					kbY -= 48;
				else if(dir == 180)
					kbX += 48;
				else
					kbY += 48;
			}

		}

		un.addAction(Actions.moveTo(kbX, kbY, 0.075f * (float)Math.sqrt(Math.pow(kbX - un.getX(), 2) + Math.pow(kbY - un.getY(), 2)) / 48f));
	}

	@Override
	public void dispose() {
		super.dispose();
		int i = 0;
	}


	private class Tornado extends Actor {
		Sprite spr;
		private int killDir, killTime;

		public Tornado(int x, int y, TextureRegion tornadoRegion) {
			spr = new Sprite(new TextureRegion(tornadoRegion));
			spr.setPosition(x, y);
			setBounds(x, y, spr.getWidth(), spr.getHeight());
			killTime = -1;
		}

		public void kill(float direction) {
			killDir = (int)direction;
			killTime = 6;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			spr.setPosition(getX(), getY());
			Sprite drawSpr = new Sprite(spr);
			drawSpr.setPosition(spr.getX() - 2, spr.getY() - 8);
			if(killTime == 0)
				this.dispose();
			else {
				if(killTime > 0)
					moveBy((float)Math.cos(Math.toRadians(killDir))*(8 / (float)Math.pow(2, 6 - killTime)), (float)Math.sin(Math.toRadians(killDir))*(8 / (float)Math.pow(2, 6 - killTime)));

				for(int i = 0; i < 6; i++) {
					float scaleAmt = 0.1f * (float)Math.pow(1.5, i + 1);
					drawSpr.translateY(3 + i);
					drawSpr.setScale(scaleAmt);
					if(killTime > 0) {
						drawSpr.setScale(scaleAmt * (0.166667f * killTime), scaleAmt);
						drawSpr.setAlpha(0.166667f * killTime);
					}
					drawSpr.translate((float)Math.cos((spin + 2 * i) * Math.PI / (12.0)) * i,
							(float)Math.sin((spin + 2 * i) * Math.PI / (12.0)) * i);
					drawSpr.draw(batch);
					drawSpr.translate(-(float)Math.cos((spin + 2 * i) * Math.PI / (12.0)) * i,
							-(float)Math.sin((spin + 2 * i) * Math.PI / (12.0)) * i);
				}
				spin++;
				if(killTime > 0)
					killTime--;
				if(spin == 97)
					spin = 0;
			}
		}

		public void dispose() {
			this.remove();
		}
	}
}
