package su.gwg.voxelengine.render;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.terrain.block.IBlockProvider;
import su.gwg.voxelengine.terrain.chunk.Chunk;

/**
 * Created by nicklaslof on 31/01/15.
 */
public interface BlockRender {

    boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block, FloatArray vertices, ShortArray indicies);
}
