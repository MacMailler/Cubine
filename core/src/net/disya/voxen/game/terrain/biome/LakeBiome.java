package net.disya.voxen.game.terrain.biome;

import net.disya.voxen.game.terrain.block.BlockProvider;
import net.disya.voxen.terrain.biome.Biome;

/**
 * Created by nicklas on 6/18/14.
 */
public class LakeBiome extends Biome{
    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public double getFieldObstacleAmmount() {
        return 2;
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
        return false;
    }

    @Override
    public double getAmmountOfWater() {
        return 14;
    }
}
