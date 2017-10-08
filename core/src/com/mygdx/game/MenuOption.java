package com.mygdx.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MenuOption extends TextButton {

	public MenuOption(String text, Skin skin, String styleName) {
		super(text, skin, styleName);
		addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				MenuOption.this.enter(event, x, y, pointer, fromActor);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				MenuOption.this.exit(event, x, y, pointer, toActor);
			}

			@Override
			public void clicked(InputEvent event, float x, float y) {
				MenuOption.this.clicked(event, x, y);
			}
		});
		if(getPrefWidth() > 150)
			getLabel().setFontScale(140 / getPrefWidth()); // If text is too big, scale it down
		pad(0, (150-getPrefWidth())/2, 0, (150-getPrefWidth())/2);
	}

	@Override
	public void setText(String text) {
		pad(0, 0, 0, 0);
		super.setText(text);
		//pad(0, (150-getPrefWidth())/2-4, 0, (150-getPrefWidth())/2-4);
	}

	public void clicked(InputEvent event, float x, float y) {
		
	}
	
	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		
	}
	
	public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		
	}
	
}
