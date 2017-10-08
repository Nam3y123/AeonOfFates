package com.mygdx.game;

import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveByAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Heroes.*;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	private Stage curStage;
	private Stage menu, game;
	private Skin skin, combatSkin;
	private ArrayList<Unit> units;
	private Group unitGroup;
	private int[] oldPathCoord;
	private float scrollAmt;
	private final float MIN_SCROLL = 0.5f, MAX_SCROLL = 2f;
	private final int STAGE_WIDTH = 3072;
	private final int STAGE_HEIGHT = 2304;
	private boolean turnSwap;
	private int monsterSel;
	private Sprite map;

	private int camMove; // 0 = Still, 1 = Up, 2 = Down, 3 = Left, 4 = Right

	final private ArrayList<Monsters> monsters = new ArrayList<Monsters>() {
		@Override
		public boolean add(Monsters e) {
			units.add(e);
			return super.add(e);
		}
	};

	@Override
	public void create () {
		units = new ArrayList<>();
		Table menuTable = new Table();
		try {
			menu = new GameClient(new ScreenViewport()) {
                @Override
                public void startGame() {
                    startGame_Sandbox();
                }
            };
		} catch (IOException e) {
			e.printStackTrace();
		}
		game = new Stage(new ScreenViewport());
		Tile.game = game;
		skin = new Skin(Gdx.files.internal("UISkin.json"));
		unitGroup = new Group();
		combatSkin = new Skin(Gdx.files.internal("CombatSkin.json"));
		oldPathCoord = new int[2];
		camMove = 0;
		scrollAmt = 1f;
		monsterSel = 0;
		turnSwap = false;
		TextButton start = new TextButton("New Game", skin, "default");
		TextButton quit = new TextButton("Quit Game", skin, "default");
		map = new Sprite(new Texture(Gdx.files.internal("HUDMap.png")));
		map.setScale(0.75f, 0.75f);
		Tile.Init(units);
		StatusEffect.Init();
		Unit.units = units;
		Projectile.units = units;
		Unit.sel = -1;
		Unit.curTurn = true;
		Unit.curSegment = 1;
		
		Unit.healthBar = new Sprite(new TextureRegion(new Texture(Gdx.files.internal("HealthBar.png")), 0, 0, 128, 78));
		Unit.statusPopup = new Sprite(new Texture(Gdx.files.internal("StatusPopup.png")));
		Unit.abBar = new Sprite(new Texture(Gdx.files.internal("AbBar.png")));
		Unit.rect = new ShapeRenderer();

		start.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				startGame_Sandbox();
			}
		});
		
		quit.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});

		Unit.vision = new Pixmap(64, 48, Pixmap.Format.RGBA8888);
		Unit.vision.setColor(Color.DARK_GRAY);
		Unit.vision.fill();
		Pixmap.setBlending(Pixmap.Blending.None);
		Unit.visionSpr = new Sprite(new Texture(Unit.vision));
		Unit.visionSpr.setPosition(Unit.visionSpr.getWidth() * 24 - 24, Unit.visionSpr.getHeight() * 24 - 24);

		/*
		menuTable.setWidth(menu.getWidth());
		menuTable.align(Align.center|Align.top);
		menuTable.setPosition(0, menu.getHeight()/2);
		menuTable.add(start).padBottom(30);
		menuTable.row();
		menuTable.add(quit);
		menu.addActor(menuTable);
		*/
		
		curStage = menu;

		InputMultiplexer im = new InputMultiplexer(curStage, this);
		Gdx.input.setInputProcessor(im);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);

		curStage.act();
		curStage.draw();
		if(curStage.equals(game)) {
			curStage.getBatch().begin();
			for (Unit unit : units)
			    if(Unit.sel == -1 || !unit.equals(units.get(Unit.sel)))
                    unit.drawHUD(curStage.getBatch(), 1f);
			if(Unit.sel > -1)
			    units.get(Unit.sel).drawHUD(curStage.getBatch(), 1f);
			//if(game.getRoot().findActor("CombatMenu").isVisible())
				//game.getRoot().findActor("CombatMenu").draw(game.getBatch(), 1);
			map.setPosition(458, -20);
			map.draw(curStage.getBatch());
			Unit.shop.draw(curStage.getBatch());
			curStage.getBatch().end();
		}
		
		if(Tile.clicked && Unit.sel > -1) {
			Tile.clicked = false;
			if(!Tile.hit && game.getRoot().findActor("CombatMenu") != null) {
				game.getRoot().findActor("CombatMenu").setVisible(false);
				Unit.shop.setDisplay(false);
				Unit.sel = -1;
				oldPathCoord[0] = -1;
				oldPathCoord[1] = -1;
				for (Unit unit : units) unit.addListener(unit.getClick());
			}
			else if(Tile.hit)
				units.get(Unit.sel).clickAct();
			Tile.hit = false;
		}
		
		boolean organized = false;
		while(!organized) {
			organized = true;
			for(int i = 0; i < unitGroup.getChildren().size - 1; i++) {
				if(unitGroup.getChildren().get(i).getY() < unitGroup.getChildren().get(i + 1).getY()) {
					unitGroup.swapActor(i, i+1);
					organized = false;
				}
			}
		}

		if(camMove != 0) {
			switch (camMove) {
				case 1:
					game.getRoot().moveBy(0f, -18f);
					break;
				case 2:
					game.getRoot().moveBy(0f, 18f);
					break;
				case 3:
					game.getRoot().moveBy(18f, 0f);
					break;
				case 4:
					game.getRoot().moveBy(-18f, 0f);
					break;
			}
			//game.getRoot().setPosition(camera.position.x * 24f - game.getRoot().getWidth() / 2f,
					//camera.position.z * 24f - game.getRoot().getHeight() / 2f);
		}

		game.getViewport().setScreenWidth(Gdx.graphics.getWidth());
		game.getViewport().setScreenHeight(Gdx.graphics.getHeight());
		if(turnSwap && (monsters.size() == 0 || monsters.get(monsterSel).getFinished())) {
			monsterSel++;
			if(monsterSel < monsters.size())
				monsters.get(monsterSel).onTurnSwap();
			else {
				monsterSel = 0;
				turnSwap = false;
				Unit.curTurn = !Unit.curTurn;
				game.getRoot().findActor("CombatMenu").setVisible(false);
				if(Unit.sel != -1)
					for (Unit unit : units) unit.addListener(unit.getClick());
				Unit.sel = -1;
				oldPathCoord[0] = -1;
				oldPathCoord[1] = -1;
				Unit.teamMoney[Unit.curTurn ? 0 : 1] += 100;

				for (int i = 0; i < units.size(); i++) {
					Unit unit = units.get(i);
					if (unit.getTeam() == Unit.curTurn)
						unit.alTurn();
					else
						unit.enTurn();
				}

				Unit.updateVisibility();
			}
		}
	}
	
	@Override
	public void dispose() {
		for (Unit unit : units) unit.dispose();
		menu.dispose();
		game.dispose();
		skin.dispose();
		Unit.vision.dispose();
		Unit.visionSpr.getTexture().dispose();
	}

	private void startGame_Sandbox() {
		/* lvl 1 */
		/*
		units.add(new Tera(1, 11, game, true));
		units.add(new Malaki(5, 11, game, false));
		units.add(new Valator(1, 13, game, true));
		units.add(new Suvic(5, 13, game, false));
		units.add(new Izuna(3, 11, game, true));
		units.add(new Syrilith(7, 12, game, false));
		units.add(new FlameKeeper(3, 13, game, true));
		*/
		/* big map */
		/*units.add(new MobileMage(5, 16, game, true));
		units.add(new Anira(9, 16, game, false));
		units.add(new Epsilon(5, 18, game, true));
		units.add(new FlameKeeper(9, 18, game, false));
		units.add(new Tybalt(7, 16, game, true));
		units.add(new Syrilith(11, 17, game, false));
		units.add(new Malaki(7, 18, game, true));*/
		/* small map*/
        units.add(new CrystalSniper(1, 15, game, true));
        units.add(new Vynn(3, 15, game, true));
        units.add(new Zaniek(1, 17, game, true));
        units.add(new FlameKeeper(3, 17, game, true));
        units.add(new Norwin(4, 15, game, false));
        units.add(new Syrilith(6, 15, game, false));
        units.add(new Malaki(4, 17, game, false));
        units.add(new Izuna(6, 17, game, false));

		/* lvl 1 */
		/*
		monsters.add(new Monsters.Guardian(9, 10, game, true));
		monsters.add(new Monsters.Guardian(38, 13, game, false));
		*/
		/* big map */
		/*monsters.add(new Monsters.Guardian(14, 20, game, true));
		monsters.add(new Monsters.Guardian(48, 20, game, false));
		monsters.add(new Monsters.Guardian(16, 14, game, true));
		monsters.add(new Monsters.Guardian(46, 14, game, false));*/

		Unit.updateVisibility();
		Unit.updatePosition();

		Image ground = new Image(new Texture(Gdx.files.internal("Ground.png"))){
			@Override
			public void draw(Batch batch, float parentAlpha) {
				super.draw(batch, parentAlpha);
				Unit.visionSpr.setScale(48f, 48f);
				Unit.visionSpr.setAlpha(0.5f);
				Unit.visionSpr.draw(batch);
			}
		};
		ground.setZIndex(0);
		game.addActor(ground);
		game.getActors().get(game.getActors().size - 1).setPosition(8, 0);
		game.addActor(new Tile());

		Image treebase = new Image(new Texture(Gdx.files.internal("Stages/NewMapStage.png"))){
			@Override
			public Actor hit(float x, float y, boolean touchable) {
				return null;
			}
		};
		treebase.moveBy(8, 0);
		treebase.setZIndex(5);
		game.addActor(treebase);

		for (Unit unit : units) {
			unitGroup.addActor(unit);
		}
		game.addActor(unitGroup);

		Image treetop = new Image(new Texture(Gdx.files.internal("Stages/NewMapTreetop.png"))){
			@Override
			public Actor hit(float x, float y, boolean touchable) {
				return null;
			}
		};
		treetop.moveBy(9, 48);
		treetop.setZIndex(6);
		game.addActor(treetop);

		Unit.dmgPopup = new Label("0", combatSkin, "combatMenu") {
			@Override
			public void setText(CharSequence newText) {
				super.setText(newText);
				setVisible(true);
				MoveByAction moveByAction = new MoveByAction() {
					private int frames = 0;

					@Override
					public boolean act(float delta) {
						//wrapper.setTransform(true);
						Unit.dmgPopup.moveBy(0, 2.5f - (frames / 5f));
						//wrapper.setScale(10f);
						//wrapper.setTransform(false);
						frames++;
						return super.act(delta);
					}
				};
				moveByAction.setAmount(35, 0);
				moveByAction.setDuration(0.4f);
				addAction(Actions.sequence(moveByAction, Actions.run(() -> setVisible(false))));
				//wrapper.addAction(Actions.sequence(moveByAction, Actions.run(() -> Unit.dmgPopup.setVisible(false))));
			}
		};
		Unit.dmgPopup.setVisible(false);
		game.addActor(Unit.dmgPopup);

		Table table = new Table();
		table.setWidth(menu.getWidth());
		table.align(Align.topLeft);
		table.setVisible(false);
		table.add(new MenuOption("Name", combatSkin, "combatMenu"));
		table.row();
		table.add(new MenuOption("Move", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Unit un = units.get(Unit.sel);
				if(!un.getMoveable()) {
					Unit.path = null;
					Tile.floorMapInit = false;
					MyGdxGame.this.mouseMoved(Gdx.input.getX(), Gdx.input.getY());
					un.setDoing("Move", false);
				}
			}
		});
		table.row();
		table.add(new MenuOption("Attack", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Unit un = units.get(Unit.sel);
				un.setDoing("Attack", false);
			}
		});
		table.row();
		table.add(new MenuOption("Abilities", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Unit un = units.get(Unit.sel);
				if(un.getAbMenu().getChildren().size > 0)
					un.openAbMenu();
			}
		});
		table.row();
		table.add(new MenuOption("Items", combatSkin, "combatMenu") {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Unit un = units.get(Unit.sel);
				table.setVisible(false);

				un.itemMenu.setPosition(un.getX() + un.getWidth(), un.getY() + un.getHeight());
				un.itemMenu.setVisible(true);
				un.setDoing("Stand", false);

				for(int i = 0; i < un.itemMenu.getChildren().size; i++)
					un.itemMenu.getChildren().get(i).setColor(Color.WHITE);
			}
		});
		table.setName("CombatMenu");
		table.setZIndex(8);
		game.addActor(table);

		for (Unit unit1 : units) {
			game.addActor(unit1.getAbMenu());
			game.addActor(unit1.itemMenu);
		}
		game.addActor(Unit.shop);

		menu.clear();
		curStage = game;

		InputMultiplexer im = new InputMultiplexer(curStage, MyGdxGame.this);
		Gdx.input.setInputProcessor(im);
	}

	@Override
	public boolean keyDown(int keycode) {
		MenuOption ab;
		if(curStage == game)
			switch(keycode) {
				case Keys.ENTER:
					if(!turnSwap) {
						turnSwap = true;
						monsterSel = 0;
						if(monsters.size() > 0)
						    monsters.get(monsterSel).onTurnSwap();
					}
					return true;
				case Keys.NUM_1:
					if(Unit.sel != -1 && units.get(Unit.sel).getAbMenu().getChildren().size > 1) {
						ab = (MenuOption)units.get(Unit.sel).getAbMenu().getChildren().get(1);
						ab.clicked(null, 0, 0);
						return true;
					} else {
						return false;
					}
				case Keys.NUM_2:
					if(Unit.sel != -1 && units.get(Unit.sel).getAbMenu().getChildren().size > 2) {
						ab = (MenuOption)units.get(Unit.sel).getAbMenu().getChildren().get(2);
						ab.clicked(null, 0, 0);
						return true;
					} else {
						return false;
					}
				case Keys.NUM_3:
					if(Unit.sel != -1 && units.get(Unit.sel).getAbMenu().getChildren().size > 3) {
						ab = (MenuOption)units.get(Unit.sel).getAbMenu().getChildren().get(3);
						ab.clicked(null, 0, 0);
						return true;
					} else {
						return false;
					}
				case Keys.NUM_4:
					if(Unit.sel != -1 && units.get(Unit.sel).getAbMenu().getChildren().size > 4) {
						ab = (MenuOption)units.get(Unit.sel).getAbMenu().getChildren().get(4);
						ab.clicked(null, 0, 0);
						return true;
					} else {
						return false;
					}
				case Keys.BACKSPACE:
					if(Tile.selTile && Unit.sel != -1) {
						units.get(Unit.sel).setDoing("Stand", true);
						return true;
					}
					else
						return false;
				case Keys.SHIFT_LEFT:
				case Keys.SHIFT_RIGHT:
					if(Unit.sel != -1) {
						units.get(Unit.sel).setShiftDown(true);
						return true;
					}
					return false;
				case Keys.UP:
					camMove = 1;
					return true;
				case Keys.DOWN:
					camMove = 2;
					return true;
				case Keys.LEFT:
					camMove = 3;
					return true;
				case Keys.RIGHT:
					camMove = 4;
					return true;
				default:
					return false;
			}
		else
			switch (keycode) {
				case Keys.ESCAPE:
					Array<Actor> actors = curStage.getActors();
					for(Actor a : actors)
						if(a instanceof HeroPage)
							a.remove();
					return true;
				default:
					return false;
			}
	}

	@Override
	public boolean keyUp(int keycode) {
		if(curStage == game)
			switch(keycode) {
				case Keys.SHIFT_LEFT:
				case Keys.SHIFT_RIGHT:
					if(Unit.sel != -1) {
						units.get(Unit.sel).setShiftDown(false);
						return true;
					}
				case Keys.UP:
				case Keys.DOWN:
				case Keys.LEFT:
				case Keys.RIGHT:
					camMove = 0;
					return true;
				default:
					return false;
			}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(curStage.equals(game)) {
			Tile.handleClick(screenX, screenY);
			return true;
		} else
			return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		int sX = (int)(screenX / (Gdx.graphics.getWidth() / 640f)) - (int)game.getRoot().getX();
		int sY = (int)((Gdx.graphics.getHeight() - screenY) / (Gdx.graphics.getHeight() / 480f)) - (int)game.getRoot().getY();
		if(Tile.drawPath && sX >= 0 && sX <= STAGE_WIDTH && sY >= 0 && sY <= STAGE_HEIGHT && Unit.sel > -1) {
			int cX = (int)Math.floor((sX - 8) / 48);
			int cY = (int)Math.floor(sY / 48);
			byte uX = (byte)Math.floor((units.get(Unit.sel).getX() - 8) / 48);
			byte uY = (byte)Math.floor(units.get(Unit.sel).getY() / 48);
			int ms = units.get(Unit.sel).getMspeed();
			boolean pathFound = false;
			int steps = 0;
			ArrayList<Tile.Path> oldPaths = new ArrayList<>();
			ArrayList<Tile.Path> newPaths = new ArrayList<>();
			boolean[][] alreadyTravelled = new boolean[ms * 2 + 1][ms * 2 + 1];
			oldPaths.add(new Tile.Path(uX, uY));
			
			if((cX != oldPathCoord[0] || cY != oldPathCoord[1]) &&
					Tile.walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE) && Tile.hit) {
				oldPathCoord[0] = cX;
				oldPathCoord[1] = cY;

				if(Unit.path != null && Unit.path.getLength() <= ms) {
					Tile.Path oldPath = Unit.path;
					int piX = oldPath.last()[0];
					int piY = oldPath.last()[1];

					boolean[] unitLoc = new boolean[4];

					if(Tile.walls.getPixel(piX + 1, piY) != Color.rgba8888(Color.WHITE) && !unitLoc[0])
						newPaths.add(new Tile.Path(oldPath, 1, 0));

					if(Tile.walls.getPixel(piX - 1, piY) != Color.rgba8888(Color.WHITE) && !unitLoc[2])
						newPaths.add(new Tile.Path(oldPath, -1, 0));

					if(Tile.walls.getPixel(piX, piY + 1) != Color.rgba8888(Color.WHITE) && !unitLoc[1])
						newPaths.add(new Tile.Path(oldPath, 0, 1));

					if(Tile.walls.getPixel(piX, piY - 1) != Color.rgba8888(Color.WHITE) && !unitLoc[3])
						newPaths.add(new Tile.Path(oldPath, 0, -1));

					while(newPaths.size() > 0 && !pathFound) {
						Tile.Path newPath = newPaths.get(0);
						if(newPath.last()[0] == cX && newPath.last()[1] == cY) {
							pathFound = true;
							int len = 0;
							for(int i = 0; i < oldPath.getLength(); i++)
								if((oldPath.getSegment(i)[0] == newPath.last()[0] && oldPath.getSegment(i)[1] == newPath.last()[1])) {
									pathFound = false;
									len = i;
								}
							if(pathFound)
								Unit.path = newPath;
							else {
								Unit.path = new Tile.Path(newPath, len);
							}
						}

						newPaths.remove(newPath);
					}
				}

				if(!pathFound) {
					Unit.path = null;
					newPaths.clear();

					while(!pathFound && steps < ms) {
						while(oldPaths.size() > 0) {
							Tile.Path oldPath = oldPaths.get(0);
							int piX = oldPath.last()[0];
							int piY = oldPath.last()[1];
							
							boolean[] unitLoc = new boolean[4];

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
				}

				if(Unit.path != null && Unit.path.last()[0] == uX && Unit.path.last()[1] == uY)
					Unit.path = new Tile.Path(uX, uY);
			}
		}

		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		scrollAmt += amount / 64f;
		if(scrollAmt > MAX_SCROLL)
			scrollAmt = MAX_SCROLL;
		if(scrollAmt < MIN_SCROLL)
			scrollAmt = MIN_SCROLL;
		game.getRoot().setOrigin(game.getRoot().getX() + game.getRoot().getWidth(),
				game.getRoot().getY() + game.getRoot().getHeight());
		game.getRoot().setScale(scrollAmt);
		return true;
	}
}
