package net.disya.voxen.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.*;

import net.disya.voxen.game.terrain.block.BlockProvider;
import net.disya.voxen.physics.PhysicsController;
import net.disya.voxen.terrain.biome.Biome;
import net.disya.voxen.terrain.biome.IBiomeProvider;
import net.disya.voxen.terrain.block.Block;
import net.disya.voxen.terrain.block.IBlockProvider;
import net.disya.voxen.terrain.chunk.Chunk;
import net.disya.voxen.terrain.chunk.IChunkProvider;
import net.disya.voxen.terrain.noise.SimplexNoise;
import net.disya.voxen.terrain.noise.SimplexNoise2;
import net.disya.voxen.terrain.noise.SimplexNoise3;
import net.disya.voxen.utils.PositionUtils;

/**
 * Created by nicklas on 4/24/14.
 */
public class World {
    //public static long SEED = -5913710905101643296l;
    public static long SEED; // Will generate a new landscape every time
    private final static int MAX_CHUNKS_PER_UPDATE = Runtime.getRuntime().availableProcessors()*4;
    public static int WIDTH = 16;
    public static int HEIGHT = 128;
    public static int CHUNKDISTANCE = 4;
    public static int GROUND_HEIGHT = 10;
    private static Array<Chunk> chunksWaitingForUpdate = new Array<Chunk>();
    private static Array<Chunk> chunksUpdatePriorityList = new Array<Chunk>();

    private static IChunkProvider chunkProvider;
    private static IBiomeProvider biomeProvider;
    public static boolean playerIsInWater;
    private final Vector3 previousCameraPosition = new Vector3();
    private static IBlockProvider blockProvider;
    private static Object prioListSync = new Object();
    public static Vector3 tmp = new Vector3();
    public static Vector3 tmp2 = new Vector3();

    public World(IBlockProvider blockProvider, IChunkProvider chunkProvider, IBiomeProvider biomeProvider, int width, int height, int viewRange) {
        //System.out.println("Starting Voxel engine with size: " + width + "*" + height + " and viewrange " + viewRange);
        this.blockProvider = blockProvider;
        this.chunkProvider = chunkProvider;
        this.biomeProvider = biomeProvider;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.CHUNKDISTANCE = viewRange;
        if (SEED == 0){
            SEED = new Random().nextLong()*100000;
        }

        System.out.println("Seed is "+SEED);
        SimplexNoise.init(SEED);
        SimplexNoise2.init(SEED / 10);
        SimplexNoise3.init(SEED / 100);
    }

    public World(IBlockProvider blockProvider, IChunkProvider chunkProvider, IBiomeProvider biomeProvider) {
        this.blockProvider = blockProvider;
        this.chunkProvider = chunkProvider;
        this.biomeProvider = biomeProvider;
    }

    public static Chunk findChunk(int x, int z) {
        return chunkProvider.getChunkAt(PositionUtils.hashOfPosition(x, z));
    }

    public static Biome findBiome(int x, int z) {return biomeProvider.getBiomeAt(x, z);}

    public static void notifyNeighoursAboutLightChange(int chunkPosX, int chunkPosZ, boolean force) {
        //System.out.println("neighbourupdate at "+chunkPosX + " "+chunkPosZ);
        /*
        -
        -          673
        -          4o1
        z          582
        x---------------------------

         */
       resetLightOnChunk(findChunk(chunkPosX + 1, chunkPosZ), force);         //1
    //    resetLightOnChunk(findChunk(chunkPosX + 1, chunkPosZ - 1), force);     //2
       // resetLightOnChunk(findChunk(chunkPosX + 1, chunkPosZ + 1), force);     //3
      resetLightOnChunk(findChunk(chunkPosX - 1, chunkPosZ),force);         //4
      // resetLightOnChunk(findChunk(chunkPosX - 1, chunkPosZ - 1), force);     //5
       //resetLightOnChunk(findChunk(chunkPosX - 1, chunkPosZ + 1), force);     //6

      resetLightOnChunk(findChunk(chunkPosX, chunkPosZ+1),force);           //7
        resetLightOnChunk(findChunk(chunkPosX, chunkPosZ-1),force);           //8
    }

    private static void resetLightOnChunk(Chunk chunk, boolean force) {
        if (chunk != null) {
            chunk.resetLight(force);
        }
    }

