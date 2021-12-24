package component;

import core.Camera;
import core.KeyListener;
import core.MouseListener;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DECIMAL;

public class EditorCamera extends Component {

    private final Camera camera;
    private Vector2f targetPos;
    private float targetZoom;

    private final float scrollSensitivity = 0.2f;

    public EditorCamera(Camera camera) {
        this.camera = camera;
        this.targetPos = new Vector2f(camera.position);
        this.targetZoom = camera.getZoom();
    }

    @Override
    public void update(float dt) {
       if (MouseListener.getScrollY() != 0) {
            float addValue = (float) Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensitivity), 0.5f);
            addValue *= -Math.signum(MouseListener.getScrollY());
            targetZoom += addValue;
            if (targetZoom < 0.1f) targetZoom = 0.1f;
            if (targetZoom > 10) targetZoom = 10;
        }

        if (camera.getZoom() != targetZoom) {
            if (Math.abs(targetZoom - camera.getZoom()) <= 0.001f) {
                camera.setZoom(targetZoom);
            } else {
                camera.addZoom((targetZoom - camera.getZoom()) * dt * 3);
            }
        }

        if (!camera.position.equals(targetPos, 0)) {
            if (camera.position.equals(targetPos, 0.5f * camera.getZoom())) {
                camera.position.set(targetPos);
            } else {
                camera.position.lerp(targetPos, dt * 3);
            }
        }
    }

    public void setTargetPos(Vector2f pos) {
        targetPos.set(pos);
    }

    public void snapTo(Vector2f pos) {
        camera.position.set(pos);
        targetPos.set(pos);
    }
}
