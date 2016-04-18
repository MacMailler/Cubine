package net.disya.voxen.game.terrain.block.render;

import net.disya.voxen.terrain.block.Block;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.block.render.BasicBlockRender;
import net.disya.voxen.terrain.chunk.Chunk;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by nicklaslof on 01/02/15.
 */
public class StrawRender extends BasicBlockRender{

    @Override
    public synchronized boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block, FloatArray vertices, ShortArray indicies) {
        Vector2[] sidesTextureUVs = block.getSidesTextureUVs();
        points[0] = pointVector0.set(x, y, z + 0.2f);
        points[1] = pointVector1.set(x + 1.0f, y, z + 0.2f);
        points[2] = pointVector2.set(x + 1.0f, y + 1.0f, z + 0.2f);
        points[3] = pointVector3.set(x, y + 1.0f, z + 0.2f);
        points[4] = pointVector4.set(x + 1.0f, y, z+0.2f);
        points[5] = pointVector5.set(x, y, z +0.2f);
        points[6] = pointVector6.set(x, y + 1.0f, z+0.2f);
        points[7] = pointVector7.set(x + 1.0f, y + 1.0f, z+0.2f);

        setTexCoords(sidesTextureUVs);
        setAOLightTop(x, y, z, chunk, block);

        addFront(vertices,indicies);
        addBack(vertices,indicies);

        points[0] = pointVector0.set(x, y, z + 0.8f);
        points[1] = pointVector1.set(x + 1.0f, y, z + 0.8f);
        points[2] = pointVector2.set(x + 1.0f, y + 1.0f, z + 0.8f);
        points[3] = pointVector3.set(x, y + 1.0f, z + 0.8f);
        points[4] = pointVector4.set(x + 1.0f, y, z+0.8f);
        points[5] = pointVector5.set(x, y, z +0.8f);
        points[6] = pointVector6.set(x, y + 1.0f, z+0.8f);
        points[7] = pointVector7.set(x + 1.0f, y + 1.0f, z+0.8f);

        addFront(vertices,indicies);
        addBack(vertices,indicies);

        points[0] = pointVector0.set(x+ 0.2f, y, z);
        points[1] = pointVector1.set(x + 0.2f, y, z + 1.0f);
        points[2] = pointVector2.set(x + 0.2f, y + 1.0f, z + 1.0f);
        points[3] = pointVector3.set(x + 0.2f, y + 1.0f, z);
        points[4] = pointVector4.set(x + 0.2f, y, z+1.0f);
        points[5] = pointVector5.set(x + 0.2f, y, z);
        points[6] = pointVector6.set(x + 0.2f, y + 1.0f, z);
        points[7] = pointVector7.set(x + 0.2f, y + 1.0f, z+1.0f);


        addFront(vertices,indicies);
        addBack(vertices,indicies);

        points[0] = pointVector0.set(x + 0.8f, y, z);
        points[1] = pointVector1.set(x + 0.8f, y, z + 1.0f);
        points[2] = pointVector2.set(x + 0.8f, y + 1.0f, z + 1.0f);
        points[3] = pointVector3.set(x + 0.8f, y + 1.0f, z);
        points[4] = pointVector4.set(x + 0.8f, y, z+1.0f);
        points[5] = pointVector5.set(x + 0.8f, y, z);
        points[6] = pointVector6.set(x + 0.8f, y + 1.0f, z);
        points[7] = pointVector7.set(x + 0.8f, y + 1.0f, z+1.0f);


        addFront(vertices,indicies);
        addBack(vertices,indicies);

        return true;
    }
}
