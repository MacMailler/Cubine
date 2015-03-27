package su.gwg.voxelengine.game.terrain.block;

import com.badlogic.gdx.math.Vector2;
import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.game.terrain.block.render.StrawRender;

/**
 * Created by nicklaslof on 30/01/15.
 */
public class Straw extends Block {
    protected Straw(byte id, String topTextureRegion) {
        super(id, topTextureRegion, topTextureRegion, topTextureRegion);
        this.blockRender = new StrawRender();
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public boolean isPlayerCollidable() {
        return false;
    }
}
