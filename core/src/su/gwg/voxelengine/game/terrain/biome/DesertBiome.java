package su.gwg.voxelengine.game.terrain.biome;

import su.gwg.voxelengine.terrain.biome.Biome;
import su.gwg.voxelengine.game.terrain.block.BlockProvider;

/**
 * Created by nicklas on 6/19/14.
 */
public class DesertBiome extends Biome {
    @Override
    public int getHeight() {
        return 4;
    }

    @Override
    public double getFieldObstacleAmmount() {
        return 0;
    }

    @Override
    public byte getGroundFillerBlock() {
        return BlockProvider.limeStone.getId();
    }

    @Override
    public byte getMountainFillerBlock() {
        return BlockProvider.limeStone.getId();
    }

    @Override
    public boolean hasSandBeach() {
        return true;
    }

    @Override
    public double getAmmountOfWater() {
        return 0;
    }
}

