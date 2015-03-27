package su.gwg.voxelengine.game.terrain.biome;

import su.gwg.voxelengine.terrain.World;
import su.gwg.voxelengine.terrain.biome.Biome;
import su.gwg.voxelengine.terrain.biome.IBiomeProvider;
import su.gwg.voxelengine.utils.PositionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by nicklas on 6/18/14.
 */
public class BiomeProvider implements IBiomeProvider {

    private final Biome highMountainBiome = new HighMountainBiome();
    private final Biome midMountainBiome = new MidMountainBiome();
    private final Biome lowMountaintBiome = new LowMountainBiome();
    private final Biome lowMountaintLakeBiome = new LowMountainLakeBiome();
    private final Biome desertBiome = new DesertBiome();
    private final Biome plainsBiome = new PlainsBiome();
    private final Biome beachPlainsBiome = new BeachPlainsBiome();
    private final Biome lakeBiome = new LakeBiome();

    private final Map<Long, Biome> biomes = new HashMap<Long, Biome>();

    @Override
    public Biome getBiomeAt(int x, int z) {

        return new Biome() {
            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public double getFieldObstacleAmmount() {
                return 0;
            }

            @Override
            public byte getGroundFillerBlock() {
                return 0;
            }

            @Override
            public byte getMountainFillerBlock() {
                return 0;
            }

            @Override
            public boolean hasSandBeach() {
                return false;
            }

            @Override
            public double getAmmountOfWater() {
                return 0;
            }
        };
        /*long pos = PositionUtils.hashOfPosition(x, z);
        if (biomes.containsKey(pos)){
            return biomes.get(pos);
        }

        double biomeSizeX = Math.floor(x/7);
        double biomeSizeZ = Math.floor(z/7);

        Random random = new Random();
        random.setSeed((long) (World.SEED + Math.floor(biomeSizeX * 7 + biomeSizeZ * 13)));

        int i = random.nextInt(10);

        Biome biome;
        switch(i){
            case 0:
                biome = highMountainBiome;
                break;
            case 1:
                biome = midMountainBiome;
                break;
            case 2:
                biome = lowMountaintBiome;
                break;
            case 3:
                biome = lowMountaintLakeBiome;
                break;
            case 4:
                biome = desertBiome;
                break;
            case 5:
                biome = plainsBiome;
                break;
            case 6:
                biome = beachPlainsBiome;
                break;
            case 7:
                biome = lakeBiome;
                break;
            default:
                biome = plainsBiome;
        }

        biomes.put(PositionUtils.hashOfPosition(x,z),biome);

        return biome;*/
    }
}
