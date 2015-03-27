package su.gwg.voxelengine.terrain.block.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import su.gwg.voxelengine.render.BlockRender;
import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.terrain.block.IBlockProvider;
import su.gwg.voxelengine.terrain.chunk.Chunk;

import java.util.Arrays;

/**
 * Created by nicklaslof on 31/01/15.
 */
public class BasicBlockRender implements BlockRender {
    protected float[] lightFloat = new float[4];;
    protected float[] lightFloat2 = new float[4];;
    protected float[] lightFloat3 = new float[4];;
    protected float[] lightFloat4 = new float[4];;
    Vector2[] texCoords = new Vector2[4];
    protected final Vector3[] points = new Vector3[8];
    protected final Vector3 pointVector0 = new Vector3();
    protected final Vector3 pointVector1 = new Vector3();
    protected final Vector3 pointVector2 = new Vector3();
    protected final Vector3 pointVector3 = new Vector3();
    protected final Vector3 pointVector4 = new Vector3();
    protected final Vector3 pointVector5 = new Vector3();
    protected final Vector3 pointVector6 = new Vector3();
    protected final Vector3 pointVector7 = new Vector3();

    private final Color color = new Color();
    //private final static HashMap<Float, float[]> finalLightValue = new HashMap<Float, float[]>();
    private final static ArrayMap<Color, float[]> lightCache = new ArrayMap<Color, float[]>();

    static final float SIZE = 1f;

    // Normal pointers for all the six sides. They never changes.
    private static final float[] frontNormal = new float[]{0.0f, 0.0f, 1.0f};
    private static final float[] backNormal = new float[]{0.0f, 0.0f, -1.0f};
    private static final float[] rightNormal = new float[]{1.0f, 0.0f, 0.0f};
    private static final float[] leftNormal = new float[]{-1.0f, 0.0f, 0.0f};
    private static final float[] topNormal = new float[]{0.0f, 1.0f, 0.0f};
    private static final float[] bottomNormal = new float[]{0.0f, -1.0f, 0.0f};

    @Override
    public synchronized boolean addBlock(Vector3 worldPosition, int x, int y, int z, IBlockProvider blockProvider, Chunk chunk, Block block, FloatArray vertices, ShortArray indicies) {
        setupMesh(x,y,z);

        Vector2[] sidesTextureUVs = block.getSidesTextureUVs();
        Vector2[] topTextureUVs = block.getTopTextureUVs();
        Vector2[] bottomTextureUVs = block.getBottomTextureUVs();

        if (block.drawSide(blockProvider, chunk,x,y,z, Block.Side.FRONT)) {
            setAOLightFront(x, y, z, chunk, block);
            setTexCoords(sidesTextureUVs);
            addFront(vertices,indicies);
        }
        if (block.drawSide(blockProvider, chunk, x, y, z, Block.Side.BACK)) {
            setAOLightBack(x, y, z, chunk, block);
            setTexCoords(sidesTextureUVs);
            addBack(vertices,indicies);
        }
        if (block.drawSide(blockProvider, chunk, x, y, z, Block.Side.RIGHT)) {
            setAOLightRight(x, y, z, chunk, block);
            setTexCoords(sidesTextureUVs);
            addRight(vertices,indicies);
        }
        if (block.drawSide(blockProvider, chunk, x, y, z, Block.Side.LEFT)) {
            setAOLightLeft(x, y, z, chunk, block);
            setTexCoords(sidesTextureUVs);
            addLeft(vertices,indicies);
        }
        if (block.drawSide(blockProvider, chunk, x, y, z, Block.Side.TOP)) {
            setAOLightTop(x, y, z, chunk, block);
            setTexCoords(topTextureUVs);
            addTop(vertices,indicies);
        }
        if (block.drawSide(blockProvider, chunk, x, y, z, Block.Side.BOTTOM)) {
            setAOLightBottom(x, y, z, chunk, block);
            setTexCoords(bottomTextureUVs);
            addBottom(vertices,indicies);
        }
        for (int j = 0; j < points.length; j++) {
            points[j] = null;
        }
        texCoords = null;


        return true;
    }

    protected void setupMesh(int x, int y, int z) {

        // Creates the 8 vector points that exists on a box. Those will be used to create the vertex.
        points[0] = pointVector0.set(x, y, z + SIZE);
        points[1] = pointVector1.set(x + SIZE, y, z + SIZE);
        points[2] = pointVector2.set(x + SIZE, y + SIZE, z + SIZE);
        points[3] = pointVector3.set(x, y + SIZE, z + SIZE);
        points[4] = pointVector4.set(x + SIZE, y, z);
        points[5] = pointVector5.set(x, y, z);
        points[6] = pointVector6.set(x, y + SIZE, z);
        points[7] = pointVector7.set(x + SIZE, y + SIZE, z);

    }


