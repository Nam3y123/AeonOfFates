package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;

public class ItemMenu extends Actor implements Disposable {
    private Sprite spr;
    private boolean display;
    private Unit sel;
    private Sprite itemIcons;
    private Label label;
    private TextButton popup;
    private short[][] itemNums;

    public ItemMenu() {
        super();
        spr = new Sprite(new Texture(Gdx.files.internal("ItemMenu.png")));
        itemIcons = new Sprite(new TextureRegion(Item.SPRITESHEET));
        Unit.combatSkin = new Skin(Gdx.files.internal("CombatSkin.json"));
        label = new Label("Testeringos", Unit.combatSkin, "combatMenu");
        popup = new TextButton("TestText1,\nTestText2,\nTestText3", Unit.combatSkin, "combatMenu");
        itemNums = new short[5][3];

        popup.pad(0, 30, 0, 30);
        spr.setPosition(40, 30);
        setBounds(spr.getX(), spr.getY(), spr.getWidth(), spr.getHeight());
        setVisible(false);
        display = false;
        addCaptureListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return onClick(x, y, button);
            }
        });
    }

    public void draw(Batch batch) { // Custom draw method needed, or else health bars would be drawn on top of the shop
        if(display) {
            spr.draw(batch);

            int ITEM_ICON_SIZE = 64;
            itemIcons.setPosition(22, getHeight() - 40);
            for(int section = 0; section < sel.getRecItems().length; section++) {
                for(int i = 0; i < sel.getRecItems()[section].length; i++) {
                    if(!sel.getRecItems()[section][i].equals("")) {
                        int itemId = Integer.parseInt(sel.getRecItems()[section][i]);
                        itemIcons.setRegion((itemId % 12) * ITEM_ICON_SIZE, (int)Math.floor(itemId / 12f) * ITEM_ICON_SIZE,
                                ITEM_ICON_SIZE, ITEM_ICON_SIZE);
                        itemIcons.setSize(32, 32);
                        itemIcons.translate(40, 0);
                        itemIcons.draw(batch);
                    }
                }
                itemIcons.translate(12, 0);
            }

            label.setText(sel.name);
            label.setPosition(spr.getX() + 18, spr.getY() + 315);
            label.draw(batch, 1f);
            label.setText(sel.holdWeight + "/15");
            label.setPosition(spr.getX() + 38, spr.getY() + 292);
            label.draw(batch, 1f);
            label.setText(Integer.toString(Unit.teamMoney[sel.getTeam() ? 0 : 1]));
            label.setPosition(spr.getX() + 98, spr.getY() + 292);
            label.draw(batch, 1f);

            for(int i = 2; i < sel.itemMenu.getChildren().size; i++) {
                Actor a = sel.itemMenu.getChildren().get(i);
                float buttonX = a.getX(), buttonY = a.getY();
                a.setPosition(spr.getX() + ((176 - a.getWidth()) / 2), spr.getY() + 283 - ((i - 1) * a.getHeight()));
                a.setVisible(false);
                a.draw(batch, 1f);
                a.setVisible(true);
                a.setPosition(buttonX, buttonY);
            }

            //popup.draw(batch, 1f);
        }
    }

    @Override
    public void act(float delta) {
        setPosition(-getParent().getX() + spr.getX(), -getParent().getY() + spr.getY());
        popup.setPosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY() - popup.getHeight());
    }

    public void setDisplay(boolean display) {
        this.display = display;
        setVisible(display);
        if(display)
            sel = Unit.units.get(Unit.sel);
    }

    public boolean onClick(float x, float y, int button) {
        if(x > spr.getWidth() - 33 && x < spr.getWidth() - 14 && y > spr.getHeight() - 30 && y < spr.getHeight() - 12)
            setDisplay(false);
        return true;
    }

    @Override
    public void dispose() {
        spr.getTexture().dispose();
        itemIcons.getTexture().dispose();
    }
}
