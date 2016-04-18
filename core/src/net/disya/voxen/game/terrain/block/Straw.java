package net.disya.voxen.game.terrain.block;

import net.disya.voxen.game.terrain.block.render.StrawRender;
import net.disya.voxen.terrain.block.Block;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by nicklaslof on 30/01/15.
 */
public class Straw extends Block {
    protected Straw(byte id, String topTextureRegion) {
        super(id, topTextureRegion, topTextureRegion, topTextureRegion);
        this.blockRender = new StrawRender();
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public boolean isPlayerCollidable() {
        return false;
    }
}
