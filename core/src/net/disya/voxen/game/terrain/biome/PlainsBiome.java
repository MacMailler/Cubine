package net.disya.voxen.game.terrain.biome;

import net.disya.voxen.game.terrain.block.BlockProvider;
import net.disya.voxen.terrain.biome.Biome;

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
