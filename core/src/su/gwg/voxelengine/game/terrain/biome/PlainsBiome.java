package su.gwg.voxelengine.game.terrain.biome;

import su.gwg.voxelengine.terrain.biome.Biome;
import su.gwg.voxelengine.game.terrain.block.BlockProvider;

/**
 * Created by nicklas on 6/18/14.
 */
public class PlainsBiome extends Biome {
    @Override
    public int getHeight() {
        return 5;
    }

    @Override
    public double getFieldObstacleAmmount() {
        return 1;
    }

    @Override
    public byte getGroundFillerBlock() {
        return BlockProvider.grass.getId();
    }

    @Override
    public byte getMountainFillerBlock() {
        return BlockProvider.grass.getId();
    }

    @Override
    public boolean hasSandBeach() {
        return false;
    }

    @Override
    public double getAmmountOfWater() {
        return 5;
    }
}
