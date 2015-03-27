package su.gwg.voxelengine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.google.common.collect.ArrayListMultimap;
import su.gwg.voxelengine.render.BoxMesh;
import su.gwg.voxelengine.render.VoxelRender;
import su.gwg.voxelengine.terrain.World;
import su.gwg.voxelengine.terrain.block.Block;
import su.gwg.voxelengine.terrain.chunk.Chunk;
import su.gwg.voxelengine.game.terrain.block.BlockProvider;

import java.util.*;

/**
 * Created by nicklas on 5/2/14.
 */
public class PhysicsController {

    private static final Object syncObject = new Object();

    private static final boolean DEBUG = false;
    private static final Vector3 gravity = new Vector3(0, -9.81f, 0);

    private static btDefaultCollisionConfiguration collisionConfiguration;
    private static btCollisionDispatcher dispatcher;
    private static btSequentialImpulseConstraintSolver solver;
    private static btDiscreteDynamicsWorld collissionWorld;

    private static Array<Pair<btRigidBody, BoxMesh>> dynamicBodies;
    private static HashMap<Mesh, btRigidBody> meshes;
    private static ArrayListMultimap<Chunk, BoxMesh> chunkMeshMap;
    private static WorldInternalTickCallback worldInternalTickCallback;
    private static DebugDrawer debugDrawer;
    private static final Array<btRigidBody> entities = new Array<btRigidBody>();
    private static final Array<btRigidBody.btRigidBodyConstructionInfo> constructions = new Array<btRigidBody.btRigidBodyConstructionInfo>();
    private static Camera camera;
    private static boolean usePhysicsForCamera;

    private static Vector3 tmp = new Vector3();
    private static Vector3 tmp2 = new Vector3();

    private static HashMap<btRigidBody, CollisionObject> entitiyRidgidBodies;
    private static HashMap<CollisionObject, btRigidBody> entitiyCollisionObjects;
    private static ClosestRayResultCallback rayResultCallback;
    private static Vector3 rayFrom = new Vector3();
    private static Vector3 rayTo = new Vector3();

    private static Array<btMotionState> states = new Array<btMotionState>();
    private static Array<btRigidBody.btRigidBodyConstructionInfo> info = new Array<btRigidBody.btRigidBodyConstructionInfo>();
    private static Array<btCollisionShape> shapes = new Array<btCollisionShape>();
    private static Array<btRigidBody> bodies = new Array<btRigidBody>();
    private static Array<btKinematicCharacterController> controllers = new Array<btKinematicCharacterController>();


    private final static short NOTHING = 0;
    private final static short WORLD = 1;
    private final static short PLAYER = 2;

    private final static short PLAYER_COLLIDES_WITH = WORLD;
    private final static short WORLD_COLLIDES_WITH = PLAYER;
    private static btKinematicCharacterController characterController;
    private static btPairCachingGhostObject playerGhostObject;
    private static btAxisSweep3 btSweep3;
    private static btCapsuleShape capsuleShape;
    private static int numberOfTicks;
    private static Vector3 previousPosition = new Vector3();

    private static ModelBuilder modelBuilder = new ModelBuilder();
    private static boolean playerInWater;
    private static boolean flight;


