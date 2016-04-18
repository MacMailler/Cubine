package net.disya.voxen.render;

import net.disya.voxen.terrain.block.Block;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.chunk.Chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by nicklaslof on 31/01/15.
 */
public interface BlockRender {

    boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block, FloatArray vertices, ShortArray indicies);
}
