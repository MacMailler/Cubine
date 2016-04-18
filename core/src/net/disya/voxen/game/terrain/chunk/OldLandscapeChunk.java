package net.disya.voxen.game.terrain.chunk;

import net.disya.voxen.game.terrain.block.BlockProvider;
import net.disya.voxen.terrain.World;
import net.disya.voxen.terrain.biome.Biome;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.chunk.Chunk;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by nicklas on 4/25/14.
 */
public class OldLandscapeChunk extends Chunk {

    public OldLandscapeChunk(IBlockProvider blockProvider, Biome biome, Vector3 baseWorldPosition, int x, int z) {
        super(blockProvider, biome, baseWorldPosition, x, z);
    }

    @Override
    protected void calculateChunk(Vector3 baseWorldPosition, Biome biome, IBlockProvider blockProvider) {
        Vector3 worldPosOfXYZ = new Vector3();
        for (int y = 0; y < World.HEIGHT; y++) {
            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    worldPosOfXYZ.set(x, y, z).add(baseWorldPosition);
                    setBlock(x, y, z, blockProvider.getBlockById(getByteAtWorldPosition(x, y, z, biome, worldPosOfXYZ)),false);
                }
            }
        }
    }

    @Override
    protected byte getByteAtWorldPosition(int x, int y, int z, Biome biome, Vector3 worldPositionOfXYZ) {

        if (y == 0) {
            return BlockProvider.limeStone.getId();
        }

        if (y < 4) {
            return BlockProvider.limeStone.getId();
        }

        /*if (y == random.nextInt(80) && random.nextFloat() < 0.19f && !isBlockTransparent(x, y - 1, z) &&
                (getByteAtWorldPosition(x, y - 1, z, biome, worldPositionOfXYZ) == BlockProvider.limeStone.getId())) {
            return BlockProvider.light.getId();
        }*/

        float maxHeight = biome.getHeight();

        double mountainDensity = Math.max(0, get2dNoise(worldPositionOfXYZ, landscapeRandomOffset1, 0.009f));

        mountainDensity = Math.sqrt(mountainDensity);
        mountainDensity *= maxHeight - World.GROUND_HEIGHT;
        mountainDensity += World.GROUND_HEIGHT;


        mountainDensity += (Math.max(0, get2dNoise(worldPositionOfXYZ, landscapeRandomOffset2, biome.getFieldObstacleAmmount() / 100)) * World.GROUND_HEIGHT) - biome.getAmmountOfWater();

        if (y > 10 && maxHeight > 30) {
            double carveDensity = (Math.max(0, get3dNoise(worldPositionOfXYZ, landscapeRandomOffset3, 0.09)));
            carveDensity *= maxHeight*2;

            if (carveDensity >= y) {
                return 0;
            }
        }

        if (mountainDensity >= y) {
            if (y == 4) {
                if (biome.hasSandBeach()) {
                    return BlockProvider.limeStone.getId();
                } else {
                    return biome.getGroundFillerBlock();
                }
            } else {
                return biome.getMountainFillerBlock();
            }
        }
        return 0;
    }


    @Override
    public void tick() {

    }

    @Override
    public void render() {
    }

    private boolean isInOpenSpace(int x, int y, int z) {
        if (isBlockTransparent(x, y + 1, z, null) &&
                isBlockTransparent(x + 1, y, z, null) &&
                isBlockTransparent(x - 1, y, z, null) &&
                isBlockTransparent(x, y, z + 1, null) &&
                isBlockTransparent(x, y, z - 1, null)) {
            return true;
        }

        return false;
    }
}
