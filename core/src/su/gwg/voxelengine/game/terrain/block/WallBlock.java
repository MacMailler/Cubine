package su.gwg.voxelengine.game.terrain.block;

import su.gwg.voxelengine.terrain.block.Block;

/**
 * Created by nicklas on 5/7/14.
 */
public class WallBlock extends Block {
    protected WallBlock(byte id, String textureRegion) {
        super(id, textureRegion,textureRegion,textureRegion);
    }

    @Override
    public int getOpacity() {
        return 32;
    }
}
