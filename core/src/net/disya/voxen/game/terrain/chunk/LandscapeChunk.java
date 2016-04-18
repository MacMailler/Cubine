package net.disya.voxen.game.terrain.chunk;

import net.disya.voxen.game.terrain.block.BlockProvider;
import net.disya.voxen.terrain.World;
import net.disya.voxen.terrain.biome.Biome;
import net.disya.voxen.terrain.block.Block;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.chunk.Chunk;
import net.disya.voxen.terrain.noise.SimplexNoise;
import net.disya.voxen.terrain.noise.SimplexNoise2;
import net.disya.voxen.terrain.noise.SimplexNoise3;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by nicklaslof on 12/01/15.
 */
public class LandscapeChunk extends Chunk {
    public LandscapeChunk(IBlockProvider blockProvider, Biome biome, Vector3 worldPosition, int chunkPosX, int chunkPosZ) {
        super(blockProvider, biome, worldPosition, chunkPosX, chunkPosZ);
    }

    @Override
    protected void calculateChunk(Vector3 baseWorldPosition, Biome biome, IBlockProvider blockProvider) {
        Vector3 worldPosOfXYZ = new Vector3();
        for (int y = 0; y < World.HEIGHT; y++) {
            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    worldPosOfXYZ.set(x, y, z).add(baseWorldPosition);
                    setBlock(x, y, z,  blockProvider.getBlockById(getByteAtWorldPosition(x, y, z, biome, worldPosOfXYZ)),false);

                }
            }
        }
        for (int y = 0; y < World.HEIGHT; y++) {
            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    float v = random.nextFloat();
                    if(getBlock(x,y,z) == BlockProvider.grass.getId() && getBlock(x,y+1,z) == 0 && getBlock(x,y+2,z) == 0) {
                        if (v < 0.2) {
                            setBlock(x, y+1, z, BlockProvider.straws, false);
                            continue;
                        }

                        if (v < 0.3){
                            setBlock(x, y+1, z, BlockProvider.flower, false);
                            continue;
                        }
                    }
                }
            }
        }


        for (int y = 0; y < World.HEIGHT-12; y++) {
            for (int x = 4; x < World.WIDTH-4; x++) {
                for (int z = 4; z < World.WIDTH-4; z++) {
                    byte block = getBlock(x, y, z);
                    if((block == BlockProvider.grass.getId() || block == BlockProvider.straws.getId()) && getBlock(x,y+1,z) == 0){
                        float v = random.nextFloat();
                        if (v < 0.009) {
                            createTree(x,y,z);
                        }
                    }
                }
            }
        }
    }

    public void createTree(int rootX, int rootY, int rootZ) {
        try {
            int treeHight = 11;

            for (int treeY = 0; treeY < treeHight; treeY++) {
                setBlock(rootX, treeY + rootY, rootZ,BlockProvider.treeTrunk,false);
            }

            int bareTrunkY = 0;
            while (bareTrunkY < 7 + (treeHight/10)){
                bareTrunkY = random.nextInt(12);
            }

            int radius = 4;
            for (int treeY = 0; treeY < treeHight; treeY++) {
                for (int xc = -radius; xc <= radius; ++xc) {
                    for (int zc = -radius; zc <= radius; ++zc) {
                        if (xc * xc + zc * zc <= radius * radius) {
                            if (random.nextInt(1000)> 100){
                                setBlock(xc + rootX, treeY+(rootY+bareTrunkY), zc + rootZ,BlockProvider.leaves,false);
                            }
                        }
                    }
                }
                radius--;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected byte getByteAtWorldPosition(int x, int y, int z, Biome biome, Vector3 worldPositionOfXYZ) {
        if (y == 1) {
            return BlockProvider.limeStone.getId();
        }

        if (y == World.HEIGHT-1 || y == World.HEIGHT){
            return 0;
        }

        /*if (y == random.nextInt(80) && random.nextFloat() < 0.19f && !isBlockTransparent(x, y - 1, z) &&
                (getByteAtWorldPosition(x, y - 1, z, biome, worldPositionOfXYZ) == BlockProvider.grass.getId())) {
            return BlockProvider.light.getId();
        }*/


       /*double caveDensity = SimplexNoise.noise(worldPositionOfXYZ.x* 0.01f, worldPositionOfXYZ.y * 0.02f, worldPositionOfXYZ.z*0.01f);
        double caveDensity2 = SimplexNoise2.noise(worldPositionOfXYZ.x * 0.01f, worldPositionOfXYZ.y * 0.02f, worldPositionOfXYZ.z * 0.01f);

        if (caveDensity > 0.45 && caveDensity < 0.70 && caveDensity2 > 0.45 && caveDensity2 < 0.70){
            return 0;
        }*/

        double baseDensity = 0;

        {
            float frequency = 0.001f;
            float weight = 1.0f-(y/World.HEIGHT);
            float weight2 = 0.1f-(y/World.HEIGHT);

            for (int i = 0; i < 3; i++) {
                baseDensity += SimplexNoise.noise(worldPositionOfXYZ.x * frequency, worldPositionOfXYZ.y * frequency, worldPositionOfXYZ.z * frequency) * weight;
                baseDensity += SimplexNoise2.noise(worldPositionOfXYZ.x * frequency, worldPositionOfXYZ.y * frequency * 2, worldPositionOfXYZ.z * frequency) * weight2;
                frequency *= 3.5f;
                weight *= 0.2f;
            }
        }

        double mountainDensity = 0;

        {
            float frequency = 0.001f;
            float weight = 1f;

            for (int i = 0; i < 3; i++) {
                mountainDensity += SimplexNoise.noise(worldPositionOfXYZ.x * frequency, worldPositionOfXYZ.y * frequency*2, worldPositionOfXYZ.z * frequency) * weight;
                mountainDensity += SimplexNoise3.noise(worldPositionOfXYZ.x * frequency, worldPositionOfXYZ.y * frequency, worldPositionOfXYZ.z * frequency) * weight;
                frequency *= 4.3f+(y/World.HEIGHT);
                weight *= 0.4f;
            }
        }
        mountainDensity += 1;

        mountainDensity *= 48;

        if (mountainDensity > y){
            double sandStoneNoise = SimplexNoise.noise(worldPositionOfXYZ.x * 0.008, worldPositionOfXYZ.y * 0.09, worldPositionOfXYZ.z * 0.006) * 0.019;
            double shaleNoise = SimplexNoise2.noise(worldPositionOfXYZ.x * 0.022, worldPositionOfXYZ.y * 0.1, worldPositionOfXYZ.z * 0.026) * 0.053;

            if (sandStoneNoise >= 0.0009){
                return BlockProvider.sandStone.getId();
            }else if (shaleNoise >= 0.0001){
                return BlockProvider.shale.getId();
            }

            return BlockProvider.limeStone.getId();
        }


        baseDensity += 1;

        baseDensity *= 48;


        if (baseDensity > y){
            if (y > 48) {
                return BlockProvider.grass.getId();
            }else{
                double sandStoneNoise = SimplexNoise.noise(worldPositionOfXYZ.x * 0.008, worldPositionOfXYZ.y * 0.09, worldPositionOfXYZ.z * 0.006) * 0.019;
                double shaleNoise = SimplexNoise2.noise(worldPositionOfXYZ.x * 0.022, worldPositionOfXYZ.y * 0.1, worldPositionOfXYZ.z * 0.026) * 0.053;

                if (sandStoneNoise >= 0.0009){
                    return BlockProvider.sandStone.getId();
                }else if (shaleNoise >= 0.0001){
                    return BlockProvider.shale.getId();
                }

                return BlockProvider.limeStone.getId();
            }
        }

        if (y < 49 && y > 0){
            return BlockProvider.water.getId();
        }

        return 0;
    }

    @Override
    public void tick() {

    }

    @Override
    public void render() {

    }
}