    // The following methods are used for adding the 6 different sides of a block.
    // It will create the verticies for every side using the 8 possible points created above.
    // Every vertex includes: position in 3D space (x, y, z), texturecoordinates (u,v), normalmapping, lightning.
    // Finally it creates the indicies for the created verticies (each side has two triangles)

    protected void addFront(FloatArray vertices, ShortArray indicies) {
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[0].x, points[0].y, points[0].z, texCoords[0].x, texCoords[0].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat4[0], lightFloat4[1], lightFloat4[2], lightFloat4[3],
                points[1].x, points[1].y, points[1].z, texCoords[1].x, texCoords[1].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat3[0], lightFloat3[1], lightFloat3[2], lightFloat3[3],
                points[2].x, points[2].y, points[2].z, texCoords[2].x, texCoords[2].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat2[0], lightFloat2[1], lightFloat2[2], lightFloat2[3],
                points[3].x, points[3].y, points[3].z, texCoords[3].x, texCoords[3].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    protected void addBack(FloatArray vertices, ShortArray indicies) {
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[4].x, points[4].y, points[4].z, texCoords[0].x, texCoords[0].y, backNormal[0], backNormal[1], backNormal[2], lightFloat4[0], lightFloat4[1], lightFloat4[2], lightFloat4[3],
                points[5].x, points[5].y, points[5].z, texCoords[1].x, texCoords[1].y, backNormal[0], backNormal[1], backNormal[2], lightFloat3[0], lightFloat3[1], lightFloat3[2], lightFloat3[3],
                points[6].x, points[6].y, points[6].z, texCoords[2].x, texCoords[2].y, backNormal[0], backNormal[1], backNormal[2], lightFloat2[0], lightFloat2[1], lightFloat2[2], lightFloat2[3],
                points[7].x, points[7].y, points[7].z, texCoords[3].x, texCoords[3].y, backNormal[0], backNormal[1], backNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    protected void addRight(FloatArray vertices, ShortArray indicies) {
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[1].x, points[1].y, points[1].z, texCoords[0].x, texCoords[0].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat4[0], lightFloat4[1], lightFloat4[2], lightFloat4[3],
                points[4].x, points[4].y, points[4].z, texCoords[1].x, texCoords[1].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat3[0], lightFloat3[1], lightFloat3[2], lightFloat3[3],
                points[7].x, points[7].y, points[7].z, texCoords[2].x, texCoords[2].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat2[0], lightFloat2[1], lightFloat2[2], lightFloat2[3],
                points[2].x, points[2].y, points[2].z, texCoords[3].x, texCoords[3].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    protected void addLeft(FloatArray vertices, ShortArray indicies) {
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[5].x, points[5].y, points[5].z, texCoords[0].x, texCoords[0].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat4[0], lightFloat4[1], lightFloat4[2], lightFloat4[3],
                points[0].x, points[0].y, points[0].z, texCoords[1].x, texCoords[1].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat3[0], lightFloat3[1], lightFloat3[2], lightFloat3[3],
                points[3].x, points[3].y, points[3].z, texCoords[2].x, texCoords[2].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat2[0], lightFloat2[1], lightFloat2[2], lightFloat2[3],
                points[6].x, points[6].y, points[6].z, texCoords[3].x, texCoords[3].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    protected void addTop(FloatArray vertices, ShortArray indicies) {
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[3].x, points[3].y, points[3].z, texCoords[0].x, texCoords[0].y, topNormal[0], topNormal[1], topNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[2].x, points[2].y, points[2].z, texCoords[1].x, texCoords[1].y, topNormal[0], topNormal[1], topNormal[2], lightFloat2[0], lightFloat2[1], lightFloat2[2], lightFloat2[3],
                points[7].x, points[7].y, points[7].z, texCoords[2].x, texCoords[2].y, topNormal[0], topNormal[1], topNormal[2], lightFloat3[0], lightFloat3[1], lightFloat3[2], lightFloat3[3],
                points[6].x, points[6].y, points[6].z, texCoords[3].x, texCoords[3].y, topNormal[0], topNormal[1], topNormal[2], lightFloat4[0], lightFloat4[1], lightFloat4[2], lightFloat4[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));

    }

    protected void addBottom(FloatArray vertices, ShortArray indicies) {
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[5].x, points[5].y, points[5].z, texCoords[0].x, texCoords[0].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[4].x, points[4].y, points[4].z, texCoords[1].x, texCoords[1].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat2[0], lightFloat2[1], lightFloat2[2], lightFloat2[3],
                points[1].x, points[1].y, points[1].z, texCoords[2].x, texCoords[2].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat3[0], lightFloat3[1], lightFloat3[2], lightFloat3[3],
                points[0].x, points[0].y, points[0].z, texCoords[3].x, texCoords[3].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat4[0], lightFloat4[1], lightFloat4[2], lightFloat4[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }


    protected void setTexCoords(Vector2[] textureUVs) {
        texCoords = new Vector2[4];
        for (int i = 0; i < 4; i++) {
            texCoords[i] = textureUVs[i];
        }
    }


    protected void resetLight(){

        Arrays.fill(lightFloat, 0);
        Arrays.fill(lightFloat2,0);
        Arrays.fill(lightFloat3,0);
        Arrays.fill(lightFloat4,0);
    }

    // Calculate the Ambient Occlusion values for each side of the block and each vertex.
    // The AO is calculated by getting the lightvalue from sides that shares the same vertex point in space. This also looks
    // at neighbour blocks. Then divide the sum of all lightvalues with how many sources that was checked. This gives both AO (fake shadows) and smooth lightning.

    protected void setAOLightFront(int x, int y, int z, Chunk chunk, Block block){
        resetLight();
        float[] lightFloatBlock = getBlockLight(x, y, z, x,  y + 1, z+1, chunk, block);
        if (block.isLightSource()){
            lightFloat[0] = lightFloat2[0] = lightFloat3[0] = lightFloat4[0] = lightFloatBlock[0];
            lightFloat[1] = lightFloat2[1] = lightFloat3[1] = lightFloat4[1] = lightFloatBlock[1];
            lightFloat[2] = lightFloat2[2] = lightFloat3[2] = lightFloat4[2] = lightFloatBlock[2];
            lightFloat[3] = lightFloat2[3] = lightFloat3[3] = lightFloat4[3] = lightFloatBlock[3];
            return;
        }


        float[] above = getBlockLight(     x,   y + 1 , z, x,   y + 1, z+1, chunk, block);
        float[] below = getBlockLight(     x,   y - 1, z, x,   y - 1, z+1, chunk, block);

        float[] frontLeft = getBlockLight(     x-1,   y-1, z, x-1,   y-1 , z+1, chunk, block);
        float[] frontRight = getBlockLight(     x+1,   y-1, z, x+1,   y-1 , z+1, chunk, block);

        float[] frontUpperLeft = getBlockLight(     x-1,   y+1, z, x-1,   y , z+1, chunk, block);
        float[] frontUpperRight = getBlockLight(     x+1,   y+1, z, x+1,   y , z+1, chunk, block);

        float[] left = getBlockLight(     x-1,   y , z, x-1,   y, z+1, chunk, block);
        float[] right = getBlockLight(    x+1,   y , z, x+1,   y, z+1, chunk, block);
        float[] leftUpper = getBlockLight(     x-1,   y+1, z, x-1,   y+1, z+1, chunk, block);
        float[] rightUpper = getBlockLight(    x+1,   y+1, z, x+1,   y+1, z+1, chunk, block);


        lightFloat[0] = (above[0] + leftUpper[0] + frontUpperLeft[0] + lightFloatBlock[0]) /4; // UpperLeft
        lightFloat[1] = (above[1] + leftUpper[1] + frontUpperLeft[1] + lightFloatBlock[1]) /4;
        lightFloat[2] = (above[2]  + leftUpper[2] + frontUpperLeft[2] + lightFloatBlock[2]) /4;
        lightFloat[3] = (above[3]  + leftUpper[3] + frontUpperLeft[3] + lightFloatBlock[3]) /4;

        lightFloat2[0] = (above[0] +  + frontUpperRight[0] + rightUpper[0] + lightFloatBlock[0]) /4; // UpperRight
        lightFloat2[1] = (above[1] +  + frontUpperRight[1] + rightUpper[1] + lightFloatBlock[1]) /4;
        lightFloat2[2] = (above[2] +  + frontUpperRight[2] + rightUpper[2] + lightFloatBlock[2]) /4;
        lightFloat2[3] = (above[3] +  + frontUpperRight[3] + rightUpper[3] + lightFloatBlock[3]) /4;

        lightFloat3[0] = (below[0] +  + right[0]+ frontRight[0] + lightFloatBlock[0]) /4; // BottomRight
        lightFloat3[1] = (below[1] +  + right[1]+ frontRight[1] + lightFloatBlock[1]) /4;
        lightFloat3[2] = (below[2] +  + right[2]+ frontRight[2] + lightFloatBlock[2]) /4;
        lightFloat3[3] = (below[3] +  + right[3]+ frontRight[3] + lightFloatBlock[3])  /4;


        lightFloat4[0] = (below[0] +  + left[0]+ frontLeft[0] + lightFloatBlock[0]) /4; // BottomLeft
        lightFloat4[1] = (below[1] +  + left[1]+ frontLeft[1] + lightFloatBlock[1]) /4;
        lightFloat4[2] = (below[2] +  + left[2]+ frontLeft[2] + lightFloatBlock[2]) /4;
        lightFloat4[3] = (below[3] +  + left[3]+ frontLeft[3] +lightFloatBlock[3]) /4;
    }


    protected void setAOLightBack(int x, int y, int z, Chunk chunk, Block block){
        resetLight();
        float[] lightFloatBlock = getBlockLight(x, y, z, x,  y + 1, z-1, chunk, block);
        if (block.isLightSource()){
            lightFloat[0] = lightFloat2[0] = lightFloat3[0] = lightFloat4[0] = lightFloatBlock[0];
            lightFloat[1] = lightFloat2[1] = lightFloat3[1] = lightFloat4[1] = lightFloatBlock[1];
            lightFloat[2] = lightFloat2[2] = lightFloat3[2] = lightFloat4[2] = lightFloatBlock[2];
            lightFloat[3] = lightFloat2[3] = lightFloat3[3] = lightFloat4[3] = lightFloatBlock[3];
            return;
        }

        float[] above = getBlockLight(     x,   y + 1 , z, x,   y + 1, z-1, chunk, block);
        float[] below = getBlockLight(     x,   y - 1, z, x,   y - 1, z-1, chunk, block);

        float[] frontLeft = getBlockLight(     x+1,   y-1, z, x+1,   y-1 , z-1, chunk, block);
        float[] frontRight = getBlockLight(     x-1,   y-1, z, x-1,   y-1 , z-1, chunk, block);

        float[] frontUpperLeft = getBlockLight(     x+1,   y, z, x+1,   y , z-1, chunk, block);
        float[] frontUpperRight = getBlockLight(     x-1,   y, z, x-1,   y , z-1, chunk, block);

        float[] left = getBlockLight(     x+1,   y , z, x+1,   y, z-1, chunk, block);
        float[] right = getBlockLight(    x-1,   y , z, x-1,   y, z-1, chunk, block);
        float[] leftUpper = getBlockLight(     x+1,   y+1, z, x+1,   y+1, z-1, chunk, block);
        float[] rightUpper = getBlockLight(    x-1,   y+1, z, x-1,   y+1, z-1, chunk, block);


        lightFloat[0] = (above[0] + leftUpper[0] + frontUpperLeft[0] + lightFloatBlock[0]) /4; // UpperLeft
        lightFloat[1] = (above[1] + leftUpper[1] + frontUpperLeft[1] + lightFloatBlock[1]) /4;
        lightFloat[2] = (above[2]  + leftUpper[2] + frontUpperLeft[2] + lightFloatBlock[2]) /4;
        lightFloat[3] = (above[3]  + leftUpper[3] + frontUpperLeft[3] + lightFloatBlock[3]) /4;

        lightFloat2[0] = (above[0] +  + frontUpperRight[0] + rightUpper[0] + lightFloatBlock[0]) /4; // UpperRight
        lightFloat2[1] = (above[1] +  + frontUpperRight[1] + rightUpper[1] + lightFloatBlock[1]) /4;
        lightFloat2[2] = (above[2] +  + frontUpperRight[2] + rightUpper[2] + lightFloatBlock[2]) /4;
        lightFloat2[3] = (above[3] +  + frontUpperRight[3] + rightUpper[3] + lightFloatBlock[3]) /4;

        lightFloat3[0] = (below[0] +  + right[0]+ frontRight[0] + lightFloatBlock[0]) /4; // BottomRight
        lightFloat3[1] = (below[1] +  + right[1]+ frontRight[1] + lightFloatBlock[1]) /4;
        lightFloat3[2] = (below[2] +  + right[2]+ frontRight[2] + lightFloatBlock[2]) /4;
        lightFloat3[3] = (below[3] +  + right[3]+ frontRight[3] + lightFloatBlock[3])  /4;


        lightFloat4[0] = (below[0] +  + left[0]+ frontLeft[0] + lightFloatBlock[0]) /4; // BottomLeft
        lightFloat4[1] = (below[1] +  + left[1]+ frontLeft[1] + lightFloatBlock[1]) /4;
        lightFloat4[2] = (below[2] +  + left[2]+ frontLeft[2] + lightFloatBlock[2]) /4;
        lightFloat4[3] = (below[3] +  + left[3]+ frontLeft[3] +lightFloatBlock[3]) /4;
    }


    protected void setAOLightLeft(int x, int y, int z, Chunk chunk, Block block){
        resetLight();
        float[] lightFloatBlock = getBlockLight(x, y, z, x-1,  y + 1, z, chunk, block);
        if (block.isLightSource()){
            lightFloat[0] = lightFloat2[0] = lightFloat3[0] = lightFloat4[0] = lightFloatBlock[0];
            lightFloat[1] = lightFloat2[1] = lightFloat3[1] = lightFloat4[1] = lightFloatBlock[1];
            lightFloat[2] = lightFloat2[2] = lightFloat3[2] = lightFloat4[2] = lightFloatBlock[2];
            lightFloat[3] = lightFloat2[3] = lightFloat3[3] = lightFloat4[3] = lightFloatBlock[3];
            return;
        }

        float[] above = getBlockLight(     x,   y + 1 , z, x-1,   y + 1, z, chunk, block);
        float[] below = getBlockLight(     x,   y - 1, z, x-1,   y - 1, z, chunk, block);

        float[] frontLeft = getBlockLight(     x,   y-1, z-1, x-1,   y-1 , z-1, chunk, block);
        float[] frontRight = getBlockLight(     x,   y-1, z+1, x-1,   y-1 , z+1, chunk, block);

        float[] frontUpperLeft = getBlockLight(     x,   y, z-1, x-1,   y , z-1, chunk, block);
        float[] frontUpperRight = getBlockLight(     x,   y, z+1, x-1,   y , z+1, chunk, block);

        float[] left = getBlockLight(     x,   y , z-1, x-1,   y, z-1, chunk, block);
        float[] right = getBlockLight(    x,   y , z+1, x-1,   y, z+1, chunk, block);
        float[] leftUpper = getBlockLight(     x,   y+1, z-1, x-1,   y+1, z-1, chunk, block);
        float[] rightUpper = getBlockLight(    x,   y+1, z+1, x-1,   y+1, z+1, chunk, block);


        lightFloat[0] = (above[0] + leftUpper[0] + frontUpperLeft[0] + lightFloatBlock[0]) /4; // UpperLeft
        lightFloat[1] = (above[1] + leftUpper[1] + frontUpperLeft[1] + lightFloatBlock[1]) /4;
        lightFloat[2] = (above[2]  + leftUpper[2] + frontUpperLeft[2] + lightFloatBlock[2]) /4;
        lightFloat[3] = (above[3]  + leftUpper[3] + frontUpperLeft[3] + lightFloatBlock[3]) /4;

        lightFloat2[0] = (above[0] +  + frontUpperRight[0] + rightUpper[0] + lightFloatBlock[0]) /4; // UpperRight
        lightFloat2[1] = (above[1] +  + frontUpperRight[1] + rightUpper[1] + lightFloatBlock[1]) /4;
        lightFloat2[2] = (above[2] +  + frontUpperRight[2] + rightUpper[2] + lightFloatBlock[2]) /4;
        lightFloat2[3] = (above[3] +  + frontUpperRight[3] + rightUpper[3] + lightFloatBlock[3]) /4;

        lightFloat3[0] = (below[0] +  + right[0]+ frontRight[0] + lightFloatBlock[0]) /4; // BottomRight
        lightFloat3[1] = (below[1] +  + right[1]+ frontRight[1] + lightFloatBlock[1]) /4;
        lightFloat3[2] = (below[2] +  + right[2]+ frontRight[2] + lightFloatBlock[2]) /4;
        lightFloat3[3] = (below[3] +  + right[3]+ frontRight[3] + lightFloatBlock[3])  /4;


        lightFloat4[0] = (below[0] +  + left[0]+ frontLeft[0] + lightFloatBlock[0]) /4; // BottomLeft
        lightFloat4[1] = (below[1] +  + left[1]+ frontLeft[1] + lightFloatBlock[1]) /4;
        lightFloat4[2] = (below[2] +  + left[2]+ frontLeft[2] + lightFloatBlock[2]) /4;
        lightFloat4[3] = (below[3] +  + left[3]+ frontLeft[3] +lightFloatBlock[3]) /4;
    }

    protected void setAOLightRight(int x, int y, int z, Chunk chunk, Block block){
        resetLight();
        float[] lightFloatBlock = getBlockLight(x, y, z, x+1,  y + 1, z, chunk, block);
        if (block.isLightSource()){
            lightFloat[0] = lightFloat2[0] = lightFloat3[0] = lightFloat4[0] = lightFloatBlock[0];
            lightFloat[1] = lightFloat2[1] = lightFloat3[1] = lightFloat4[1] = lightFloatBlock[1];
            lightFloat[2] = lightFloat2[2] = lightFloat3[2] = lightFloat4[2] = lightFloatBlock[2];
            lightFloat[3] = lightFloat2[3] = lightFloat3[3] = lightFloat4[3] = lightFloatBlock[3];
            return;
        }

        float[] above = getBlockLight(     x,   y + 1 , z, x+1,   y + 1, z, chunk, block);
        float[] below = getBlockLight(     x,   y - 1, z, x+1,   y - 1, z, chunk, block);

        float[] frontLeft = getBlockLight(     x,   y-1, z+1, x+1,   y-1 , z+1, chunk, block);
        float[] frontRight = getBlockLight(     x,   y-1, z-1, x+1,   y-1 , z-1, chunk, block);

        float[] frontUpperLeft = getBlockLight(     x,   y, z+1, x+1,   y , z+1, chunk, block);
        float[] frontUpperRight = getBlockLight(     x,   y, z-1, x+1,   y , z-1, chunk, block);

        float[] left = getBlockLight(     x,   y , z+1, x+1,   y, z+1, chunk, block);
        float[] right = getBlockLight(    x,   y , z-1, x+1,   y, z-1, chunk, block);
        float[] leftUpper = getBlockLight(     x,   y+1, z+1, x+1,   y+1, z+1, chunk, block);
        float[] rightUpper = getBlockLight(    x,   y+1, z-1, x+1,   y+1, z-1, chunk, block);


        lightFloat[0] = (above[0] + leftUpper[0] + frontUpperLeft[0] + lightFloatBlock[0]) /4; // UpperLeft
        lightFloat[1] = (above[1] + leftUpper[1] + frontUpperLeft[1] + lightFloatBlock[1]) /4;
        lightFloat[2] = (above[2]  + leftUpper[2] + frontUpperLeft[2] + lightFloatBlock[2]) /4;
        lightFloat[3] = (above[3]  + leftUpper[3] + frontUpperLeft[3] + lightFloatBlock[3]) /4;

        lightFloat2[0] = (above[0] +  + frontUpperRight[0] + rightUpper[0] + lightFloatBlock[0]) /4; // UpperRight
        lightFloat2[1] = (above[1] +  + frontUpperRight[1] + rightUpper[1] + lightFloatBlock[1]) /4;
        lightFloat2[2] = (above[2] +  + frontUpperRight[2] + rightUpper[2] + lightFloatBlock[2]) /4;
        lightFloat2[3] = (above[3] +  + frontUpperRight[3] + rightUpper[3] + lightFloatBlock[3]) /4;

        lightFloat3[0] = (below[0] +  + right[0]+ frontRight[0] + lightFloatBlock[0]) /4; // BottomRight
        lightFloat3[1] = (below[1] +  + right[1]+ frontRight[1] + lightFloatBlock[1]) /4;
        lightFloat3[2] = (below[2] +  + right[2]+ frontRight[2] + lightFloatBlock[2]) /4;
        lightFloat3[3] = (below[3] +  + right[3]+ frontRight[3] + lightFloatBlock[3])  /4;


        lightFloat4[0] = (below[0] +  + left[0]+ frontLeft[0] + lightFloatBlock[0]) /4; // BottomLeft
        lightFloat4[1] = (below[1] +  + left[1]+ frontLeft[1] + lightFloatBlock[1]) /4;
        lightFloat4[2] = (below[2] +  + left[2]+ frontLeft[2] + lightFloatBlock[2]) /4;
        lightFloat4[3] = (below[3] +  + left[3]+ frontLeft[3] +lightFloatBlock[3]) /4;
    }


    protected void setAOLightTop(int x, int y, int z, Chunk chunk, Block block){
        resetLight();
        float[] lightFloatBlock = getBlockLight(x, y, z, x,  y + 1, z, chunk, block);
        if (block.isLightSource()){
            lightFloat[0] = lightFloat2[0] = lightFloat3[0] = lightFloat4[0] = lightFloatBlock[0];
            lightFloat[1] = lightFloat2[1] = lightFloat3[1] = lightFloat4[1] = lightFloatBlock[1];
            lightFloat[2] = lightFloat2[2] = lightFloat3[2] = lightFloat4[2] = lightFloatBlock[2];
            lightFloat[3] = lightFloat2[3] = lightFloat3[3] = lightFloat4[3] = lightFloatBlock[3];
            return;
        }
        float[] north = getBlockLight(     x,   y, z+1, x,   y + 1, z+1, chunk, block);
        float[] northEast = getBlockLight(x+1, y, z+1, x+1, y + 1, z+1, chunk, block);
        float[] northWest = getBlockLight( x-1, y, z+1, x-1, y + 1, z+1, chunk, block);

        float[] west = getBlockLight(      x-1, y, z,   x-1, y + 1, z, chunk, block);
        float[] east = getBlockLight(     x+1, y, z,   x+1, y + 1, z, chunk, block);

        float[] south = getBlockLight(     x,   y, z-1, x,   y + 1, z-1, chunk, block);
        float[] southEast = getBlockLight(x+1, y, z-1, x+1, y + 1, z-1, chunk, block);
        float[] southWest = getBlockLight( x-1, y, z-1, x-1, y + 1, z-1, chunk, block);

        float[] above = getBlockLight(     x,   y+1, z, x,   y + 1, z, chunk, block);
        //float[] below = getBlockLight(     x,   y-1, z, x,   y - 1, z, chunk, block);


        lightFloat[0] = (north[0] + northWest[0] + west[0] + above[0] + lightFloatBlock[0]) /5;
        lightFloat[1] = (north[1] + northWest[1] + west[1] + above[1] + lightFloatBlock[1]) /5;
        lightFloat[2] = (north[2] + northWest[2] + west[2] + above[2] + lightFloatBlock[2]) /5;
        lightFloat[3] = (north[3] + northWest[3] + west[3] + above[3] + lightFloatBlock[3]) /5;

        lightFloat2[0] = (north[0] + northEast[0] + east[0] + above[0] + lightFloatBlock[0]) /5;
        lightFloat2[1] = (north[1] + northEast[1] + east[1] + above[1] + lightFloatBlock[1]) /5;
        lightFloat2[2] = (north[2] + northEast[2] + east[2] + above[2] + lightFloatBlock[2]) /5;
        lightFloat2[3] = (north[3] + northEast[3] + east[3] + above[3] + lightFloatBlock[3]) /5;

        lightFloat4[0] = (south[0] + southWest[0] + west[0] + above[0] + lightFloatBlock[0]) /5;
        lightFloat4[1] = (south[1] + southWest[1] + west[1] + above[1] + lightFloatBlock[1]) /5;
        lightFloat4[2] = (south[2] + southWest[2] + west[2] + above[2] + lightFloatBlock[2]) /5;
        lightFloat4[3] = (south[3] + southWest[3] + west[3] + above[3] + lightFloatBlock[3]) /5;

        lightFloat3[0] = (south[0] + southEast[0] + east[0] + above[0] + lightFloatBlock[0]) /5;
        lightFloat3[1] = (south[1] + southEast[1] + east[1] + above[1] + lightFloatBlock[1]) /5;
        lightFloat3[2] = (south[2] + southEast[2] + east[2] + above[2] + lightFloatBlock[2]) /5;
        lightFloat3[3] = (south[3] + southEast[3] + east[3] + above[3] + lightFloatBlock[3]) /5;
    }

    protected void setAOLightBottom(int x, int y, int z, Chunk chunk, Block block){
        resetLight();
        float[] lightFloatBlock = getBlockLight(x, y, z, x,  y - 1, z, chunk, block);

        if (block.isLightSource()){
            lightFloat[0] = lightFloat2[0] = lightFloat3[0] = lightFloat4[0] = lightFloatBlock[0];
            lightFloat[1] = lightFloat2[1] = lightFloat3[1] = lightFloat4[1] = lightFloatBlock[1];
            lightFloat[2] = lightFloat2[2] = lightFloat3[2] = lightFloat4[2] = lightFloatBlock[2];
            lightFloat[3] = lightFloat2[3] = lightFloat3[3] = lightFloat4[3] = lightFloatBlock[3];
            return;
        }

        float[] north = getBlockLight(     x,   y, z-1, x,   y - 1, z-1, chunk, block);
        float[] northEast = getBlockLight(x+1, y, z-1, x+1, y - 1, z-1, chunk, block);
        float[] northWest = getBlockLight( x-1, y, z-1, x-1, y - 1, z-1, chunk, block);

        float[] west = getBlockLight(      x-1, y, z,   x-1, y - 1, z, chunk, block);
        float[] east = getBlockLight(     x+1, y, z,   x+1, y - 1, z, chunk, block);

        float[] south = getBlockLight(     x,   y, z+1, x,   y - 1, z+1, chunk, block);
        float[] southEast = getBlockLight(x+1, y, z+1, x+1, y - 1, z+1, chunk, block);
        float[] southWest = getBlockLight( x-1, y, z+1, x-1, y - 1, z+1, chunk, block);

        float[] below = getBlockLight(     x,   y-1, z, x,   y - 2, z, chunk, block);
        //float[] below = getBlockLight(     x,   y-1, z, x,   y - 1, z, chunk, block);


        lightFloat[0] = (north[0] + northWest[0] + west[0] + below[0] + lightFloatBlock[0]) /5;
        lightFloat[1] = (north[1] + northWest[1] + west[1] + below[1] + lightFloatBlock[1]) /5;
        lightFloat[2] = (north[2] + northWest[2] + west[2] + below[2] + lightFloatBlock[2]) /5;
        lightFloat[3] = (north[3] + northWest[3] + west[3] + below[3] + lightFloatBlock[3]) /5;

        lightFloat2[0] = (north[0] + northEast[0] + east[0] + below[0] + lightFloatBlock[0]) /5;
        lightFloat2[1] = (north[1] + northEast[1] + east[1] + below[1] + lightFloatBlock[1]) /5;
        lightFloat2[2] = (north[2] + northEast[2] + east[2] + below[2] + lightFloatBlock[2]) /5;
        lightFloat2[3] = (north[3] + northEast[3] + east[3] + below[3] + lightFloatBlock[3]) /5;

        lightFloat4[0] = (south[0] + southWest[0] + west[0] + below[0] + lightFloatBlock[0]) /5;
        lightFloat4[1] = (south[1] + southWest[1] + west[1] + below[1] + lightFloatBlock[1]) /5;
        lightFloat4[2] = (south[2] + southWest[2] + west[2] + below[2] + lightFloatBlock[2]) /5;
        lightFloat4[3] = (south[3] + southWest[3] + west[3] + below[3] + lightFloatBlock[3]) /5;

        lightFloat3[0] = (south[0] + southEast[0] + east[0] + below[0] + lightFloatBlock[0]) /5;
        lightFloat3[1] = (south[1] + southEast[1] + east[1] + below[1] + lightFloatBlock[1]) /5;
        lightFloat3[2] = (south[2] + southEast[2] + east[2] + below[2] + lightFloatBlock[2]) /5;
        lightFloat3[3] = (south[3] + southEast[3] + east[3] + below[3] + lightFloatBlock[3]) /5;
    }


    protected float[] getBlockLight(int x, int y, int z, int x1, int y1, int z1, Chunk chunk, Block block) {
        byte blockLight;

        float[] finalLight;
        if (block.isLightSource()) {
            blockLight = chunk.getBlockLight(x, y, z);
            color.set(block.getTileColor(x, y, z));

        } else {
            blockLight = chunk.getBlockLight(x1, y1, z1);
            color.set(block.getTileColor(x1, y1, z1));
        }

        float lightValueR = (float) Math.pow(0.96d, blockLight);
        float lightValueG = (float) Math.pow(0.96d, blockLight);
        float lightValueB = (float) Math.pow(0.96d, blockLight);

        color.mul(lightValueR, lightValueG, lightValueB, 0);

        if (lightCache.containsKey(color)) {
            return lightCache.get(color);
        }


        float alpha = 1;
        if (block.getOpacity() < 32){
            alpha = 0;
        }

        finalLight = new float[]{color.r, color.g, color.b, alpha};

        lightCache.put(color.cpy(), finalLight);
        return finalLight;
    }

}
