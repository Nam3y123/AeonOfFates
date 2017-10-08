package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import static com.badlogic.gdx.Gdx.graphics;
import static com.badlogic.gdx.Gdx.input;

public class Tile extends Actor {
	private static Sprite tileSpr, staticSpr;
	private static TextureRegion tileRegion;
	private static boolean animate;
	public static boolean clicked, hit;
	private static ArrayList<Unit> units;
	public static int clickX, clickY;
	public static Pixmap walls;
	private static boolean[][] floorMap;
	public static boolean floorMapInit;
	public static boolean drawPath;
	public static boolean selTile;
	public static Stage game;
	
	public Tile() {
		super();
		setZIndex(1);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		for(int i = 0; i < units.size(); i++) {
			Unit un = units.get(i);
			if(i == Unit.sel)
				selTile = un.drawTile(batch, parentAlpha);
			else
				un.drawTile(batch, parentAlpha);
			for(int j = 0; j < un.getParticles().size(); j++) {
				un.getParticles().get(j).display(batch, parentAlpha);
			}
		}
	}
	
	public static void Init( ArrayList<Unit> units) {
		tileRegion = new TextureRegion(new Texture(Gdx.files.internal("Tile.png")), 0, 48, 46, 46);
		tileSpr = new Sprite(tileRegion);
		staticSpr = new Sprite(tileRegion);
		walls = new Pixmap(Gdx.files.internal("Stages/NewMap.bmp"));
		animate = false;
		hit = false;
		drawPath = false;
		Tile.units = units;
		floorMapInit = false;
	}
	
	public static void disp(Batch batch, float x, float y, Color col) {
		if(x >= 8 && x < 3064) {
			tileSpr.setColor(col);
			tileSpr.setAlpha(0.5f);
			tileSpr.setPosition(x + 2, y);
			tileSpr.draw(batch);
			
			staticSpr.setColor(col);
			staticSpr.setAlpha(0.5f);
			staticSpr.setPosition(x + 2, y);
			staticSpr.draw(batch);
		}
	}
	
	public static void drawShape(Batch batch, float parentAlpha, float x, float y, String shape,  int range) {
		switch(shape) {
			case "diamond":
				for(int xi = range; xi >= -range; xi--) {
					for(int yi = range; yi >= -range; yi--) {
						int cX = (int)Math.floor((x - 8) / 48) + xi;
						int cY = (int)Math.floor(y / 48) + yi;
						if(Math.abs(xi) + Math.abs(yi) <= range && walls.getPixel(cX, cY) != Color.rgba8888(Color.WHITE))
							disp(batch, x + xi * 48, y + yi * 48, Color.BLUE);
					}
				}
				break;
			case "circle":
				for(int xi = range; xi >= -3; xi--) {
					for(int yi = range; yi >= -range; yi--) {
						if(Math.sqrt(Math.pow(xi, 2) + Math.pow(yi, 2)) <= range + 0.5f)
							disp(batch, x + xi * 48, y + yi * 48, Color.BLUE);
					}
				}
				break;
			case "line":
				for(int xi = range; xi >= -range; xi--) {
					if(xi != 0)
						disp(batch, x + xi * 48, y, Color.BLUE);
				}
				for(int yi = range; yi >= -range; yi--) {
					if(yi != 0)
						disp(batch, x, y + yi * 48, Color.BLUE);
				}
				break;
			case "none":
			default:
				break;
		}
		if(animate) {
			tileRegion.setRegionX(tileRegion.getRegionX() + 1);
			tileRegion.setRegionY(tileRegion.getRegionY() - 1);
			if(tileRegion.getRegionX() == 49)
			{
				tileRegion.setRegionX(2);
				tileRegion.setRegionY(46);
			}
			tileRegion.setRegionWidth(46);
			tileRegion.setRegionHeight(46);
			tileSpr.setRegion(tileRegion);
			animate = false;
		} else {
			animate = true;
		}
	}
	
