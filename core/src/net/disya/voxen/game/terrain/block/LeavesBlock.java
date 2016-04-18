package net.disya.voxen.game.terrain.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

import net.disya.voxen.terrain.block.Block;

/**
 * Created by nicklaslof on 19/01/15.
 */
public class LeavesBlock extends Block {
    private final Random random;
    private final Color yellow = new Color(242/255f,228/255f,36/255f,1);
    private final Color yellow2 = new Color(242/255f,218/255f,36/255f,1);
    private final Color yellow3 = new Color(242/255f,208/255f,36/255f,1);
    private final Color yellow4 = new Color(242/255f,198/255f,36/255f,1);
    private final Color yellow5 = new Color(242/255f,188/255f,36/255f,1);
    private final Color orange = new Color(242/255f,178/255f,36/255f,1);
    private final Color orange2 = new Color(242/255f,168/255f,36/255f,1);
    private final Color orange3 = new Color(242/255f,158/255f,36/255f,1);
    private final Color orange4 = new Color(242/255f,148/255f,36/255f,1);
    private final Color orange5 = new Color(242/255f,138/255f,36/255f,1);

    protected LeavesBlock(byte id, String textureRegion) {
        super(id, textureRegion, textureRegion, textureRegion);
        random = new Random();
    }

    @Override
    public Color getTileColor(int x, int y, int z) {

        float sinX = MathUtils.sin(x);
        float sinZ = MathUtils.sin(z);

        float v = Math.abs(sinX - sinZ);

        /*if (sinX < sinZ) {
            return orange;

        } else {
            return yellow;
        }*/

        if (v < 0.2){
            return yellow;
        }
        if (v < 0.4){
            return orange;
        }

        if (v < 0.6){
            return orange2;
        }

        if (v < 0.8){
            return orange3;
        }

        if (v < 1.2){
            return orange4;
        }

        if (v < 1.4){
            return orange5;
        }
        if (v < 1.6){
            return yellow2;
        }
        if (v < 1.8){
            return yellow3;
        }
        if (v < 2.0){
            return yellow4;
        }


        return yellow5;
    }

}
