package su.gwg.voxelengine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ShortArray;
import su.gwg.voxelengine.physics.PhysicsController;

/**
 * Created by nicklas on 4/24/14.
 */
public class BoxMesh {
    public final Object rebuilding = new Object();
    protected Matrix4 transform;
    protected Matrix4 transformWithRealY;

    private float[] v;
    private short[] i;
    protected FloatArray vertices;
    protected ShortArray indicies;

    private float[] nonColliadablev;
    private short[] nonColliadablei;
    protected FloatArray nonColliadableVertices;
    protected ShortArray nonColliadableIndicies;


    private boolean needsRebuild = false;
    private Mesh mesh;
    private static int meshBuilding;
    private Mesh nonColliadableMesh;

    private BoundingBox meshBoundingBox = new BoundingBox();
    private BoundingBox nonColliadableMeshBoundingBox = new BoundingBox();

    public Matrix4 getTransform() {
        return transform;
    }

    public com.badlogic.gdx.graphics.Mesh getMesh() {
        return mesh;
    }

    public com.badlogic.gdx.graphics.Mesh getnonColliadableMesh() {
        return nonColliadableMesh;
    }

    public boolean needsRebuild() {
        return needsRebuild;
    }

    public void update() {
        if (needsRebuild) {
            rebuild();
        }
    }

    public void setNeedsRebuild() {
        synchronized (rebuilding) {

            v = vertices.toArray();
            i = indicies.toArray();
            nonColliadablev = nonColliadableVertices.toArray();
            nonColliadablei = nonColliadableIndicies.toArray();
            needsRebuild = true;
        }

    }

    public boolean isNeedsRebuild() {
        return needsRebuild;
    }

    protected void setupMesh(){

            if (vertices == null) {
                vertices = new FloatArray();
            }

            if (indicies == null) {
                indicies = new ShortArray();
            }

            if (nonColliadableVertices == null){
                nonColliadableVertices = new FloatArray();
            }

            if (nonColliadableIndicies == null) {
                nonColliadableIndicies = new ShortArray();
            }
    }


    /**
     * Rebuilds this mesh by creating a LibGDX mesh out of our collection of verticies and indicies.
     */
    private void rebuild() {
        if (meshBuilding < 4) {
            meshBuilding +=2;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronized (rebuilding) {
                            rebuild(vertices, indicies, v, i, transform, true);
                            if (nonColliadableVertices.size > 0) {
                                rebuild(nonColliadableVertices, nonColliadableIndicies, nonColliadablev, nonColliadablei, transform, false);
                            } else {
                                meshBuilding--;
                            }
                            needsRebuild = false;
                         }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            };

            Gdx.app.postRunnable(runnable); // Needs to run on main thread since it's there the opengl context exists.
        }
    }

    private void setMesh(Mesh newMesh){
        if (newMesh != null && newMesh.getNumVertices() > 0 && newMesh.getNumIndices() > 0) {
            if (this.mesh != null){
                this.mesh.dispose();
                this.mesh = null;
            }
            this.mesh = newMesh;
            this.mesh.calculateBoundingBox(meshBoundingBox);
        }else{
            //System.out.println("Denied mesh");
        }
    }

    private void setNonColliadableMesh(Mesh mesh){

        if (mesh != null && mesh.getNumVertices() > 0  && mesh.getNumIndices() > 0){
            if (this.nonColliadableMesh != null){
                this.nonColliadableMesh.dispose();
            }
            this.nonColliadableMesh = mesh;
            this.nonColliadableMesh.calculateBoundingBox(nonColliadableMeshBoundingBox);
        }else{
            //System.out.println("Denied mesh");
        }
    }

    public BoundingBox getMeshBoundingBox() {
        return meshBoundingBox;
    }

    public BoundingBox getNonColliadableMeshBoundingBox() {
        return nonColliadableMeshBoundingBox;
    }

    private void rebuild(FloatArray vertices, ShortArray indicies, float[] v, short[] i, Matrix4 transform, boolean tempAddToPhysics){

        Mesh inProcessMesh = null;
        try {
            inProcessMesh = new Mesh(true, 4 * (v.length / 12), 6 * i.length, VertexAttribute.Position(), VertexAttribute.TexCoords(0), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
            inProcessMesh.setVertices(v);
            inProcessMesh.setIndices(i);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        // Clear everything so it can be garbage collected
        vertices.clear();
        indicies.clear();
        vertices.shrink();
        indicies.shrink();


       // if (tempAddToPhysics) {
            try {
                if (inProcessMesh.getNumVertices() > 0 && inProcessMesh.getNumIndices() > 0) {
                    PhysicsController.addGroundMesh(inProcessMesh, transform, !tempAddToPhysics);
                    if (!tempAddToPhysics){
                        PhysicsController.removeMesh(getnonColliadableMesh());
                    }else{
                        PhysicsController.removeMesh(getMesh());
                    }

                }
            } catch (GdxRuntimeException ex) {
                // need to figure out the "Mesh must be indexed and triangulated" exception
            } catch (Exception ex){
                ex.printStackTrace();

            }
     //   }
        if (tempAddToPhysics) {
            setMesh(inProcessMesh);
        } else {
            setNonColliadableMesh(inProcessMesh);
        }

        meshBuilding--;
    }
}