	public static void drawShape(Batch batch, float parentAlpha, float x, float y, String shape,  int range,
	 Color col, boolean clickable) {
		hit = false;
		drawPath = false;
		switch(shape) {
			case "diamond":
				for(int xi = range; xi >= -range; xi--) {
					for(int yi = range; yi >= -range; yi--) {
						if(Math.abs(xi) + Math.abs(yi) <= range) {
							disp(batch, x + xi * 48, y + yi * 48, col);
							if(clickX > x + xi * 48 - 2 && clickX < x + (xi * 48) + 48 &&
							 clickY > y + yi * 48 - 2 && clickY < y + (yi * 48) + 48)
								hit = true;
						}
					}
				}
				break;
			case "moveDiamond":
				boolean[][] newFloor = new boolean[range * 2 + 1][range * 2 + 1];
				newFloor[range][range] = true;
				if(!floorMapInit || (floorMap.length - 1) / 2 != range) {
					int cX = (int)Math.floor((x - 8) / 48);
					int cY = (int)Math.floor(y / 48);
					floorMap = null;
					floorMap = new boolean[range * 2 + 1][range * 2 + 1];
					floorMap[range][range] = true;
					floorMapInit = true;
					for(int i = 0; i < range; i++) {
						for(int xi = 0; xi < range * 2 + 1; xi++) {
							for(int yi = 0; yi < range * 2 + 1; yi++) {
								if(floorMap[xi][yi]) {
									int piX = xi - range + cX;
									int piY = yi - range + cY;
									boolean[] unitLoc = new boolean[4];
									if(walls.getPixel(piX + 1, piY) != Color.rgba8888(Color.WHITE) && !unitLoc[0])
										newFloor[xi + 1][yi] = true;
									if(walls.getPixel(piX, piY + 1) != Color.rgba8888(Color.WHITE) && !unitLoc[1])
										newFloor[xi][yi + 1] = true;
									if(walls.getPixel(piX - 1, piY) != Color.rgba8888(Color.WHITE) && !unitLoc[2])
										newFloor[xi - 1][yi] = true;
									if(walls.getPixel(piX, piY - 1) != Color.rgba8888(Color.WHITE) && !unitLoc[3])
										newFloor[xi][yi- 1] = true;
								}
							}
						}
						for(int xi = 0; xi < range * 2 + 1; xi++) {
							for(int yi = 0; yi < range * 2 + 1; yi++) {
								if(newFloor[xi][yi]) {
									int locX = cX + xi - range;
									int locY = cY + yi - range;
									boolean viableTile = true;
									for(StatusEffect effect : units.get(Unit.sel).getStatus())
										if(effect instanceof StatusEffect.MoveRule && viableTile)
											viableTile = ((StatusEffect.MoveRule)effect).viableTile(units.get(Unit.sel), locX, locY);
									if(viableTile)
										floorMap[xi][yi] = true;
								}
							}
						}
					}
				}
				for(int xi = range; xi >= -range; xi--) {
					for(int yi = range; yi >= -range; yi--) {
						if(floorMap[xi + range][yi + range]) {
							disp(batch, x + xi * 48, y + yi * 48, col);
							Stage game = units.get(0).game;
							Tile.setClickPos();
							if (clickX > x + xi * 48 - 2 && clickX < x + (xi * 48) + 47 &&
									clickY > y + yi * 48 - 2 && clickY < y + (yi * 48) + 48)
								hit = true;
							drawPath = true;
						}
					}
				}
				break;
			case "circle":
				for(int xi = range; xi >= -range; xi--) {
					for(int yi = range; yi >= -range; yi--) {
						if(Math.sqrt(Math.pow(xi, 2) + Math.pow(yi, 2)) <= range + 0.5f) {
							disp(batch, x + xi * 48, y + yi * 48, col);
							if(clickX > x + xi * 48 - 1 && clickX < x + (xi * 48) + 47
									&& clickY > y + yi * 48 - 2 && clickY < y + (yi * 48) + 48)
								hit = true;
						}
					}
				}
				break;
			case "line":
				for(int xi = range; xi >= -range; xi--) {
					if(xi != 0) {
						disp(batch, x + xi * 48, y, col);
						if(clickX > x + xi * 48 - 1 && clickX < x + (xi * 48) + 47 &&
						 clickY > y - 1 && clickY < y + 48)
							hit = true;
					}
				}
				for(int yi = range; yi >= -range; yi--) {
					if(yi != 0) {
						disp(batch, x, y + yi * 48, col);
						if(clickX > x - 2 && clickX < x + 47 &&
						 clickY > y + yi * 48 - 1 && clickY < y + (yi * 48) + 48)
							hit = true;
					}
				}
				break;
			case "none":
				hit = false;
			default:
				break;
		}
		if(clickable) {
			if(animate) {
				tileRegion.setRegionX(tileRegion.getRegionX() + 1);
				tileRegion.setRegionY(tileRegion.getRegionY() - 1);
				if(tileRegion.getRegionX() == 49)
				{
					tileRegion.setRegionX(2);
					tileRegion.setRegionY(46);
				}
				tileRegion.setRegionWidth(46);
				tileRegion.setRegionHeight(46);
				tileSpr.setRegion(tileRegion);
				animate = false;
			} else {
				animate = true;
			}
		}
	}
	
