package com.mygdx.game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

public class Unit extends Actor {
	protected int maxHp;
	protected int hp;
	protected int atk;
	protected int def;
	protected int mdef;
	protected int maxRes;
	protected int res;
	protected int mag;
	protected int shield;
	protected ArrayList<Integer> shields;
	private float moveFrame;
	private float frameSpeed;
	protected String anim;
	protected Sprite spr;
	protected float spOfsX, spOfsY;
	protected TextureAtlas atlas;
	protected boolean hasMoved;
	protected Stage game;
	protected static int sel;
	protected int num;
	private static int nextNum = 0;
	protected String doing;
	public static ArrayList<Unit> units;
	protected boolean team;
	static Sprite healthBar, statusPopup, abBar;
	protected String name;
	protected int range;
	protected Color resCol;
	public static boolean curTurn;
	protected Table abMenu, itemMenu;
	protected static Skin combatSkin;
	protected boolean moving;
	protected ArrayList<StatusEffect> statusEffect;
	protected int[] cd, maxCd;
	protected int level, mspeed;
	static Tile.Path path;
	protected static int curSegment;
	protected boolean primary, secondary;
	protected TextureRegion particleRegion;
	protected ArrayList<Particle> particles;
	static ShapeRenderer rect;
	protected Color charCol;
	private static Label stats;
	protected static boolean shiftDown;
	private int exp;
	protected ArrayList<Item> items; // 0=talisman, 1-6=normal, 7-8=storage
	protected int[] statPerLevel; //|0=atk|1=def|2=mag|3=hp|
	/*
	private ModelInstance model;
	private BoundingBox bBox;
	private Material normal;
	*/
	private ClickListener click;
	protected int holdWeight;
	private final int MAX_HOLD_WEIGHT = 15;
	private boolean stunned;
	protected byte visibility;
	protected int visionRange;
	private int position;
	private float dmgReduction, dmgIncrease;
	protected String[][] recommendedItems;
	protected int resSegmentSize, resBigInterval;
	protected boolean targetable, movementImpared;

	public static Pixmap vision;
	public static Sprite visionSpr;
	public static Label dmgPopup;
	public static int[] teamMoney = {600, 800};
	public final static ItemMenu shop = new ItemMenu();
	
	public Unit(int x, int y, Stage game, boolean team, boolean isHero) {
		this.num = nextNum;
		String atlasLoc = "Spritesheets/Characters/TestChar.atlas";

		hasMoved = false;
		moveFrame = 1.2f;
		anim = "Stand";
		doing = "Stand";
		name = "Null";
		frameSpeed = 0.2f;
		String[] abNames = new String[5];
		Arrays.fill(abNames, "Null");
		particleRegion = new TextureRegion(new Texture(Gdx.files.internal("Spritesheets/Particles/TestChar_Particles.png")),0, 64, 32, 32);
		particles = new ArrayList<>();
		cd = new int[4];
		maxCd = new int[4];
		mspeed = 6;
		visibility = 2;
		visionRange = 3;
		spOfsX = 0;
		spOfsY = 0;
		dmgReduction = 1f;
		dmgIncrease = 1f;
		stunned = false;
		charCol = Color.WHITE;
		shiftDown = false;
		statPerLevel = new int[4];
		items = new ArrayList<>();
		
		for(int i = 0; i < 4; i++)
			cd[i] = 0;
		
		maxHp = 100;
		shield = 0;
		shields = new ArrayList<>();
		maxRes = 0;
		resSegmentSize = 20;
		resBigInterval = 5;
		level = 1;
		range = 2;
		holdWeight = 0;
		resCol = Color.WHITE;
		hasMoved = false;
		primary = false;
		secondary = false;
		targetable = true;
		movementImpared = false;
		hp = 100;
		atk = 25;
		def = 25;
		mdef = 25;
		mag = 25;
		exp = 0;
		
		abMenu = new Table();
		abMenu.setWidth(game.getWidth());
		abMenu.align(Align.topLeft);
		abMenu.setVisible(false);
		abMenu.add(new MenuOption("Cancel", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				abMenu.setVisible(false);
				showMenu();
			}
		});
		abMenu.row();

