package net.disya.voxen.terrain.block;

import net.disya.voxen.render.BlockRender;
import net.disya.voxen.terrain.World;
import net.disya.voxen.terrain.block.render.BasicBlockRender;
import net.disya.voxen.terrain.chunk.Chunk;
import net.disya.voxen.utils.TextureUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by nicklas on 5/2/14.
 */
public abstract class Block {

    private final byte id;
    private final String topTextureRegion;
    private final String bottomTextureRegion;
    private final String sidesTextureRegion;
    private Vector2[] topTextureUVs;
    private Vector2[] bottomTextureUVs;
    private Vector2[] sidesTextureUVs;
    protected BlockRender blockRender;
    
    public static enum Side {
        FRONT(new Vector3(0,0,1)),
        BACK(new Vector3(0,0,-1)),
        RIGHT(new Vector3(1,0,0)),
        LEFT(new Vector3(-1,0,0)),
        TOP(new Vector3(0,1,0)),
        BOTTOM(new Vector3(0,-1,0));

        private Vector3 sideDirection;

        Side(Vector3 sideDirection) {
            this.sideDirection = sideDirection;
        }

        public byte getBlockAt(Chunk chunk,int x, int y, int z){
            return chunk.getByte(x + (int) sideDirection.x, y + (int) sideDirection.y, z + (int) sideDirection.z);
        }

    }

    protected Block(byte id, String topTextureRegion, String bottomTextureRegion, String sidesTextureRegion) {
        this.id = id;

        this.topTextureRegion = topTextureRegion;
        this.bottomTextureRegion = bottomTextureRegion;
        this.sidesTextureRegion = sidesTextureRegion;
        this.blockRender = new BasicBlockRender();
    }

    public byte getId() {
        return id;
    }

    public Vector2[] getBottomTextureUVs() {
        if (bottomTextureUVs == null){
            bottomTextureUVs = TextureUtils.calculateUVMapping(bottomTextureRegion);
        }
        return bottomTextureUVs;
    }

    public Vector2[] getSidesTextureUVs() {
        if (sidesTextureUVs == null){
            sidesTextureUVs = TextureUtils.calculateUVMapping(sidesTextureRegion);
        }
        return sidesTextureUVs;
    }

    public Vector2[] getTopTextureUVs() {
        if (topTextureUVs == null){
            topTextureUVs = TextureUtils.calculateUVMapping(topTextureRegion);
        }
        return topTextureUVs;
    }

    public final boolean drawSide(IBlockProvider blockProvider, Chunk chunk, int x, int y, int z, Side side){
        if (y == 0 && side == Side.BOTTOM){
            return false;
        }
        return blockRenderSide(blockProvider, chunk, x, y, z, side);
    }

    protected boolean blockRenderSide(IBlockProvider blockProvider, Chunk chunk, int x, int y, int z, Side side) {
        byte blockAtSide = side.getBlockAt(chunk, x, y, z);
        if (blockAtSide == 0){
            return true;
        }
        if (blockProvider.getBlockById(blockAtSide).getOpacity() < 32){
            return true;
        }

        return false;
    }

    public void onNeighbourBlockChange(int x, int y, int z){

    }

    public boolean isLightSource() {
        return false;
    }

    public int getOpacity() {
        return 32;
    }

    public Color getTileColor(int x, int y, int z){
        return Color.WHITE;
    };

    public BlockRender getBlockRender() {
        return blockRender;
    }

    public boolean isPlayerCollidable(){
        return true;
    }

    public boolean isLiquid(){ return false; }
}
