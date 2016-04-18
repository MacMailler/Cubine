package net.disya.voxen.game.terrain.block;

import net.disya.voxen.terrain.block.Block;

/**
 * Created by nicklas on 5/7/14.
 */
public class GlassBlock extends Block {
    protected GlassBlock(byte id, String textureRegion) {
        super(id, textureRegion, textureRegion, textureRegion);
    }

    @Override
    public int getOpacity() {
        return 8;
    }
}
