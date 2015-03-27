package su.gwg.voxelengine.game.terrain.block;

import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.terrain.block.IBlockProvider;
import su.gwg.voxelengine.terrain.chunk.Chunk;

/**
 * Created by nicklas on 5/2/14.
 */
public class LightBlock extends Block {
    protected LightBlock(byte id, String textureRegion) {
        super(id, textureRegion,textureRegion,textureRegion);
    }

    @Override
    public boolean isLightSource() {
        return true;
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    protected boolean blockRenderSide(IBlockProvider blockProvider, Chunk chunk, int x, int y, int z, Side side) {
        byte blockAtSide = side.getBlockAt(chunk, x, y, z);
        if (blockAtSide == 0){
            return true;
        }
        if (blockProvider.getBlockById(blockAtSide) == this){
            return false;
        }

        return true;
    }
}
