package com.mygdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Timer;

public class Projectile extends Actor {
	protected boolean team;
	protected float direction;
	public static ArrayList<Unit> units;
	protected Sprite spr;
	protected float speed;
	protected Unit sourceUn;
	protected int duration;
	protected boolean teamDef;
	protected ArrayList<Unit> hitUnits;

	public Projectile(Unit sourceUn, float direction, float x, float y, TextureRegion particle, float speed, float range, float size) {
		this.direction = direction;
		spr = new Sprite(particle);
		spr.setSize(size, size);
		setBounds(x, y, spr.getWidth(), spr.getHeight());
		this.speed = speed;
		this.sourceUn = sourceUn;
		teamDef = false;
		duration = (int)(speed * range);
		hitUnits = new ArrayList<>();
	}
	
	public Projectile(Unit sourceUn, boolean team, float direction, float x, float y, TextureRegion particle, float speed, float range) {
		this.team = team;
		this.direction = direction;
		spr = new Sprite(particle);
		setBounds(x, y, spr.getWidth(), spr.getHeight());
		this.speed = speed;
		this.sourceUn = sourceUn;
		teamDef = true;
		duration = (int)(speed * range);
		hitUnits = new ArrayList<>();
	}
	
	@Override
	public void act(float delta) {
		float move = 48 / speed;
		this.moveBy((float)Math.cos(Math.toRadians(direction))*move, (float)Math.sin(Math.toRadians(direction))*move);
		spr.setPosition(getX(), getY());
		for (Unit un : units) {
			if (teamDef) {
				if (un.getTeam() != team && un.getTargetable() && un.getX() + 24 < getX() + getWidth() && un.getX() + 24 > getX()
						&& un.getY() + 1 < getY() + getHeight() && un.getY() + 48 > getY() && !hitUnits.contains(un)) {
					effect(un);
					hitUnits.add(un);
				}
			} else {
				if (un != sourceUn && un.getX() < getX() + getWidth() && un.getX() + 48 > getX()
						&& un.getY() < getY() + getHeight() && un.getY() + 48 > getY() && !hitUnits.contains(un)) {
					effect(un);
					hitUnits.add(un);
				}
			}
		}
		duration--;
		if(duration == 0)
			remove();
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		spr.setRotation(getRotation());
		spr.draw(batch);
	}
	
	@Override
	public boolean remove() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                sourceUn.setDoing("Stand", true);
            }
        }, 0.2f);
		return super.remove();
	}
	
	public float getDirection() {
		return direction;
	}
	
	public void effect(Unit un) {
		
	}

	public Unit getSourceUn() {
	    return sourceUn;
    }
}
