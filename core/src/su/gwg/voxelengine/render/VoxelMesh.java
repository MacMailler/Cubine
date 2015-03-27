package su.gwg.voxelengine.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.terrain.block.IBlockProvider;
import su.gwg.voxelengine.terrain.chunk.Chunk;

/**
 * Created by nicklas on 4/24/14.
 */
public class VoxelMesh extends BoxMesh {


    public boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block) {
        if (block.getId() == 0) {
            return true;
        }

        // In case that we are rebuilding the mesh we syncronize which will make this call to wait for the rebuild to finish
        // before modifying it.
        synchronized (rebuilding) {
            setupMesh();
            if (transform == null) {
                transform = new Matrix4().setTranslation(worldPosition);
                transformWithRealY = transform.cpy().translate(0, y, 0);
            }
            if (block.isPlayerCollidable()) {
                return block.getBlockRender().addBlock(worldPosition, x, y, z, blockProvider, chunk, block, vertices, indicies);
            }else{
                return block.getBlockRender().addBlock(worldPosition, x, y, z, blockProvider, chunk, block, nonColliadableVertices, nonColliadableIndicies);
            }
        }

    }

}
