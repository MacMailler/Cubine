package net.disya.voxen.game.terrain.block.render;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import java.util.Random;

import net.disya.voxen.terrain.block.Block;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.block.render.BasicBlockRender;
import net.disya.voxen.terrain.chunk.Chunk;

/**
 * Created by nicklaslof on 02/02/15.
 */
public class FlowerRender  extends BasicBlockRender {

    private final Random random = new Random();

    @Override
    public synchronized boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block, FloatArray vertices, ShortArray indicies) {
        Vector2[] sidesTextureUVs = block.getSidesTextureUVs();
        float sinX = MathUtils.sin(x);
        float sinY = MathUtils.sin(y);
        float sinZ = MathUtils.sin(z);

        float v = Math.min(1.0f,Math.min(0.0f,Math.abs(sinX - sinZ)));

        points[0] = pointVector0.set(x, y, z + v);
        points[1] = pointVector1.set(x + 1.0f, y, z + v);
        points[2] = pointVector2.set(x + 1.0f, y + 1.0f, z + v);
        points[3] = pointVector3.set(x, y + 1.0f, z + v);
        points[4] = pointVector4.set(x + 1.0f, y, z + v);
        points[5] = pointVector5.set(x, y, z + v);
        points[6] = pointVector6.set(x, y + 1.0f, z + v);
        points[7] = pointVector7.set(x + 1.0f, y + 1.0f, z + v);

        setTexCoords(sidesTextureUVs);
        setAOLightTop(x, y, z, chunk, block);

        addFront(vertices, indicies);
        addBack(vertices, indicies);


        v = Math.min(1.0f,Math.min(0.0f,Math.abs(sinX - (sinZ+sinY))));
        points[0] = pointVector0.set(x+ v, y, z);
        points[1] = pointVector1.set(x + v, y, z + 1.0f);
        points[2] = pointVector2.set(x + v, y + 1.0f, z + 1.0f);
        points[3] = pointVector3.set(x + v, y + 1.0f, z);
        points[4] = pointVector4.set(x + v, y, z+1.0f);
        points[5] = pointVector5.set(x + v, y, z);
        points[6] = pointVector6.set(x + v, y + 1.0f, z);
        points[7] = pointVector7.set(x + v, y + 1.0f, z+1.0f);


        addFront(vertices,indicies);
        addBack(vertices,indicies);

        return true;
    }
}
