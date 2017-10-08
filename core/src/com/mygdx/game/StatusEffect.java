package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Timer;

public class StatusEffect {
	String type;
	protected int duration;
	protected int level;
	private boolean visible;
	private Unit parent;
	
	public static StatusEffect snare, stun, mesmerize;
	
	public static void Init() {
		snare = new StatusEffect("OnAlTurn") {
            @Override
			public void effect(Unit target, Unit source) {
				target.setMoveable(false);
				target.getStatus().remove(this);
                target.movementImpared = true;
			}
		};
		stun = new StatusEffect("OnAlTurn") {
			@Override
			public void effect(Unit target, Unit source) {
				target.setStunned(true);
				target.getStatus().remove(this);
                target.movementImpared = true;
			}
		};
		mesmerize = new StatusEffect("OnDamaged") {
			@Override
			public void effect(Unit target, Unit source) {
				target.getStatus().remove(stun);
                target.movementImpared = false;
			}
		};
	}
	
	public static StatusEffect slow(int lvl) {
		StatusEffect slow = new StatusEffect("OnAlTurn") {
            @Override
			public void effect(Unit target, Unit source) {
				target.setMspeed(target.getMspeed() - this.level);
				target.getStatus().remove(this);
				target.movementImpared = true;
				
				StatusEffect slowEnd = new StatusEffect("OnEnTurn") {
					@Override
					public void effect(Unit target, Unit source) {
						target.setMspeed(target.getMspeed() + this.level);
						target.getStatus().remove(this);
                        target.movementImpared = false;
					}
				};
				slowEnd.setLevel(this.level);
				target.getStatus().add(slowEnd);
			}
		};
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                slow.parent.movementImpared = true;
            }
        }, Gdx.graphics.getDeltaTime());
		slow.setLevel(lvl);
		return slow;
	}
	
	public StatusEffect(String type) {
		this.type = type;
		visible = true;
		start();
	}
	
	public StatusEffect(String type, boolean visible) {
		this.type = type;
		this.visible = visible;
		start();
	}
	
	public String getType() {
		return type;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void changeDuration(int amount) {
		duration += amount;
	}

	public void setDuration(int value) { duration = value; }
	
	public void setLevel(int value) {
		level = value;
	}

	public int getLevel() { return level; }

	public void setParent(Unit parent) {
	    this.parent = parent;
    }
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	
	public void start() {
//		if(visible)
//			icon = new Texture(Gdx.files.internal("Icons/NoIcon.png"));
	}

	public void effect(Unit target, Unit source, int dmg) {
		effect(target, source);
	}
	
	public void effect(Unit target, Unit source) {
		
	}
	
	public void end() {
		
	}



	public static class MoveRule extends StatusEffect {
		public MoveRule() {
			super("MoveRule");
		}

		public boolean viableTile(Unit target, int tileX, int tileY) {
			return true;
		}
	}
}
