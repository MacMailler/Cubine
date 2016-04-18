package net.disya.voxen.terrain.biome;

/**
 * Created by nicklas on 6/18/14.
 */
public abstract class Biome {

    public abstract int getHeight();


    public abstract double getFieldObstacleAmmount();

    public abstract byte getGroundFillerBlock();

    public abstract byte getMountainFillerBlock();

    public abstract boolean hasSandBeach();

    public abstract double getAmmountOfWater();
}
