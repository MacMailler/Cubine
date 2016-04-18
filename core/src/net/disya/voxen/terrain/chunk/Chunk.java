package net.disya.voxen.terrain.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.disya.voxen.render.BoxMesh;
import net.disya.voxen.render.VoxelMesh;
import net.disya.voxen.terrain.World;
import net.disya.voxen.terrain.biome.Biome;
import net.disya.voxen.terrain.block.Block;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.noise.SimplexNoise;
import net.disya.voxen.utils.PositionUtils;


/**
 * Created by nicklas on 4/24/14.
 */
public abstract class Chunk {
    public final static byte LIGHT = 1; // 1 is brightest
    private final static byte DARKNESS = 32; // 32 is darkest
    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
    protected static Random random;
    protected static Vector3 landscapeRandomOffset1;
    protected static Vector3 landscapeRandomOffset2;
    protected static Vector3 landscapeRandomOffset3;
    protected static Vector3 landscapeRandomOffset4;
    protected static Vector3 landscapeRandomOffset5;

    // Contains the block byte id for each x,y,z location in this chunk.
    private final byte[] map;
    private boolean isReady;
    // Contains the byte light for each x,y,z location in this chunk.
    private byte[] lightMap;
    // True or false if the block at this position can "see the sky".
    private boolean[] heightMap;
    private final IBlockProvider blockProvider;
    private Biome biome;
    private final Vector3 worldPosition;
    private final int chunkPosX;
    private final int chunkPosZ;
    private final Object syncToken = new Object();
    protected Array<VoxelMesh> meshes = new Array<VoxelMesh>();
    protected Array<VoxelMesh> alphaMeshes = new Array<VoxelMesh>();
    private int blockCounter = 0;
    private boolean active = true;
    private boolean isRecalculating;
    private ArrayMap<Long, Double> noiseCache2d = new ArrayMap<Long, Double>();
    private ArrayMap<Long, Double> noiseCache3d = new ArrayMap<Long, Double>();
    private boolean needLightUpdate = true;
    private boolean fullRebuildOfLight;
    private boolean needMeshUpdate;
    private int timesSinceUpdate;