	public static void drawRect(Batch batch, float parentAlpha, int x, int y, int w, int h, Color col){
		for(int ix = x; ix < x + 48 * w; ix +=48)
			for(int iy = y; iy < y + 48 * h; iy +=48)
				Tile.disp(batch, ix, iy, col);
	}

	public static void drawRect(Batch batch, float parentAlpha, int x, int y, int w, int h, Color col, boolean clickable) {
		hit = false;
		Tile.setClickPos();
		for (int ix = x; ix < x + 48 * w; ix += 48)
			for (int iy = y; iy < y + 48 * h; iy += 48) {
				disp(batch, ix, iy, col);
				if (clickX > ix && clickX < ix + 48
						&& clickY > iy && clickY < iy + 48)
					hit = true;
			}
	}

	public static void drawRing(Batch batch, float parentAlpha, float x, float y, int radius, int holeRadius, Color col, boolean clickable) {
		if(clickable) {
			Tile.setClickPos();
			hit = false;
		}
		for(int xi = radius; xi >= -radius; xi--) {
			for(int yi = radius; yi >= -radius; yi--) {
				float dist = (float)Math.sqrt(Math.pow(xi, 2) + Math.pow(yi, 2));
				if(dist <= radius + 0.5f && dist > holeRadius + 0.5f) {
					disp(batch, x + xi * 48, y + yi * 48, col);
					if(clickable && clickX > x + xi * 48 - 1 && clickX < x + (xi * 48) + 48
							&& clickY > y + yi * 48 - 2 && clickY < y + (yi * 48) + 48)
						hit = true;
				}
			}
		}
	}
	
	public static void handleClick(int screenX, int screenY) {
		clicked = true;
		setClickPos();
	}

	public static void setClickPos() {
		Stage game = units.get(0).game;
		clickX = (int) (input.getX() / (graphics.getWidth() / 640f)) - (int) game.getRoot().getX();
		clickY = (int) ((graphics.getHeight() - input.getY()) / (graphics.getHeight() / 480f)) - (int) game.getRoot().getY();
	}
	
	
	public static class Path {
		private int[][] sequence;

		public Path(int startX, int startY) {
			sequence = new int[1][2];
			sequence[0][0] = startX;
			sequence[0][1] = startY;
		}
		
		public Path(Path parent, int x, int y) {
			sequence = new int[parent.getLength() + 1][2];
			for(int i = 0; i < parent.getLength(); i++) {
				sequence[i][0] = parent.getSegment(i)[0];
				sequence[i][1] = parent.getSegment(i)[1];
			}
			sequence[parent.getLength()][0] = parent.last()[0] + x;
			sequence[parent.getLength()][1] = parent.last()[1] + y;
		}

		public Path(Path parent, int len) {
			sequence = new int[len][2];
			for(int i = 0; i < len - 1; i++) {
				sequence[i][0] = parent.getSegment(i)[0];
				sequence[i][1] = parent.getSegment(i)[1];
			}
		}
		
		public int[] getSegment(int segment) {
			return sequence[segment];
		}
		
		public int getLength() {
			return sequence.length;
		}
		
		public int[] last() {
			if(sequence.length > 0)
				return sequence[sequence.length - 1];
			else
				return new int[] {-1,-1};
		}
	}
}
