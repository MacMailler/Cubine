package net.disya.voxen.physics;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by nicklas on 6/16/14.
 */
public class FlyingCameraController extends CameraController {
    public FlyingCameraController(Camera camera) {
        super(camera, false);
    }

    @Override
    void update(float deltaTime) {
        super.update(deltaTime);
        int UP = Input.Keys.SPACE;
        int SHIFT = Input.Keys.SHIFT_LEFT;
        if (keys.containsKey(UP)) {
            tmp.set(camera.direction).crs(camera.up).nor().scl(1).y = deltaTime * (velocity/2);
            moveVector.add(tmp);
        }

        if (keys.containsKey(SHIFT)){
            tmp.set(camera.direction).crs(camera.up).nor().scl(1).y = -deltaTime * (velocity/2);
            moveVector.add(tmp);
        }
    }

    @Override
    protected void movePlayer(Vector3 moveVector, boolean jump) {
        camera.translate(moveVector);
    }
}