    public Chunk(final IBlockProvider blockProvider, final Biome biome, final Vector3 worldPosition, final int chunkPosX, final int chunkPosZ) {
        isReady = false;
        this.blockProvider = blockProvider;
        this.biome = biome;
        this.worldPosition = worldPosition;
        this.chunkPosX = chunkPosX;
        this.chunkPosZ = chunkPosZ;
        map = new byte[(World.WIDTH * World.WIDTH) * World.HEIGHT];
        lightMap = new byte[(World.WIDTH * World.WIDTH) * World.HEIGHT];
        heightMap = new boolean[(World.WIDTH * World.WIDTH) * World.HEIGHT];
        Arrays.fill(lightMap, DARKNESS);
        Arrays.fill(heightMap, false);

        // This will prepare all Voxel meshes for this chunk
        // It will use a new mesh for every 16 block in height. So if the chunk is 128 blocks high it will end up with 8 meshes.
        for (int i = 0; i < World.HEIGHT / 16; i++) {
            meshes.add(new VoxelMesh());
        }

        for (int i = 0; i < World.HEIGHT / 16; i++) {
            alphaMeshes.add(new VoxelMesh());
        }


        if (random == null) {
            random = new Random();
            if (World.SEED != 0) {
                random.setSeed(World.SEED);
            }
            landscapeRandomOffset1 = new Vector3((float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset2 = new Vector3((float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset3 = new Vector3((float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset4 = new Vector3((float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
            landscapeRandomOffset5 = new Vector3((float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);

        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    isRecalculating = true;
                    fullRebuildOfLight = true;
                    calculateChunk(worldPosition, biome, blockProvider);
                    updateLight();
                    resetMesh();
                    World.notifyNeighoursAboutLightChange(chunkPosX, chunkPosZ, false);
                    isReady = true;
                    recalculateMesh();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        executorService.submit(runnable);

    }

    private void fillSunlight() {
        for (int x = 0; x < World.WIDTH; x++) {
            for (int z = 0; z < World.WIDTH; z++) {

                height:
                for (int y = World.HEIGHT-1; y > 0; y--) {
                    byte block = getByte(x, y, z);
                    if (block == 0) {
                        setLight(x, y, z, LIGHT);
                        heightMap[getLocationInArray(x,y,z)] = true;
                    } else {
                        break height;
                    }
                }


            }
        }
    }

    protected abstract void calculateChunk(Vector3 position, Biome biome, IBlockProvider blockProvider);

    protected abstract byte getByteAtWorldPosition(int x, int y, int z, Biome biome, Vector3 worldPositionOfXYZ);


    protected double get2dNoise(Vector3 pos, Vector3 offset, double scale) {
        long posHash = PositionUtils.hashOfPosition((int) (pos.x + offset.x), (int) (pos.z + offset.z));
        if (noiseCache2d.containsKey(posHash)) {
            return noiseCache2d.get(posHash);
        }

        double noiseX = (double) (pos.x + offset.x) * scale;
        double noiseZ = (double) (pos.z + offset.z) * scale;

        double noise = SimplexNoise.noise(noiseX, noiseZ);

        noiseCache2d.put(posHash, noise);

        return noise;
    }

    protected double get3dNoise(Vector3 pos, Vector3 offset, double scale) {
        long posHash = PositionUtils.hashOfPosition((int) (pos.x + offset.x), (int) (pos.z + offset.z));
        if (noiseCache3d.containsKey(posHash)) {
            return noiseCache3d.get(posHash);
        }

        double noiseX = (double) (pos.x + offset.x) * scale;
        double noiseY = (double) (pos.y + offset.y) * scale;
        double noiseZ = (double) (pos.z + offset.z) * scale;

        double noise = SimplexNoise.noise(noiseX, noiseY, noiseZ);

        noiseCache3d.put(posHash, noise);

        return noise;
    }


    public Array<VoxelMesh> getMeshes() {
        return meshes;
    }

    public Array<VoxelMesh> getAlphaMeshes() {
        return alphaMeshes;
    }

    public Vector3 getWorldPosition() {
        return worldPosition;
    }


    public void setBlock(int x, int y, int z, Block block, boolean updateLight) {
        if(outsideThisChunkBounds(x,z)){
            int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
            int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

            Chunk chunk = World.findChunk(xToFind, zToFind);
            if (chunk == this) {
                System.out.println("Found myself!");
            }
            if (chunk != null) {
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                chunk.setBlock(normalizedX,y,normalizedZ,block, updateLight);
                return;

            }
        }
        if (isRecalculating && isReady){
            return;
        }
        //System.out.println("Block set at localpos "+x+" "+y+" "+z);
        map[getLocationInArray(x, y, z)] = block.getId();
        blockCounter++;


        if (updateLight) {
            resetLight(true);
        }
        needLightUpdate = true;
        resetMesh();
    }

    public byte getBlock(int x, int y, int z) {
        if (outsideHeightBounds(y)){
            return 0;
        }

        int localX = (int)x&15;
        int localY = (int)y;
        int localZ = (int)z&15;

        return map[getLocationInArray(localX, localY, localZ)];
    }

    private int getLocationInArray(int x, int y, int z) {
        int loc = (x * World.WIDTH + z) + (y * World.WIDTH * World.WIDTH);
        return loc;
    }

    public int getBlockCounter() {
        return blockCounter;
    }

    /**
     * Takes all the blocks stored in this chunk and adds it to the voxelMesh.
     * Removed blocks isn't taken care of yet.
     */
    public void recalculateMesh() {
        if (!isReady){
            return;
        }
        needMeshUpdate = false;
        isRecalculating = true;
        //System.out.println("Recalculating meshes at "+chunkPosX+" "+chunkPosZ);
        Set<VoxelMesh> toRebuild = new HashSet<VoxelMesh>();
        Set<VoxelMesh> toRebuildAlpha = new HashSet<VoxelMesh>();
        for (int y = 0; y < World.HEIGHT; y++) {

            VoxelMesh voxelMesh = meshes.get((int) Math.floor(y / 16));
            VoxelMesh alphaVoxelMesh = alphaMeshes.get((int) Math.floor(y / 16));

            for (int x = 0; x < World.WIDTH; x++) {
                for (int z = 0; z < World.WIDTH; z++) {
                    byte block = map[getLocationInArray(x, y, z)];
                    if (block == 0) continue;
                    Block blockById = blockProvider.getBlockById(block);
                    if (blockById.getOpacity() > 31){
                        voxelMesh.addBlock(worldPosition, x, y, z, blockProvider, Chunk.this, blockById);
                        toRebuild.add(voxelMesh);
                    }else {
                        alphaVoxelMesh.addBlock(worldPosition, x, y, z, blockProvider, Chunk.this, blockById);
                        toRebuildAlpha.add(alphaVoxelMesh);
                    }
                }
            }
        }

        synchronized (syncToken) {

            for (BoxMesh voxelMesh : toRebuild) {
                voxelMesh.setNeedsRebuild();
            }

            for (BoxMesh voxelMesh : toRebuildAlpha) {
                voxelMesh.setNeedsRebuild();
            }

        }
       // System.out.println("Done recalculating meshes at "+chunkPosX+" "+chunkPosZ);
        isRecalculating = false;
    }

    private boolean updateLight() {
        //System.out.println("calculating light for chunk "+chunkPosX+" "+chunkPosZ);
        boolean lightUpdated = false;
        boolean lightUpdatedInLoop = true;
        boolean borderupdated = false;
        if (fullRebuildOfLight){
            //System.out.println("Full light rebuild "+chunkPosX+" "+chunkPosZ);
            Arrays.fill(lightMap, DARKNESS);
            Arrays.fill(heightMap, false);
            fillSunlight();
        }

        while(lightUpdatedInLoop) {
            lightUpdatedInLoop = false;
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int x = 0; x < World.WIDTH; x++) {
                    for (int z = 0; z < World.WIDTH; z++) {
                        byte calculatedLight = calculatedLight(x, y, z);
                        byte currentLight = getBlockLight(x, y, z);

                        if (calculatedLight != currentLight) {
                            boolean b = setLight(x, y, z, calculatedLight);
                            if (b){
                                lightUpdated = true;
                                lightUpdatedInLoop = true;
                                if (z == 0 || z == World.WIDTH-1 || x == 0 || x == World.WIDTH -1){
                                    borderupdated = true;
                                }
                             }
                        }
                    }
                }
            }
         }

        needLightUpdate = lightUpdated;
        fullRebuildOfLight = false;


        if (borderupdated){
           World.notifyNeighoursAboutLightChange(chunkPosX, chunkPosZ, false);
        }

        return !needLightUpdate;

    }

    private byte calculatedLight(int x, int y, int z) {
        if (y == 0) {
            return DARKNESS;
        }

        if (heightMap[getLocationInArray(x,y,z)]){
            return LIGHT;
        }

        byte block = map[getLocationInArray(x, y, z)];

        if (block != 0 && blockProvider.getBlockById(block).isLightSource()) { // A block that lights
            return LIGHT;
        } else {

            int opaqueValue = 3;

            if (block != 0 && !blockProvider.getBlockById(block).isLightSource()) {
                opaqueValue = blockProvider.getBlockById(block).getOpacity();
            }

            byte lightFront = getBlockLight(x + 1, y, z);
            byte lightBack = getBlockLight(x - 1, y, z);
            byte lightAbove = getBlockLight(x, y + 1, z);
            byte lightBelow = getBlockLight(x, y - 1, z);
            byte lightLeft = getBlockLight(x, y, z + 1);
            byte lightRight = getBlockLight(x, y, z - 1);

            byte finalLight = DARKNESS;

            if (lightFront < finalLight) finalLight = (byte) (lightFront + opaqueValue);
            if (lightBack < finalLight) finalLight = (byte) (lightBack + opaqueValue);
            if (lightAbove < finalLight) finalLight = (byte) (lightAbove + opaqueValue);
            if (lightBelow < finalLight) finalLight = (byte) (lightBelow + opaqueValue);
            if (lightLeft < finalLight) finalLight = (byte) (lightLeft + opaqueValue);
            if (lightRight < finalLight) finalLight = (byte) (lightRight + opaqueValue);


            if (finalLight > DARKNESS) {
                finalLight = DARKNESS;
            }

            if (finalLight < LIGHT) {
                finalLight = LIGHT;
            }


            return finalLight;
        }
    }


    private boolean setLight(int x, int y, int z, byte light) {
        int existingLight = lightMap[getLocationInArray(x, y, z)];

        if ((existingLight != light)) {
            lightMap[getLocationInArray(x, y, z)] = light;
            return true;
        }
        return false;
    }

    /**
     * Check if the block at the specified worldPosition exists or not. This is used by the VoxelMesh
     * to determinate if it needs to draw a mesh face or not depending on if it will be visible
     * or hidden by another block.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isBlockTransparent(int x, int y, int z, Block sourceBlock) {

        if (y < 0) {
            return false; //Bottom should be false since we will never see it.
        }

        byte b = getByte(x, y, z);
        Block blockById = blockProvider.getBlockById(b);
        if (sourceBlock != null && sourceBlock.getId() == b){
            return false;
        }
        if (blockById.getOpacity() < 32){
            return true;
        }
        switch (b) {
            case 0:
                return true;
            default:
                return false;
        }
    }

    public byte getByte(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return 0;
        }
        if (outsideThisChunkBounds(x, z)) {
            Vector3 worldPosition = this.worldPosition.cpy().add(x, y, z);
            Chunk chunk = World.findChunk((int) Math.floor(worldPosition.x / World.WIDTH), (int) Math.floor(worldPosition.z / World.WIDTH));
            if (chunk != null && chunk.isReady){
                int normalizedX = x & 15;
                int normalizedZ = z & 15;
                return chunk.getBlock(normalizedX,y, normalizedZ);
            }
            Biome biome = World.findBiome((int) Math.floor(worldPosition.x / World.WIDTH), (int) Math.floor(worldPosition.z / World.WIDTH));
            return getByteAtWorldPosition(x, y, z, biome, worldPosition);
            // }
        }
        return map[getLocationInArray(x, y, z)];
    }

    private boolean outsideThisChunkBounds(int x, int z) {
        return x < 0 || z < 0 || x >= World.WIDTH || z >= World.WIDTH;
    }

    private boolean outsideHeightBounds(int y) {
        return y < 0 || y >= World.HEIGHT;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public boolean isNeedLightUpdate() {
        return needLightUpdate;
    }

    public void resetLight(boolean force) {

        //System.out.println("reset light on "+chunkPosX+"  "+chunkPosZ +" force: "+force);
        if (force){
            fullRebuildOfLight = true;
        }
        needLightUpdate = true;
    }

    public byte getBlockLight(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return DARKNESS;
        }
        try {
            if (outsideThisChunkBounds(x, z)) {
                int xToFind = (int) Math.floor(chunkPosX + (x / 16f));
                int zToFind = (int) Math.floor(chunkPosZ + (z / 16f));

                Chunk chunk = World.findChunk(xToFind, zToFind);
                if (chunk == this) {
                    System.out.println("Found myself!");
                }
                if (chunk != null) {
                    int normalizedX = x & 15;
                    int normalizedZ = z & 15;
                    return chunk.getBlockLight(normalizedX, y, normalizedZ);
                } else {
                    return DARKNESS;
                }
            }
            if (heightMap[getLocationInArray(x,y,z)]){
                return LIGHT;
            }
            byte light = lightMap[getLocationInArray(x, y, z)];
            return light;
        } catch (Exception ex) {
            System.out.println("Out of bounds " + x + " " + y + " " + z);
            return DARKNESS;
        }
    }


    public void update() {
        timesSinceUpdate++;
        if (timesSinceUpdate > 100){
            timesSinceUpdate = 0;
            needLightUpdate = true;
        }
        if (!needLightUpdate && !needMeshUpdate){
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (needLightUpdate){
                    timesSinceUpdate = 0;
                    boolean b = updateLight();
                    if (b) resetMesh();
                    return;
                }

                if (needMeshUpdate){
                    recalculateMesh();
                    return;
                }
            }
        };

        executorService.submit(runnable);
        //runnable.run();
    }

    public abstract void tick();

    public abstract void render();

    public void resetMesh(){
        //System.out.println("Need meshupdate on "+chunkPosX+" "+chunkPosZ);
        needMeshUpdate = true;
        if (isReady) {
            World.postChunkPriorityUpdate(this);
        }
    }

    public boolean isNeedMeshUpdate() {
        return needMeshUpdate;
    }

    public boolean isRecalculating() {
        return isRecalculating;
    }
}
