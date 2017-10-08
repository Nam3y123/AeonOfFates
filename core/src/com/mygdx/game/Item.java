package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;

import java.io.*;
import java.util.HashMap;

// Note: "/--" equals the end of each section
// Note: Store items on the server on release! No hacks allowed!
/*
Item File Setup:

Weight
Sell gold
Stats...
 */

public class Item {
    private HashMap<String, Integer> stats;
    private int sellGold;
    private short itemId;
    private short active;
    private int weight;
    private TextureRegion icon;
    private String name;
    private MenuOption option;

    public final static TextureRegion SPRITESHEET = new TextureRegion(new Texture(Gdx.files.internal("ItemIcons.png")));

    public Item(short itemId) {
        this.itemId = itemId;
        String fileName = "Data/Items/item" + itemId + ".item";
        String fileSplit;
        String[] segments;
        stats = new HashMap<>();
        int ITEM_ICON_SIZE = 64;
        SPRITESHEET.setRegion((itemId % 12) * ITEM_ICON_SIZE, (int)Math.floor(itemId / 12f) * ITEM_ICON_SIZE,
                ITEM_ICON_SIZE, ITEM_ICON_SIZE);
        icon = new TextureRegion(SPRITESHEET);
        active = -1;
        try {
            String line;

            BufferedReader bufferedReader =
                    Gdx.files.internal(fileName).reader(8192);

            StringBuilder builder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            fileSplit = builder.toString();
            bufferedReader.close();

            // Sell gold & stats
            segments = fileSplit.split("\\s*\\r?\\n\\s*");
            name = segments[0];
            weight = Integer.parseInt(segments[1]);
            sellGold = Integer.parseInt(segments[2]);
            for(int i = 3; i < segments.length; i++) {
                String[] stat = segments[i].split(":");
                stats.put(stat[0], Integer.parseInt(stat[1]));
            }

        } catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
    }

    public void addStats(Unit buyer) {
        buyer.holdWeight += weight;
        for(String stat : stats.keySet())
            switch(stat) {
                case "atk":
                    buyer.atk += stats.get(stat);
                    break;
                case "mag":
                    buyer.mag += stats.get(stat);
                    break;
                case "def":
                    buyer.def += stats.get(stat);
                    break;
                case "hp":
                    buyer.maxHp += stats.get(stat);
                    buyer.hp += stats.get(stat);
                    break;
                case "passive":
                    getPassive(buyer, stats.get(stat));
                    break;
                case "active":
                    active = (short)((int)stats.get(stat)); // Wow this looks dumb
                    break;
            }
    }

    public void removeStats() {

    }

    public void onBuy(Unit buyer) {
        addStats(buyer);

        Sprite spr = new Sprite(icon);
        buyer.itemMenu.row();
        option = new MenuOption(name, Unit.combatSkin, "combatMenu") {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getActive(buyer);
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if(isVisible()) {
                    spr.setPosition(getX() + getWidth(), getY());
                    spr.draw(batch, parentAlpha);
                }
            }
        };
        final float ICON_SIZE = option.getHeight();
        spr.setSize(ICON_SIZE, ICON_SIZE);
        //option.pad(0, option.getPadLeft() - ICON_SIZE, 0, option.getPadRight() + ICON_SIZE);
        buyer.itemMenu.add(option);
    }

    public void onSell(Unit seller) {
        removeStats();
    }

    public TextureRegion getIcon() {
        return icon;
    }

    public void getActive(Unit user) {
        switch (active) {
            case 0:
                healthPotion(user, this);
                break;
            default:
                break;
        }
    }

    public void getPassive(Unit user, int passive) {
        switch (passive) {
            case 0:
                stalkerTalisman(user);
                break;
            case 1:
                slayerTalisman(user);
                break;
            default:
                break;
        }
    }


    //DIV: Actives
    private static void healthPotion(Unit user, Item item) {
        user.heal((int)(user.getMaxHp() * 0.1));
        user.items.remove(item);
        user.itemMenu.removeActor(item.option);
        user.holdWeight -= item.weight;
    }


    //DIV: Passives
    private static void stalkerTalisman(Unit user) {
        user.getStatus().add(new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source) {
                if(duration <= 1) {
                    user.setExp(user.getExp() + 2);
                    target.takeTrueDmg(5 + level);
                    user.heal(3 + level);
                    if(level < 3)
                        level++;
                    duration = 2;
                }
            }

            @Override
            public void end() {
                level = 0;
            }
        });
    }

    private static void slayerTalisman(Unit user) {
        user.getStatus().add(new StatusEffect("OnDamage") {
            @Override
            public void effect(Unit target, Unit source) {
                if(duration == 0) {
                    user.setExp(user.getExp() + 2);
                    user.getStatus().add(new StatusEffect("OnAlTurn") {

                        @Override
                        public void start() {
                            duration = 2;
                        }

                        @Override
                        public void effect(Unit target, Unit source) {
                            user.setExp(user.getExp() + 2);
                        }

                        @Override
                        public void end() {
                            user.getStatus().remove(this);
                        }


                    });
                    target.takeTrueDmg(18);
                    duration = 3;
                }
            }
        });
    }
}