    public static void init(){
        if (btSweep3 == null || collisionConfiguration == null || dispatcher == null || solver == null || collissionWorld == null || dynamicBodies == null || meshes == null || worldInternalTickCallback == null) {
            Bullet.init(true,true);
            btSweep3 = new com.badlogic.gdx.physics.bullet.collision.btAxisSweep3(new Vector3(-1000f, -1000f, -1000f), new Vector3(1000, 1000, 1000));
            collisionConfiguration = new btDefaultCollisionConfiguration();
            dispatcher = new btCollisionDispatcher(collisionConfiguration);
            solver = new btSequentialImpulseConstraintSolver();
            collissionWorld = new btDiscreteDynamicsWorld(dispatcher, btSweep3, solver, collisionConfiguration);
            collissionWorld.setGravity(gravity);
            collissionWorld.getDispatchInfo().setAllowedCcdPenetration(0.0001f);
            dynamicBodies = new Array<Pair<btRigidBody, BoxMesh>>();
            entitiyRidgidBodies = new HashMap<btRigidBody, CollisionObject>();
            entitiyCollisionObjects = new HashMap<CollisionObject, btRigidBody>();
            meshes = new HashMap<Mesh, btRigidBody>();

            chunkMeshMap = ArrayListMultimap.create();

            if (DEBUG) {
                collissionWorld.setDebugDrawer(debugDrawer = new DebugDrawer());
            }

            worldInternalTickCallback = new WorldInternalTickCallback(collissionWorld);

            rayResultCallback = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);

        }
    }

    public static void movePlayer(Vector3 force, boolean jump) {


        if (playerInWater&& jump){
            characterController.setGravity(-5.5f);
        }

        if (playerInWater && !jump){
            characterController.setGravity(0.1f);
        }

        if (playerInWater){
            force.scl(0.3f);
        }
        characterController.setWalkDirection(force);

        if (!playerInWater && jump) {
            characterController.setJumpSpeed(8f);
            characterController.jump();
        }
    }

    public static void setPlayerInWater(boolean playerInWater) {
        PhysicsController.playerInWater = playerInWater;
    }

    public static boolean isPlayerInWater() {
        return playerInWater;
    }

    public static void toggleFlight() {
        flight = !flight;
    }

    static class WorldInternalTickCallback extends InternalTickCallback {

        WorldInternalTickCallback(btDynamicsWorld dynamicsWorld) {
            super(dynamicsWorld, true);
        }

        @Override
        public void onInternalTick(btDynamicsWorld dynamicsWorld, float timeStep) {

        }
    }

    public static CollisionObject addEntity(float height, float width, Matrix4 transform){

        btBoxShape collisionShape = new btBoxShape(new Vector3(width / 2, height / 2, width / 2));


        btMotionState dynamicMotionState = new btDefaultMotionState();
        dynamicMotionState.setWorldTransform(transform);
        Vector3 dynamicInertia = new Vector3(0, 0, 0);

        collisionShape.calculateLocalInertia(1f, dynamicInertia);


        btRigidBody.btRigidBodyConstructionInfo dynamicConstructionInfo = new btRigidBody.btRigidBodyConstructionInfo(1f, dynamicMotionState, collisionShape, dynamicInertia);
        constructions.add(dynamicConstructionInfo);

        btRigidBody body = new btRigidBody(dynamicConstructionInfo);

        body.setActivationState(4);
        body.setContactProcessingThreshold(0.0f);
        body.setRestitution(0);
        body.setDamping(0.9f, 0.9f);
        body.setLinearFactor(new Vector3(1, 1, 1));
        body.setAngularFactor(Vector3.Zero);
        body.setContactCallbackFlag(2);
        body.setContactCallbackFilter(2);

        collissionWorld.addRigidBody(body);


        CollisionObject collisionObject = new CollisionObject();

        entitiyRidgidBodies.put(body, collisionObject);
        entitiyCollisionObjects.put(collisionObject,body);


        return collisionObject;


    }

    public static void removeEntity(CollisionObject collisionObject){
        if (entitiyCollisionObjects.containsKey(collisionObject)){
            btRigidBody btRigidBody = entitiyCollisionObjects.get(collisionObject);
            collissionWorld.removeCollisionObject(btRigidBody);
            entitiyRidgidBodies.remove(btRigidBody);
        }
    }


    public static void addGroundMesh(Mesh mesh, Matrix4 transform, boolean nonColliadable) {
        synchronized (syncObject) {
            modelBuilder.begin();
            MeshPart part = modelBuilder.part(UUID.randomUUID().toString(), mesh, GL20.GL_TRIANGLES, null);
            modelBuilder.end();

            Array<MeshPart> meshParts = new Array<MeshPart>();
            meshParts.add(part);
            btBvhTriangleMeshShape btBvhTriangleMeshShape = new btBvhTriangleMeshShape(meshParts);
            shapes.add(btBvhTriangleMeshShape);

            //btBoxShape collisionShape = new btBoxShape(new Vector3(8,8,8));

            btMotionState groundMotionState = new btDefaultMotionState();
            states.add(groundMotionState);
            groundMotionState.setWorldTransform(transform);
            btRigidBody.btRigidBodyConstructionInfo groundBodyConstructionInfo = new btRigidBody.btRigidBodyConstructionInfo(0, groundMotionState, btBvhTriangleMeshShape, new Vector3(0, 0, 0));
            constructions.add(groundBodyConstructionInfo);
            //groundBodyConstructionInfo.setLinearDamping(0.2f);
            groundBodyConstructionInfo.setFriction(0);
            //groundBodyConstructionInfo.setAngularDamping(0.0f);
            btRigidBody groundRigidBody = new btRigidBody(groundBodyConstructionInfo);

            if (!nonColliadable) {
                collissionWorld.addRigidBody(groundRigidBody, (short) btBroadphaseProxy.CollisionFilterGroups.StaticFilter,
                        (short) (btBroadphaseProxy.CollisionFilterGroups.CharacterFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
            }else{
                collissionWorld.addRigidBody(groundRigidBody, (short) 64,
                        (short) (btBroadphaseProxy.CollisionFilterGroups.CharacterFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
            }

            entities.add(groundRigidBody);

            meshes.put(mesh, groundRigidBody);
        }
    }

    public static void removeMesh(Mesh mesh) {
        synchronized (syncObject) {
            btRigidBody btRigidBody = meshes.get(mesh);
            if (btRigidBody != null) {
                collissionWorld.removeRigidBody(btRigidBody);
                btRigidBody.dispose();
            }

            if (chunkMeshMap.containsValue(mesh)) {
                chunkMeshMap.values().remove(mesh);
            }
        }
    }

    public static void addCamera(Camera camera, boolean usePhysicsForCamera) {
        PhysicsController.camera = camera;
        PhysicsController.usePhysicsForCamera = usePhysicsForCamera;

        playerGhostObject = new btPairCachingGhostObject();
        Matrix4 matrix4 = new Matrix4().setToTranslation(camera.position);
        playerGhostObject.setWorldTransform(matrix4);
        btSweep3.getOverlappingPairCache().setInternalGhostPairCallback(new btGhostPairCallback());

        capsuleShape = new btCapsuleShape(0.45f, 0.9f);

        playerGhostObject.setCollisionShape(capsuleShape);
        playerGhostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
        characterController = new btKinematicCharacterController(playerGhostObject, capsuleShape, 0.25f);

        collissionWorld.addCollisionObject(playerGhostObject,
                (short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
                (short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
        collissionWorld.addAction(characterController);

        controllers.add(characterController);


    }


    public static void update(float delta) {
        //System.out.println(characterController.getGravity());

        if (playerInWater && characterController.getGravity() == 29.4f) {
            characterController.setGravity(0.1f);
        }
        if (!playerInWater && (characterController.getGravity() != 29.4f)) {
            characterController.setGravity(29.4f);
        }

        synchronized (syncObject){
            collissionWorld.stepSimulation(delta, 5);
          }
        numberOfTicks++;
        if (camera != null && usePhysicsForCamera) {


            if (DEBUG) {
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                debugDrawer.getShapeRenderer().setProjectionMatrix(camera.combined);
                debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawAabb);
                debugDrawer.begin(camera);
                collissionWorld.debugDrawWorld();
                debugDrawer.end();
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
            }

            playerGhostObject.getWorldTransform().getTranslation(tmp);
            double headBob = 0.0;
            float mov = tmp.dst(previousPosition);
            if (!playerInWater && (mov > 0.01f || mov < -0.01f)) {
                headBob = 0.05 * MathUtils.sin((float) (numberOfTicks * 0.5 * MathUtils.PI / 7));
            }
            tmp2.set(tmp.x, (float) (tmp.y+headBob+0.7f),tmp.z);
            camera.position.set(tmp2);
            previousPosition.set(tmp);
        }

        for (Map.Entry<btRigidBody, CollisionObject> object : entitiyRidgidBodies.entrySet()) {
            object.getKey().getWorldTransform().getTranslation(tmp);
            object.getValue().setPosition(tmp);

            object.getKey().applyCentralImpulse(object.getValue().getVelocity());
            object.getValue().resetVelocity();
        }
    }


    public static void rayPick(int button){

        Ray pickRay = camera.getPickRay(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
        rayFrom.set(pickRay.origin);
        rayTo.set(pickRay.direction.scl(5f).add(rayFrom));


        rayResultCallback.setCollisionObject(null);
        rayResultCallback.setClosestHitFraction(1f);
        rayResultCallback.setCollisionFilterGroup((short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter);


        rayResultCallback.setRayFromWorld(rayFrom);
        rayResultCallback.setRayToWorld(rayTo);

        collissionWorld.rayTest(rayFrom, rayTo, rayResultCallback);

        if(rayResultCallback.hasHit()) {
            System.out.println("collisionObject:" + rayResultCallback.getCollisionObject()+ " "+rayResultCallback.getFlags());
            rayResultCallback.getHitPointWorld(tmp);
            rayResultCallback.getHitNormalWorld(tmp2);
            double hitPosDelX = Math.floor(tmp.x - tmp2.x/2);
            double hitPosDelY = Math.floor(tmp.y - tmp2.y/2);
            double hitPosDelZ = Math.floor(tmp.z - tmp2.z/2);

            double hitPosAddX = Math.floor(tmp.x + tmp2.x/2);
            double hitPosAddY = Math.floor(tmp.y + tmp2.y/2);
            double hitPosAddZ = Math.floor(tmp.z + tmp2.z/2);

            if (button == Input.Buttons.LEFT){
                World.setBlock((float) hitPosDelX, (float) hitPosDelY, (float) hitPosDelZ, BlockProvider.air, true);
            }
            if (button == Input.Buttons.RIGHT){
                if (tmp.dst(camera.position) < 1.5f){
                    System.out.println(tmp.dst(camera.position));
                    return;
                }
                Block block;
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)){
                    block = BlockProvider.wall;
                }else{
                    block = BlockProvider.light;
                }
                World.setBlock((float) hitPosAddX, (float) hitPosAddY, (float) hitPosAddZ, block, true);
            }
        }
    }
}
