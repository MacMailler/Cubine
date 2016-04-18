package com.mygdx.game.desktop;

import net.disya.voxen.game.VoxelGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Created by MacMailler
 */

public class DesktopLauncher {
	public static final int DEFAULT_WINDOW_WIDTH = 1280;
	public static final int DEFAULT_WINDOW_HEIGTH = 720;
	
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = DEFAULT_WINDOW_WIDTH;
		config.height = DEFAULT_WINDOW_HEIGTH;
		config.useGL30 = false;
		config.vSyncEnabled = true;
        config.backgroundFPS = 0;
        config.foregroundFPS = 0;
		
		new LwjglApplication(new VoxelGame(), config);
	}
}
