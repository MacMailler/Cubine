package su.gwg.voxelengine.terrain.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import java.util.Collection;

/**
 * Created by nicklas on 5/8/14.
 */
public interface IChunkProvider {
    public Chunk getChunkAt(int x, int y, int z);
    public Chunk getChunkAt(long position);

    public ArrayMap.Values<Chunk> getAllChunks();

    void createChunk(Vector3 worldPosition, int x, int z);
}
