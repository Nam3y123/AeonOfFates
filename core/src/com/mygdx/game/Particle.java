package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Particle {
	protected Sprite spr;
	protected float duration;
	private String tag;
	
	public Particle(TextureRegion tex, int x, int y) {
		spr = new Sprite(new TextureRegion(tex));
		spr.setPosition(x, y);
		duration = 0;
		tag = "";
		start();
	}

	public Particle(TextureRegion tex, int x, int y, String parTag) {
		spr = new Sprite(new TextureRegion(tex));
		spr.setPosition(x, y);
		duration = 0;
		tag = parTag;
		start();
	}
	
	public void start() {
		
	}
	
	public void display(Batch batch, float parentAlpha) {
		spr.draw(batch);
	}
	
	public Sprite getSpr() {
		return spr;
	}
	
	public void increment() {
		duration++;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public void remove() {

	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
