package su.gwg.voxelengine.game.terrain.block;

import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.terrain.block.IBlockProvider;

/**
 * Created by nicklas on 5/8/14.
 */
public class BlockProvider implements IBlockProvider {
    public final static Block air = new Air((byte)0);
    public final static Block limeStone = new StoneBlock((byte) 1, "textures/limestone");
    public final static Block grass = new GrassBlock((byte) 2, "textures/grass_top","textures/dirt", "textures/grass_sides");
    public final static Block light = new LightBlock((byte) 3, "textures/lightbox");
    public final static Block glass = new GlassBlock((byte) 4, "textures/lightbox");
    public final static Block wall = new WallBlock((byte) 5, "textures/wall");
    public final static Block treeTrunk = new TreeTrunkBlock((byte) 6, "textures/trunk_top","textures/trunk_top","textures/trunk");
    public final static Block leaves = new LeavesBlock((byte) 7, "textures/uncolored_leaves");
    public final static Block straws = new Straw((byte) 8, "textures/straw");
    public final static Block flower = new FlowerBlock((byte) 9, "textures/flower");
    public final static Block water = new WaterBlock((byte) 10, "textures/water");
    public final static Block shale = new StoneBlock((byte) 11, "textures/shale");
    public final static Block sandStone = new StoneBlock((byte) 12, "textures/sandstone");
    private final Block[] blocks = new Block[128];

    public BlockProvider() {
        addBlock(air);
        addBlock(limeStone);
        addBlock(shale);
        addBlock(sandStone);
        addBlock(grass);
        addBlock(light);
        addBlock(glass);
        addBlock(wall);
        addBlock(treeTrunk);
        addBlock(leaves);
        addBlock(straws);
        addBlock(flower);
        addBlock(water);
    }

    private void addBlock(Block block) {
        blocks[block.getId()] = block;
    }

    @Override
    public Block getBlockById(byte blockId) {
        return blocks[blockId];
    }

}
