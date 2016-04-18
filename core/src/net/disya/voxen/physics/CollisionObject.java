package net.disya.voxen.physics;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nicklas on 5/11/14.
 */
public class CollisionObject {
    private final Collection<PositionListener> listeners = new ArrayList<PositionListener>();
    private Vector3 velocity = new Vector3();

    public void setPosition(Vector3 position) {
        for (PositionListener listener : listeners) {
            listener.onPositionChange(position);
        }
    }

    public void moveObject(Vector3 velocity) {
        this.velocity.set(velocity);
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void resetVelocity() {
        velocity.set(Vector3.Zero);
    }

    public void addListener(PositionListener positionListener) {
        listeners.add(positionListener);
    }

    public interface PositionListener {
        public void onPositionChange(Vector3 position);
    }
}
