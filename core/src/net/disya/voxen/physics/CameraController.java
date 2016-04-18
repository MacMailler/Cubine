package net.disya.voxen.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

/**
 * Created by nicklas on 5/2/14.
 */
public class CameraController extends InputAdapter {
    private static final Vector3 playerPosition = new Vector3();
    private static final Vector3 cameraPosition = new Vector3();
    public static Camera camera;
    protected final IntIntMap keys = new IntIntMap();
    protected final Vector3 tmp = new Vector3();
    private final float acceleration = 50f;
    private final float maxForce = 10f;
    private final Vector3 previousCameraDirection = new Vector3();
    protected final Vector3 moveVector = new Vector3();
    private float currentForce = 0f;
    protected float velocity = 0.05f;
    private boolean fullscreen;
    private boolean cursorCatch;
    private boolean jump = false;
    private long timeLastSpace;

    public CameraController(Camera camera, boolean usePhysicsForCamera) {
        CameraController.camera = camera;
        PhysicsController.addCamera(camera, usePhysicsForCamera);
    }

    @Override
    public boolean keyDown(int keycode) {
        keys.put(keycode, keycode);

        previousCameraDirection.set(Vector3.Zero);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        keys.remove(keycode, 0);
        if (keycode == Input.Keys.F) {
            if (fullscreen) {
                Gdx.graphics.setDisplayMode(1280, 768, false);
                fullscreen = false;
            } else {
                Graphics.DisplayMode desktopDisplayMode = Gdx.graphics.getDesktopDisplayMode();
                Gdx.graphics.setDisplayMode(desktopDisplayMode.width, desktopDisplayMode.height, true);
                fullscreen = true;
            }
        }

        if (keycode == Input.Keys.Q) {
            Gdx.app.exit();
        }

        if (keycode == Input.Keys.SPACE){
            if (System.currentTimeMillis()-timeLastSpace < 200) {
                PhysicsController.toggleFlight();
            }

            timeLastSpace = System.currentTimeMillis();
        }
        
        if(keycode == Input.Keys.ESCAPE) {
        	if(!cursorCatch) {
        		Gdx.input.setCursorCatched(false);
        		cursorCatch = true;
        	} else {
        		Gdx.input.setCursorCatched(true);
        		cursorCatch = false;
        	}
        }

        return true;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity * 10f;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        PhysicsController.rayPick(button);
        return true;
    }

    public void update() {
        try {
            update(Gdx.graphics.getDeltaTime());
            movePlayer(moveVector,jump);
            camera.update(true);
            jump = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void update(float deltaTime) {


        float degreesPerPixel = 0.3f;
        float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel;
        float deltaY = -Gdx.input.getDeltaY() * degreesPerPixel;
        if (camera.direction.y > 0.99 && deltaY > 0.0f){
            return;
        }else if (camera.direction.y < -0.99 && deltaY < 0.0f){
            return;
        }
        camera.direction.rotate(camera.up, deltaX);
        tmp.set(camera.direction).crs(camera.up).nor();
        camera.direction.rotate(tmp, deltaY);

        previousCameraDirection.set(camera.direction);
        moveVector.set(0, 0, 0);


        int FORWARD = Input.Keys.W;
        if (keys.containsKey(FORWARD) || keys.containsKey(Input.Keys.UP)) {
            tmp.set(camera.direction).nor().scl(velocity).y=0;
            moveVector.add(tmp);
        }
        int BACKWARD = Input.Keys.S;
        if (keys.containsKey(BACKWARD) || keys.containsKey(Input.Keys.DOWN)) {
            tmp.set(camera.direction).nor().scl(-velocity).y = 0;
            moveVector.add(tmp);
        }
        int STRAFE_LEFT = Input.Keys.A;
        if (keys.containsKey(STRAFE_LEFT) || keys.containsKey(Input.Keys.LEFT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(-velocity).y = 0;
            moveVector.add(tmp);
        }
        int STRAFE_RIGHT = Input.Keys.D;
        if (keys.containsKey(STRAFE_RIGHT) || keys.containsKey(Input.Keys.RIGHT)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(velocity).y = 0;
            moveVector.add(tmp);
        }

        if (keys.containsKey(Input.Keys.SPACE)) {
            jump = true;
           //keys.remove(Input.Keys.SPACE, 0);
       }



    }

    protected void movePlayer(Vector3 moveVector, boolean jump) {
        PhysicsController.movePlayer(moveVector, jump);
    }
}