    public void update(Vector3 camPos) {

        for (Chunk chunk : chunkProvider.getAllChunks()) {
            chunk.tick();
        }


        if (chunksUpdatePriorityList.size > 0) {
            synchronized (prioListSync) {
                Chunk chunk = chunksUpdatePriorityList.pop();
                chunk.update();
                //System.out.println("priority list size " + chunksUpdatePriorityList.size);
            }
        } else {

            if (chunksWaitingForUpdate.size == 0) {
                ArrayMap.Values<Chunk> allChunks = chunkProvider.getAllChunks();
                for (Chunk chunk : allChunks) {
                    if (chunk.isActive()) {
                        chunksWaitingForUpdate.add(chunk);
                    }
                }

            }


            Iterator<Chunk> iterator = chunksWaitingForUpdate.iterator();

            for (int i = 0; i < MAX_CHUNKS_PER_UPDATE; i++) {
                if (iterator.hasNext()) {
                    Chunk chunk = iterator.next();
                    iterator.remove();
                    if (chunk.isActive()) {
                        chunk.update();
                    }
                }
            }
        }

        if (camPos.dst(previousCameraPosition) < 16) {
            return;
        }

        previousCameraPosition.set(camPos);

        checkAndCreateChunk(camPos, 0, 0);

        int radius = 16*CHUNKDISTANCE;
            for (int xc = -radius; xc <= radius; xc+=WIDTH) {
                for (int zc = -radius; zc <= radius; zc+=WIDTH) {
                    if (xc == 0 && zc == 0) continue;
                    if (xc * xc + zc * zc <= radius * radius) {
                        checkAndCreateChunk(camPos, xc, zc);
                    }
            }
        }
    }

    private void checkAndCreateChunk(Vector3 camPos, int xc, int zc) {
        Chunk chunk = findChunk((int) Math.floor((camPos.x+xc) / WIDTH), (int) Math.floor((camPos.z+zc) / WIDTH));
        if (chunk == null) {
            Vector3 worldPosition = new Vector3((int) Math.floor((camPos.x+xc) / WIDTH) * WIDTH, 0, (int) Math.floor((camPos.z+zc) / WIDTH) * WIDTH);
            int x2 = (int) Math.floor(worldPosition.x / WIDTH);
            int z2 = (int) Math.floor(worldPosition.z / WIDTH);

            //System.out.println("Creating chunk at x:" + (int) Math.floor((camPos.x+xc) / WIDTH) * WIDTH + " z:" + (int) Math.floor((camPos.z+zc) / WIDTH) * WIDTH);
            chunkProvider.createChunk(worldPosition, x2, z2);
        }
    }

    public ArrayMap.Values<Chunk> getChunks() {
        return chunkProvider.getAllChunks();
    }

    public static void setBlock(float x, float y, float z, Block block, boolean updateLight) {
        System.out.println("Setblock on "+x+" "+y+" "+z);
        Chunk chunk = World.findChunk((int) Math.floor(x / World.WIDTH), (int) Math.floor(z / World.WIDTH));
        if (chunk != null){
            int localX = (int)x&15;
            int localY = (int)y;
            int localZ = (int)z&15;


            chunk.setBlock(localX,localY,localZ, block, updateLight);

            try {
                getBlock((int) x + 1, (int) y, (int) z).onNeighbourBlockChange((int) x + 1, (int) y, (int) z);
                getBlock((int) x + 1, (int) y, (int) z - 1).onNeighbourBlockChange((int) x + 1, (int) y, (int) z - 1);
                getBlock((int) x + 1, (int) y, (int) z + 1).onNeighbourBlockChange((int) x + 1, (int) y, (int) z + 1);
                getBlock((int) x - 1, (int) y, (int) z).onNeighbourBlockChange((int) x - 1, (int) y, (int) z);
                getBlock((int) x - 1, (int) y, (int) z - 1).onNeighbourBlockChange((int) x - 1, (int) y, (int) z - 1);
                getBlock((int) x - 1, (int) y, (int) z + 1).onNeighbourBlockChange((int) x - 1, (int) y, (int) z + 1);
                getBlock((int) x, (int) y, (int) z + 1).onNeighbourBlockChange((int) x, (int) y, (int) z + 1);
                getBlock((int) x, (int) y, (int) z - 1).onNeighbourBlockChange((int) x, (int) y, (int) z - 1);
            }catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }
    }

    public static void postChunkPriorityUpdate(Chunk chunk) {
        synchronized(prioListSync) {
            chunksUpdatePriorityList.add(chunk);
        }
    }

    public boolean isPlayerInWater(Camera camera) {
        tmp.set(camera.position.x,camera.position.y+0.3f,camera.position.z);

        int x = (int) Math.floor(tmp.x);
        int y = (int) tmp.y;
        int z = (int) Math.floor(tmp.z);
        //System.out.println(x+" "+y+" "+z);
        Chunk chunk = World.findChunk((int) Math.floor(tmp.x / World.WIDTH), (int) Math.floor(tmp.z / World.WIDTH));
        if (chunk != null){
           byte block1 = chunk.getBlock(x, y, z);
           if (isBlockLiquid(blockProvider.getBlockById(block1))){
               PhysicsController.setPlayerInWater(true);
               return true;
           }
        }
        PhysicsController.setPlayerInWater(false);
        return false;
    }


    private boolean isBlockLiquid(Block block){
        if (block.isLiquid()){
            return true;
        }
        return false;
    }

    public static Block getBlock(int x, int y, int z) {
        Chunk chunk = World.findChunk((int) Math.floor(tmp.x / World.WIDTH), (int) Math.floor(tmp.z / World.WIDTH));
        if (chunk != null) {
            byte block = chunk.getBlock(x, y, z);
            Block blockById = blockProvider.getBlockById(block);
            return blockById;
        }
        return blockProvider.getBlockById((byte)0);
    }
}
