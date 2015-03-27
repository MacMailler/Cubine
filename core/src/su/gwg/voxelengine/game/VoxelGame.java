package su.gwg.voxelengine.game;

import com.badlogic.gdx.Game;
import su.gwg.voxelengine.game.screen.GameScreen;

public class VoxelGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
