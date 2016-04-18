package net.disya.voxen.game;

import net.disya.voxen.game.screen.GameScreen;

import com.badlogic.gdx.Game;

public class VoxelGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
