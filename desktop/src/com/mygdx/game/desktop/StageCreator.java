package com.mygdx.game.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;

public class StageCreator {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        final String lvl = arg[0];

        new LwjglApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                super.create();
                Pixmap walls = new Pixmap(Gdx.files.internal("Stages/" + lvl +".bmp"));
                Pixmap treebase = new Pixmap(Gdx.files.internal("Temp/Treebase.png"));
                Pixmap treetop = new Pixmap(Gdx.files.internal("Temp/Treetop.png"));
                Pixmap stage = new Pixmap(walls.getWidth() * 48, walls.getHeight() * 48, Pixmap.Format.RGBA8888);
                Pixmap top = new Pixmap(walls.getWidth() * 48, walls.getHeight() * 48, Pixmap.Format.RGBA8888);

                for(int ix = 0; ix < walls.getWidth(); ix++)
                    for(int iy = 0; iy < walls.getWidth(); iy++) {
                        if(walls.getPixel(ix, walls.getHeight() - iy - 1) == Color.rgba8888(Color.WHITE)) {
                            stage.drawPixmap(treebase, ix * 48, iy * 48);
                            top.drawPixmap(treetop, ix * 48, iy * 48);
                        }
                    }

                FileHandle stageOut = Gdx.files.local("Stages/" + lvl + "Stage.png");
                PixmapIO.writePNG(stageOut, stage);
                //stageOut.writeBytes(stage.getPixels().array(), false);
                FileHandle topOut = Gdx.files.local("Stages/" + lvl + "Treetop.png");
                PixmapIO.writePNG(topOut, top);
                //topOut.writeBytes(top.getPixels().array(), false);

                walls.dispose();
                treebase.dispose();
                treetop.dispose();
                stage.dispose();
                top.dispose();
                Gdx.app.log("Success", "Stage successfully created!");
                Gdx.app.exit();
            }
        }, config);
    }
}
