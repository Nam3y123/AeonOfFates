package com.mygdx.game.Heroes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.MenuOption;
import com.mygdx.game.Projectile;
import com.mygdx.game.StatusEffect;
import com.mygdx.game.Tile;
import com.mygdx.game.Unit;

import java.util.ArrayList;

public class Kaiv extends Unit {
	Projectile blade;
	StatusEffect passive;
	
	public Kaiv(int x, int y, Stage game, boolean team) {
		super(x, y, game, team, true);
		maxHp = 200;
		atk = 35;
		mag = 0;
		hp = maxHp;
		name = "Kaiv";
		range = 1;
		charCol = Color.CHARTREUSE;
		passive = new StatusEffect("Dummy");

		
		abMenu.add(new MenuOption("Endless Pursuit", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[0] == 0 && (!secondary || !primary))
					Kaiv.this.setDoing("ab1", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Achilles' Steel", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[1] == 0 && !primary)
					Kaiv.this.setDoing("ab2", false);
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("Focus Fire", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[2] == 0)
					Kaiv.this.statusEffect.add(new StatusEffect("OnHit") {
						public void start() {
							duration = 3;
							cd[2] = -1;
							abMenu.setVisible(false);
							showMenu();
							abMenu.getChildren().get(2).setColor(Color.DARK_GRAY);
							//icon = new Texture(Gdx.files.internal("Icons/NoIcon.png"));
						}
						
						public void effect(Unit target, Unit source) {
							target.takeTrueDmg((int)(Kaiv.this.atk * 0.2 * (4 - duration)));
							statusEffect.remove(this);
							cd[2] = maxCd[2];
						}
						
						public void end() {
							statusEffect.remove(this);
							cd[2] = maxCd[2] + 1;
						}
					});
			}
		});
		abMenu.row();
		abMenu.add(new MenuOption("No Escape", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(cd[3] == 0 && (!secondary || !primary)) {
					cd[3] = maxCd[3];
					Kaiv.this.statusEffect.add(new StatusEffect("OnHit") {
						public void start() {
							duration = 3;
							//cd[3] = maxCd[3];
							abMenu.setVisible(false);
							showMenu();
							abMenu.getChildren().get(3).setColor(Color.DARK_GRAY);
							//icon = new Texture(Gdx.files.internal("Icons/NoIcon.png"));
						}

						public void effect(Unit target, Unit source) {
							ArrayList<StatusEffect> se = Kaiv.this.statusEffect;
							target.takePhysDmg(atk, Kaiv.this);
							for(int i = 0; i < se.size(); i++)
								if((se.get(i).getType() == "OnHit" || se.get(i).getType() == "OnAttack") && se.get(i) != this)
									se.get(i).effect(target, Kaiv.this);

							boolean pull = true;
							int kbX = (int)target.getX(), kbY = (int)target.getY();
							if(kbX > Kaiv.this.getX())
								kbX -= 48;
							else if(kbY > Kaiv.this.getY())
								kbY -= 48;
							else if(kbX < Kaiv.this.getX())
								kbX += 48;
							else
								kbY += 48;
							int cX = (int)(Math.floor((kbX - 8) / 48));
							int cY = (int)(Math.floor(kbY / 48));

							if(cX > 13 || cY > 10 || cX < 0 || cY < 0)
								pull = false;
							else if(Tile.walls.getPixel(cX, cY) == Color.rgba8888(Color.WHITE))
								pull = false;
							else
								for(int i = 0; i < units.size(); i++)
									if(units.get(i).getX() == kbX && units.get(i).getY() == kbY)
										pull = false;

							if(pull)
								target.addAction(Actions.moveTo(kbX, kbY, 0.075f * (float)Math.sqrt(Math.pow(kbX - target.getX(), 2) + Math.pow(kbY - target.getY(), 2)) / 48f));
							
							range = 1;
							statusEffect.remove(this);
						}

						public void end() {
							statusEffect.remove(this);
							range = 1;
						}
					});
					mspeed = 8;
					range = 2;
					statusEffect.add(new StatusEffect("OnEnTurn") {
						@Override
						public void effect(Unit target, Unit source) {
							target.setMspeed(target.getMspeed() - 2);
							target.getStatus().remove(this);
						}
					});
				}
			}
		});
		
		statusEffect.add(new StatusEffect("OnHit") {
			@Override
			public void effect(Unit target, Unit source) {
				if(target.getStatus().contains(passive)) {
					target.takePhysDmg(15 + (int)(0.25f * atk));
					target.getStatus().remove(passive);
				} else
					target.getStatus().add(passive);
			}
		});
		
		maxCd[0] = 3;
		maxCd[1] = 3;
		maxCd[2] = 2;
		maxCd[3] = 12;
	}
	
	@Override
	public void openAbMenu() {
		super.openAbMenu();
		if((primary && secondary) || cd[0] > 0)
			abMenu.getChildren().get(1).setColor(Color.DARK_GRAY);
		if(primary || cd[1] > 0)
			abMenu.getChildren().get(2).setColor(Color.DARK_GRAY);
		if(cd[2] > 0)
			abMenu.getChildren().get(3).setColor(Color.DARK_GRAY);
		if((primary && secondary) || cd[3] > 0)
			abMenu.getChildren().get(4).setColor(Color.DARK_GRAY);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if(!moving && doing.equals("ab1Dash"))
			doing = "Stand";
		
		if(blade != null) {
			int cX = (int)Math.floor((blade.getX() - 8) / 48);
			int cY = (int)Math.floor(blade.getY() / 48);
			if(Tile.walls.getPixel(cX, cY) == Color.rgba8888(Color.WHITE) || cY < 0) {
				ab1();
				setDoing("ab1Dash", false);
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
					Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 5, Color.BLUE, true);
					ret = true;
					abMenu.setVisible(false);
					break;
				case "ab2":
					Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", 1, Color.BLUE, true);
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
			TextureRegion particle = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/TestChar_Particles.png")),0, 64, 32, 32);
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
			particle.setRegion(particle.getRegionX(), parY, particle.getRegionWidth(), particle.getRegionHeight());
			cd[0] = maxCd[0];
			blade = new Projectile(this, team, dir, getX() + (48 - particle.getRegionWidth()) / 2,
			 getY() + (48 - particle.getRegionHeight()) / 2, particle, 4, 5){
				@Override
				public void effect(Unit un) {
					Kaiv.this.ab1(un);
					this.remove();
					Kaiv.this.setDoing("ab1Dash", false);
				}
			};
			getParent().addActor(blade);
			doing = "Ab1Anim";
			if(secondary)
				primary = true;
			else
				secondary = true;
			hasMoved = true;
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
		} else if(doing.equals("ab2")) {
			int dirX = 0;
			int dirY = 0;
			if(cX > stX)
				dirX = 48;
			else if(cY > stY)
				dirY = 48;
			else if(cX < stX)
				dirX = -48;
			else
				dirY = -48;
			cd[1] = maxCd[1];
			doing = "Stand";
			primary = true;
			abMenu.setVisible(false);
			for(int i = 0; i < units.size(); i++) {
				Unit un = units.get(i);
				if(un.getX() == getX() + dirX && un.getY() == getY() + dirY) {
					ab2(un, dirX, dirY);
				}
			}
			hasMoved = true;
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
		}
	}
	
	public void ab1(Unit un) {
		int ofsX = 0;
		int ofsY = 0;
		if(un.getX() > getX())
			ofsX = -48;
		else if(un.getY() > getY())
			ofsY = -48;
		else if(un.getX() < getX())
			ofsX = 48;
		else
			ofsY = 48;
		
		int cX = (int)Math.floor((un.getX() - 8 + ofsX) / 48);
		int cY = (int)Math.floor((un.getY() + ofsY) / 48);
		int stX = (int)Math.floor((getX() - 8) / 48);
		int stY = (int)Math.floor(getY() / 48);
		
		un.takePhysDmg(20 + (int)(0.85f * atk), this);
		if(!un.getStatus().contains(passive))
			un.getStatus().add(passive);
		addAction(Actions.moveTo(un.getX() + ofsX, un.getY() + ofsY, (float)Math.sqrt(Math.pow(cX - stX, 2) + Math.pow(cY - stY, 2)) / 12f));
	}
	
	public void ab1() {
		int ofsX = 0;
		int ofsY = 0;
		if(blade.getDirection() == 0)
			ofsX = -48;
		else if(blade.getDirection() == 90)
			ofsY = -48;
		else if(blade.getDirection() == 180)
			ofsX = 48;
		else
			ofsY = 48;
		
		int cX = (int)Math.floor((blade.getX() - 8 + ofsX) / 48);
		int cY = (int)Math.floor((blade.getY() + ofsY) / 48);
		int stX = (int)Math.floor((getX() - 8) / 48);
		int stY = (int)Math.floor(getY() / 48);
		
		Kaiv.this.addAction(Actions.moveTo(cX * 48 + 8, cY * 48 + 4, (float)Math.sqrt(Math.pow(cX - stX, 2) + Math.pow(cY - stY, 2)) / 12f));
		blade.remove();
		blade = null;
	}
	
	public void ab2(Unit un, int dirX, int dirY) {
		un.takePhysDmg(12 + atk + (3 * level));
		//un.getStatus().add(StatusEffect.snare);
		if(un.getStatus().contains(passive)) {
			cd[0] = 0;
			un.getStatus().remove(passive);
		}

		int destX = 0, destY = 0;
		boolean knockback = false;
		if(dirX == 48)
			destX = -96;
		else if(dirY == 48)
			destY = -96;
		else if(dirX == -48)
			destX = 96;
		else
			destY = 96;
		while(!knockback) {
			int kbX = (int)getX() + destX;
			int kbY = (int)getY() + destY;
			knockback = true;
			int cX = (int)(Math.floor((kbX - 8) / 48));
			int cY = (int)(Math.floor(kbY / 48));

			if(cX > 64 || cY > 48 || cX < 0 || cY < 0)
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
				if(destX > 0)
					destX -= 48;
				else if(destY > 0)
					destY -= 48;
				else if(destX < 0)
					destX += 48;
				else
					destY += 48;
			}
		}
		addAction(Actions.moveBy(destX, destY, 0.2f));
	}
}