		itemMenu = new Table();
		itemMenu.setWidth(game.getWidth());
		itemMenu.align(Align.topLeft);
		itemMenu.setVisible(false);
		itemMenu.add(new MenuOption("Cancel", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				itemMenu.setVisible(false);
				showMenu();
			}
		});
		itemMenu.row();
		itemMenu.add(new MenuOption("Buy Items", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				shop.setDisplay(true);
			}
		});

		if(stats == null) {
			stats = new Label("CombatSkin", combatSkin, "smallTxt");
		}
		
		statusEffect = new ArrayList<StatusEffect>() {
            @Override
            public boolean add(StatusEffect o) {
                o.setParent(Unit.this);
                return super.add(o);
            }
        };
		
		this.game = game;
		this.team = team;
		nextNum++;
		
		setSpr(atlasLoc, x, y);

		click = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(Unit.sel == -1 && (visibility < 2 || curTurn == team)) {
					Unit.sel = getUnPosition();
					game.getRoot().addAction(Actions.moveTo(-spr.getX() + Gdx.graphics.getWidth() / 2 - spr.getWidth() / 2,
							-spr.getY() + Gdx.graphics.getHeight() / 2 - spr.getHeight() / 2, 0.1f));
					for(Unit un : units)
						un.removeListener(un.getClick());
				}
			}
		};
		addListener(click);

		//items.add(new Item((short)0));

		statusEffect.add(new StatusEffect("OnDamage") {
			@Override
			public void effect(Unit target, Unit source) {
				visibility = 1;
			}
		});

		addAction(Actions.run(() -> {
			String fileName;
			if(isHero)
				fileName = "Data/Recommended_Items/" + name + ".itemdat";
			else
				fileName = "Data/Recommended_Items/Default.itemdat";

			try {
				String line;

				BufferedReader bufferedReader =
						Gdx.files.internal(fileName).reader(8192);

				StringBuilder builder = new StringBuilder();
				while((line = bufferedReader.readLine()) != null) {
					builder.append(line + "\n");
				}
				String comleteStr = builder.toString();

				String[] split = comleteStr.split("-");
				recommendedItems = new String[split.length][3];
				for(int i = 0; i < split.length; i++)
					recommendedItems[i] = split[i].split("\\s*\\r?\\n\\s*");
			} catch(FileNotFoundException ex) {
				System.out.println(
						"Unable to open file '" +
								fileName + "'");
			} catch(IOException ex) {
				System.out.println(
						"Error reading file '"
								+ fileName + "'");
			} catch(GdxRuntimeException ex) {
				try {
					String line;

					BufferedReader bufferedReader =
							Gdx.files.internal("Data/Recommended_Items/Default.itemdat").reader(8192);

					StringBuilder builder = new StringBuilder();
					while((line = bufferedReader.readLine()) != null) {
						builder.append(line + "\n");
					}
					String comleteStr = builder.toString();

					String[] split = comleteStr.split("-");
					recommendedItems = new String[split.length][3];
					for(int i = 0; i < split.length; i++)
						recommendedItems[i] = split[i].split("\\s*\\r?\\n\\s*");
				} catch(FileNotFoundException e) {
					System.out.println(
							"Unable to open file '" +
									fileName + "'");
				} catch(IOException e) {
					System.out.println(
							"Error reading file '"
									+ fileName + "'");
				}
			}
		}));
	}

	public void setSpr(String atlasLoc, int x, int y) {
		atlas = new TextureAtlas(Gdx.files.internal(atlasLoc));
		TextureRegion region;
		region = atlas.findRegion("Stand1");
		spr = new Sprite(region);
		spr.setPosition(8 + x * 48,  4 + y * 48);
		setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
	}
	
	public void takePhysDmg(int dmg, Unit attacker) {
		int health = hp + shield;
		int dmgTaken = (int)(dmg * (75.0 / (75 + def)) * dmgReduction * dmgIncrease);
		if(shield > 0) {
			shield -= dmgTaken;
			shields.set(shields.size() - 1, shields.get(shields.size() - 1) - dmgTaken);
			while(shields.size() > 0 && shields.get(shields.size() - 1) < 0) {
				if(shields.size() > 1)
					shields.set(shields.size() - 2, shields.get(shields.size() - 2) + shields.get(shields.size() - 1));
				shields.remove(shields.size() - 1);
			}
			if(shield < 0) {
				hp += shield;
				shield = 0;
			}
		} else
			hp -= dmgTaken;

		for(int i = 0; i < attacker.getStatus().size(); i++)
			if(attacker.getStatus().get(i).getType().equals("OnDamage"))
				attacker.getStatus().get(i).effect(this, attacker, dmgTaken);

		for(int i = 0; i < statusEffect.size(); i++)
			if(statusEffect.get(i).getType().equals("OnDamaged"))
                statusEffect.get(i).effect(this, attacker, dmgTaken);

		dmgPopup.setPosition(getX(), getY() + 24);
		dmgPopup.setText(Integer.toString(health - (hp + shield)));
	}

	public void takeMagDmg(int dmg, Unit attacker) {
		int health = hp + shield;
		int dmgTaken = (int)(dmg * (75.0 / (75 + mdef)) * dmgReduction * dmgIncrease);
		if(shield > 0) {
			shield -= dmgTaken;
			shields.set(shields.size() - 1, shields.get(shields.size() - 1) - dmgTaken);
			while(shields.size() > 0 && shields.get(shields.size() - 1) < 0) {
				if(shields.size() > 1)
					shields.set(shields.size() - 2, shields.get(shields.size() - 2) + shields.get(shields.size() - 1));
				shields.remove(shields.size() - 1);
			}
			if(shield < 0) {
				hp += shield;
				shield = 0;
			}
		} else
			hp -= dmgTaken;

		for(int i = 0; i < attacker.getStatus().size(); i++)
			if(attacker.getStatus().get(i).getType().equals("OnDamage") || attacker.getStatus().get(i).getType().equals("OnSpellHit"))
				attacker.getStatus().get(i).effect(this, attacker, dmgTaken);

        for(int i = 0; i < statusEffect.size(); i++)
            if(statusEffect.get(i).getType().equals("OnDamaged"))
                statusEffect.get(i).effect(this, attacker, dmgTaken);

		dmgPopup.setPosition(getX(), getY() + 24);
		dmgPopup.setText(Integer.toString(health - (hp + shield)));
	}
	
	public void takeTrueDmg(int dmg, Unit attacker) {
		int health = hp + shield;
		int dmgTaken = (int)(dmg * dmgReduction * dmgIncrease);
		if(shield > 0) {
			shield -= dmg;
			shields.set(shields.size() - 1, shields.get(shields.size() - 1) - dmgTaken);
			while(shields.size() > 0 && shields.get(shields.size() - 1) < 0) {
				if(shields.size() > 1)
					shields.set(shields.size() - 2, shields.get(shields.size() - 2) + shields.get(shields.size() - 1));
				shields.remove(shields.size() - 1);
			}
			if(shield < 0) {
				hp += shield;
				shield = 0;
			}
		} else
			hp -= dmgTaken;

		for(int i = 0; i < attacker.getStatus().size(); i++)
			if(attacker.getStatus().get(i).getType().equals("OnDamage"))
				attacker.getStatus().get(i).effect(this, attacker, dmgTaken);

        for(int i = 0; i < statusEffect.size(); i++)
            if(statusEffect.get(i).getType().equals("OnDamaged"))
                statusEffect.get(i).effect(this, attacker, dmgTaken);

		dmgPopup.setPosition(getX(), getY() + 24);
		dmgPopup.setText(Integer.toString(health - (hp + shield)));
	}
	
	public void takePhysDmg(int dmg) {
		int dmgTaken = (int)(dmg * (75.0 / (75 + def)));
		if(shield > 0) {
			shield -= dmgTaken;
			shields.set(shields.size() - 1, shields.get(shields.size() - 1) - dmgTaken);
			while(shields.size() > 0 && shields.get(shields.size() - 1) < 0) {
				if(shields.size() > 1)
					shields.set(shields.size() - 2, shields.get(shields.size() - 2) + shields.get(shields.size() - 1));
				shields.remove(shields.size() - 1);
			}
			if(shield < 0) {
				hp += shield;
				shield = 0;
			}
		} else
			hp -= dmgTaken;
	}

	public void takeMagDmg(int dmg) {
		int dmgTaken = (int)(dmg * (75.0 / (75 + mdef)));
		if(shield > 0) {
			shield -= dmgTaken;
			shields.set(shields.size() - 1, shields.get(shields.size() - 1) - dmgTaken);
			while(shields.size() > 0 && shields.get(shields.size() - 1) < 0) {
				if(shields.size() > 1)
					shields.set(shields.size() - 2, shields.get(shields.size() - 2) + shields.get(shields.size() - 1));
				shields.remove(shields.size() - 1);
			}
			if(shield < 0) {
				hp += shield;
				shield = 0;
			}
		} else
			hp -= dmgTaken;
	}

	public void takeTrueDmg(int dmg) {
		int dmgTaken = (int)(dmg * dmgReduction * dmgIncrease);
		if(shield > 0) {
			shield -= dmg;
			shields.set(shields.size() - 1, shields.get(shields.size() - 1) - dmgTaken);
			while(shields.size() > 0 && shields.get(shields.size() - 1) < 0) {
				if(shields.size() > 1)
					shields.set(shields.size() - 2, shields.get(shields.size() - 2) + shields.get(shields.size() - 1));
				shields.remove(shields.size() - 1);
			}
			if(shield < 0) {
				hp += shield;
				shield = 0;
			}
		} else
			hp -= dmgTaken;
	}
	
	public void openAbMenu() {
		game.getRoot().findActor("CombatMenu").setVisible(false);
		
		abMenu.setPosition(getX() + getWidth(), getY() + getHeight());
		abMenu.setVisible(true);
		doing = "abMenu";
		
		for(int i = 0; i < abMenu.getChildren().size; i++)
			abMenu.getChildren().get(i).setColor(Color.WHITE);
	}
	
	public void setMoveable(boolean moveable) {
		hasMoved = !moveable;
	}
	
	public int getNum() {
		return num;
	}
	
	Table getAbMenu() {
		return abMenu;
	}
	
	public int getMaxHp() {
		return maxHp;
	}
	
	public int getHp() {
		return hp;
	}
	
	public int getMspeed() {
		return mspeed;
	}

	public ClickListener getClick() {
		return click;
	}

	public boolean getStunned() { return stunned; }
	
	ArrayList<Particle> getParticles() {
		return particles;
	}

	protected float getDirection(float x1, float x2, float y1, float y2) {
	    if(x1 - x2 == 0) { // Nobody likes dividing by zero
	        if(y1 > y2) // Going straight down
	            return 270;
	        else // Going straight up
	            return 90;
        }
		float dir = (float)Math.atan((y1 - y2) / (x1 - x2));
		if(x1 >= x2)
			dir += Math.PI;
		return (float)Math.toDegrees(dir);
	}

	public int getAtk() {
		return atk;
	}

	public void setAtk(int atk) {
		this.atk = atk;
	}

	public int getDef() {
		return def;
	}

	public void setDef(int def) {
		this.def = def;
	}

	public int getMDef() {
		return mdef;
	}

	public void setMDef(int def) {
		this.mdef = def;
	}

	public int getMag() {
		return mag;
	}

	public void setMag(int mag) {
		this.mag = mag;
	}

	public int getShield() {
		return shield;
	}

	public void addShield(int shield) {
		this.shield += shield;
	}

	public ArrayList<Integer> getShieldArray() {
		return shields;
	}

	public String[][] getRecItems() {
		return recommendedItems;
	}
	
	public void heal(int health) {
		hp += health;
		if(hp > maxHp)
			hp = maxHp;
	}
	
	public void showMenu() {
		Table combatMenu = game.getRoot().findActor("CombatMenu");
		
		MenuOption nameSlot = (MenuOption)combatMenu.getChildren().get(0);
		nameSlot.pad(0, 0, 0, 0);
		nameSlot.setText(name);
		nameSlot.pad(0, (150-nameSlot.getPrefWidth())/2-4, 0, (150-nameSlot.getPrefWidth())/2-4);
		
		for(int i = 0; i < combatMenu.getChildren().size; i++)
			combatMenu.getChildren().get(i).setColor(Color.WHITE);
		MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
		if(primary)
			atk.setColor(Color.DARK_GRAY);
		MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
		if(hasMoved)
			mo.setColor(Color.DARK_GRAY);

		if(!stunned)
			combatMenu.setVisible(true);
		combatMenu.setPosition(getX() + getWidth(), getY() + getHeight());
		combatMenu.setZIndex(8);
		if(combatMenu.getX() > Tile.walls.getWidth() * 48 - combatMenu.getWidth())
			combatMenu.setX(Tile.walls.getWidth() * 48 - combatMenu.getWidth());
		if(combatMenu.getX() > Tile.walls.getHeight() * 48 - combatMenu.getHeight())
			combatMenu.setX(Tile.walls.getHeight() * 48 - combatMenu.getHeight());
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		moving = false;
		if(doing.equals("MoveAnim") && path.getSegment(curSegment)[0] * 48 + 8 == Math.round(getX())
				&& path.getSegment(curSegment)[1] * 48 + 4 == Math.round(getY())) {
			curSegment++;
			if(curSegment < path.getLength())
				addAction(Actions.moveTo(path.getSegment(curSegment)[0] * 48 + 8, path.getSegment(curSegment)[1] * 48 + 4, 0.15f));
			else {
				doing = "Stand";
				curSegment = 0;
			}
		}
		if(getX() != spr.getX() - spOfsX || getY() != spr.getY() - spOfsY) {
			moving = true;
			updateVisibility();
			if(sel == num)
				game.getRoot().addAction(Actions.moveBy((spr.getX() - spOfsX - getX()), (spr.getY() - spOfsY - getY())));
			spr.setPosition(getX() + spOfsX, getY() + spOfsY);
			Table combatMenu = game.getRoot().findActor("CombatMenu");
			if(sel == num)
				combatMenu.setPosition(getX() + getWidth(), getY() + getHeight());
		} else if(doing.equals("MoveAnim")) {
			doing = "Stand";
		}
		if(doing.equals("Stand") && !game.getRoot().findActor("CombatMenu").isVisible() && !itemMenu.isVisible() && sel == num && team == curTurn)
			showMenu();
		if(doing.equals("Stand") && Tile.hit)
			Tile.hit = false;
		
		if(sel != num && abMenu.isVisible())
			abMenu.setVisible(false);
		if(sel != num && itemMenu.isVisible())
			itemMenu.setVisible(false);

		for (int i = 0; i < particles.size(); i++) particles.get(i).increment();
		
		if(hp <= 0)
			hp = 0;
		if(res > maxRes)
			res = maxRes;
	}
	
	public void setDoing(String newDoing, boolean setMenu) {
		doing = newDoing;
		game.getRoot().findActor("CombatMenu").setVisible(setMenu);
	}

	public String getDoing() { return doing; }

	void setShiftDown(boolean shiftDown) {
		this.shiftDown = shiftDown;
	}
	
	public boolean getTeam() {
		return team;
	}
	
	boolean getMoveable() {
		return hasMoved;
	}
	
	public ArrayList<StatusEffect> getStatus() {
		return statusEffect;
	}

	/**
	 * Gets the grid position of this unit's x position, accommodating for the 8 pixels to the left
	 * @return The result of the check
	 */
	public int getGridX() {
		return (int)Math.floor((getX() + 16) / 48);
	}

	/**
	 * Gets the grid position of this unit's y position
	 * @return The result of the check
	 */
	public int getGridY() {
		return (int)Math.floor((getY() + 24) / 48);
	}

	/**
	 * Gets the grid position of a target location, accommodating for the 8 pixels to the left
	 * @param x: The value to caalculate
	 * @return The result of the check
     */
	protected int getGridX(float x) {
		return (int)Math.floor((x - 7) / 48);
	}

	/**
	 * Gets the grid position of a target location, not accommodating for the 8 pixels to the left
	 * @param y: The value to caalculate
	 * @return The result of the check
	 */
	protected int getGridY(float y) {
		return (int)Math.floor(y / 48);
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int value) {
		exp = value;
	}
	
	public void setMspeed(int value) {
		mspeed = value;
	}

	public void setStunned(boolean value) {
		stunned = value;
	}

	public void silence() {
	    primary = true;
	    secondary = true;
    }

	public boolean drawTile(Batch batch, float parentAlpha) {
		boolean ret = false;
		if(num == sel) {
			switch(doing) {
			case "Move":
				Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "moveDiamond", mspeed, Color.BLUE, true);
				if(path != null)
					for(int i = 0; i < path.getLength(); i++)
						Tile.disp(batch, path.getSegment(i)[0] * 48 + 8, path.getSegment(i)[1] * 48 , Color.GREEN);
				ret = true;
				break;
			case "Attack":
				if(!primary) {
					Tile.drawShape(batch, parentAlpha, getX(), getY() - 4, "line", range, Color.BLUE, true);
					ret = true;
				} else {
					anim = "Stand";
					showMenu();
				}
				break;
			default:
				break;
			}
		} else if (!Objects.equals(doing, "MoveAnim")) {
			doing = "Stand";
		}
		return ret;
	}
	
	public void clickAct() {
		int cX = (int)Math.floor((Tile.clickX - 8) / 48);
		int cY = (int)Math.floor(Tile.clickY / 48);
		int stX = (int)Math.floor((getX() - 8) / 48);
		int stY = (int)Math.floor(getY() / 48);
		if(Objects.equals(doing, "Move") && path != null) {
			boolean canMove = true;
			for (Unit unit : units) {
				int unStX = (int) Math.floor((unit.getX() - 8) / 48);
				int unStY = (int) Math.floor(unit.getY() / 48);
				if (cX == unStX && cY == unStY)
					canMove = false;
			}
			if(canMove) {
				doing = "MoveAnim";
				curSegment = 1;
				addAction(Actions.moveTo(path.getSegment(1)[0] * 48 + 8, path.getSegment(1)[1] * 48 + 4, 0.15f));
				Tile.floorMapInit = false;
				MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
				mo.setColor(Color.DARK_GRAY);
				hasMoved = true;
				Tile.drawPath = false;
			}
		}
		if(Objects.equals(doing, "Attack")) {
			doing = "Stand";
			float dir;
			int parY;
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
			 getY() + (48 - particleRegion.getRegionHeight()) / 2, particleRegion, 4, range) {
				@Override
				public void act(float delta) {
					super.act(delta);
					atkAct(this);
				}

				@Override
				public void effect(Unit un) {
					atkEffect(un);
                    this.remove();
				}
			});
			doing = "AtkAnim";
			MenuOption atk = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(2);
			atk.setColor(Color.DARK_GRAY);
			MenuOption mo = (MenuOption)((Table)game.getRoot().findActor("CombatMenu")).getChildren().get(1);
			mo.setColor(Color.DARK_GRAY);
			primary = true;
			hasMoved = true;
		}
	}

	protected void atkAct(Projectile atkProj) {

	}

	protected void atkEffect(Unit un) {
        ArrayList<StatusEffect> se = Unit.this.statusEffect;
        int dmg = atk;
        int curHp = un.getHp();
        un.takePhysDmg(dmg, Unit.this);
        for (int i = 0; i < se.size(); i++)
            if (Objects.equals(se.get(i).getType(), "OnHit") || Objects.equals(se.get(i).getType(), "OnAttack"))
                se.get(i).effect(un, Unit.this, dmg);
        while(dmgPopup.getActions().size > 0) {
            dmgPopup.removeAction(dmgPopup.getActions().get(0));
        }
        dmgPopup.setText(Integer.toString(curHp - un.getHp()));
    }

	@Override
	public void draw(Batch batch, float parentAlpha) {
		spr.setColor(charCol);
		if(visibility < 2) // If this hero is visible or revealed
			spr.draw(batch);
		else if(curTurn == team) { // If this hero is stealthed
			spr.setAlpha(0.5f);
			spr.draw(batch);
			spr.setAlpha(1f);
		}
		String str = anim + (int)moveFrame;
		if(atlas.findRegion(str) == null) {
			moveFrame = 1;
			str = anim + 1;
			spr.setRegion(atlas.findRegion(str));
		}
		else
			spr.setRegion(atlas.findRegion(str));
		if(hp > 0)
			moveFrame+=frameSpeed;
		
	}
	
	public void drawHUD(Batch batch, float parentAlpha) {
		healthBar.setRotation(0);
		healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 0, 0, 128, 78));
		healthBar.setSize(96, 58.5f);
		healthBar.setPosition(getX() - 24 + game.getRoot().getX(), getY() + getHeight() + game.getRoot().getY());
		healthBar.draw(batch);
		
		if(curTurn == team)
			healthBar.setColor(targetable ? 0 : 0.75f, 0.75f, 0, 1);
		else
			healthBar.setColor(0.75f, targetable ? 0 : 0.375f, 0, 1);
		healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 8, 120, 112, 16));
		healthBar.setSize(84 * (hp / (float)maxHp), 12);
		healthBar.setPosition(getX() - 18 + game.getRoot().getX(), getY() + getHeight() + 34.5f + game.getRoot().getY());
		healthBar.draw(batch);
		if(num == sel) {
			healthBar.setSize(112 * (hp / (float)maxHp), 16);
			healthBar.setPosition(8, statusPopup.getY() + 14);
			healthBar.draw(batch);
		}

		healthBar.setColor(1, 1, 1, 0.33f);
		healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 0, 160, 8, 16));
		healthBar.setSize(6, 12f);
		healthBar.setPosition(getX() - 22 + game.getRoot().getX(), getY() + getHeight() + 34.5f + game.getRoot().getY());
		float divAmt = (float)Math.floor(maxHp / 20d);
		int segments = (int)Math.floor(hp / 20d);
		for(int i = 0; i < segments; i++) {
			healthBar.translate(84f / divAmt, 0);
			if((i + 1) % 5 != 0)
				healthBar.draw(batch);
			else {
				healthBar.setColor(1, 1, 1, 0.75f);
				healthBar.draw(batch);
				healthBar.setColor(1, 1, 1, 0.33f);
			}
		}
		
		if(maxRes > 0) {
			healthBar.setColor(resCol);
			healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 8, 120, 112, 16));
			healthBar.setSize(75 * (res / (float)maxRes), 5f);
			healthBar.setPosition(getX() - 14 + game.getRoot().getX(), getY() + getHeight() + 29 + game.getRoot().getY());
			healthBar.draw(batch);
			if(num == sel) {
				healthBar.setSize(100 * (hp / (float)maxHp), 7);
				healthBar.setPosition(14, statusPopup.getY() + 7);
				healthBar.draw(batch);
			}
			healthBar.setColor(1, 1, 1, 1);
			healthBar.setSize(75 * (exp / (float)(10 * level)), 5f);
			healthBar.setPosition(getX() - 14 + game.getRoot().getX(), getY() + getHeight() + 21 + game.getRoot().getY());
			healthBar.draw(batch);


			healthBar.setColor(1, 1, 1, 0.5f);
			healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 0, 160, 8, 16));
			healthBar.setSize(3f, 5f);
			healthBar.setPosition(getX() - 16 + game.getRoot().getX(), getY() + getHeight() + 29 + game.getRoot().getY());
			float resDivAmt = (float)Math.floor(maxRes / (float)resSegmentSize);
			int resSegments = (int)Math.floor(res / (float)resSegmentSize);
			for(int i = 0; i < resSegments; i++) {
				healthBar.translate(75f / resDivAmt, 0);
				if((i + 1) % resBigInterval != 0 || i * resSegmentSize == maxRes)
					healthBar.draw(batch);
				else {
					healthBar.setColor(1, 1, 1, 1f);
					healthBar.draw(batch);
					healthBar.draw(batch);
					healthBar.setColor(1, 1, 1, 0.5f);
				}
			}
		}
		
		if(num == sel) {
			int mX = Gdx.input.getX();
			int mY = Gdx.graphics.getHeight() - Gdx.input.getY();

			statusPopup.setPosition(0, 0);
			if(mX < 128 && mY < 96)
				statusPopup.setColor(new Color(1, 1, 1, 0.25f));
			statusPopup.draw(batch);
			
			if(curTurn == team)
				healthBar.setColor(0, 0.75f, 0, 1);
			else
				healthBar.setColor(0.75f, 0, 0, 1);
			healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 8, 120, 112, 16));
			healthBar.setSize(112 * (hp / (float)maxHp), 16);
			healthBar.setPosition(8, statusPopup.getY() + 14);
			if(mX < 128 && mY < 96)
				healthBar.setAlpha(0.25f);
			healthBar.draw(batch);
			
			if(maxRes > 0) {
				healthBar.setColor(resCol);
				healthBar.setRegion(new TextureRegion(healthBar.getTexture(), 8, 120, 112, 16));
				healthBar.setSize(100 * (res / (float)maxRes), 7);
				healthBar.setPosition(14, statusPopup.getY() + 7);
				if(mX < 128 && mY < 96)
					healthBar.setAlpha(0.25f);
				healthBar.draw(batch);
			}
			statusPopup.setColor(Color.WHITE);
			healthBar.setColor(Color.WHITE);

			if(Unit.curTurn == team) {
				game.getBatch().end();
				rect.setProjectionMatrix(game.getCamera().combined);
				rect.begin(ShapeRenderer.ShapeType.Filled);
				Gdx.gl20.glEnable(GL20.GL_BLEND);
				for(int i = 0; i < 4; i++) {
					if(cd[i] > 0) {
						rect.setColor(new Color(0, 0, 0, 0.75f));
						if(mX > 202 && mX < 458 && mY < 64)
							rect.setColor(new Color(0, 0, 0, 0.1875f));
						rect.rect(233 + 52 * i , 13, 38, 38 * (cd[i] / (float)maxCd[i]));
					}
				}
				rect.end();
				game.getBatch().begin();

				abBar.setPosition(202, 0);
				if(mX > 202 && mX < 458 && mY < 64)
					abBar.setColor(new Color(1, 1, 1, 0.25f));
				abBar.draw(batch);
			}
			abBar.setColor(Color.WHITE);
			rect.setColor(Color.WHITE);

			stats.setText("ATK:" + atk);
			stats.setPosition(64, 64);
			stats.draw(batch, parentAlpha);
			stats.setText("DEF:" + def);
			stats.setPosition(64, 48);
			stats.draw(batch, parentAlpha);
			stats.setText("MAG:" + mag);
			stats.setPosition(64, 32);
			stats.draw(batch, parentAlpha);
			if(shield == 0)
				stats.setText(hp + "/" + maxHp);
			else
				stats.setText(hp  + "+(" + shield + ")/" + maxHp);
			stats.setPosition(64 - (stats.getPrefWidth() / 2), 13);
			stats.draw(batch, parentAlpha);
		}
		
		healthBar.setColor(Color.WHITE);

		/*
		if(abMenu.isVisible())
			abMenu.draw(batch, parentAlpha);
		if(itemMenu.isVisible())
			itemMenu.draw(batch, parentAlpha);
		*/
	}
	
	public void dispose() {
		spr.getTexture().dispose();
		atlas.dispose();
	}
	
	public void alTurn() {
		if(hp > 0) hp += (int)(maxHp * 0.04f);
		hasMoved = false;
		primary = false;
		secondary = false;
        movementImpared = false;
		
		if(res > maxRes)
			res = maxRes;
		if(hp > maxHp)
			hp = maxHp;
		
		int statusCheck = 0;
		int statusStore = statusEffect.size();
		while(statusCheck < statusEffect.size()) {
			if(statusEffect.get(statusCheck).getType().equals("OnAlTurn"))
				statusEffect.get(statusCheck).effect(this, null);
			if(statusStore == statusCheck && statusCheck < statusEffect.size()) {
				statusEffect.get(statusCheck).changeDuration(-1);
				if(statusEffect.get(statusCheck).getDuration() <= 0)
					statusEffect.get(statusCheck).end();
				statusCheck++;
			} else {
				statusStore = statusCheck;
			}
		}
		
		for(int i = 0; i < 4; i++)
			if(cd[i] > 0)
				cd[i]--;

		if(curTurn) {
			//exp += 3;

			if(exp >= 10 * level)
				levelUp();
		}
	}
	
	public void enTurn() {
		stunned = false;
		if(visibility == 0)
			visibility = 2;
		for (int i = 0; i < statusEffect.size(); i++)
			if (Objects.equals(statusEffect.get(i).getType(), "OnEnTurn"))
				statusEffect.get(i).effect(this, this);

		if(curTurn) {
			//exp += 3;

			if(exp >= 10 * level)
				levelUp();
		}
	}

	private void levelUp() {
		atk += statPerLevel[0];
		def += statPerLevel[1];
		mag += statPerLevel[2];
		maxHp += statPerLevel[3];
		hp += statPerLevel[3];
		exp -= 10 * level;
		level++;
	}

	public void calcPath(int sX, int sY) {
		final int STAGE_WIDTH = Tile.walls.getWidth() * 48;
		final int STAGE_HEIGHT = Tile.walls.getHeight() * 48;
		int cX = (int)Math.floor((sX - 8) / 48);
		int cY = (int)Math.floor(sY / 48);
		byte uX = (byte)Math.floor((getX() - 8) / 48);
		byte uY = (byte)Math.floor(getY() / 48);
		int ms = mspeed;
		boolean pathFound = false;
		int steps = 0;
		ArrayList<Tile.Path> oldPaths = new ArrayList<>();
		ArrayList<Tile.Path> newPaths = new ArrayList<>();
		boolean[][] alreadyTravelled = new boolean[ms * 2 + 1][ms * 2 + 1];
		oldPaths.add(new Tile.Path(uX, uY));

		Unit.path = null;
		newPaths.clear();

		while(!pathFound && steps < ms) {
			while(oldPaths.size() > 0) {
				Tile.Path oldPath = oldPaths.get(0);
				int piX = oldPath.last()[0];
				int piY = oldPath.last()[1];

				boolean[] unitLoc = new boolean[4];
				for(Unit un : units)
					if(un.getTeam() != team && un.getTargetable()) {
						if(piX + 1 == un.getGridX() && piY == un.getGridY())
							unitLoc[0] = true;
						if(piX == un.getGridX() && piY + 1 == un.getGridY())
							unitLoc[1] = true;
						if(piX - 1 == un.getGridX() && piY == un.getGridY())
							unitLoc[2] = true;
						if(piX == un.getGridX() && piY - 1 == un.getGridY())
							unitLoc[3] = true;
					}

				if(Tile.walls.getPixel(piX + 1, piY) != Color.rgba8888(Color.WHITE)
						&& !alreadyTravelled[piX - uX + 1 + ms][piY - uY + ms] && !unitLoc[0])
					newPaths.add(new Tile.Path(oldPath, 1, 0));

				if(Tile.walls.getPixel(piX - 1, piY) != Color.rgba8888(Color.WHITE)
						&& !alreadyTravelled[piX - uX - 1 + ms][piY - uY + ms] && !unitLoc[2])
					newPaths.add(new Tile.Path(oldPath, -1, 0));

				if(Tile.walls.getPixel(piX, piY + 1) != Color.rgba8888(Color.WHITE)
						&& !alreadyTravelled[piX - uX + ms][piY - uY + 1 + ms] && !unitLoc[1])
					newPaths.add(new Tile.Path(oldPath, 0, 1));

				if(Tile.walls.getPixel(piX, piY - 1) != Color.rgba8888(Color.WHITE)
						&& !alreadyTravelled[piX - uX + ms][piY - uY - 1 + ms] && !unitLoc[3])
					newPaths.add(new Tile.Path(oldPath, 0, -1));

				oldPaths.remove(oldPath);
			}

			while(newPaths.size() > 0 && !pathFound) {
				Tile.Path newPath = newPaths.get(0);
				alreadyTravelled[newPath.last()[0] - uX + ms][newPath.last()[1] - uY + ms] = true;
				if(newPath.last()[0] == cX && newPath.last()[1] == cY) {
					Unit.path = newPath;
					pathFound = true;
				}

				oldPaths.add(newPath);
				newPaths.remove(newPath);
			}
			steps++;
		}

		if(Unit.path != null && Unit.path.last()[0] == uX && Unit.path.last()[1] == uY)
				Unit.path = new Tile.Path(uX, uY);
	}

	public void setVisibility(byte visibility) {
		this.visibility = visibility;
	}

	public int getVisibility() {
		if(visibility == 0)
			return 0;
		else if(visibility == 3)
			return 3;

		int vis = 2;
		for(Unit un : units) {
			float dist = (float)Math.sqrt(Math.pow(un.getGridX() - getGridX(), 2)
					+ Math.pow(un.getGridY() - getGridY(), 2));
			if(dist <= un.visionRange + 0.5 && un.getTeam() != getTeam())
				vis = 1;
		}
		if(Tile.walls.getPixel(getGridX(), getGridY()) == Color.rgba8888(Color.GREEN))
			vis = 1;
		if(Tile.walls.getPixel(getGridX(), getGridY()) == Color.rgba8888(Color.BLUE) && !getTeam())
			vis = 1;
		if(Tile.walls.getPixel(getGridX(), getGridY()) == Color.rgba8888(Color.RED) && getTeam())
			vis = 1;
		return vis;
	}

	/*
	Visibility Meaning:
	0 = Revealed
	1 = Visible
	2 = Not Visible
	3 = Stealthed
	 */
	public static void updateVisibility() {
		vision.fill();
		vision.setColor(1f, 1f, 1f, 0.1f);
		for(int ix = 0; ix < 64; ix++)
			for(int iy = 0; iy < 48; iy++) {
				if(Tile.walls.getPixel(ix, vision.getHeight() - iy) == Color.rgba8888(Color.GREEN))
					vision.drawPixel(ix, iy - 1);
				if(Tile.walls.getPixel(ix, vision.getHeight() - iy) == Color.rgba8888(Color.BLUE) && curTurn)
					vision.drawPixel(ix, iy - 1);
				if(Tile.walls.getPixel(ix, vision.getHeight() - iy) == Color.rgba8888(Color.RED) && !curTurn)
					vision.drawPixel(ix, iy - 1);
			}
		for(Unit un : units) {
			if(un.visibility != 0 && un.visibility != 3) {
				un.visibility = 2;
				for(Unit un2 : units) {
					float dist = (float)Math.sqrt(Math.pow(un2.getGridX() - un.getGridX(), 2)
							+ Math.pow(un2.getGridY() - un.getGridY(), 2));
					if(dist <= un2.visionRange + 0.5 && un2.getTeam() != un.getTeam())
						un.visibility = 1;
				}
				if(un.getTeam() == curTurn) {
					un.visibility = 1;
					for(int ix = 0; ix < 7; ix++)
						for(int iy = 0; iy < 7; iy++)
							if(Math.sqrt(Math.pow(ix - 3, 2) + Math.pow(iy - 3, 2)) <= un.visionRange + 0.5)
								vision.drawPixel(un.getGridX() + ix - 3, vision.getHeight() - un.getGridY() + iy - 4);
				}
				if(Tile.walls.getPixel(un.getGridX(), un.getGridY()) == Color.rgba8888(Color.GREEN))
					un.visibility = 1;
				if(Tile.walls.getPixel(un.getGridX(), un.getGridY()) == Color.rgba8888(Color.BLUE) && !un.getTeam())
					un.visibility = 1;
				if(Tile.walls.getPixel(un.getGridX(), un.getGridY()) == Color.rgba8888(Color.RED) && un.getTeam())
					un.visibility = 1;
			}
		}
		vision.setColor(Color.DARK_GRAY);
		visionSpr.getTexture().dispose();
		visionSpr.setTexture(new Texture(vision));
	}

	public static void updatePosition() {
		for(int i = 0; i < units.size(); i++) {
			units.get(i).setUnPosition(i);
			units.get(i).num = i;
		}
	}

	protected static float getDistance(float x1, float x2, float y1, float y2) {
		return (float)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public int getUnPosition() {
		return position;
	}

	public void setUnPosition(int position) { // I forgot that setPosition is alreay a method! I'm bad kid ;_;
		this.position = position;
	}

	public boolean getMoving() { return moving; }

	public float getDmgReduction() {
		return dmgReduction;
	}

	public void setDmgReduction(float dmgReduction) {
		this.dmgReduction *= dmgReduction;
	}

	public void setDmgReductionFlat(float dmgReduction) {
		this.dmgReduction = dmgReduction;
	}

	public float getDmgIncrease() {
		return dmgIncrease;
	}

	public void setDmgIncrease(float dmgIncrease) {
		this.dmgIncrease *= dmgIncrease;
	}

	public int getRes() { return res; }

	public void setRes(int res) { this.res = res; }

	public boolean getTargetable() { return targetable; }

	public void setTargetable(boolean targetable) { this.targetable = targetable; }

    public boolean getMovementImpared() { return movementImpared; }

    public void setMovementImpared(boolean mi) {
	    movementImpared = mi;
    }
}
