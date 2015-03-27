package su.gwg.voxelengine.utils;

/**
 * Created by nicklas on 5/6/14.
 */
public class PositionUtils {

    // http://stackoverflow.com/questions/682438/hash-function-providing-unique-uint-from-an-integer-coordinate-pair
    public static long hashOfPosition(int x, int z){
        return ( z << 16 ) ^ x;
    }
}
